#version 150
in vec2 texCoord;
in vec3 normal;
in vec3 light;
in vec3 viewDirection;

uniform sampler2D mosaic;

out vec4 outColor; // output from the fragment shader
void main() {
	vec3 ambient = vec3(0.2);

	float Ndot = max(0, dot(normalize(normal), normalize(light)));
	vec3 diffuse = Ndot * vec3(0.7);

	vec3 halfVector = normalize(light + viewDirection);
	// je nezadouci aby to bylo zaporne a pak to snizovalo efekt tech dalsich slozek
	float NdotH = max(0.0, dot(normalize(normal), halfVector));
	// typicky pro Phonga 4, Pro Blinn-Phonga tedyd 16
	//rozkopíruje do 3 slozek
	vec3 specular = vec3(pow(NdotH, 16));

	//outColor = vec4(1.0, 0.0, 0.0, 1.0);
	// pri tomhle by dost vadilo, kdyby difuse byla mene nez 0;
	vec3 colorIntensity = ambient + diffuse + specular;
	vec3 textureColor = texture(mosaic, texCoord).rgb;

	// texture(sampler, souradnice do textury);
	outColor = vec4(colorIntensity * textureColor,1.0);
	// outColor = vec4(normalize(normal),1.0);
	//souradnici povrchu bych si musel poslat z vertex shaderu.
	// V tom mi ale nic nebrani. ve vert dam out vec3 position a ve frag ji zas prijmu
	//outColor = vec4(texCoord,0,1.0);
}