#version 150
in vec2 texCoord;
in vec3 normal;
in vec3 light;
in vec3 viewDirection;
in vec4 depthTextureCoord;
in float distanceFromLight;
in vec3 pos3View;
in vec3 mmpDehomog;

uniform sampler2D depthTexture;
uniform sampler2D mosaic;
uniform sampler2D human;
uniform float constantAttenuation;
uniform float linearAttenuation;
uniform float quadraticAttenuation;
uniform float spotCutOff;
uniform vec3 spotDirection;
uniform int appMode;
uniform bool ambientOn;
uniform bool specularON;
uniform bool diffuseON;
uniform int solid;
uniform int solid1Type;
uniform bool blendSpotlight;

out vec4 outColor;// output from the fragment shader

void main() {
    vec3 ambient;
    if (ambientOn) ambient = vec3(0.2);
    else ambient = vec3(0);

    vec3 diffuse;
    if (diffuseON){
        float NdotL = max(0, dot(normalize(normal), normalize(light)));
        diffuse = NdotL * vec3(0.7);
    } else {
        diffuse = vec3(0);
    }

    vec3 specular;
    if (specularON){
        vec3 halfVector = normalize(light + viewDirection);
        // je nezadouci aby to bylo zaporne a pak to snizovalo efekt tech dalsich slozek
        float NdotH = max(0.0, dot(normalize(normal), halfVector));
        // typicky pro Phonga 4, Pro Blinn-Phonga tedyd 16
        //rozkopíruje do 3 slozek
        specular = vec3(pow(NdotH, 16));
    } else {
        specular = vec3(0);
    }


    float att=1.0/(constantAttenuation +linearAttenuation * distanceFromLight
    + quadraticAttenuation * distanceFromLight * distanceFromLight);

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

    vec3 colorIntensity;

    // light source
    if (solid == 3){
        outColor = vec4(1.0, 1.0, 0.0, 1.0);
    } else {
        if (appMode == 0){
            // appMode0 = spot, att, stiny
            float spotEffect = max(dot(normalize(spotDirection), normalize(-light)), 0);
            float  blend = clamp((spotEffect-spotCutOff)/(1-spotCutOff),0.0,1.0);

            if (spotEffect > spotCutOff) {
                colorIntensity = ambient + att * (diffuse + specular);
            } else {
                colorIntensity = ambient;
            }

            if (shadow){
                outColor = vec4(ambient * textureColor, 1.0);
            } else {
                if(blendSpotlight) outColor = vec4(textureColor * mix(ambient, ambient+att*(diffuse + specular),blend),1);
                else outColor = vec4(colorIntensity * textureColor,1.0);
            }
        } else if (appMode == 1){
            // appMode1 = stiny
            colorIntensity = ambient + diffuse + specular;
            if (shadow){
                outColor = vec4(ambient * textureColor, 1.0);
            } else {
                outColor = vec4(colorIntensity * textureColor, 1.0);
            }
        } else if (appMode == 2){
            colorIntensity = ambient + diffuse + specular;
            outColor = vec4(colorIntensity, 1.0);
        } else if (appMode == 3){
            // normala model
            outColor = vec4(normalize(normal), 1.0);
        } else if (appMode == 4){
            // normala pozorovatel, normala je modifikovana pro tento ucel ve VS
            // mohl by byt asi spolecny if jako ten nahore, ale pro prehlednost radsi necham takhle
            outColor = vec4(normalize(normal), 1.0);
        } else if (appMode == 5){
            // hloubka
            outColor = vec4(gl_FragCoord.zzz, 1.0);
        } else if (appMode == 6){
            // vzdalenost od svetla. Pro lepsi viditelnost vydeleno 15
            outColor = vec4(distanceFromLight/15, distanceFromLight/15, distanceFromLight/15, 1.0);
        } else if (appMode == 7){
            // souradnice do textury
            outColor = vec4(texCoord, 0, 1.0);
        } else if (appMode == 8){
            // pozice SS pozorovatel
            outColor = vec4(pos3View, 1.0);
        } else if (appMode == 9){
            // pozice SS model
            outColor = vec4(mmpDehomog, 1.0);
        } else if (appMode == 10){
            // textura rgba
            if (solid == 1 && solid1Type == 0) outColor = texture(human, 1-texCoord).rgba;
            else outColor = texture(mosaic, texCoord).rgba;
        }
    }
}