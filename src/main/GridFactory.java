package main;

import lwjglutils.OGLBuffers;

public class GridFactory {

    /**
     * @param a počet vracholu na radku
     * @param b počet vrcholu ve sloupci
     * @return OGLBuffers
     */
    public static OGLBuffers createSimpleGrid(int a, int b) {

        float[] vb = createVertexBuffer(a,b);

        int[] ib = new int[(a - 1) * (b - 1) * 2 * 3];
        int index2 = 0;
        for (int j = 0; j < b - 1; j++) {
            int offset = j * a;
            for (int i = 0; i < a - 1; i++) {
                ib[index2++] = offset + i;
                ib[index2++] = offset + i + 1;
                ib[index2++] = offset + i + a;
                ib[index2++] = offset + i + a;
                ib[index2++] = offset + i + 1;
                ib[index2++] = offset + i + a + 1;
            }
        }

        OGLBuffers.Attrib[] attributes = {
                new OGLBuffers.Attrib("inPosition", 2)
        };
        return new OGLBuffers(vb, attributes, ib);
    }

    public static OGLBuffers createEfficientGrid(int a, int b){
        float[] vb = createVertexBuffer(a,b);

        int index=0;
        int[] ib = new int[2*a*2*b/2 + b*2];
        for (int j = 0; j < b; j+=2) {
            int offset = j * a;

            for (int i = 0; i < a; i++) {
                ib[index++] = offset + i;
                ib[index++] = offset + i + a;
            }
            ib[index++] = offset + a * 2 - 1;
            ib[index++] = offset + a * 2 - 1;

            // posledni zpetny pruchod neprovedu, pokud je to liche
            if((j == b -1) && b % 2 == 1) break;

            for (int i = 0; i < a; i++) {
                ib[index++] = offset + a * 3 - 1 - i;
                ib[index++] = offset + a * 2 - 1 - i;
            }
            ib[index++] = offset + a * 2;
            ib[index++] = offset + a * 2;
        }

        OGLBuffers.Attrib[] attributes = {
                new OGLBuffers.Attrib("inPosition", 2)
        };
        return new OGLBuffers(vb,attributes,ib);
    }

    private static float[] createVertexBuffer(int a, int b){
        float[] vb = new float[a * b * 2];
        int index = 0;
        for (int j = 0; j < b; j++) {
            for (int i = 0; i < a; i++) {
                vb[index++] = i / (float) (a - 1);
                vb[index++] = j / (float) (b - 1);
            }
        }
        return vb;
    }

}
