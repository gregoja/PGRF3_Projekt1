package main;

import lwjglutils.*;
import org.lwjgl.glfw.*;
import transforms.*;

import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30C.glBindFramebuffer;

public class Renderer extends AbstractRenderer {

    private double oldMx, oldMy;
    private boolean leftButtonPressed, rightButtonPressed, middleButtonPressed;

    private int shaderProgramViewer, shaderProgramLight;
    private OGLBuffers buffers;
    private OGLRenderTarget renderTarget;
    private OGLTexture2D mosaicTexture, humanTexture;
    private OGLTexture.Viewer viewer;

    private Camera camera, cameraLight;
    private Mat4 projection, model;

    private int locView, locProjection, locModel, locSolid, locLightPosition, locEyePosition, locLightVP;
    private int locViewLight, locProjectionLight, locSolidLight, locModelLight;
    private int locConstantAttenuation, locLinearAttenuation, locQuadraticAttenuation, locSpotDirection, locSpotCutOff;
    private int locAppMode, locTransformInTimeMode, locTime, locSolid1Type, locBlendSpotlight;
    private int locLightTransformInTimeMode, locLightTime, locLightSolid1Type;
    private int locAmbientOn, locDiffuseON, locSpecularON;

    private float time;
    private int a = 150;
    private int appMode = 0;
    private int polygonMode = 0;
    private float spotCutOff = 0.965f;
    private int triangleMode = GL_TRIANGLE_STRIP;
    private int solid1Type = 0;

    private boolean perspectiveMode = true;
    private boolean animationsEnabled = true;
    private boolean ambientOn = true;
    private boolean diffuseON = true;
    private boolean specularON = true;
    private boolean transformInTimeMode = false;
    private boolean blendSpotlight = false;


