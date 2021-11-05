#version 150

in vec2 inPosition;

uniform mat4 view;
uniform mat4 projection;

uniform int solid;

const float PI = 3.14159265;

vec3 getSphere(vec2 pos){
    float az = pos.x * PI; // souradnice X je v <-1;1> a chceme v rozsahu <-PI; PI>
    float ze = pos.y * PI / 2; // souradnice Y je v <-1;1> a chceme v rozsahu <-PI/2; PI/2>
    float r = 1;

    float x = r * cos(az) * cos(ze);
    float y = 2 * r * sin(az) * cos(ze);
    float z = 0.5 * r * sin(ze);

    return vec3(x ,y,z);
}

vec3 getPlane(vec2 pos){
    return vec3(pos * 3 ,-1);
    //return vec3(pos * 3 ,sin(pos.x*5));
}

void main() {
    vec2 position = inPosition * 2 - 1;

    vec3 pos3;
    if(solid == 1){
        pos3 = getSphere(position);
    }else if(solid == 2){
        pos3 = getPlane(position);
    }

    gl_Position = projection * view * vec4(pos3, 1.0);
}