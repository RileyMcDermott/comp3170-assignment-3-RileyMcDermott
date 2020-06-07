#version 410

// simple fragment shader

in vec3 v_normal;			// MODEL

layout(location = 0) out vec4 colour;	// RGBA

void main() {	
	colour = vec4(v_normal,1);
}

