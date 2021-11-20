#version 150
// in se meni se s kazdym novym vrcholem, ktery tam prijde
in vec2 inPosition;// input from the vertex buffer

// uniform je typ hodnoty, ktera kdyz ji nastavite, tak bude pro cely ten cyklus vykresleni telesea stejna
uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
uniform mat4 lightVP;

uniform int solid;
uniform vec3 lightPosition;
uniform vec3 eyePosition;
uniform int appMode;
uniform int solid1Type;
uniform bool transformInTimeMode;
uniform float time;

out vec2 texCoord;
out vec3 normal;
out vec3 light;
out vec3 viewDirection;
out vec4 depthTextureCoord;
out float distanceFromLight;
out vec3 pos3View;
out vec3 mmpDehomog;

const float PI = 3.14159265;

// sfericka 1
vec3 getSphere(vec2 pos){
    float az = pos.x * PI;// souradnice X je v <-1;1> a chceme v rozsahu <-PI; PI>
    float ze = pos.y * PI / 2;// souradnice Y je v <-1;1> a chceme v rozsahu <-PI/2; PI/2>
    //float r = abs(sin(time/30))+0.2;
    float r = 1;

    float x = r * cos(az) * cos(ze);
    float y = r * sin(az) * cos(ze);
    float z = r * sin(ze);

    return vec3(x, y, z);
}

vec3 getSphereNormal(vec2 pos){
    vec3 u = getSphere(pos + vec2(0.001, 0)) - getSphere(pos - vec2(0.001, 0));
    vec3 v = getSphere(pos + vec2(0, 0.001)) - getSphere(pos - vec2(0, 0.001));
    return cross(u, v);
}

vec3 getSpehereNormalByDerivative(vec2 pos){
    float az = pos.x * PI;
    float ze = pos.y * PI / 2;

    vec3 u = vec3(-sin(az) * cos(ze) * PI, cos(az) * cos(ze) * PI, 0);
    vec3 v = vec3(cos(az) * -sin(ze) * PI/ 2, sin(az) * -sin(ze) * PI / 2, cos(ze) * PI/2);
    return cross(u, v);
}

vec3 getSphereLight(vec2 pos){
    float az = pos.x * PI;// souradnice X je v <-1;1> a chceme v rozsahu <-PI; PI>
    float ze = pos.y * PI / 2;// souradnice Y je v <-1;1> a chceme v rozsahu <-PI/2; PI/2>
    float r = 0.3;

    float x = r * cos(az) * cos(ze);
    float y = r * sin(az) * cos(ze);
    float z = r * sin(ze);

    return vec3(x, y, z);
}

// sfericka 2
vec3 getSphere2(vec2 pos){
    float az = pos.x * PI;// souradnice X je v <-1;1> a chceme v rozsahu <-PI; PI>
    float ze = pos.y * PI / 2;// souradnice Y je v <-1;1> a chceme v rozsahu <-PI/2; PI/2>
    float r = 1;

    float x = r * cos(az) * cos(ze);
    float y = r * sin(az) * cos(ze);
    float z = 0.5 * r * sin(ze);

    return vec3(x, y, z);
}

vec3 getSphere2Normal(vec2 pos){
    vec3 u = getSphere2(pos + vec2(0.001, 0)) - getSphere2(pos - vec2(0.001, 0));
    vec3 v = getSphere2(pos + vec2(0, 0.001)) - getSphere2(pos - vec2(0, 0.001));
    return cross(u, v);
}

// sfericka 3
vec3 getSphere3(vec2 pos){
    float az = (pos.x + 1) * PI;
    float ze = pos.y * PI / 2;
    float r = 1;

    float x = r * pow(cos(ze), 3) * cos(az);
    float y = r * pow(cos(ze), 3) * sin(az);
    float z = r * sin(ze);

    return vec3(x, y, z);
}

vec3 getSphere3Normal(vec2 pos){
    vec3 u = getSphere3(pos + vec2(0.001, 0)) - getSphere3(pos - vec2(0.001, 0));
    vec3 v = getSphere3(pos + vec2(0, 0.001)) - getSphere3(pos - vec2(0, 0.001));
    return cross(u, v);
}

// kartezka 1
vec3 getHill(vec2 pos){
    float z = 0.5 * cos(sqrt(20 * pow(pos.x, 2) + 20 * pow(pos.y, 2)));;
    return vec3(pos.x, pos.y, z);
}

vec3 getHillNormal(vec2 pos){
    vec3 u = getHill(pos + vec2(0.001, 0)) - getHill(pos - vec2(0.001, 0));
    vec3 v = getHill(pos + vec2(0, 0.001)) - getHill(pos - vec2(0, 0.001));
    return cross(u, v);
}

// kartezka 2
vec3 getDepression(vec2 pos){
    float z = sin(sqrt(pow(pos.x, 2) + pow(pos.y, 2)));;
    return vec3(pos.x, pos.y, z);
}

