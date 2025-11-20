package org.firstinspires.ftc.teamcode.rubik;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.openftc.easyopencv.*;

@TeleOp(name = "Rubiks Color Calibration Tool", group = "Rubiks")
public class RubiksColorCalibration extends LinearOpMode {

    private OpenCvCamera webcam;
    private StickerColorPipeline pipeline;

    private int currentSticker = 0;   // 0..8
    // --- ADDED PREV VARS FOR DPAD L/R (FIX) ---
    private boolean dpadUpPrev, dpadDownPrev, dpadRightPrev, dpadLeftPrev;
    private boolean aPrev;

    // Store min/max values for each color
    private double[] minH = new double[6];
    private double[] maxH = new double[6];
    private double[] minS = new double[6];
    private double[] maxS = new double[6];
    private double[] minV = new double[6];
    private double[] maxV = new double[6];

    private String[] colorNames = {"WHITE", "RED", "ORANGE", "YELLOW", "GREEN", "BLUE"};
    private int currentColor = 0; // Which color we're calibrating

    private boolean recordingMode = false;

    // --- MATS FOR REUSING (FIX) ---
    private org.opencv.core.Mat bgrMat;
    private org.opencv.core.Mat hsvMat;
    // ------------------------------

    @Override
    public void runOpMode() throws InterruptedException {

        // Initialize min/max arrays
        for (int i = 0; i < 6; i++) {
            minH[i] = 999;
            maxH[i] = -1;
            minS[i] = 999;
            maxS[i] = -1;
            minV[i] = 999;
            maxV[i] = -1;
        }

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
                webcam.startStreaming(640, 480, OpenCvCameraRotation.UPRIGHT);
            }

