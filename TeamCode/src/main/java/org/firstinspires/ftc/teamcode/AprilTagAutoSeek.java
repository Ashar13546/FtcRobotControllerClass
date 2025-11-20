//package org.firstinspires.ftc.teamcode;
//
//import com.qualcomm.robotcore.eventloop.opmode.Disabled;
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
//import com.qualcomm.robotcore.hardware.DcMotor;
//import com.qualcomm.robotcore.hardware.Servo;
//import com.qualcomm.robotcore.util.ElapsedTime;
//
//import org.firstinspires.ftc.vision.VisionPortal;
//import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
//import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
//import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
//
//import java.util.List;
//@Disabled
//@TeleOp(name = "AprilTagAutoSeek", group = "Vision")
//public class AprilTagAutoSeek extends com.qualcomm.robotcore.eventloop.opmode.LinearOpMode {
//
//    private VisionPortal visionPortal;
//    private AprilTagProcessor aprilTag;
//
//    private Servo tilt_servo;
//    private Servo pan_servo;
//    private DcMotor leftMotor;
//    private DcMotor rightMotor;
//
//    private double tiltPGain = 0.00003;
//    private double tiltDGain = 0.0;
//    private double tiltIGain = 0.0;
//
//    private final int centerX = 320;
//    private final int centerY = 240;
//    private final int deadZone = 50;
//    private final int[] validTagIDs = {20, 21, 22, 23};
//
//    private double tiltLastError = 0.0;
//    private double tiltIntegral = 0.0;
//
//    private double smoothedTiltError = 0.0;
//    private final double smoothingFactor = 0.2;
//
//    private Integer currentlyTrackedTagID = null;
//    private boolean tagVisible = false;
//
//    private final double TILT_START_POS = 0.33;
//    private final double PAN_CENTER_POS = 0.5;
//
//    private final double PAN_SWEEP_MIN = 0.2;
//    private final double PAN_SWEEP_MAX = 0.8;
//    private final double PAN_SWEEP_STEP = 0.005;
//    private boolean panIncreasing = true;
//
//    private final double DRIVE_POWER = 0.4;
//    private final double TURN_KP = 0.0015;
//    private final double MIN_DRIVE_DISTANCE_INCHES = 8.0;
//
//    private ElapsedTime timer = new ElapsedTime();
//
//    @Override
//    public void runOpMode() {
//        tilt_servo = hardwareMap.get(Servo.class, "tilt_servo");
//        pan_servo = hardwareMap.get(Servo.class, "pan_servo");
//        leftMotor = hardwareMap.get(DcMotor.class, "left_motor");
//        rightMotor = hardwareMap.get(DcMotor.class, "right_motor");
//
//        leftMotor.setDirection(DcMotor.Direction.FORWARD);
//        rightMotor.setDirection(DcMotor.Direction.REVERSE);
//
//        tilt_servo.setDirection(Servo.Direction.REVERSE);
//        tilt_servo.scaleRange(0.2, 0.8);
//        tilt_servo.setPosition(TILT_START_POS);
//
//        pan_servo.setPosition(PAN_CENTER_POS);
//
//        aprilTag = new AprilTagProcessor.Builder()
//                .setTagFamily(AprilTagProcessor.TagFamily.TAG_36h11)
//                .build();
//
//        visionPortal = new VisionPortal.Builder()
//                .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))
//                .addProcessor(aprilTag)
//                .build();
//
//        telemetry.addLine("Initialized. Waiting for start...");
//        telemetry.update();
//        waitForStart();
//
//        timer.reset();
//
//        while (opModeIsActive()) {
//            double deltaTime = timer.seconds();
//            timer.reset();
//
//            List<AprilTagDetection> detections = aprilTag.getDetections();
//            tagVisible = false;
//            AprilTagDetection targetDetection = null;
//
//            for (AprilTagDetection detection : detections) {
//                int id = detection.id;
//                if (currentlyTrackedTagID != null && id == currentlyTrackedTagID) {
//                    targetDetection = detection;
//                    tagVisible = true;
//                    break;
//                } else if (currentlyTrackedTagID == null && isValidTag(id)) {
//                    currentlyTrackedTagID = id;
//                    targetDetection = detection;
//                    tagVisible = true;
//                    break;
//                }
//            }
//
//            double turnCorrection = 0.0;
//            double drivePower = 0.0;
//
//            if (targetDetection != null) {
//                double panError = targetDetection.center.x - centerX;
//                double tiltError = targetDetection.center.y - centerY;
//
//                smoothedTiltError = smoothingFactor * tiltError + (1 - smoothingFactor) * smoothedTiltError;
//
//                // Keep pan servo fixed once tag is found
//                // (Optional: you could add pan correction here if needed)
//
//                // Tilt correction (up/down)
//                if (Math.abs(smoothedTiltError) > deadZone) {
//                    tiltIntegral += smoothedTiltError * deltaTime;
//                    double tiltDerivative = (smoothedTiltError - tiltLastError) / deltaTime;
//                    if (Math.signum(smoothedTiltError) != Math.signum(tiltLastError)) tiltIntegral = 0.0;
//
//                    double tiltPosition = tilt_servo.getPosition();
//                    tiltPosition += smoothedTiltError * tiltPGain + tiltIntegral * tiltIGain + tiltDerivative * tiltDGain;
//                    tiltPosition = Math.max(0.0, Math.min(1.0, tiltPosition));
//                    tilt_servo.setPosition(tiltPosition);
//
//                    tiltLastError = smoothedTiltError;
//                } else {
//                    tiltIntegral = 0.0;
//                }
//
//                // Robot turning correction
//                if (Math.abs(panError) > deadZone) {
//                    turnCorrection = panError * TURN_KP;
//                }
//
//                double distance = targetDetection.ftcPose.range; // in inches
//                if (distance > MIN_DRIVE_DISTANCE_INCHES) {
//                    drivePower = DRIVE_POWER;
//                } else {
//                    drivePower = 0.0;
//                }
//
//                telemetry.addData("Status", "Tracking AprilTag ID: %d", targetDetection.id);
//                telemetry.addData("Pan Error", "%.2f", panError);
//                telemetry.addData("Turn Correction", "%.3f", turnCorrection);
//                telemetry.addData("Distance (in)", "%.2f", distance);
//            } else {
//                telemetry.addData("Status", "Searching for AprilTag...");
//
//                // Sweep the pan servo left and right
//                double currentPan = pan_servo.getPosition();
//                if (panIncreasing) {
//                    currentPan += PAN_SWEEP_STEP;
//                    if (currentPan >= PAN_SWEEP_MAX) {
//                        currentPan = PAN_SWEEP_MAX;
//                        panIncreasing = false;
//                    }
//                } else {
//                    currentPan -= PAN_SWEEP_STEP;
//                    if (currentPan <= PAN_SWEEP_MIN) {
//                        currentPan = PAN_SWEEP_MIN;
//                        panIncreasing = true;
//                    }
//                }
//                pan_servo.setPosition(currentPan);
//
//                // Reset tilt while searching
//                tilt_servo.setPosition(TILT_START_POS);
//                currentlyTrackedTagID = null;
//            }
//
//            double leftPower = drivePower + turnCorrection;
//            double rightPower = drivePower - turnCorrection;
//
//            double max = Math.max(Math.abs(leftPower), Math.abs(rightPower));
//            if (max > 1.0) {
//                leftPower /= max;
//                rightPower /= max;
//            }
//
//            leftMotor.setPower(leftPower * 0.7);
//            rightMotor.setPower(rightPower * 0.7);
//
//            telemetry.update();
//            sleep(10);
//        }
//
//        visionPortal.close();
//    }
//
//    private boolean isValidTag(int id) {
//        for (int validID : validTagIDs) {
//            if (id == validID) return true;
//        }
//        return false;
//    }
//}
