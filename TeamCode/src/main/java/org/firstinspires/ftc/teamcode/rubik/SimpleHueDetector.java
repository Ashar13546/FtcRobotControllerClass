package org.firstinspires.ftc.teamcode; // Use your team's package name

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvPipeline;

@TeleOp(name = "Simple H Detector", group = "Debug")
public class SimpleHueDetector extends LinearOpMode {

    OpenCvCamera webcam;
    HuePipeline pipeline;

    @Override
    public void runOpMode() throws InterruptedException {
        telemetry.addLine("Initializing webcam...");
        telemetry.update();

        // --- Webcam and Pipeline Initialization ---
        int cameraMonitorViewId = hardwareMap.appContext.getResources()
                .getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());

        webcam = OpenCvCameraFactory.getInstance()
                .createWebcam(hardwareMap.get(WebcamName.class, "Webcam 1"), cameraMonitorViewId);

        pipeline = new HuePipeline();
        webcam.setPipeline(pipeline);

        webcam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                // Use the resolution you normally use (e.g., 640x480)
                webcam.startStreaming(640, 480, OpenCvCameraRotation.UPRIGHT);
            }

            @Override
            public void onError(int errorCode) {
                telemetry.addData("Error", "Webcam could not open. Code: %d", errorCode);
                telemetry.update();
            }
        });
        // -----------------------------------------

        telemetry.addLine("Ready! Point the camera at a color.");
        telemetry.addLine("The HSV values of the center box will be shown.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {

            // The pipeline is processing in the background
            // We just read the public variables H, S, and V from it

            telemetry.addLine("--- Live HSV Values (Center) ---");
            telemetry.addData("H (Hue)", "%.1f", pipeline.H);
            telemetry.addData("S (Saturation)", "%.1f", pipeline.S);
            telemetry.addData("V (Value)", "%.1f", pipeline.V);
            telemetry.addLine();
            telemetry.addLine("H (Hue) is the main color component (0-180).");
            telemetry.update();

            sleep(50); // Small pause
        }
    }

    // ========================================================================================
    //  This is the vision pipeline. It's defined *inside* the OpMode,
    //  so you don't need a separate .java file.
    // ========================================================================================
    class HuePipeline extends OpenCvPipeline {

        // These variables are public so the OpMode can access them
        public volatile double H = 0;
        public volatile double S = 0;
        public volatile double V = 0;

        // A Mat to store the HSV version of the frame
        private Mat hsvMat = new Mat();

        // A Scalar to store the average color
        private Scalar avgColor = new Scalar(0, 0, 0);

        // The center point of the 640x480 frame
        private Point centerPoint = new Point(320, 240);
        private int boxSize = 50; // 50x50 pixel box

        @Override
        public Mat processFrame(Mat input) {
            // 1. Define the Region of Interest (ROI)
            // This is a 50x50 box in the center of the screen
            int halfSize = boxSize / 2;
            Point p1 = new Point(centerPoint.x - halfSize, centerPoint.y - halfSize);
            Point p2 = new Point(centerPoint.x + halfSize, centerPoint.y + halfSize);
            Rect roi = new Rect(p1, p2);

            // 2. Convert the entire frame from BGR to HSV
            Imgproc.cvtColor(input, hsvMat, Imgproc.COLOR_BGR2HSV);

            // 3. Get the sub-matrix (just the ROI) from the HSV Mat
            Mat roiMat = hsvMat.submat(roi);

            // 4. Calculate the average HSV values within the ROI
            // This is much more stable than sampling a single pixel
            avgColor = Core.mean(roiMat);

            // 5. Store the H, S, and V values
            H = avgColor.val[0];
            S = avgColor.val[1];
            V = avgColor.val[2];

            // 6. Draw a green rectangle on the *original* (BGR) frame
            // This shows you what area is being sampled
            Imgproc.rectangle(
                    input,     // The frame to draw on
                    p1,        // Top-left corner
                    p2,        // Bottom-right corner
                    new Scalar(0, 255, 0), // Green color
                    2          // 2 pixel thick border
            );

            // 7. Release the temporary sub-matrix
            roiMat.release();

            // 8. Return the original frame (with the rectangle drawn on it)
            return input;
        }
    }
}