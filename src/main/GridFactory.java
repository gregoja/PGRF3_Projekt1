package main;

import lwjglutils.OGLBuffers;

public class GridFactory {

    /**
     * @param a pocet vracholu na radku
     * @param b pocet vrcholu ve sloupci
     * @return OGLBuffers
     */
    public static OGLBuffers createSimpleGrid(int a, int b) {
        float[] vb = createVertexBuffer(a, b);

        int[] ib = new int[(a - 1) * (b - 1) * 2 * 3];
        int index = 0;
        for (int j = 0; j < b - 1; j++) {
            int offset = j * a;
            for (int i = 0; i < a - 1; i++) {
                ib[index++] = offset + i;
                ib[index++] = offset + i + 1;
                ib[index++] = offset + i + a;
                ib[index++] = offset + i + a;
                ib[index++] = offset + i + 1;
                ib[index++] = offset + i + a + 1;
            }
        }

        OGLBuffers.Attrib[] attributes = {
                new OGLBuffers.Attrib("inPosition", 2)
        };
        return new OGLBuffers(vb, attributes, ib);
    }

    public static OGLBuffers createEfficientGrid(int a, int b) {
        float[] vb = createVertexBuffer(a, b);

        int index = 0;
        int[] ib = new int[((b - 1) * (a * 2 + 2))];
        for (int j = 0; j < b - 1; j++) {
            if (j % 2 == 0) {
                for (int i = 0; i < a; i++) {
                    ib[index++] = (i + j * a);
                    ib[index++] = (i + (j + 1) * a);
                }
                ib[index++] = (a - 1 + (j + 1) * a);
                ib[index++] = (a - 1 + (j + 1) * a);
            } else {
                for (int i = 0; i < a; i++) {
                    ib[index++] = ((a - 1) - i + (j + 1) * a);
                    ib[index++] = ((a - 1) - i + j * a);
                }
                ib[index++] = (j + 1) * a;
                ib[index++] = (j + 1) * a;
            }
        }


        OGLBuffers.Attrib[] attributes = {
                new OGLBuffers.Attrib("inPosition", 2)
        };
        return new OGLBuffers(vb, attributes, ib);
    }

    private static float[] createVertexBuffer(int a, int b) {
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
