package org.firstinspires.ftc.teamcode.rubik;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;


import org.openftc.easyopencv.*;
import org.opencv.core.Point;

@TeleOp(name = "Rubiks Cube Solver 2 Webcams", group = "Rubiks")
public class RubiksTwoWebcams extends LinearOpMode {

    private DcMotor baseRotate;
    private DcMotor faceTurnMotor;
    private Servo gripper;

    // *** MODIFIED: Renamed 'webcam' to 'webcam1' and added 'webcam2' ***
    private OpenCvCamera webcam1;
    private OpenCvCamera webcam2;
    // *** NEW: Variable to track which camera is currently being used for scanning/pipeline ***
    private OpenCvCamera currentWebcam;

    private StickerColorPipeline pipeline;

    private RubiksCubeState cubeState = new RubiksCubeState();

    private char currentFace = 'U';   // U,R,F,D,L,B
    private int currentSticker = 0;   // 0..8

    private boolean dpadUpPrev, dpadDownPrev, dpadLeftPrev, dpadRightPrev;
    private boolean aPrev, bPrev, yPrev, rbPrev;

    // *** NEW: State for gamepad2 button and active camera tracking ***
    private boolean gamepad2XPrev;
    private boolean isWebcam1Active = true;

    private enum State { INPUT, SHOW_SOLUTION }
    private State state = State.INPUT;

    private String[] solutionMoves = new String[0];
    private int moveIndex = 0;

    // *** NEW: Camera Name variables for clarity ***
    private String webcam1Name = "Webcam 1";
    private String webcam2Name = "Webcam 2";

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

        // *** MODIFIED: Find monitor view IDs for BOTH webcams ***
        int camId1 = hardwareMap.appContext.getResources()
                .getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        int camId2 = hardwareMap.appContext.getResources()
                .getIdentifier("cameraMonitorViewId2", "id", hardwareMap.appContext.getPackageName());

        // *** MODIFIED: Create and initialize BOTH webcams ***
        webcam1 = OpenCvCameraFactory.getInstance()
                .createWebcam(hardwareMap.get(WebcamName.class, webcam1Name), camId1);
        webcam2 = OpenCvCameraFactory.getInstance()
                .createWebcam(hardwareMap.get(WebcamName.class, webcam2Name), camId2);

        // Only set the pipeline on the camera used for scanning (Webcam 1 to start)
        webcam1.setPipeline(pipeline);
        currentWebcam = webcam1;

        // *** MODIFIED: Open and start streaming BOTH cameras asynchronously ***
        webcam1.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                // Use 640x480 for Logitech C270
                webcam1.startStreaming(640, 480, OpenCvCameraRotation.UPRIGHT);
            }

            @Override
            public void onError(int errorCode) {}
        });

        webcam2.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                // Start the stream for the second view
                webcam2.startStreaming(640, 480, OpenCvCameraRotation.UPRIGHT);
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

            // *** NEW: Check for camera switch button (gamepad2.x) ***
            if (gamepad2.x && !gamepad2XPrev) {
                switchWebcamPipeline();
            }
            gamepad2XPrev = gamepad2.x;

            sleep(50);
        }

        // *** NEW: Stop both streams on exit ***
        webcam1.stopStreaming();
        webcam2.stopStreaming();
    }

    // *** NEW: Method to switch which camera is feeding the scanning pipeline ***
    private void switchWebcamPipeline() {
        if (currentWebcam == webcam1) {
            // Stop pipeline on webcam1, start pipeline on webcam2
            webcam1.setPipeline(null);
            webcam2.setPipeline(pipeline);
            currentWebcam = webcam2;
            isWebcam1Active = false;
        } else {
            // Stop pipeline on webcam2, start pipeline on webcam1
            webcam2.setPipeline(null);
            webcam1.setPipeline(pipeline);
            currentWebcam = webcam1;
            isWebcam1Active = true;
        }

        telemetry.addData("Scanning Webcam Switched To", isWebcam1Active ? webcam1Name : webcam2Name);
        telemetry.update();
        sleep(200);
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
            StickerColorPipeline.CubeColor c = pipeline.sampleIndex(currentSticker);
            cubeState.setSticker(currentFace, currentSticker, mapColor(c));
        }

        // ========= FULL FACE SCAN (B) =========
        if (gamepad1.b && !bPrev) {
            for (int i = 0; i < 9; i++) {
                StickerColorPipeline.CubeColor c = pipeline.sampleIndex(i);
                cubeState.setSticker(currentFace, i, mapColor(c));
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


        // ========= SOLVE (RB) =========
        if (gamepad1.right_bumper && !rbPrev) {
            if (!cubeState.isComplete()) {
                telemetry.addLine("Cube incomplete!");
                telemetry.update();
                sleep(800);
            } else {
                String solution = CubeSolver.solve(cubeState);
                if (solution.equals("INVALID")) {
                    telemetry.addLine("Invalid cube!");
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
        rbPrev = gamepad1.right_bumper;
    }

    private void showInputScreen() {
        telemetry.clearAll();
        telemetry.addLine("=== INPUT CUBE COLORS ===");
        // *** NEW: Display active scanning camera ***
        telemetry.addData("Scanning Cam", isWebcam1Active ? webcam1Name : webcam2Name);
        telemetry.addData("Face", currentFace);
        telemetry.addData("Sticker", currentSticker);
        telemetry.addLine("");

        char[] f = cubeState.getFaceColors(currentFace);
        telemetry.addLine(row(f,0));
        telemetry.addLine(row(f,3));
        telemetry.addLine(row(f,6));

        telemetry.addLine("");
        telemetry.addLine("A = scan one sticker");
        telemetry.addLine("B = scan whole face (9 stickers)");
        telemetry.addLine("RB = Solve");
        telemetry.addLine("Dpad = change sticker/face");
        // *** NEW: Note for switching camera pipeline ***
        telemetry.addLine("Gamepad2 X = Switch Scanning Camera");
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
        // *** NEW: Display active scanning camera ***
        telemetry.addData("Scanning Cam", isWebcam1Active ? webcam1Name : webcam2Name);
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
    private String row(char[] f, int start) {
        return String.format("%s %s %s",
                f[start], f[start+1], f[start+2]);
    }

    private char mapColor(StickerColorPipeline.CubeColor c) {
        switch (c) {
            case WHITE:  return 'U';  // white = up
            case YELLOW: return 'D';  // yellow = down
            case RED:    return 'F';  // red = front
            case ORANGE: return 'B';  // orange = back
            case BLUE:   return 'R';  // blue = right
            case GREEN:  return 'L';  // green = left
            default:     return '-';
        }
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