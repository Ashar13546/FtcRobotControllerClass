package org.firstinspires.ftc.teamcode.rubik;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name = "Rubiks Live Cube Input", group = "Rubiks")
public class RubiksLiveInput extends LinearOpMode {

    private DcMotor baseRotate;
    private DcMotor faceTurnMotor;
    private Servo gripper;

    private RubiksCubeState cubeState = new RubiksCubeState();

    private char currentFace = 'U';   // U,R,F,D,L,B
    private int currentSticker = 0;   // 0..8

    private boolean dpadUpPrev, dpadDownPrev, dpadLeftPrev, dpadRightPrev;
    private boolean aPrev, bPrev, xPrev, yPrev, lbPrev, ltPrev, rbPrev;

    private enum State { INPUT, SHOW_SOLUTION }
    private State state = State.INPUT;

    private String[] solutionMoves = new String[0];
    private int moveIndex = 0;

    @Override
    public void runOpMode() throws InterruptedException {
        // Optional hardware init
        try {
            baseRotate = hardwareMap.get(DcMotor.class, "baseRotate");
            faceTurnMotor = hardwareMap.get(DcMotor.class, "faceTurnMotor");
            gripper = hardwareMap.get(Servo.class, "gripper");
        } catch (Exception ignored) {}

        telemetry.addLine("Rubik's Live Input");
        telemetry.addLine("Use gamepad1 to input colors.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            if (state == State.INPUT) {
                updateInputControls();
                showCubeLive();
            } else {
                updateSolutionControls();
                showSolutionScreen();
            }

            sleep(60);
        }
    }

    // ---------------- INPUT ----------------
    private void updateInputControls() {
        // face navigation
        if (gamepad1.dpad_right && !dpadRightPrev) { currentFace = nextFace(currentFace); currentSticker = 0; }
        if (gamepad1.dpad_left && !dpadLeftPrev)  { currentFace = prevFace(currentFace); currentSticker = 0; }

        // sticker navigation
        if (gamepad1.dpad_up && !dpadUpPrev) { currentSticker = (currentSticker + 1) % 9; }
        if (gamepad1.dpad_down && !dpadDownPrev) { currentSticker = (currentSticker - 1 + 9) % 9; }

        // Kociemba correct colors: U=white, R=red, F=green, D=yellow, L=orange, B=blue
        if (gamepad1.a && !aPrev) cubeState.setSticker(currentFace, currentSticker, 'U'); // white
        if (gamepad1.b && !bPrev) cubeState.setSticker(currentFace, currentSticker, 'R'); // red
        if (gamepad1.x && !xPrev) cubeState.setSticker(currentFace, currentSticker, 'F'); // green
        if (gamepad1.y && !yPrev) cubeState.setSticker(currentFace, currentSticker, 'D'); // yellow
        if (gamepad1.left_bumper && !lbPrev) cubeState.setSticker(currentFace, currentSticker, 'L'); // orange
        if (gamepad1.left_trigger > 0.5 && !ltPrev) cubeState.setSticker(currentFace, currentSticker, 'B'); // blue

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

        dpadUpPrev = gamepad1.dpad_up;
        dpadDownPrev = gamepad1.dpad_down;
        dpadLeftPrev = gamepad1.dpad_left;
        dpadRightPrev = gamepad1.dpad_right;
        aPrev = gamepad1.a;
        bPrev = gamepad1.b;
        xPrev = gamepad1.x;
        yPrev = gamepad1.y;
        lbPrev = gamepad1.left_bumper;
        ltPrev = gamepad1.left_trigger > 0.5;
        rbPrev = gamepad1.right_bumper;
    }

    // ---------------- LIVE CUBE DISPLAY ----------------
    private void showCubeLive() {
        telemetry.clearAll();
        telemetry.addLine("=== LIVE CUBE INPUT ===");
        telemetry.addData("Face", currentFace);
        telemetry.addData("Sticker", currentSticker);

        // Show all 6 faces
        char[] faces = {'U','R','F','D','L','B'};
        for (char f : faces) {
            char[] c = cubeState.getFaceColors(f);
            telemetry.addLine(f + " Face:");
            for (int i=0; i<9; i+=3) {
                String row = "";
                for (int j=0; j<3; j++) {
                    int idx = i+j;
                    String s = colorSymbol(c[idx]);
                    if (f==currentFace && idx==currentSticker) s = "["+s+"]"; // highlight current
                    row += s + " ";
                }
                telemetry.addLine(row);
            }
            telemetry.addLine("");
        }

        telemetry.addLine("Controls:");
        telemetry.addLine("DPad L/R: change face, DPad U/D: change sticker");
        telemetry.addLine("A: White(U), B: Red(R), X: Green(F), Y: Yellow(D)");
        telemetry.addLine("LB: Orange(L), LT>0.5: Blue(B)");
        telemetry.addLine("RB: Solve");
        telemetry.update();
    }

    private String colorSymbol(char c) {
        switch(c) {
            case 'U': return "W";
            case 'R': return "R";
            case 'F': return "G";
            case 'D': return "Y";
            case 'L': return "O";
            case 'B': return "B";
            case '-': return "-";
        }
        return "?";
    }

    // ---------------- SOLUTION ----------------
    private void updateSolutionControls() {
        if (gamepad1.a && !aPrev) { if (moveIndex<solutionMoves.length) moveIndex++; }
        if (gamepad1.b && !bPrev) { if (moveIndex<solutionMoves.length) moveIndex++; }
        if (gamepad1.y && !yPrev) {
            cubeState = new RubiksCubeState();
            currentFace = 'U'; currentSticker = 0; state = State.INPUT;
        }

        aPrev = gamepad1.a; bPrev = gamepad1.b; yPrev = gamepad1.y;
    }

    private void showSolutionScreen() {
        telemetry.clearAll();
        telemetry.addLine("=== SOLUTION ===");
        telemetry.addData("Total Moves", solutionMoves.length);
        telemetry.addData("Current Index", moveIndex+1);
        telemetry.addLine("");

        if (moveIndex < solutionMoves.length) {
            String mv = solutionMoves[moveIndex];
            telemetry.addData("Move", mv);
            telemetry.addLine(explainMove(mv));
            telemetry.addLine("");
            telemetry.addLine("Press A after you do this move.");
            telemetry.addLine("Press B to skip.");
        } else {
            telemetry.addLine("Done! Cube should be solved.");
            telemetry.addLine("Press Y to start over.");
        }
        telemetry.update();
    }

    // ---------------- HELPERS ----------------
    private char nextFace(char f) { char[] faces = {'U','R','F','D','L','B'}; for(int i=0;i<faces.length;i++) if(faces[i]==f) return faces[(i+1)%faces.length]; return 'U'; }
    private char prevFace(char f) { char[] faces = {'U','R','F','D','L','B'}; for(int i=0;i<faces.length;i++) if(faces[i]==f) return faces[(i-1+faces.length)%faces.length]; return 'U'; }
    private String explainMove(String mv) { char face=mv.charAt(0); boolean prime=mv.contains("'"); boolean doubleTurn=mv.contains("2"); return "Turn "+face+" "+(doubleTurn?"180°":"90°")+(prime?" counter-clockwise":" clockwise"); }
}
