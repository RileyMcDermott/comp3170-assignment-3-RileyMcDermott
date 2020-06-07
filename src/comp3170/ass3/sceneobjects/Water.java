package comp3170.ass3.sceneobjects;

import org.joml.Vector3f;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;

import comp3170.SceneObject;
import comp3170.Shader;

public class Water extends SceneObject{
	private Vector3f[] vertices;
	private int vertexBuffer;
	
	private float[] colour = {0, 0, 0.7f, 0.7f};

	
	public Water(Shader shader, int width, int depth){
		super(shader);
		//just 2 triangles
		vertices = new Vector3f[6];
		vertices[0] = new Vector3f(0, 0, 0);
		vertices[2] = new Vector3f(width-1, 0, 0);
		vertices[1] = new Vector3f(0, 0, depth-1);
		
		vertices[3] = new Vector3f(width-1, 0, 0);
		vertices[5] = new Vector3f(width-1, 0, depth-1);
		vertices[4] = new Vector3f(0, 0, depth-1);
		
		vertexBuffer = shader.createBuffer(vertices);
	}
	
	
	
	@Override
	protected void drawSelf(Shader shader){
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		shader.setUniform("u_mvpMatrix", this.mvpMatrix);		
		shader.setAttribute("a_position", this.vertexBuffer);
		shader.setUniform("u_colour", this.colour);

		gl.glDrawArrays(GL.GL_TRIANGLES, 0, this.vertices.length);
	}
}
