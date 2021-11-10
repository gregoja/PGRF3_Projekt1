package main;

import lwjglutils.*;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import transforms.*;

import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30C.glBindFramebuffer;

public class Renderer extends AbstractRenderer {

	private double oldMx, oldMy;
	private boolean mousePressed;

	private int shaderProgramViewer, shaderProgramLight;
	private OGLBuffers buffers;
	private OGLRenderTarget renderTarget;

	private Camera camera, cameraLight;
	private Mat4 projection;
	private int locView, locProjection, locSolid, locLightPosition, locEyePosition, locLightVP;
	private int locViewLight, locProjectionLight, locSolidLight;
	private OGLTexture2D mosaicTexture;
	private OGLTexture.Viewer viewer;

	@Override
	public void init() {
		// OGLUtils.printOGLparameters();
		// OGLUtils.printJAVAparameters();
		// OGLUtils.printLWJLparameters();
		// OGLUtils.shaderCheck();

		glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
		shaderProgramViewer = ShaderUtils.loadProgram("/start");
		shaderProgramLight = ShaderUtils.loadProgram("/light");

		locView = glGetUniformLocation(shaderProgramViewer, "view");
		locProjection = glGetUniformLocation(shaderProgramViewer, "projection");
		locSolid = glGetUniformLocation(shaderProgramViewer, "solid");
		locLightPosition = glGetUniformLocation(shaderProgramViewer, "lightPosition");
		locEyePosition = glGetUniformLocation(shaderProgramViewer,"eyePosition");
		locLightVP = glGetUniformLocation(shaderProgramViewer,"lightVP");

		locViewLight = glGetUniformLocation(shaderProgramLight,"view");
		locProjectionLight = glGetUniformLocation(shaderProgramLight,"projection");
		locSolidLight = glGetUniformLocation(shaderProgramLight,"solid");

//        view = new Mat4ViewRH();
//        camera = new Camera(
//                new Vec3D(6, 6, 5),
//                5 / 4.0 * Math.PI,
//                -1 / 5.0 * Math.PI,
//                1.0,
//                true
//        );
		camera = new Camera()
				.withPosition(new Vec3D(-3, 3, 3))
				.withAzimuth(-1 / 4.0 * Math.PI)
				.withZenith(-1.3 / 5.0 * Math.PI);

		projection = new Mat4PerspRH(Math.PI / 3, 600 / 800f, 1.0, 20.0);
//        projection = new Mat4OrthoRH();

		buffers = GridFactory.createEfficientGrid(150, 150);
		renderTarget = new OGLRenderTarget(1024, 1024);
		viewer = new OGLTexture2D.Viewer();

		cameraLight = new Camera()
				.withPosition(new Vec3D(5, 5, 5))
				.withAzimuth(5 / 4f * Math.PI)
				.withZenith(-1 / 5f * Math.PI);

		// zadefinovany v abstracTRenderer
		textRenderer = new OGLTextRenderer(width, height);

		try {
			mosaicTexture = new OGLTexture2D("textures/mosaic.jpg");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void display() {
		glEnable(GL_DEPTH_TEST); //zapnout z-buffer (kvuli textRenderu, ktery si ho vypina)

		renderFromLight();
		renderFromViewer();

		//glFrontFace(GL_CCW);
		//glPolygonMode(GL_FRONT, GL_LINE);
		//glPolygonMode(GL_BACK, GL_FILL);


		//glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

		// spodní textura
		viewer.view(renderTarget.getColorTexture(), -1, -1, 0.7);
		viewer.view(renderTarget.getDepthTexture(), -1, -0.3, 0.7);

		textRenderer.addStr2D(width - 90, height - 5, "PGRF");
	}

	private void renderFromLight() {
		glUseProgram(shaderProgramLight);
		renderTarget.bind();

		glClearColor(0.5f,0,0,1);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		glUniformMatrix4fv(locViewLight,false,cameraLight.getViewMatrix().floatArray());
		glUniformMatrix4fv(locProjectionLight,false,projection.floatArray());

		glUniform1i(locSolidLight,1);
		buffers.draw(GL_TRIANGLE_STRIP,shaderProgramLight);

		glUniform1i(locSolidLight,2);
		buffers.draw(GL_TRIANGLE_STRIP,shaderProgramLight);
	}

	private void renderFromViewer() {
		glUseProgram(shaderProgramViewer);

		// vychozi viewoirt - render do obrazovky
		glBindFramebuffer(GL_FRAMEBUFFER,0);

		// nutno opravit viewport, protoze render target si nastuje vlastni
		glViewport(0, 0, width, height);

		glClearColor(0,0.5f,0,1);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		renderTarget.getDepthTexture().bind(shaderProgramViewer,"depthTexture",1);
		mosaicTexture.bind(shaderProgramViewer, "mosaic", 0);

		//cameraLight = cameraLight.up(0.05);
		glUniform3fv(locLightPosition, ToFloatArray.convert(cameraLight.getPosition()));
		glUniform3fv(locEyePosition, ToFloatArray.convert(camera.getEye()));

		// 4*4 matice, floatova, zadana jako vector/pole
		glUniformMatrix4fv(locView, false, camera.getViewMatrix().floatArray());
		glUniformMatrix4fv(locProjection, false, projection.floatArray());
		glUniformMatrix4fv(locLightVP,false,cameraLight.getViewMatrix().mul(projection).floatArray());


		glUniform1i(locSolid, 1);
		buffers.draw(GL_TRIANGLE_STRIP, shaderProgramViewer);

		glUniform1i(locSolid, 2);
		buffers.draw(GL_TRIANGLE_STRIP, shaderProgramViewer);
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
					projection = new Mat4PerspRH(Math.PI / 3,height/(float)width,1.0,20);
				}
			}
		};
	}

	@Override
	public GLFWCursorPosCallback getCursorCallback() {
		return cursorPosCallback;
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
			if (mousePressed) {
				camera = camera.addAzimuth(Math.PI * (oldMx - x) / LwjglWindow.WIDTH);
				camera = camera.addZenith(Math.PI * (oldMy - y) / LwjglWindow.HEIGHT);
				oldMx = x;
				oldMy = y;
			}
		}
	};

	private final GLFWMouseButtonCallback mouseButtonCallback = new GLFWMouseButtonCallback() {
		@Override
		public void invoke(long window, int button, int action, int mods) {
			if (button == GLFW_MOUSE_BUTTON_LEFT) {
				double[] xPos = new double[1];
				double[] yPos = new double[1];
				glfwGetCursorPos(window, xPos, yPos);
				oldMx = xPos[0];
				oldMy = yPos[0];
				mousePressed = (action == GLFW_PRESS);
			}
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
				}
			}
		}
	};
}