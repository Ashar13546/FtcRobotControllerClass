package org.firstinspires.ftc.teamcode.rubik;

public class RubiksCubeState {

    // Sticker order for Kociemba/min2phase:
    // U (0-8) R (9-17) F (18-26) D (27-35) L (36-44) B (45-53)
    private final char[] stickers = new char[54];

    public RubiksCubeState() {
        for (int i = 0; i < 54; i++) {
            stickers[i] = '-';
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

    public void setSticker(char face, int pos, char color) {
        int offset = getFaceOffset(face);
        if (pos < 0 || pos > 8) return;
        stickers[offset + pos] = color;
    }

    public char[] getFaceColors(char face) {
        char[] result = new char[9];
        int offset = getFaceOffset(face);
        System.arraycopy(stickers, offset, result, 0, 9);
        return result;
    }

    public boolean isComplete() {
        for (char c : stickers) {
            if (c == '-') return false;
        }
        return true;
    }

    /** Facelet string in min2phase format */
    public String toKociembaString() {
        return new String(stickers);
    }
}
