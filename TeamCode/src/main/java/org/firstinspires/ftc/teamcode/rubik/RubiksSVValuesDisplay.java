package org.firstinspires.ftc.teamcode.rubik;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;


import org.openftc.easyopencv.*;
import org.opencv.core.Point;

@TeleOp(name = "Rubiks SV Values Display", group = "Rubiks")
public class RubiksSVValuesDisplay extends LinearOpMode {

    private DcMotor baseRotate;
    private DcMotor faceTurnMotor;
    private Servo gripper;

    private OpenCvCamera webcam;
    private StickerColorPipeline pipeline;

    private RubiksCubeSVState cubeState = new RubiksCubeSVState();

    private char currentFace = 'U';   // U,R,F,D,L,B
    private int currentSticker = 0;   // 0..8

    private boolean dpadUpPrev, dpadDownPrev, dpadLeftPrev, dpadRightPrev;
    private boolean aPrev, bPrev, yPrev, rbPrev;

    private enum State { INPUT, SHOW_SOLUTION }
    private State state = State.INPUT;

    private String[] solutionMoves = new String[0];
    private int moveIndex = 0;

    @Override
    public void runOpMode() throws InterruptedException {

        // motors (safe try block)
        try {
            baseRotate = hardwareMap.get(DcMotor.class, "baseRotate");
            faceTurnMotor = hardwareMap.get(DcMotor.class, "faceTurnMotor");
            gripper = hardwareMap.get(Servo.class, "gripper");
        } catch (Exception ignored) {}

        telemetry.addLine("Initializing webcam...");
        telemetry.update();

        // Create pipeline
        pipeline = new StickerColorPipeline();

        // Find monitor view ID
        int camId = hardwareMap.appContext.getResources()
                .getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());

        // Create webcam
        webcam = OpenCvCameraFactory.getInstance()
                .createWebcam(hardwareMap.get(WebcamName.class, "Webcam 1"), camId);

        webcam.setPipeline(pipeline);

        webcam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                // Use 640x480 for Logitech C270
                webcam.startStreaming(640, 480, OpenCvCameraRotation.UPRIGHT);
            }

            @Override
            public void onError(int errorCode) {}
        });

        telemetry.addLine("Ready!");
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

            sleep(50);
        }
    }

    // ========== INPUT MODE ==========
    private void updateInputControls() {

        // Change face selection
        if (gamepad1.dpad_right && !dpadRightPrev) {
            currentFace = nextFace(currentFace);
            currentSticker = 0;
        }
        if (gamepad1.dpad_left && !dpadLeftPrev) {
            currentFace = prevFace(currentFace);
            currentSticker = 0;
        }

        // Change sticker index manually
        if (gamepad1.dpad_up && !dpadUpPrev)
            currentSticker = (currentSticker + 1) % 9;

        if (gamepad1.dpad_down && !dpadDownPrev)
            currentSticker = (currentSticker + 8) % 9;

        // ========= SINGLE STICKER SCAN (A) =========
        if (gamepad1.a && !aPrev) {
            double[] sv = pipeline.sampleIndexSV(currentSticker);
            cubeState.setSticker(currentFace, currentSticker, sv[0], sv[1]);
        }

        // ========= FULL FACE SCAN (B) =========
        if (gamepad1.b && !bPrev) {
            for (int i = 0; i < 9; i++) {
                double[] sv = pipeline.sampleIndexSV(i);
                cubeState.setSticker(currentFace, i, sv[0], sv[1]);
            }

            telemetry.addData("Scanned face:", currentFace);
            telemetry.update();
            sleep(400);

            // auto-advance to next face
            currentFace = nextFace(currentFace);
            currentSticker = 0;
        }

        // Grid adjustment
        if (gamepad1.dpad_up)    pipeline.offsetY -= 2;
        if (gamepad1.dpad_down)  pipeline.offsetY += 2;
        if (gamepad1.dpad_left)  pipeline.offsetX -= 2;
        if (gamepad1.dpad_right) pipeline.offsetX += 2;

        if (gamepad1.left_bumper)  pipeline.spacing -= 2;
        if (gamepad1.right_bumper) pipeline.spacing += 2;

        if (pipeline.spacing < 60)  pipeline.spacing = 60;
        if (pipeline.spacing > 200) pipeline.spacing = 200;

        dpadUpPrev = gamepad1.dpad_up;
        dpadDownPrev = gamepad1.dpad_down;
        dpadLeftPrev = gamepad1.dpad_left;
        dpadRightPrev = gamepad1.dpad_right;
        aPrev = gamepad1.a;
        bPrev = gamepad1.b;
        rbPrev = gamepad1.right_bumper;
    }

    private void showInputScreen() {
        telemetry.clearAll();
        telemetry.addLine("=== INPUT CUBE SV VALUES ===");
        telemetry.addData("Face", currentFace);
        telemetry.addData("Sticker", currentSticker);
        telemetry.addLine("");

        String[] f = cubeState.getFaceSVStrings(currentFace);
        telemetry.addLine(row(f,0));
        telemetry.addLine(row(f,3));
        telemetry.addLine(row(f,6));

        telemetry.addLine("");
        telemetry.addLine("A = scan one sticker");
        telemetry.addLine("B = scan whole face (9 stickers)");
        telemetry.addLine("Dpad = change sticker/face");
        telemetry.update();
    }

    // =============================================
    //   SOLUTION MODE
    // =============================================
    private void updateSolutionControls() {
        if (gamepad1.a && !aPrev)
            if (moveIndex < solutionMoves.length) moveIndex++;

        if (gamepad1.b && !bPrev)
            if (moveIndex < solutionMoves.length) moveIndex++;

        if (gamepad1.y && !yPrev) {
            cubeState = new RubiksCubeSVState();
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
        telemetry.addData("Step", moveIndex + 1);
        telemetry.addData("Total", solutionMoves.length);

        if (moveIndex < solutionMoves.length) {
            telemetry.addData("Move", solutionMoves[moveIndex]);
        } else {
            telemetry.addLine("DONE!");
        }

        telemetry.update();
    }

    // =============================================
    //   Helper Methods
    // =============================================
    private String row(String[] f, int start) {
        return String.format("%s | %s | %s",
                f[start], f[start+1], f[start+2]);
    }

    private char nextFace(char f) {
        char[] faces = {'U','R','F','D','L','B'};
        for(int i=0;i<faces.length;i++)
            if(faces[i]==f) return faces[(i+1)%faces.length];
        return 'U';
    }

    private char prevFace(char f) {
        char[] faces = {'U','R','F','D','L','B'};
        for(int i=0;i<faces.length;i++)
            if(faces[i]==f) return faces[(i-1+faces.length)%faces.length];
        return 'U';
    }
}
