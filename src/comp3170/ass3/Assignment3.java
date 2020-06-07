package comp3170.ass3;

import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFrame;

import org.joml.Matrix4f;
import org.joml.Vector3f;
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
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import comp3170.GLException;
import comp3170.InputManager;
import comp3170.SceneObject;
import comp3170.Shader;
import comp3170.ass3.sceneobjects.Axes;
import comp3170.ass3.sceneobjects.HeightMap;
import comp3170.ass3.sceneobjects.Plane;
import comp3170.ass3.sceneobjects.Sun;
import comp3170.ass3.sceneobjects.Water;

public class Assignment3 extends JFrame implements GLEventListener {

	// TAU = 2 * PI radians = 360 degrees
	final private float TAU = (float) (Math.PI * 2);

	private JSONObject level;
	private GLCanvas canvas;
	
	// Animator
	Animator animator;
	// shaders
	
	final private File SHADER_DIRECTORY = new File("src/comp3170/ass3/shaders");
	final private File TEXTURE_DIRECTORY = new File("src/comp3170/ass3/textures/grass.jpg");
	private Shader simpleShader;
	final private String SIMPLE_VERTEX_SHADER = "simpleVertex.glsl";
	final private String SIMPLE_FRAGMENT_SHADER = "simpleFragment.glsl";

	private Shader colourShader;
	final private String COLOUR_VERTEX_SHADER = "coloursVertex.glsl";
	final private String COLOUR_FRAGMENT_SHADER = "coloursFragment.glsl";
	
	// heightmap light shader
		private Shader diffuseShader;
		final private String DIFFUSE_VERTEX_SHADER = "diffuseVertex.glsl";
		final private String DIFFUSE_FRAGMENT_SHADER = "diffuseFragment.glsl";

	// matrices
	
	private Matrix4f mvpMatrix;
	private Matrix4f viewMatrix;
	private Matrix4f projectionMatrix;
	private Matrix4f cameraTransform;

	// window size in pixels
	
	private int screenWidth = 1000;
	private int screenHeight = 1000;

	// Scene objects

	private SceneObject root;
	private SceneObject camera;

	// Input Manager
	
	private InputManager input;

	// Camera parameters
	
	private float cameraHeight = 2;
	private float cameraDistance = 10;
	private float camerFOVY = TAU / 6;
	private float cameraAspect = 1;
	private float cameraNear = 1.0f;
	private float cameraFar = 40.0f;
	private float rotationSpeed = (float) Math.PI/100;
	private Vector3f moveVelocity = new Vector3f(0,0,-0.05f);
	public Assignment3(JSONObject level) {
		super(level.getString("name"));
		this.level = level;

		// Set up GL Canvas
		
		GLProfile profile = GLProfile.get(GLProfile.GL4);
		GLCapabilities capabilities = new GLCapabilities(profile);
		//antialiasing
		capabilities.setSampleBuffers(true);
		capabilities.setNumSamples(4);

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
		
		//set up animator
		animator = new Animator(canvas);
		this.animator.start();
	}

	@Override
	/**
	 * Initialise the GLCanvas
	 */
	public void init(GLAutoDrawable drawable) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		// Enable flags
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glEnable(GL.GL_CULL_FACE);
		gl.glCullFace(GL.GL_BACK);
		//to enable transparency
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		// Load shaders
		
		this.simpleShader = loadShader(SIMPLE_VERTEX_SHADER, SIMPLE_FRAGMENT_SHADER);
		this.colourShader = loadShader(COLOUR_VERTEX_SHADER, COLOUR_FRAGMENT_SHADER);
		this.diffuseShader = loadShader(DIFFUSE_VERTEX_SHADER, DIFFUSE_FRAGMENT_SHADER); //tried to add lighting... made a huge mess, got rid of it
		//load textures (spent too much time on trying to do lighting)
//		Texture tex = null;
//		try {
//			tex = TextureIO.newTexture(TEXTURE_DIRECTORY, true);
//		} catch (com.jogamp.opengl.GLException e) {
//			System.out.println("some kind of texture problem");
//		} catch (IOException e) {
//			System.out.println("failed to load texture");
//		}
//		int textureID = tex.getTextureObject();
//		gl.glBindTexture(GL.GL_TEXTURE_2D, textureID);
//		
//		gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
		// Allocate matrices
		
		this.mvpMatrix = new Matrix4f();
		this.viewMatrix = new Matrix4f();
		this.projectionMatrix = new Matrix4f();
		this.cameraTransform = new Matrix4f();
		// Construct the scene-graph
		
		this.root = new SceneObject();
		
		this.camera = new SceneObject();
		this.camera.setParent(this.root);
		this.camera.localMatrix.translate(0, cameraHeight, cameraDistance);

		// Example objects (should not be included in your final submission)
		
//		Plane plane = new Plane(this.simpleShader, 10);
//		plane.setParent(this.root);
//		plane.localMatrix.scale(5,5,5);
//		
//		Axes axes = new Axes(this.colourShader);
//		axes.setParent(this.root);
//		axes.localMatrix.translate(0,0.5f,0);
		
		
		// Creating Height map 
		
		JSONObject jsonMap = this.level.getJSONObject("map");
		int width = jsonMap.getInt("width");
		int depth = jsonMap.getInt("depth");
		JSONArray heights = jsonMap.getJSONArray("height");
		
		HeightMap map = new HeightMap(simpleShader, width, depth, heights);
		map.setParent(this.root);
		map.localMatrix.translate(-width/2,0,-depth/2);
		
		//Adding water
		float waterHeight = level.getFloat("waterHeight");
		Water water = new Water(simpleShader, width, depth);
		water.setParent(root);
		water.localMatrix.translate(-width/2, waterHeight, -depth/2);
		
		//Adding sun
		Sun sun = new Sun(simpleShader);
		sun.setParent(this.root);
		sun.localMatrix.translate(0, 5, -10);
		sun.localMatrix.rotateX(-(float)Math.PI/4);
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
		
		//clear the depth-buffer
		gl.glClear(GL.GL_DEPTH_BUFFER_BIT);  
		
		//Adjusting the camera if specific keys pressed
		if(input.isKeyDown(KeyEvent.VK_W)) {
			camera.localMatrix.rotateX(rotationSpeed);
		}
		if(input.isKeyDown(KeyEvent.VK_S)) {
			camera.localMatrix.rotateX(-rotationSpeed);
		}
		if(input.isKeyDown(KeyEvent.VK_A)) {
			camera.localMatrix.rotateY(rotationSpeed);
		}
		if(input.isKeyDown(KeyEvent.VK_D)) {
			camera.localMatrix.rotateY(-rotationSpeed);
		}
		if(input.isKeyDown(KeyEvent.VK_SPACE)) {
			camera.localMatrix.translate(moveVelocity);
		}
		// Set the view matrix
		this.viewMatrix.identity();
		this.viewMatrix.mul(camera.localMatrix);
		this.viewMatrix.invert();
		
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
		
		this.screenWidth =  this.canvas.getWidth();
		this.screenHeight = this.canvas.getHeight();
		
		cameraAspect = (float)screenWidth/(float)screenHeight;
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
