package comp3170.ass3;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFrame;

import org.joml.Matrix4f;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;

import comp3170.GLException;
import comp3170.InputManager;
import comp3170.SceneObject;
import comp3170.Shader;
import comp3170.ass3.sceneobjects.Axes;
import comp3170.ass3.sceneobjects.HeightMap;
import comp3170.ass3.sceneobjects.Plane;

public class Assignment3 extends JFrame implements GLEventListener {

	// TAU = 2 * PI radians = 360 degrees
	final private float TAU = (float) (Math.PI * 2);

	private JSONObject level;
	private GLCanvas canvas;

	// shaders
	
	final private File SHADER_DIRECTORY = new File("src/comp3170/ass3/shaders");
	
	private Shader simpleShader;
	final private String SIMPLE_VERTEX_SHADER = "simpleVertex.glsl";
	final private String SIMPLE_FRAGMENT_SHADER = "simpleFragment.glsl";

	private Shader colourShader;
	final private String COLOUR_VERTEX_SHADER = "coloursVertex.glsl";
	final private String COLOUR_FRAGMENT_SHADER = "coloursFragment.glsl";

	// matrices
	
	private Matrix4f mvpMatrix;
	private Matrix4f viewMatrix;
	private Matrix4f projectionMatrix;

	// window size in pixels
	
	private int screenWidth = 1000;
	private int screenHeight = 1000;

	// Scene objects

	private SceneObject root;
	private SceneObject camera;

	// Input Manager
	
	private InputManager input;

	// Camera parameters
	
	private float cameraHeight = 1;
	private float cameraDistance = 5;
	private float camerFOVY = TAU / 6;
	private float cameraAspect = 1;
	private float cameraNear = 1.0f;
	private float cameraFar = 40.0f;


	public Assignment3(JSONObject level) {
		super(level.getString("name"));
		this.level = level;

		// Set up GL Canvas
		
		GLProfile profile = GLProfile.get(GLProfile.GL4);
		GLCapabilities capabilities = new GLCapabilities(profile);

		this.canvas = new GLCanvas(capabilities);
		this.canvas.addGLEventListener(this);
		this.add(canvas);

		// Set up Input manager

		this.input = new InputManager();
		input.addListener(this);
		input.addListener(this.canvas);

		// Set up the JFrame

		this.setSize(screenWidth, screenHeight);
		this.setVisible(true);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

	}

	@Override
	/**
	 * Initialise the GLCanvas
	 */
	public void init(GLAutoDrawable drawable) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		// Enable flags

		// Load shaders
		
		this.simpleShader = loadShader(SIMPLE_VERTEX_SHADER, SIMPLE_FRAGMENT_SHADER);
		this.colourShader = loadShader(COLOUR_VERTEX_SHADER, COLOUR_FRAGMENT_SHADER);

		// Allocate matrices
		
		this.mvpMatrix = new Matrix4f();
		this.viewMatrix = new Matrix4f();
		this.projectionMatrix = new Matrix4f();
		
		// Construct the scene-graph
		
		this.root = new SceneObject();
		
		this.camera = new SceneObject();
		this.camera.setParent(this.root);
		this.camera.localMatrix.translate(0, cameraHeight, cameraDistance);

		// Example objects (should not be included in your final submission)
		
		Plane plane = new Plane(this.simpleShader, 10);
		plane.setParent(this.root);
		plane.localMatrix.scale(5,5,5);

		Axes axes = new Axes(this.colourShader);
		axes.setParent(this.root);
		axes.localMatrix.translate(0,0.5f,0);
		
		// Height map (incomplete)
		
		JSONObject jsonMap = this.level.getJSONObject("map");
		int width = jsonMap.getInt("width");
		int depth = jsonMap.getInt("depth");
		JSONArray heights = jsonMap.getJSONArray("height");
		
		HeightMap map = new HeightMap(simpleShader, width, depth, heights);

	}

	/**
	 * Load and compile a vertex shader and fragment shader 
	 * 
	 * @param vs	The name of the vertex shader
	 * @param fs	The name of the fragment shader
	 * @return
	 */
	private Shader loadShader(String vs, String fs) {		
		try {
			File vertexShader = new File(SHADER_DIRECTORY, vs);
			File fragmentShader = new File(SHADER_DIRECTORY, fs);
			return new Shader(vertexShader, fragmentShader);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (GLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		// Unreachable
		return null;
	}


	@Override
	/**
	 * Called when the canvas is redrawn
	 */
	public void display(GLAutoDrawable drawable) {
		GL4 gl = (GL4) GLContext.getCurrentGL();

		// Set the viewport to the window dimensions
		
		gl.glViewport(0, 0, this.screenWidth, this.screenHeight);

		// Set the background colour to black
		
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);

		// Set the view matrix
		
		this.viewMatrix.identity();
		this.viewMatrix.translate(0, -cameraHeight, -cameraDistance);
		
		// Set the projection matrix
		
		this.projectionMatrix.setPerspective(camerFOVY, cameraAspect, cameraNear, cameraFar);

		// Draw the objects in the scene graph recursively
		
		this.mvpMatrix.identity();
		this.mvpMatrix.mul(projectionMatrix);
		this.mvpMatrix.mul(viewMatrix);
		this.root.draw(mvpMatrix);

	}

	@Override
	/**
	 * Called when the canvas is resized
	 */
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
	}

	@Override
	/**
	 * Called when we dispose of the canvas
	 */
	public void dispose(GLAutoDrawable drawable) {
	}

	/**
	 * Main method expects a JSON level filename to be give as an argument.
	 * 
	 * @param args
	 * @throws IOException
	 * @throws GLException
	 */
	public static void main(String[] args) throws IOException, GLException {
		File levelFile = new File(args[0]);
		BufferedReader in = new BufferedReader(new FileReader(levelFile));
		JSONTokener tokener = new JSONTokener(in);
		JSONObject level = new JSONObject(tokener);

		new Assignment3(level);
	}

}
