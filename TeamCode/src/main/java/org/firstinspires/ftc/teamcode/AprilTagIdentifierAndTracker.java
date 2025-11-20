//package org.firstinspires.ftc.teamcode;
//
//import com.qualcomm.robotcore.eventloop.opmode.Disabled;
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
//import com.qualcomm.robotcore.hardware.Servo;
//import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
//import org.firstinspires.ftc.vision.VisionPortal;
//import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
//import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
//import com.qualcomm.robotcore.hardware.IMU;
//import com.qualcomm.robotcore.util.Range;
//import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
//import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
//import java.util.List;
//
//@Disabled
//@TeleOp(name = "AprilTag Tracker (ID 22)", group = "TeleOp")
//public class AprilTagIdentifierAndTracker extends LinearOpMode {
//
//    // Define the specific tag ID to track
//    private static final int TAG_ID_TO_TRACK = 22;
//
//    private Servo pan_servo = null;
//    private Servo tilt_servo = null;
//    private VisionPortal visionPortal;
//    private AprilTagProcessor aprilTagProcessor;
//    private IMU imu;
//
//    private double panPosition = 0.5;
//    private double tiltPosition = 0.5;
//
//    // Tuning variables for proportional control
//    private static final double PAN_GAIN = 0.005;
//    private static final double TILT_GAIN = 0.005;
//
//    @Override
//    public void runOpMode() {
//        // --- Hardware Initialization ---
//        pan_servo = hardwareMap.get(Servo.class, "pan_servo");
//        tilt_servo = hardwareMap.get(Servo.class, "tilt_servo");
//
//        pan_servo.setPosition(panPosition);
//        tilt_servo.setPosition(tiltPosition);
//
//        imu = hardwareMap.get(IMU.class, "imu");
//        RevHubOrientationOnRobot.LogoFacingDirection logoDirection = RevHubOrientationOnRobot.LogoFacingDirection.RIGHT;
//        RevHubOrientationOnRobot.UsbFacingDirection usbDirection = RevHubOrientationOnRobot.UsbFacingDirection.UP;
//        RevHubOrientationOnRobot orientationOnRobot = new RevHubOrientationOnRobot(logoDirection, usbDirection);
//        imu.initialize(new IMU.Parameters(orientationOnRobot));
//        imu.resetYaw();
//
//        // --- VisionPortal and AprilTag Processor Initialization ---
//        aprilTagProcessor = new AprilTagProcessor.Builder()
//                .setTagFamily(AprilTagProcessor.TagFamily.TAG_36h11) // Set tag family
//                .build();
//
//        visionPortal = new VisionPortal.Builder()
//                .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))
//                .addProcessor(aprilTagProcessor)
//                .build();
//
//        telemetry.addData("Status", "Initialized. Awaiting Start.");
//        telemetry.addData("Tracking", "Looking for Tag ID: " + TAG_ID_TO_TRACK);
//        telemetry.update();
//
//        waitForStart();
//
//        if (opModeIsActive()) {
//            while (opModeIsActive()) {
//                List<AprilTagDetection> currentDetections = aprilTagProcessor.getDetections();
//                AprilTagDetection targetTag = null;
//
//                // Find the specific tag ID and ensure it has valid pose data
//                for (AprilTagDetection detection : currentDetections) {
//                    if (detection.id == TAG_ID_TO_TRACK && detection.ftcPose != null) {
//                        targetTag = detection;
//                        break; // Found the tag, no need to check others
//                    }
//                }
//
//                if (targetTag != null) {
//                    // A valid tag with pose data was found
//                    double panError = targetTag.ftcPose.bearing;
//                    double tiltError = targetTag.ftcPose.elevation;
//
//                    panPosition -= panError * PAN_GAIN;
//                    tiltPosition += tiltError * TILT_GAIN;
//
//                    panPosition = Range.clip(panPosition, 0.0, 1.0);
//                    tiltPosition = Range.clip(tiltPosition, 0.0, 1.0);
//
//                    pan_servo.setPosition(panPosition);
//                    tilt_servo.setPosition(tiltPosition);
//
//                    telemetry.addData("Tracking", "ID %d found", targetTag.id);
//                    telemetry.addData("Pan Position", "%.2f", panPosition);
//                    telemetry.addData("Tilt Position", "%.2f", tiltPosition);
//                    telemetry.addData("Pan Error (bearing)", "%.2f", panError);
//                    telemetry.addData("Tilt Error (elevation)", "%.2f", tiltError);
//                } else {
//                    // No tag with ID 22 and valid pose data found
//                    telemetry.addData("Tracking", "Tag ID %d not found", TAG_ID_TO_TRACK);
//                }
//
//                telemetry.addData("Robot Heading (Yaw)", "%.2f", imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES));
//                telemetry.update();
//            }
//        }
//    }
//}
