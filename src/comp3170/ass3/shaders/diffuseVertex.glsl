#version 410

// simple vertex shader

in vec3 a_position;			// MODEL
in vec3 a_normal;			// MODEL
uniform mat4 u_mvpMatrix;	// MODEL -> NDC

out vec3 v_normal;          // MODEL

void main() {
    gl_Position = u_mvpMatrix * vec4(a_position, 1);
    v_normal = a_normal;
}

