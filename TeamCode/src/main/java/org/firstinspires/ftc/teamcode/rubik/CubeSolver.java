package org.firstinspires.ftc.teamcode.rubik;

import org.firstinspires.ftc.teamcode.rubik.min2phase.Search;

public class CubeSolver {

    private static final Search search = new Search();

    public static String solve(RubiksCubeState state) {
        String facelets = state.toKociembaString();

        if (!isValidInput(facelets)) {
            return "INVALID";
        }

        // maxDepth 21, timeout 5 seconds (in nanoseconds), probeMax=0, probeMin=0
        String solution = search.solution(facelets, 21, 5_000_000_000L, 0, 0);

        if (solution == null || solution.startsWith("Error")) {
            return "INVALID";
        }

        return solution.trim();
    }

    private static boolean isValidInput(String facelets) {
        if (facelets == null || facelets.length() != 54) return false;

        int[] counts = new int[6];
        for (char c : facelets.toCharArray()) {
            int idx = colorIndex(c);
            if (idx < 0) return false;
            counts[idx]++;
        }
        for (int count : counts) {
            if (count != 9) return false;
        }
        return true;
    }

    private static int colorIndex(char c) {
        switch (c) {
            case 'U': return 0;
            case 'R': return 1;
            case 'F': return 2;
            case 'D': return 3;
            case 'L': return 4;
            case 'B': return 5;
            default:  return -1;
        }
    }
}