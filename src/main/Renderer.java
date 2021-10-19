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

public class Renderer extends AbstractRenderer {

	private double oldMx, oldMy;
	private boolean mousePressed;

	private int shaderProgram;
	private OGLBuffers buffers;

	private Camera camera, cameraLight;
	private Mat4 projection;
	private int locView, locProjection, locSolid, locLightPosition;
	private OGLTexture2D mosaicTexture;

	@Override
	public void init() {
		OGLUtils.printOGLparameters();
		OGLUtils.printJAVAparameters();
		OGLUtils.printLWJLparameters();
		OGLUtils.shaderCheck();

		glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
		shaderProgram = ShaderUtils.loadProgram("/start");

		locView = glGetUniformLocation(shaderProgram, "view");
		locProjection = glGetUniformLocation(shaderProgram, "projection");
		locSolid = glGetUniformLocation(shaderProgram, "solid");
		locLightPosition = glGetUniformLocation(shaderProgram, "lightPosition");

//        view = new Mat4ViewRH();
//        camera = new Camera(
//                new Vec3D(6, 6, 5),
//                5 / 4.0 * Math.PI,
//                -1 / 5.0 * Math.PI,
//                1.0,
//                true
//        );
		camera = new Camera()
				.withPosition(new Vec3D(3, 3, 2.5))
				.withAzimuth(5 / 4.0 * Math.PI)
				.withZenith(-1 / 5.0 * Math.PI);

		projection = new Mat4PerspRH(Math.PI / 3, 600 / 800f, 1.0, 20.0);
//        projection = new Mat4OrthoRH();

		buffers = GridFactory.createEfficientGrid(150, 150);

		cameraLight = new Camera().withPosition(new Vec3D(7, 0, 0));

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
		glUseProgram(shaderProgram);
		glViewport(0, 0, width, height);
		glEnable(GL_DEPTH_TEST);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		mosaicTexture.bind(shaderProgram, "mosaic", 0);

		cameraLight = cameraLight.up(0.05);
		glUniform3fv(locLightPosition, ToFloatArray.convert(cameraLight.getPosition()));

		// 4*4 matice, floatova, zadana jako vector/pole
		glUniformMatrix4fv(locView, false, camera.getViewMatrix().floatArray());
		glUniformMatrix4fv(locProjection, false, projection.floatArray());

		glUniform1i(locSolid, 1);
		buffers.draw(GL_TRIANGLE_STRIP, shaderProgram);

		glUniform1i(locSolid, 2);
		buffers.draw(GL_TRIANGLE_STRIP, shaderProgram);

		textRenderer.addStr2D(width - 90, height - 5, "PGRF");
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