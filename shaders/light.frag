#version 150

out vec4 outColor;

void main(){
    // souradnice toho prave zpracovavaneho fragmentu
    // zajima nas hlavne z
    outColor = vec4(gl_FragCoord.zzz,1.0);
}