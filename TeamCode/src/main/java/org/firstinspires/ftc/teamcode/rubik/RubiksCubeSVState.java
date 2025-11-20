package org.firstinspires.ftc.teamcode.rubik;

public class RubiksCubeSVState {

    // Store S and V values for each sticker
    // U (0-8) R (9-17) F (18-26) D (27-35) L (36-44) B (45-53)
    private final double[] saturation = new double[54];
    private final double[] value = new double[54];
    private final boolean[] scanned = new boolean[54];

    public RubiksCubeSVState() {
        for (int i = 0; i < 54; i++) {
            saturation[i] = 0;
            value[i] = 0;
            scanned[i] = false;
        }
    }

    private int getFaceOffset(char face) {
        switch (face) {
            case 'U': return 0;
            case 'R': return 9;
            case 'F': return 18;
            case 'D': return 27;
            case 'L': return 36;
            case 'B': return 45;
            default:  return 0;
        }
    }

    public void setSticker(char face, int pos, double s, double v) {
        int offset = getFaceOffset(face);
        if (pos < 0 || pos > 8) return;
        int index = offset + pos;
        saturation[index] = s;
        value[index] = v;
        scanned[index] = true;
    }

    public String[] getFaceSVStrings(char face) {
        String[] result = new String[9];
        int offset = getFaceOffset(face);
        for (int i = 0; i < 9; i++) {
            int index = offset + i;
            if (scanned[index]) {
                result[i] = String.format("S%.0f V%.0f", saturation[index], value[index]);
            } else {
                result[i] = "---";
            }
        }
        return result;
    }

    public boolean isComplete() {
        for (boolean s : scanned) {
            if (!s) return false;
        }
        return true;
    }
}