    @Override
    public void init() {
        // OGLUtils.printOGLparameters();
        // OGLUtils.printJAVAparameters();
        // OGLUtils.printLWJLparameters();
        // OGLUtils.shaderCheck();

        model = new Mat4Identity();

        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        shaderProgramViewer = ShaderUtils.loadProgram("/start");
        shaderProgramLight = ShaderUtils.loadProgram("/light");

        locModel = glGetUniformLocation(shaderProgramViewer, "model");
        locView = glGetUniformLocation(shaderProgramViewer, "view");
        locProjection = glGetUniformLocation(shaderProgramViewer, "projection");
        locSolid = glGetUniformLocation(shaderProgramViewer, "solid");
        locLightPosition = glGetUniformLocation(shaderProgramViewer, "lightPosition");
        locEyePosition = glGetUniformLocation(shaderProgramViewer, "eyePosition");
        locLightVP = glGetUniformLocation(shaderProgramViewer, "lightVP");

        locConstantAttenuation = glGetUniformLocation(shaderProgramViewer, "constantAttenuation");
        locLinearAttenuation = glGetUniformLocation(shaderProgramViewer, "linearAttenuation");
        locQuadraticAttenuation = glGetUniformLocation(shaderProgramViewer, "quadraticAttenuation");
        locSpotDirection = glGetUniformLocation(shaderProgramViewer, "spotDirection");
        locSpotCutOff = glGetUniformLocation(shaderProgramViewer, "spotCutOff");
        locAppMode = glGetUniformLocation(shaderProgramViewer, "appMode");
        locAmbientOn = glGetUniformLocation(shaderProgramViewer, "ambientOn");
        locDiffuseON = glGetUniformLocation(shaderProgramViewer, "diffuseON");
        locSpecularON = glGetUniformLocation(shaderProgramViewer, "specularON");
        locBlendSpotlight = glGetUniformLocation(shaderProgramViewer, "blendSpotlight");
        locTransformInTimeMode = glGetUniformLocation(shaderProgramViewer, "transformInTimeMode");
        locTime = glGetUniformLocation(shaderProgramViewer, "time");
        locSolid1Type = glGetUniformLocation(shaderProgramViewer, "solid1Type");

        locLightTransformInTimeMode = glGetUniformLocation(shaderProgramLight, "transformInTimeMode");
        locLightTime = glGetUniformLocation(shaderProgramLight, "time");
        locModelLight = glGetUniformLocation(shaderProgramLight, "model");
        locViewLight = glGetUniformLocation(shaderProgramLight, "view");
        locProjectionLight = glGetUniformLocation(shaderProgramLight, "projection");
        locSolidLight = glGetUniformLocation(shaderProgramLight, "solid");
        locLightSolid1Type = glGetUniformLocation(shaderProgramLight, "solid1Type");

        camera = new Camera()
                .withPosition(new Vec3D(-3, 3, 3))
                .withAzimuth(-1 / 4.0 * Math.PI)
                .withZenith(-1.3 / 5.0 * Math.PI);

        updateProjectionMatrix();
        createBuffers();

        renderTarget = new OGLRenderTarget(1024, 1024);
        viewer = new OGLTexture2D.Viewer();

        setCameraLight();

        // zadefinovany v abstractRenderer
        textRenderer = new OGLTextRenderer(width, height);

        try {
            mosaicTexture = new OGLTexture2D("textures/mosaic.jpg");
            humanTexture = new OGLTexture2D("textures/human.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void display() {
        glEnable(GL_DEPTH_TEST); //zapnout z-buffer (kvuli textRenderu, ktery si ho vypina)
        switch (polygonMode) {
            case 0 -> glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
            case 1 -> glPolygonMode(GL_FRONT_AND_BACK, GL_POINT);
            case 2 -> glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        }

        if (animationsEnabled) {
            time++;
            double lightSpeed = time / 25.;
            double lightX = 2 * Math.sin(lightSpeed);
            double lightY = 2 * Math.cos(lightSpeed);
            double lightZ = cameraLight.getPosition().getZ();
            cameraLight = cameraLight.withPosition(new Vec3D(lightX, lightY, lightZ));
        }

        renderFromLight();
        renderFromViewer();

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        viewer.view(renderTarget.getColorTexture(), -1, -1, 0.7);
        viewer.view(renderTarget.getDepthTexture(), -1, -0.3, 0.7);

        textRenderer.addStr2D(width - 90, height - 5, "PGRF");

        textRenderer.addStr2D(5, 20, "[ú,)] Grid size: " + a + "x" + a);
        textRenderer.addStr2D(5, 40, String.format("[M] AppMode: %d [N]Solid Type: %d", appMode, solid1Type));
        textRenderer.addStr2D(5, 60, "[O] PolygonMode: " + polygonMode);
        textRenderer.addStr2D(5, 80, "[T] TriangleStrip: " + triangleMode / 5);
        textRenderer.addStr2D(5, 100, "[P] PerspectiveMode: " + perspectiveMode);
        textRenderer.addStr2D(5, 120, "[Q,5] AnimationsOn: " + animationsEnabled);

        textRenderer.addStr2D(width - 120, 20, "[+] Ambient ON " + ambientOn);
        textRenderer.addStr2D(width - 120, 40, "[ě] Diffuse ON " + diffuseON);
        textRenderer.addStr2D(width - 120, 60, "[š] Specular ON " + specularON);

        textRenderer.addStr2D(width / 2 - 60, 20, "Additional controls:[LMB, RMB, MMB]");
        if (appMode == 0) {
            textRenderer.addStr2D(width / 2 - 60, 40, String.format("[1,3] SpotCutOff: %.3f", spotCutOff));
            textRenderer.addStr2D(width / 2 - 60, 60, "[4,6;2,8] SpotDirection");
            textRenderer.addStr2D(width / 2 - 60, 80, "[B] SpotlightBlending: "+ blendSpotlight);
        }
    }

    private void renderFromLight() {
        glUseProgram(shaderProgramLight);
        renderTarget.bind();

        glClearColor(0.5f, 0, 0, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUniform1i(locLightTransformInTimeMode, transformInTimeMode ? 1 : 0);
        glUniform1f(locLightTime, time);
        glUniform1i(locLightSolid1Type, solid1Type);

        glUniformMatrix4fv(locModelLight, false, model.floatArray());
        glUniformMatrix4fv(locViewLight, false, cameraLight.getViewMatrix().floatArray());
        glUniformMatrix4fv(locProjectionLight, false, projection.floatArray());

        glUniform1i(locSolidLight, 1);
        buffers.draw(triangleMode, shaderProgramLight);

        glUniform1i(locSolidLight, 2);
        buffers.draw(triangleMode, shaderProgramLight);
    }

    private void renderFromViewer() {
        glUseProgram(shaderProgramViewer);

        // vychozi viewoirt - render do obrazovky
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        // nutno opravit viewport, protoze render target si nastuje vlastni
        glViewport(0, 0, width, height);

        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderTarget.getDepthTexture().bind(shaderProgramViewer, "depthTexture", 1);
        mosaicTexture.bind(shaderProgramViewer, "mosaic", 0);
        humanTexture.bind(shaderProgramViewer, "human", 2);

        glUniform3fv(locLightPosition, ToFloatArray.convert(cameraLight.getPosition()));
        glUniform3fv(locEyePosition, ToFloatArray.convert(camera.getEye()));

        // 4*4 matice, floatova, zadana jako vector/pole
        glUniformMatrix4fv(locModel, false, model.floatArray());
        glUniformMatrix4fv(locView, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(locProjection, false, projection.floatArray());
        glUniformMatrix4fv(locLightVP, false, cameraLight.getViewMatrix().mul(projection).floatArray());

        glUniform1f(locConstantAttenuation, 0);
        glUniform1f(locLinearAttenuation, 0);
        glUniform1f(locQuadraticAttenuation, 0.05f);

        glUniform1i(locAmbientOn, ambientOn ? 1 : 0);
        glUniform1i(locDiffuseON, diffuseON ? 1 : 0);
        glUniform1i(locSpecularON, specularON ? 1 : 0);

        glUniform1i(locTransformInTimeMode, transformInTimeMode ? 1 : 0);
        glUniform1f(locTime, time);
        glUniform1i(locSolid1Type, solid1Type);
        glUniform1i(locAppMode, appMode);
        glUniform1f(locSpotCutOff, spotCutOff);
        glUniform3fv(locSpotDirection, ToFloatArray.convert(cameraLight.getViewVector()));
        glUniform1i(locBlendSpotlight, blendSpotlight ? 1 : 0);

        glUniform1i(locSolid, 1);
        buffers.draw(triangleMode, shaderProgramViewer);

        glUniform1i(locSolid, 2);
        buffers.draw(triangleMode, shaderProgramViewer);

        //lightSource
        glUniform1i(locSolid, 3);
        buffers.draw(triangleMode, shaderProgramViewer);
    }

    private void updateProjectionMatrix() {
        if (perspectiveMode) {
            projection = new Mat4PerspRH(Math.PI / 3, LwjglWindow.HEIGHT / (float) LwjglWindow.WIDTH, 1.0, 20.0);
        } else {
            projection = new Mat4OrthoRH(LwjglWindow.WIDTH / 25., LwjglWindow.HEIGHT / 25., 1.0, 20.0);
        }
    }

    private void setCameraLight() {
        cameraLight = new Camera()
                .withPosition(new Vec3D(0, 0, 8))
                .withAzimuth(0)
                .withZenith(-Math.PI / 2);
    }

    private void createBuffers() {
        if (triangleMode == GL_TRIANGLE_STRIP) buffers = GridFactory.createEfficientGrid(a, a);
        else buffers = GridFactory.createSimpleGrid(a, a);
    }

    @Override
    public GLFWWindowSizeCallback getWsCallback() {
        return new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int w, int h) {
                if (w > 0 && h > 0) {
                    Renderer.this.width = w;
                    Renderer.this.height = h;
                    System.out.println("Windows resize to [" + w + ", " + h + "]");
                    if (textRenderer != null) {
                        textRenderer.resize(Renderer.this.width, Renderer.this.height);
                    }
                    updateProjectionMatrix();
                }
            }
        };
    }

    @Override
    public GLFWCursorPosCallback getCursorCallback() {
        return cursorPosCallback;
    }

    @Override
    public GLFWScrollCallback getScrollCallback() {
        return scrollCallback;
    }

    @Override
    public GLFWMouseButtonCallback getMouseCallback() {
        return mouseButtonCallback;
    }

    @Override
    public GLFWKeyCallback getKeyCallback() {
        return keyCallback;
    }

    private final GLFWCursorPosCallback cursorPosCallback = new GLFWCursorPosCallback() {
        @Override
        public void invoke(long window, double x, double y) {
            double dx = (oldMx - x);
            double dy = (oldMy - y);
            if (leftButtonPressed) {
                camera = camera.addAzimuth(Math.PI * (oldMx - x) / LwjglWindow.WIDTH);
                camera = camera.addZenith(Math.PI * (oldMy - y) / LwjglWindow.HEIGHT);
            } else if (rightButtonPressed) {
                // nema nic spolecneho s dx, dy
                double ddx = model.get(3, 0);
                double ddy = model.get(3, 1);
                double ddz = model.get(3, 2);
                if (ddx == 0 && ddy == 0 && ddz == 0) {
                    model = model.mul(new Mat4RotXYZ(0, Math.PI * (dy) / LwjglWindow.HEIGHT
                            , -(Math.PI * (dx) / LwjglWindow.WIDTH)));
                } else {
                    model = model.mul(new Mat4Transl(-ddx, -ddy, -ddz));
                    model = model.mul(new Mat4RotXYZ(0, Math.PI * (dy) / LwjglWindow.HEIGHT
                            , -(Math.PI * (dx) / LwjglWindow.WIDTH)));
                    model = model.mul(new Mat4Transl(ddx, ddy, ddz));
                }
            } else if (middleButtonPressed) {
                model = model.mul(new Mat4Transl(0, (oldMx - x) / 10.0, (oldMy - y) / 10.0));
            }
            oldMx = x;
            oldMy = y;
        }
    };


    private final GLFWMouseButtonCallback mouseButtonCallback = new GLFWMouseButtonCallback() {
        @Override
        public void invoke(long window, int button, int action, int mods) {
            if (button == GLFW_MOUSE_BUTTON_LEFT || button == GLFW_MOUSE_BUTTON_RIGHT) {
                double[] xPos = new double[1];
                double[] yPos = new double[1];
                glfwGetCursorPos(window, xPos, yPos);
                oldMx = xPos[0];
                oldMy = yPos[0];
            }
            if (button == GLFW_MOUSE_BUTTON_LEFT) leftButtonPressed = (action == GLFW_PRESS);
            else if (button == GLFW_MOUSE_BUTTON_RIGHT) rightButtonPressed = (action == GLFW_PRESS);
            else if (button == GLFW_MOUSE_BUTTON_MIDDLE) middleButtonPressed = (action == GLFW_PRESS);
        }
    };

    protected GLFWScrollCallback scrollCallback = new GLFWScrollCallback() {
        @Override
        public void invoke(long window, double dx, double dy) {
            double scale = dy > 0 ? 1.1 : 0.9;
            model = model.mul(new Mat4Scale(scale));
        }
    };

    private final GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
        @Override
        public void invoke(long window, int key, int scancode, int action, int mods) {
            if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                switch (key) {
                    case GLFW_KEY_A -> camera = camera.left(0.1);
                    case GLFW_KEY_D -> camera = camera.right(0.1);
                    case GLFW_KEY_W -> camera = camera.forward(0.1);
                    case GLFW_KEY_S -> camera = camera.backward(0.1);
                    case GLFW_KEY_R -> camera = camera.up(0.1);
                    case GLFW_KEY_F -> camera = camera.down(0.1);
                    case GLFW_KEY_O -> polygonMode = (polygonMode + 1) % 3;
                    case GLFW_KEY_1 -> ambientOn = !ambientOn;
                    case GLFW_KEY_2 -> diffuseON = !diffuseON;
                    case GLFW_KEY_3 -> specularON = !specularON;
                    case GLFW_KEY_Q -> transformInTimeMode = !transformInTimeMode;
                    case GLFW_KEY_KP_5 -> animationsEnabled = !animationsEnabled;
                    case GLFW_KEY_B -> blendSpotlight = !blendSpotlight;
                    case GLFW_KEY_KP_1 -> spotCutOff = spotCutOff - 0.001f;
                    case GLFW_KEY_KP_3 -> spotCutOff = spotCutOff + 0.001f;
                    case GLFW_KEY_KP_8 -> {
                        if (appMode == 0) cameraLight = cameraLight.addZenith(0.05);
                    }
                    case GLFW_KEY_KP_2 -> {
                        if (appMode == 0) cameraLight = cameraLight.addZenith(-0.05);
                    }
                    case GLFW_KEY_KP_4 -> {
                        if (appMode == 0) cameraLight = cameraLight.addAzimuth(-0.05);
                    }
                    case GLFW_KEY_KP_6 -> {
                        if (appMode == 0) cameraLight = cameraLight.addAzimuth(0.05);
                    }
                    case GLFW_KEY_RIGHT_BRACKET -> {
                        a += 1;
                        createBuffers();
                    }
                    case GLFW_KEY_LEFT_BRACKET -> {
                        a = Math.max(a - 1, 2);
                        createBuffers();
                    }
                    case GLFW_KEY_T -> {
                        if (triangleMode == GL_TRIANGLE_STRIP) triangleMode = GL_TRIANGLES;
                        else triangleMode = GL_TRIANGLE_STRIP;
                        createBuffers();
                    }
                    case GLFW_KEY_P -> {
                        perspectiveMode = !perspectiveMode;
                        updateProjectionMatrix();
                    }
                    case GLFW_KEY_N -> solid1Type = (solid1Type + 1) % 7;
                    case GLFW_KEY_M -> {
                        // pokud byl appMode 0, tak byl zapnuty spotlight a se svetlem nekdo mohl hybat. => pro zjednoduseni resetovat
                        if (appMode == 0) setCameraLight();
                        appMode = (appMode + 1) % 11;
                    }

                }
            }
        }
    };
}