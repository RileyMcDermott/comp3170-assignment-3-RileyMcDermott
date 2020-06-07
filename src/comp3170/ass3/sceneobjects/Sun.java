package comp3170.ass3.sceneobjects;

import org.joml.Vector3f;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;

import comp3170.SceneObject;
import comp3170.Shader;

public class Sun extends SceneObject{
	int NSIDES = 100;
	
	private Vector3f[] vertices = new Vector3f[NSIDES * 3];
	
	private int vertexBuffer;
	
	private float[] colour = {1, 1, 0, 1};
	public Sun(Shader shader) {
		super(shader);
		//making a circle for the sun
		int j = 0;
		for (int i = 0; i < NSIDES; i++) {
			
			float angle0 = i * (float)Math.PI*2 / NSIDES;
			float angle1 = (i+1) * (float)Math.PI*2 / NSIDES;
			
			// first point is in the centre of the circle
			vertices[j++] = new Vector3f(0, 10, 0);
			
			// second point is at angle0
			vertices[j++] = new Vector3f((float)Math.cos(angle0), 10, (float)Math.sin(angle0));

			// third point is at angle1
			vertices[j++] = new Vector3f((float)Math.cos(angle1), 10f, (float)Math.sin(angle1));	
		}
		 this.vertexBuffer = shader.createBuffer(this.vertices); 
	}
	
	@Override
	protected void drawSelf(Shader shader) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		shader.setUniform("u_mvpMatrix", this.mvpMatrix);
		shader.setAttribute("a_position", this.vertexBuffer);	    
		shader.setUniform("u_colour", this.colour);	 
        gl.glDrawArrays(GL.GL_TRIANGLES, 0, this.vertices.length);      	
	
	}
}
