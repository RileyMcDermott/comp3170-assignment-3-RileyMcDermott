package comp3170.ass3.sceneobjects;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.json.JSONArray;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;

import comp3170.SceneObject;
import comp3170.Shader;

public class HeightMap extends SceneObject {
	private Vector3f[] vertices;
	private int[] indices;
	
	private Vector4f colour = new Vector4f(0,1,0,1);
	
	private int vertexBuffer;
	
	private int indexBuffer;
	
	private Vector3f[] normals;
	private int normalBuffer;
	
	private Vector3f[] normalFace;
	
	public HeightMap(Shader shader, int width, int depth, JSONArray heightArray) {
		super(shader);
				
		//
		// Load the array of heights from the JSONArray directly into vertices
		//
		
		vertices = new Vector3f[width*depth];
		
		int k = 0;
		for (int j = 0; j < depth; j++) {
			for(int i = 0; i < width; i++) {
				vertices[k] = new Vector3f((float)i, heightArray.getFloat(k++),(float)j);
			}
		}		
		
		indices = new int[vertices.length*3*2];
		//for every square pick out the 6 vertices that will make the triangles
		int n = 0;
		for(int i = 0; i < width-1; i++) {
			for(int j = 0; j < depth-1; j++) {
				indices[n++] = i*width+j;
				indices[n++] = (i+1)*width+j;
				indices[n++] = (i+1)*width+j+1;
				
				indices[n++] = i*width+j;
				indices[n++] = (i+1)*width+j+1;
				indices[n++] = i*width+j+1;
				
			}
		}
		
		vertexBuffer = shader.createBuffer(vertices);
		indexBuffer = shader.createIndexBuffer(indices);
	}
	
	@Override
	protected void drawSelf(Shader shader) {
		GL4 gl = (GL4) GLContext.getCurrentGL();

		shader.setUniform("u_mvpMatrix", this.mvpMatrix);		
		shader.setAttribute("a_position", this.vertexBuffer);
		shader.setUniform("u_colour", colour);
		gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, this.indexBuffer);		
		gl.glDrawElements(GL.GL_TRIANGLES, this.indices.length, GL.GL_UNSIGNED_INT, 0);
	}
}
