package org.firstinspires.ftc.teamcode.rubik;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name = "Rubiks Manual Input", group = "Rubiks")
public class RubiksManualInput extends LinearOpMode {

    // (Optional) Hardware if you later want auto-robot moves
    private DcMotor baseRotate;
    private DcMotor faceTurnMotor;
    private Servo gripper;

    private RubiksCubeState cubeState = new RubiksCubeState();

    private char currentFace = 'U';   // U,R,F,D,L,B
    private int currentSticker = 0;   // 0..8

    // edge detector flags
    private boolean dpadUpPrev, dpadDownPrev, dpadLeftPrev, dpadRightPrev;
    private boolean aPrev, bPrev, xPrev, yPrev, rbPrev;

    private enum State { INPUT, SHOW_SOLUTION }
    private State state = State.INPUT;

    private String[] solutionMoves = new String[0];
    private int moveIndex = 0;

    @Override
    public void runOpMode() throws InterruptedException {
        // Optional hardware init; safe even if not configured
        try {
            baseRotate = hardwareMap.get(DcMotor.class, "baseRotate");
            faceTurnMotor = hardwareMap.get(DcMotor.class, "faceTurnMotor");
            gripper = hardwareMap.get(Servo.class, "gripper");
        } catch (Exception ignored) {}

        telemetry.addLine("Rubik's Manual Input");
        telemetry.addLine("Use gamepad1 to input colors.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            if (state == State.INPUT) {
                updateInputControls();
                showInputScreen();
            } else {
                updateSolutionControls();
                showSolutionScreen();
            }

            sleep(60);
        }
    }

    // ========== INPUT MODE ==========

    private void updateInputControls() {
        // face navigation
        if (gamepad1.dpad_right && !dpadRightPrev) {
            currentFace = nextFace(currentFace);
            currentSticker = 0;
        }
        if (gamepad1.dpad_left && !dpadLeftPrev) {
            currentFace = prevFace(currentFace);
            currentSticker = 0;
        }

        // sticker navigation
        if (gamepad1.dpad_up && !dpadUpPrev) {
            currentSticker = (currentSticker + 1) % 9;
        }
        if (gamepad1.dpad_down && !dpadDownPrev) {
            currentSticker = (currentSticker - 1 + 9) % 9;
        }

        // color selection: U,R,F,D,L,B
        if (gamepad1.a && !aPrev) {
            cubeState.setSticker(currentFace, currentSticker, 'W'); // white
        }
        if (gamepad1.b && !bPrev) {
            cubeState.setSticker(currentFace, currentSticker, 'R'); // red
        }
        if (gamepad1.x && !xPrev) {
            cubeState.setSticker(currentFace, currentSticker, 'B'); // blue
        }
        if (gamepad1.y && !yPrev) {
            cubeState.setSticker(currentFace, currentSticker, 'Y'); // yellow
        }
        if (gamepad1.left_bumper) {
            cubeState.setSticker(currentFace, currentSticker, 'G'); // green
        }
        if (gamepad1.left_trigger > 0.5) {
            cubeState.setSticker(currentFace, currentSticker, 'O'); // orange
        }

        // RB: solve
        if (gamepad1.right_bumper && !rbPrev) {
            if (!cubeState.isComplete()) {
                telemetry.addLine("Cube not fully entered (some '-' remain).");
                telemetry.update();
                sleep(800);
            } else {
                String solution = CubeSolver.solve(cubeState);
                if (solution.equals("INVALID")) {
                    telemetry.addLine("Invalid cube state or solver failed.");
                    telemetry.update();
                    sleep(1500);
                } else {
                    solutionMoves = solution.trim().split("\\s+");
                    moveIndex = 0;
                    state = State.SHOW_SOLUTION;
                }
            }
        }

        // remember previous button states
        dpadUpPrev = gamepad1.dpad_up;
        dpadDownPrev = gamepad1.dpad_down;
        dpadLeftPrev = gamepad1.dpad_left;
        dpadRightPrev = gamepad1.dpad_right;
        aPrev = gamepad1.a;
        bPrev = gamepad1.b;
        xPrev = gamepad1.x;
        yPrev = gamepad1.y;
        rbPrev = gamepad1.right_bumper;
    }

    private void showInputScreen() {
        telemetry.clearAll();
        telemetry.addLine("=== INPUT CUBE COLORS ===");
        telemetry.addData("Face", faceName(currentFace));
        telemetry.addData("Sticker", stickerName(currentSticker));
        telemetry.addLine("");

        char[] f = cubeState.getFaceColors(currentFace);
        telemetry.addLine("Face layout (W=U,G=R,R=F,Y=D,B=L,O=B):");
        telemetry.addLine(row(f,0));
        telemetry.addLine(row(f,3));
        telemetry.addLine(row(f,6));
        telemetry.addLine("");

        telemetry.addLine("Controls:");
        telemetry.addLine("  DPad L/R: change face");
        telemetry.addLine("  DPad U/D: change sticker");
        telemetry.addLine("  A: U (white)");
        telemetry.addLine("  B: F (red)");
        telemetry.addLine("  X: L (blue)");
        telemetry.addLine("  Y: D (yellow)");
        telemetry.addLine("  LB: R (green)");
        telemetry.addLine("  LT>0.5: B (orange)");
        telemetry.addLine("  RB: Solve");
        telemetry.update();
    }

    private String row(char[] f, int start) {
        return String.format("  %s %s %s",
                colorSymbol(f[start]),
                colorSymbol(f[start+1]),
                colorSymbol(f[start+2]));
    }

    // ========== SOLUTION MODE ==========

    private void updateSolutionControls() {
        // A: "I did this move, go next"
        if (gamepad1.a && !aPrev) {
            if (moveIndex < solutionMoves.length) {
                // If you want robot to execute, call executeMove(solutionMoves[moveIndex]);
                moveIndex++;
            }
        }
        // B: just skip to next without robot motion
        if (gamepad1.b && !bPrev) {
            if (moveIndex < solutionMoves.length) {
                moveIndex++;
            }
        }
        // Y: reset to input
        if (gamepad1.y && !yPrev) {
            cubeState = new RubiksCubeState();
            currentFace = 'U';
            currentSticker = 0;
            state = State.INPUT;
        }

        aPrev = gamepad1.a;
        bPrev = gamepad1.b;
        yPrev = gamepad1.y;
    }

    private void showSolutionScreen() {
        telemetry.clearAll();
        telemetry.addLine("=== SOLUTION ===");
        telemetry.addData("Total Moves", solutionMoves.length);
        telemetry.addData("Current Index", moveIndex + 1);
        telemetry.addLine("");

        if (moveIndex < solutionMoves.length) {
            String mv = solutionMoves[moveIndex];
            telemetry.addData("Move", mv);
            telemetry.addLine(explainMove(mv));
            telemetry.addLine("");
            telemetry.addLine("Press A after you do this move.");
            telemetry.addLine("Press B to skip to next.");
        } else {
            telemetry.addLine("Done! Cube should be solved.");
            telemetry.addLine("Press Y to start over.");
        }
        telemetry.update();
    }

    // ========== Helper methods ==========

    private char nextFace(char f) {
        char[] faces = {'U','R','F','D','L','B'};
        for (int i=0;i<faces.length;i++) {
            if (faces[i]==f) return faces[(i+1)%faces.length];
        }
        return 'U';
    }

    private char prevFace(char f) {
        char[] faces = {'U','R','F','D','L','B'};
        for (int i=0;i<faces.length;i++) {
            if (faces[i]==f) return faces[(i-1+faces.length)%faces.length];
        }
        return 'U';
    }

    private String faceName(char f) {
        switch (f) {
            case 'U': return "U (Up / white)";
            case 'R': return "R (Right / green)";
            case 'F': return "F (Front / red)";
            case 'D': return "D (Down / yellow)";
            case 'L': return "L (Left / blue)";
            case 'B': return "B (Back / orange)";
        }
        return "?";
    }

    private String stickerName(int i) {
        String[] names = {
                "0: top-left", "1: top-center", "2: top-right",
                "3: mid-left", "4: CENTER",     "5: mid-right",
                "6: bottom-left","7: bottom-center","8: bottom-right"
        };
        return names[i];
    }

    private String colorSymbol(char c) {
        switch (c) {
            case 'U': return "W";
            case 'R': return "G";
            case 'F': return "R";
            case 'D': return "Y";
            case 'L': return "B";
            case 'B': return "O";
            case '-': return "-";
        }
        return "?";
    }

    private String explainMove(String mv) {
        char face = mv.charAt(0);
        boolean prime = mv.contains("'");
        boolean doubleTurn = mv.contains("2");

        String faceStr = faceName(face);
        String amount = doubleTurn ? "180°" : "90°";
        String dir = prime ? "counter-clockwise" : "clockwise";

        return "Turn " + faceStr + " " + amount + " " + dir;
    }

    @SuppressWarnings("unused")
    private void executeMove(String mv) {
        // TODO: map mv ("R", "R2", "R'") to your servos/motors if you want automation later.
    }
}