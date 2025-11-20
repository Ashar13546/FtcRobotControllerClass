package org.firstinspires.ftc.teamcode.rubik;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;

@TeleOp(name = "Rubiks SV Debug Viewer", group = "Rubiks")
public class RubiksSVDebugViewer extends LinearOpMode {

    // =============================
    //  SV THRESHOLD VARIABLES
    // =============================
    // You can tune these during testing by looking at telemetry
    // and then copying good ranges into your classifier later.

    // WHITE
    double WHITE_S_MAX = 10;
    double WHITE_V_MIN = 200;

    // RED
    double RED_S_MIN = 195;
    double RED_V_MIN = 145;

    // ORANGE
    double ORANGE_S_MIN = 200;
    double ORANGE_V_MIN = 222;

    // YELLOW
    double YELLOW_S_MIN = 175;
    double YELLOW_V_MIN = 203;

    // GREEN
    double GREEN_S_MIN = 125;
    double GREEN_V_MIN = 150;

    // BLUE
    double BLUE_S_MIN = 195;
    double BLUE_V_MIN = 160;

    // =============================

    private DcMotor baseRotate;
    private DcMotor faceTurnMotor;
    private Servo gripper;

    private OpenCvCamera webcam;
    private StickerColorPipeline pipeline;

    private char currentFace = 'U';   // U,R,F,D,L,B

    private boolean dpadLeftPrev, dpadRightPrev;
    private boolean aPrev, bPrev;

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

        telemetry.addLine("SV Debug Viewer Ready!");
        telemetry.addLine("Dpad Left/Right = change face label (U,R,F,D,L,B)");
        telemetry.addLine("B = scan full face (9 stickers) and print S/V");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {

            updateControls();

            showHud();

            sleep(50);
        }
    }

    private void updateControls() {
        // Change face label only (for your reference)
        if (gamepad1.dpad_right && !dpadRightPrev) {
            currentFace = nextFace(currentFace);
        }
        if (gamepad1.dpad_left && !dpadLeftPrev) {
            currentFace = prevFace(currentFace);
        }

        // Optional: single sticker sample on A (center sticker index = 4)
        if (gamepad1.a && !aPrev) {
            double[] sv = pipeline.sampleIndexSV(4);
            telemetry.addLine("Single Sticker (index 4) S/V:");
            telemetry.addData("S", sv[0]);
            telemetry.addData("V", sv[1]);
            telemetry.update();
        }

        // B = scan whole face (9 stickers) and print all S/V values
        if (gamepad1.b && !bPrev) {
            telemetry.clearAll();
            telemetry.addLine("=== FULL FACE SCAN SV VALUES ===");
            telemetry.addData("Face label", currentFace);
            telemetry.addLine("");

            for (int i = 0; i < 9; i++) {
                double[] sv = pipeline.sampleIndexSV(i);
                telemetry.addData("Idx " + i, "S: %.1f  V: %.1f", sv[0], sv[1]);
            }

            telemetry.addLine("");
            telemetry.addLine("Thresholds (current):");
            telemetry.addData("WHITE",  "S<=%.0f  V>=%.0f", WHITE_S_MAX, WHITE_V_MIN);
            telemetry.addData("RED",    "S>=%.0f  V>=%.0f", RED_S_MIN,   RED_V_MIN);
            telemetry.addData("ORANGE", "S>=%.0f  V>=%.0f", ORANGE_S_MIN, ORANGE_V_MIN);
            telemetry.addData("YELLOW", "S>=%.0f  V>=%.0f", YELLOW_S_MIN, YELLOW_V_MIN);
            telemetry.addData("GREEN",  "S>=%.0f  V>=%.0f", GREEN_S_MIN,  GREEN_V_MIN);
            telemetry.addData("BLUE",   "S>=%.0f  V>=%.0f", BLUE_S_MIN,   BLUE_V_MIN);

            telemetry.update();
        }

        dpadLeftPrev  = gamepad1.dpad_left;
        dpadRightPrev = gamepad1.dpad_right;
        aPrev         = gamepad1.a;
        bPrev         = gamepad1.b;
    }

    private void showHud() {
        telemetry.addLine();
        telemetry.addLine("=== SV Debug HUD ===");
        telemetry.addData("Face label", currentFace);
        telemetry.addLine("B = Scan full face (prints S/V for idx 0..8)");
        telemetry.addLine("A = Sample center (idx 4) once");
        telemetry.update();
    }

    private char nextFace(char f) {
        char[] faces = {'U','R','F','D','L','B'};
        for (int i = 0; i < faces.length; i++)
            if (faces[i] == f) return faces[(i + 1) % faces.length];
        return 'U';
    }

    private char prevFace(char f) {
        char[] faces = {'U','R','F','D','L','B'};
        for (int i = 0; i < faces.length; i++)
            if (faces[i] == f) return faces[(i - 1 + faces.length) % faces.length];
        return 'U';
    }
}
