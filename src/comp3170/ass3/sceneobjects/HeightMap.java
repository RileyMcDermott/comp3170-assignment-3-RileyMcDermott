package comp3170.ass3.sceneobjects;

import org.json.JSONArray;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;

import comp3170.SceneObject;
import comp3170.Shader;

public class HeightMap extends SceneObject {

	public HeightMap(Shader shader, int width, int depth, JSONArray heightArray) {
		super(shader);
				
		//
		// Load the array of heights from the JSONArray
		//
		
		
		float[][] height = new float[width][depth];
		
		int k = 0;
		for (int j = 0; j < depth; j++) {
			for (int i = 0; i < width; i++) {
				height[i][j] = heightArray.getFloat(k++);
			}
		}		

		// TODO: Complete this
		
	}
	
	
	@Override
	protected void drawSelf(Shader shader) {
		GL4 gl = (GL4) GLContext.getCurrentGL();

		// TODO: Complete this
		
	}


	
}
