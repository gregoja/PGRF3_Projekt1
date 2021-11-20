#version 150

in vec2 inPosition;

uniform mat4 view;
uniform mat4 projection;
uniform mat4 model;

uniform int solid;
uniform bool transformInTimeMode;
uniform float time;
uniform int solid1Type;

const float PI = 3.14159265;

// sfericka 1
vec3 getSphere(vec2 pos){
    float az = pos.x * PI;// souradnice X je v <-1;1> a chceme v rozsahu <-PI; PI>
    float ze = pos.y * PI / 2;// souradnice Y je v <-1;1> a chceme v rozsahu <-PI/2; PI/2>
    float r = 1;

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

// kartezka 1
vec3 getHill(vec2 pos){
    float z = 0.5 * cos(sqrt(20 * pow(pos.x, 2) + 20 * pow(pos.y, 2)));;
    return vec3(pos.x, pos.y, z);
}

// kartezka 2
vec3 getDepression(vec2 pos){
    float z = sin(sqrt(pow(pos.x, 2) + pow(pos.y, 2)));;
    return vec3(pos.x, pos.y, z);
}

// cylindricka 1
vec3 getSombrero(vec2 pos){
    // <-1;1> -> <0;2> -> <0; 2PI>
    float az = (pos.y + 1) * PI;
    // <-1;1> -> <0;2> -> <0;2PI>
    float r = ((pos.x + 1) * PI);
    float h = 2 * sin(r);

    float x = r * cos(az) * 0.25;
    float y = r * sin(az) * 0.25;
    float z = h * 0.25;

    return vec3(x, y, z);
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

vec3 getPlane(vec2 pos){
    if (transformInTimeMode) return vec3(pos * 3, sin(pos.x * 5 + time/50));
    return vec3(pos * 3, -1);
}

void main() {
    vec2 position = inPosition * 2 - 1;

    vec3 pos3;
    mat4 modelMatrix;
    if (solid == 1){
        if (solid1Type == 0) pos3 = getSphere(position);
        else if (solid1Type == 1) pos3 = getSphere2(position);
        else if (solid1Type == 2) pos3 = getSphere3(position);
        else if (solid1Type == 3) pos3 = getHill(position);
        else if (solid1Type == 4) pos3 = getDepression(position);
        else if (solid1Type == 5) pos3 = getSombrero(position);
        else if (solid1Type == 6) pos3 = getCylindric2(position);
        modelMatrix = model;
    } else if (solid == 2){
        pos3 = getPlane(position);
    }
    if (solid != 1) modelMatrix = mat4(1);

    gl_Position = projection * view * modelMatrix * vec4(pos3, 1.0);
}