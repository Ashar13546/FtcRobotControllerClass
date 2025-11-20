//package org.firstinspires.ftc.teamcode;
//
//
//import com.qualcomm.robotcore.eventloop.opmode.Disabled;
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
//import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
//import org.firstinspires.ftc.vision.VisionPortal;
//import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
//import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
//import java.util.List;
//
//@Disabled
//@TeleOp(name = "AprilTagIdentifier", group = "Concept")
//public class AprilTagIdentifier extends LinearOpMode {
//
//    // You must change these values to match your specific AprilTag IDs
//    private static final int TAG_ID_1 = 21;
//    private static final int TAG_ID_2 = 22;
//    private static final int TAG_ID_3 = 23;
//
//    // This variable will hold the assigned value based on the detected tag
//    private int detectedTagValue = -1; // Default value if no specific tag is found
//
//    private VisionPortal visionPortal;
//    private AprilTagProcessor aprilTagProcessor;
//
//    @Override
//    public void runOpMode() {
//        initAprilTag();
//
//        telemetry.addData("Status", "Initialized. Awaiting Start.");
//        telemetry.addData("Camera State", "Waiting for streaming...");
//        telemetry.update();
//
//        waitForStart();
//
//        if (opModeIsActive()) {
//            while (opModeIsActive()) {
//                // Add telemetry to check the camera's status
//                telemetry.addData("Camera State", visionPortal.getCameraState());
//
//                // Check for AprilTag detections in the current frame
//                List<AprilTagDetection> currentDetections = aprilTagProcessor.getDetections();
//
//                // Reset the value at the start of each frame's processing
//                detectedTagValue = -1;
//
//                // Loop through all detected tags
//                for (AprilTagDetection detection : currentDetections) {
//                    int currentId = detection.id;
//
//                    // Check if the detected tag's ID matches one of our target IDs
//                    if (currentId == TAG_ID_1) {
//                        detectedTagValue = 0;
//                        telemetry.addData("Tag Found", "ID: %d, Value: %d", currentId, detectedTagValue);
//                        telemetry.addData("Colors:", "Green, Purple, Purple");
//                        break; // Exit the loop since we found our desired tag
//                    } else if (currentId == TAG_ID_2) {
//                        detectedTagValue = 1;
//                        telemetry.addData("Tag Found", "ID: %d, Value: %d", currentId, detectedTagValue);
//                        telemetry.addData("Colors:", "Purple, Green, Purple");
//                        break;
//                    } else if (currentId == TAG_ID_3) {
//                        detectedTagValue = 2;
//                        telemetry.addData("Tag Found", "ID: %d, Value: %d", currentId, detectedTagValue);
//                        telemetry.addData("Colors:", "Purple, Purple, Green");
//                        break;
//                    }
//                }
//
//                // If the loop finished without finding a matching tag, the value remains -1
//                if (detectedTagValue == -1) {
//                    telemetry.addData("Tag Not Found", "Searching...");
//                }
//
//                telemetry.update();
//            }
//        }
//
//        telemetry.addData("Final Value", detectedTagValue);
//        telemetry.update();
//    }
//
//    /**
//     * Initialize the AprilTag processor and VisionPortal.
//     */
//    private void initAprilTag() {
//        aprilTagProcessor = new AprilTagProcessor.Builder()
//                .setTagFamily(AprilTagProcessor.TagFamily.TAG_36h11)
//                .build();
//
//        visionPortal = new VisionPortal.Builder()
//                .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))
//                .addProcessor(aprilTagProcessor)
//                .build();
//    }
//}