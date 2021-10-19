#version 150
in vec2 texCoord;
in vec3 normal;
in vec3 light;

uniform sampler2D mosaic;

out vec4 outColor; // output from the fragment shader
void main() {
	vec3 ambient = vec3(0.2);

	float Ndot = max(0, dot(normalize(normal), normalize(light)));
	vec3 diffuse = Ndot * vec3(0.7);
	//outColor = vec4(1.0, 0.0, 0.0, 1.0);
	// pri tomhle by dost vadilo, kdyby difuse byla mene nez 0;
	vec3 colorIntensity = ambient + diffuse;
	vec3 textureColor = texture(mosaic, texCoord).rgb;

	// texture(sampler, souradnice do textury);
	outColor = vec4(colorIntensity * textureColor,1.0);
}