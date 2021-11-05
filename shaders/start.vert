#version 150
// in se meni se s kazdym novym vrcholem, ktery tam prijde
in vec2 inPosition; // input from the vertex buffer

// uniform je typ hodnoty, ktera kdyz ji nastavite, tak bude pro cely ten cyklus vykresleni telesea stejna
uniform mat4 view;
uniform mat4 projection;

// priznak telesa
uniform int solid;
uniform vec3 lightPosition;
uniform vec3 eyePosition;

out vec2 texCoord;
out vec3 normal;
out vec3 light;
out vec3 viewDirection;

const float PI = 3.14159265;

vec3 getSphere(vec2 pos){
	// s vyhodou vyuzito ze grid je od 0 do 1

	float az = pos.x * PI; // souradnice X je v <-1;1> a chceme v rozsahu <-PI; PI>
	float ze = pos.y * PI / 2; // souradnice Y je v <-1;1> a chceme v rozsahu <-PI/2; PI/2>
	float r = 1;

	float x = r * cos(az) * cos(ze);
	float y = 2 * r * sin(az) * cos(ze);
	float z = 0.5 * r * sin(ze);

	return vec3(x ,y,z);
}

vec3 getSpehereNormal(vec2 pos){
	vec3 u = getSphere(pos + vec2(0.001,0)) - getSphere(pos - vec2(0.001,0));
	vec3 v = getSphere(pos + vec2(0,0.001)) - getSphere(pos - vec2(0,0.001));
	return cross(u,v);
}

vec3 getSpehereNormalByDerivative(vec2 pos){
	float az = pos.x * PI;
	float ze = pos.y * PI / 2;
	//x = cos(u * PI) * cos(v * PI/2)¨;
	//y = 2* sin(u * PI) * cos(v * PI/2)¨;
	//z = 0.5 * 1 * sin(v * PI/2)¨;

	vec3 u = vec3(-sin(az) * cos(ze) * PI, cos(az) * cos(ze) * PI,0);
	vec3 v = vec3(cos(az) * -sin(ze) * PI/ 2,sin(az) * -sin(ze) * PI / 2, cos(ze) * PI/2);
	return cross(u,v);
}

vec3 getPlane(vec2 pos){
	return vec3(pos * 3 ,-1);
	//return vec3(pos * 3 ,sin(pos.x*5));
}

vec3 getPlaneNormal(vec2 pos){
	vec3 u = getPlane(pos + vec2(0.001,0)) - getPlane(pos - vec2(0.001,0));
	vec3 v = getPlane(pos + vec2(0,0.001)) - getPlane(pos - vec2(0,0.001));
	return cross(u,v);
}

void main() {
	texCoord = inPosition;
	// zmenit jeji rozsah
	vec2 position = inPosition * 2 - 1;

	vec3 pos3;
	if(solid == 1){
		pos3 = getSphere(position);
		normal = getSpehereNormal(position);
	}else if(solid == 2){
		pos3 = getPlane(position);
		normal = getPlaneNormal(position);
	}

	gl_Position = projection * view * vec4(pos3, 1.0);

	light = lightPosition - pos3;
	viewDirection = eyePosition - pos3;
}
