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
//@TeleOp(name = "AprilTag Brute Force Search", group = "TeleOp")
//public class AprilTagBruteForceSearch extends LinearOpMode {
//
//    private static final int TAG_ID_TO_TRACK = 22;
//
//    private Servo pan_servo = null;
//    private Servo tilt_servo = null;
//    private VisionPortal visionPortal;
//    private AprilTagProcessor aprilTagProcessor;
//    private IMU imu;
//
//    // Servo positions and sweep settings
//    private double panPosition = 0.5;
//    private double tiltPosition = 0.5;
//
//    private static final double PAN_INCREMENT = 0.005; // Amount to move pan servo each loop
//    private static final double TILT_INCREMENT = 0.005; // Amount to move tilt servo each loop
//
//    private int panDirection = 1; // 1 for right, -1 for left
//    private int tiltDirection = 1; // 1 for up, -1 for down
//
//    @Override
//    public void runOpMode() {
//        // --- Hardware Initialization ---
//        pan_servo = hardwareMap.get(Servo.class, "pan_servo");
//        tilt_servo = hardwareMap.get(Servo.class, "tilt_servo");
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
//                .setTagFamily(AprilTagProcessor.TagFamily.TAG_36h11)
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
//                boolean tagFound = false;
//
//                // Check for the specific tag ID, without needing pose data
//                for (AprilTagDetection detection : currentDetections) {
//                    if (detection.id == TAG_ID_TO_TRACK) {
//                        tagFound = true;
//                        break;
//                    }
//                }
//
//                if (tagFound) {
//                    // Tag found: Stop the servos
//                    telemetry.addData("Tracking", "Tag ID %d found! Servos holding position.", TAG_ID_TO_TRACK);
//                } else {
//                    // Tag not found: Continue sweeping
//                    telemetry.addData("Tracking", "Tag ID %d not found. Searching...", TAG_ID_TO_TRACK);
//
//                    // Move pan servo
//                    panPosition += PAN_INCREMENT * panDirection;
//                    if (panPosition >= 1.0 || panPosition <= 0.0) {
//                        panDirection *= -1; // Reverse direction
//                        panPosition = Range.clip(panPosition, 0.0, 1.0);
//                    }
//                    pan_servo.setPosition(panPosition);
//
//                    // Move tilt servo
//                    tiltPosition += TILT_INCREMENT * tiltDirection;
//                    if (tiltPosition >= 1.0 || tiltPosition <= 0.0) {
//                        tiltDirection *= -1; // Reverse direction
//                        tiltPosition = Range.clip(tiltPosition, 0.0, 1.0);
//                    }
//                    tilt_servo.setPosition(tiltPosition);
//                }
//
//                telemetry.addData("Pan Position", "%.2f", panPosition);
//                telemetry.addData("Tilt Position", "%.2f", tiltPosition);
//                telemetry.addData("Robot Heading (Yaw)", "%.2f", imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES));
//                telemetry.update();
//
//                // Add a small delay to prevent rapid servo movement
//                sleep(10);
//            }
//        }
//    }
//}
