#version 150
in vec2 texCoord;
in vec3 normal;
in vec3 light;
in vec3 viewDirection;
in vec4 depthTextureCoord;

uniform sampler2D depthTexture;
uniform sampler2D mosaic;

out vec4 outColor; // output from the fragment shader

void main() {
	vec3 ambient = vec3(0.2);

	float NdotL = max(0, dot(normalize(normal), normalize(light)));
	vec3 diffuse = NdotL * vec3(0.7);

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

	// "z" hodnota z textury
	// nutna dehomogenizace (probehla ve VS)
	// R, G, B slozky jsou stejne, protoze gl_FragCoord.zzz
	// r -> v light.frag ukladame gl_FragCoord.zzz, takze jsou vsechny hodnoty stejne
	//zLight je  hodnota ke svetlu nejblizsiho pixelu na teto pozici
	float zLight = texture(depthTexture, depthTextureCoord.xy).r;
	float zActual = depthTextureCoord.z;
	// zActual vetsi nez Z light? Stin
	bool shadow = zActual > zLight + 0.001;

	if(shadow){
		outColor = vec4(ambient * textureColor,1.0);
	}else{
		outColor = vec4(colorIntensity * textureColor,1.0);
		//outColor = vec4(colorIntensity * textureColor,1.0);
	}


	// texture(sampler, souradnice do textury);
		
	// outColor = vec4(normalize(normal),1.0);
	//souradnici povrchu bych si musel poslat z vertex shaderu.
	// V tom mi ale nic nebrani. ve vert dam out vec3 position a ve frag ji zas prijmu
	//outColor = vec4(texCoord,0,1.0);
}