vec3 getDepressionNormal(vec2 pos){
    vec3 u = getDepression(pos + vec2(0.001, 0)) - getDepression(pos - vec2(0.001, 0));
    vec3 v = getDepression(pos + vec2(0, 0.001)) - getDepression(pos - vec2(0, 0.001));
    return cross(u, v);
}

// cylindricka 1
vec3 getSombrero(vec2 pos){
    // <-1;1> -> <0;2> -> <0; 2PI>
    float az = (pos.y + 1) * PI;
    // <-1;1> -> <0;2> -> <0;2PI>
    float r = (pos.x + 1) * PI;
    float h = 2 * sin(r);

    float x = r * cos(az) * 0.25;
    float y = r * sin(az) * 0.25;
    float z = h * 0.25;

    return vec3(x, y, z);
}

vec3 getSombreroNormal(vec2 pos){
    vec3 u = getSombrero(pos + vec2(0.001, 0)) - getSombrero(pos - vec2(0.001, 0));
    vec3 v = getSombrero(pos + vec2(0, 0.001)) - getSombrero(pos - vec2(0, 0.001));
    return cross(u, v);
}

// cylindricka 2
vec3 getCylindric2(vec2 pos){
    // <-1;1> -> <0;2> -> <0;2PI>
    float az = (pos.y + 1) * PI;
    // <-1;1> -> <-3;3>
    float r = pos.x * 3;
    float h = r;

    float x = 0.25 * 3 * cos(az) * sin(r);
    float y = 0.25 * 3 * sin(az) * cos(r);
    float z = 0.25 * h;

    return vec3(x, y, z);
}

vec3 getCylindric2Normal(vec2 pos){
    vec3 u = getCylindric2(pos + vec2(0.001, 0)) - getCylindric2(pos - vec2(0.001, 0));
    vec3 v = getCylindric2(pos + vec2(0, 0.001)) - getCylindric2(pos - vec2(0, 0.001));
    return cross(u, v);
}

vec3 getPlane(vec2 pos){
    if (transformInTimeMode){
        return vec3(pos * 3, sin(pos.x * 5 + time/50));
    }
    return vec3(pos * 3, -1);
}

vec3 getPlaneNormal(vec2 pos){
    vec3 u = getPlane(pos + vec2(0.001, 0)) - getPlane(pos - vec2(0.001, 0));
    vec3 v = getPlane(pos + vec2(0, 0.001)) - getPlane(pos - vec2(0, 0.001));
    return cross(u, v);
}

void main() {
    texCoord = inPosition;
    // zmenit jeji rozsah
    vec2 position = inPosition * 2 - 1;

    vec3 pos3;
    mat4 modelMatrix;
    if (solid == 1){
        if (solid1Type == 0){
            pos3 = getSphere(position);
            normal = getSphereNormal(position);
        } else if (solid1Type == 1){
            pos3 = getSphere2(position);
            normal = getSphere2Normal(position);
        } else if (solid1Type == 2){
            pos3 = getSphere3(position);
            normal = getSphere3Normal(position);
        } else if (solid1Type == 3){
            pos3 = getHill(position);
            normal = getHillNormal(position);
        } else if (solid1Type == 4){
            pos3 = getDepression(position);
            normal = getDepressionNormal(position);
        } else if (solid1Type == 5){
            pos3 = getSombrero(position);
            normal = getSombreroNormal(position);
        } else if (solid1Type == 6){
            pos3 = getCylindric2(position);
            normal = getCylindric2Normal(position);
        }
        modelMatrix = model;
    } else if (solid == 2){
        pos3 = getPlane(position);
        normal = getPlaneNormal(position);
    } else if (solid == 3){
        pos3 = getSphereLight(position);
        pos3 = pos3 + lightPosition;
    }
    // modelovaci transformace aplikovany jen na solid 1
    if (solid != 1) modelMatrix = mat4(1);
    if (appMode != 4){
        normal = inverse(transpose(mat3(modelMatrix)))*normal;
    } else {
        // appMode = 4; ve vysledku zobrazena normala k pozorovateli
        normal = inverse(transpose(mat3(view * modelMatrix)))*normal;
    }
    if (appMode == 8){
        // pozice SS pozorovatel
        pos3View = inverse(transpose(mat3(view * modelMatrix)))*pos3;
    }

    vec4 modelMulPosition = modelMatrix * vec4(pos3, 1.0);
    mmpDehomog = modelMulPosition.xyz/modelMulPosition.w;

    gl_Position = projection * view * modelMulPosition;

    light = lightPosition - mmpDehomog;
    viewDirection = eyePosition - mmpDehomog;
    distanceFromLight = length(light);

    // ziskavame pozici vrcholu, tak jak ten vrchol vidi svetlo
    depthTextureCoord = lightVP * modelMulPosition;
    // XY jako souradnice v obrazovce a Z jako vzdalenost od pozorovatele (v tomto pripade svetla)
    // dehomogenizace; po slozkach vydeleno w
    depthTextureCoord.xyz = depthTextureCoord.xyz / depthTextureCoord.w;
    // obrazovka je <-1;1>
    // textura je <0;1>
    depthTextureCoord.xyz = (depthTextureCoord.xyz + 1) / 2;
}