            @Override
            public void onError(int errorCode) {}
        });

        telemetry.addLine("Ready!");
        telemetry.update();

        // --- INITIALIZE MATS (FIX) ---
        bgrMat = new org.opencv.core.Mat(1, 1, org.opencv.core.CvType.CV_8UC3);
        hsvMat = new org.opencv.core.Mat();
        // -----------------------------

        waitForStart();

        while (opModeIsActive()) {
            updateControls();
            showDebugScreen();
            sleep(100);
        }
    }

    // --- THIS METHOD IS NOW FULLY FIXED (FIX) ---
    private void updateControls() {
        // Change sticker position
        if (gamepad1.dpad_up && !dpadUpPrev) {
            currentSticker = (currentSticker + 1) % 9;
        }
        if (gamepad1.dpad_down && !dpadDownPrev) {
            currentSticker = (currentSticker + 8) % 9;
        }

        // Change which color we're calibrating
        if (gamepad1.dpad_right && !dpadRightPrev) {
            currentColor = (currentColor + 1) % 6;
        }
        if (gamepad1.dpad_left && !dpadLeftPrev) {
            currentColor = (currentColor + 5) % 6;
        }

        // Toggle recording mode (A button)
        if (gamepad1.a && !aPrev) {
            recordingMode = !recordingMode;
        }

        // Grid adjustment (same as before)
        // Note: This logic will fight with the "change sticker position" logic
        // You are using dpad up/down/left/right for TWO things at once.
        // The "change sticker" and "change color" logic will only run on the FIRST frame
        // you press the dpad, then grid adjustment will take over.
        if (gamepad1.dpad_up)    pipeline.offsetY -= 2;
        if (gamepad1.dpad_down)  pipeline.offsetY += 2;
        if (gamepad1.dpad_left)  pipeline.offsetX -= 2;
        if (gamepad1.dpad_right) pipeline.offsetX += 2;

        if (gamepad1.left_bumper)  pipeline.spacing -= 2;
        if (gamepad1.right_bumper) pipeline.spacing += 2;

        if (pipeline.spacing < 60)  pipeline.spacing = 60;
        if (pipeline.spacing > 200) pipeline.spacing = 200;

        // If in recording mode, continuously sample and update min/max
        if (recordingMode) {
            double[] bgr = pipeline.sampleIndexRaw(currentSticker);
            double[] hsv = bgrToHSV(bgr);

            double H = hsv[0];
            double S = hsv[1];
            double V = hsv[2];

            // Update min/max for current color
            if (H < minH[currentColor]) minH[currentColor] = H;
            if (H > maxH[currentColor]) maxH[currentColor] = H;
            if (S < minS[currentColor]) minS[currentColor] = S;
            if (S > maxS[currentColor]) maxS[currentColor] = S;
            if (V < minV[currentColor]) minV[currentColor] = V;
            if (V > maxV[currentColor]) maxV[currentColor] = V;
        }

        // Update all previous variables
        dpadUpPrev = gamepad1.dpad_up;
        dpadDownPrev = gamepad1.dpad_down;
        dpadRightPrev = gamepad1.dpad_right;
        dpadLeftPrev = gamepad1.dpad_left;
        aPrev = gamepad1.a;
    }

    private void showDebugScreen() {
        telemetry.clearAll();
        telemetry.addLine("=== COLOR CALIBRATION TOOL ===");
        telemetry.addLine("");

        // Get current HSV values
        double[] bgr = pipeline.sampleIndexRaw(currentSticker);
        double[] hsv = bgrToHSV(bgr);

        telemetry.addData("Sticker Position", currentSticker);
        telemetry.addData("Current Color", colorNames[currentColor]);
        telemetry.addData("Recording", recordingMode ? "YES ✓" : "NO");
        telemetry.addLine("");

        // Show current readings
        telemetry.addLine("--- CURRENT READINGS ---");
        telemetry.addData("H (Hue)", String.format("%.1f", hsv[0]));
        telemetry.addData("S (Saturation)", String.format("%.1f", hsv[1]));
        telemetry.addData("V (Value/Brightness)", String.format("%.1f", hsv[2]));
        telemetry.addData("Detected Color", pipeline.sampleIndex(currentSticker));
        telemetry.addLine("");

        // Show recorded min/max for current color
        if (minH[currentColor] < 999) {
            telemetry.addLine("--- RECORDED RANGE FOR " + colorNames[currentColor] + " ---");
            telemetry.addData("H Range", String.format("%.1f - %.1f", minH[currentColor], maxH[currentColor]));
            telemetry.addData("S Range", String.format("%.1f - %.1f", minS[currentColor], maxS[currentColor]));
            telemetry.addData("V Range", String.format("%.1f - %.1f", minV[currentColor], maxV[currentColor]));
            telemetry.addLine("");

            // Suggest threshold values (use min values with some margin)
            telemetry.addLine("--- SUGGESTED THRESHOLDS ---");
            if (currentColor == 0) { // WHITE
                telemetry.addData("WHITE_MAX_SATURATION", String.format("%.0f", maxS[currentColor] + 10));
                telemetry.addData("WHITE_MIN_VALUE", String.format("%.0f", minV[currentColor] - 10));
            } else if (currentColor == 1) { // RED
                telemetry.addData("RED_MAX_HUE_LOW", String.format("%.0f", maxH[currentColor]));
                telemetry.addData("RED_MIN_HUE_HIGH", String.format("%.0f", minH[currentColor]));
                telemetry.addData("RED_MIN_SATURATION", String.format("%.0f", minS[currentColor] - 20));
                telemetry.addData("RED_MIN_VALUE", String.format("%.0f", minV[currentColor] - 20));
            } else {
                String colorUpper = colorNames[currentColor];
                telemetry.addData(colorUpper + "_MIN_HUE", String.format("%.0f", minH[currentColor] - 5));
                telemetry.addData(colorUpper + "_MAX_HUE", String.format("%.0f", maxH[currentColor] + 5));
                telemetry.addData(colorUpper + "_MIN_SATURATION", String.format("%.0f", minS[currentColor] - 20));
                telemetry.addData(colorUpper + "_MIN_VALUE", String.format("%.0f", minV[currentColor] - 20));
            }
        }

        telemetry.addLine("");
        telemetry.addLine("--- CONTROLS ---");
        telemetry.addLine("Dpad Up/Down: Change sticker position (on press)");
        telemetry.addLine("Dpad Left/Right: Change color to calibrate (on press)");
        telemetry.addLine("A: Toggle recording (hold cube steady!)");
        telemetry.addLine("LB/RB: Adjust grid spacing");
        telemetry.addLine("Dpad (hold): Adjust grid position");
        telemetry.addLine("");
        telemetry.addLine("--- INSTRUCTIONS ---");
        telemetry.addLine("1. Select color with Dpad Left/Right");
        telemetry.addLine("2. Point camera at that color");
        telemetry.addLine("3. Press A to start recording");
        telemetry.addLine("4. Move cube slightly to capture variations");
        telemetry.addLine("5. Press A to stop, copy suggested values");

        telemetry.update();
    }

    // Helper to convert BGR to HSV
    // --- THIS METHOD IS NOW FIXED (FIX) ---
    private double[] bgrToHSV(double[] bgr) {
        // REUSE the member variable bgrMat
        bgrMat.put(0, 0, new double[]{bgr[0], bgr[1], bgr[2]});

        // REUSE the member variable hsvMat
        org.opencv.imgproc.Imgproc.cvtColor(bgrMat, hsvMat, org.opencv.imgproc.Imgproc.COLOR_BGR2HSV);

        double H = hsvMat.get(0, 0)[0];
        double S = hsvMat.get(0, 0)[1];
        double V = hsvMat.get(0, 0)[2];

        return new double[]{H, S, V};
    }
}