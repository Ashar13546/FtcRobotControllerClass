//package org.firstinspires.ftc.teamcode;
//
//import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
//import com.qualcomm.robotcore.hardware.DcMotor;
//import com.qualcomm.robotcore.hardware.DcMotorSimple;
//import com.qualcomm.robotcore.hardware.IMU;
//import com.qualcomm.robotcore.hardware.Servo;
//
//import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
//import org.firstinspires.ftc.vision.VisionPortal;
//import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
//import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
//
//import java.util.List;
//
//@TeleOp(name = "MechamDriveShooter_AprilTagAlign_FIXED", group = "Linear OpMode")
//public class MechamDriveShooter_AprilTagAlign2 extends LinearOpMode {
//
//    // Motors
//    private DcMotor frontLeftMotor, frontRightMotor, backLeftMotor, backRightMotor;
//    private DcMotor intakeMotor, shooterMotor1, shooterMotor2, placeholderMotor;
//
//    // Servos
//    private Servo hold_servo, push_servo;
//
//    // IMU
//    private IMU imu;
//
//    // Vision
//    private VisionPortal visionPortal;
//    private AprilTagProcessor aprilTag;
//
//    // Constants
//    private static final double INTAKE_POWER = 1.0;
//    private static final double SHOOTER_POWER = 0.5;
//    private static final double TARGET_DISTANCE = 0.25;
//
//    // Vision tuning
//    private final int centerX = 320;
//    private final int deadZone = 40;
//    private final int[] validTagIDs = {20, 21, 22, 23};
//    private Integer trackedTagID = null;
//
//    // Toggles
//    private boolean shooterActive = false;
//    private boolean rightBumperPressed = false;
//    private boolean intakeFeedActive = false;
//    private boolean rightTriggerPressed = false;
//
//    @Override
//    public void runOpMode() {
//        initHardware();
//        initIMU();
//        initVision();
//
//        telemetry.addData("Status", "Initialized");
//        telemetry.update();
//        waitForStart();
//
//        while (opModeIsActive()) {
//
//            // Default drive inputs
//            double y = gamepad1.left_stick_y;
//            double x = -gamepad1.left_stick_x;
//            double rx = gamepad1.right_stick_x;
//
//            // Tag alignment
//            if (gamepad1.b) {
//                AprilTagDetection target = getTrackedTag();
//
//                if (target != null) {
//                    double errorX = target.center.x - centerX;
//                    double kP = 0.002; // Tunable
//                    double strafeCorrection = Math.abs(errorX) > deadZone ? -errorX * kP : 0.0;
//
//                    x = strafeCorrection;
//                    y = -TARGET_DISTANCE;
//                    rx = 0;
//
//                    telemetry.addData("Mode", "Auto Align");
//                    telemetry.addData("Target ID", target.id);
//                    telemetry.addData("ErrorX", errorX);
//                    telemetry.addData("Strafe Correction", x);
//                } else {
//                    trackedTagID = null;
//                    telemetry.addLine("No valid tag for alignment");
//                }
//            }
//
//            // Mecanum drive power calculation
//            driveMecanum(x, y, rx);
//
//            // Shooter toggle
//            if (gamepad2.right_bumper && !rightBumperPressed) {
//                shooterActive = !shooterActive;
//                rightBumperPressed = true;
//            } else if (!gamepad2.right_bumper) rightBumperPressed = false;
//
//            if (shooterActive) {
//                shooterMotor1.setPower(-SHOOTER_POWER);
//                shooterMotor2.setPower(SHOOTER_POWER);
//            } else {
//                shooterMotor1.setPower(-0.1);
//                shooterMotor2.setPower(0.1);
//            }
//
//            // Intake toggle
//            if (gamepad2.right_trigger > 0.5 && !rightTriggerPressed) {
//                intakeFeedActive = !intakeFeedActive;
//                rightTriggerPressed = true;
//            } else if (gamepad2.right_trigger <= 0.5) rightTriggerPressed = false;
//
//            if (intakeFeedActive) {
//                intakeMotor.setPower(-INTAKE_POWER);
//                placeholderMotor.setPower(INTAKE_POWER);
//            } else {
//                intakeMotor.setPower(0);
//                placeholderMotor.setPower(0);
//            }
//
//            // Servo control
//            if (gamepad2.a) {
//                hold_servo.setPosition(0);
//                push_servo.setPosition(0);
//            } else {
//                hold_servo.setPosition(1);
//                push_servo.setPosition(1);
//            }
//
//            telemetry.update();
//        }
//    }
//
//    private void initHardware() {
//        frontLeftMotor = hardwareMap.get(DcMotor.class, "front_left_motor");
//        frontRightMotor = hardwareMap.get(DcMotor.class, "front_right_motor");
//        backLeftMotor = hardwareMap.get(DcMotor.class, "back_left_motor");
//        backRightMotor = hardwareMap.get(DcMotor.class, "back_right_motor");
//
//        intakeMotor = hardwareMap.get(DcMotor.class, "intake_motor");
//        shooterMotor1 = hardwareMap.get(DcMotor.class, "shooter_motor_1");
//        shooterMotor2 = hardwareMap.get(DcMotor.class, "shooter_motor_2");
//        placeholderMotor = hardwareMap.get(DcMotor.class, "feed_motor");
//
//        hold_servo = hardwareMap.get(Servo.class, "hold_servo");
//        push_servo = hardwareMap.get(Servo.class, "push_servo");
//
//        frontLeftMotor.setDirection(DcMotorSimple.Direction.REVERSE);
//        backLeftMotor.setDirection(DcMotorSimple.Direction.REVERSE);
//        frontRightMotor.setDirection(DcMotorSimple.Direction.FORWARD);
//        backRightMotor.setDirection(DcMotorSimple.Direction.FORWARD);
//
//        shooterMotor1.setDirection(DcMotorSimple.Direction.REVERSE);
//        shooterMotor2.setDirection(DcMotorSimple.Direction.REVERSE);
//
//        setDriveMotorRunMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//    }
//
//    private void initIMU() {
//        imu = hardwareMap.get(IMU.class, "imu");
//        imu.initialize(new IMU.Parameters(new RevHubOrientationOnRobot(
//                RevHubOrientationOnRobot.LogoFacingDirection.UP,
//                RevHubOrientationOnRobot.UsbFacingDirection.LEFT
//        )));
//        imu.resetYaw();
//    }
//
//    private void initVision() {
//        aprilTag = new AprilTagProcessor.Builder()
//                .setTagFamily(AprilTagProcessor.TagFamily.TAG_36h11)
//                .build();
//
//        visionPortal = new VisionPortal.Builder()
//                .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))
//                .addProcessor(aprilTag)
//                .build();
//    }
//
//    private void setDriveMotorRunMode(DcMotor.RunMode mode) {
//        frontLeftMotor.setMode(mode);
//        frontRightMotor.setMode(mode);
//        backLeftMotor.setMode(mode);
//        backRightMotor.setMode(mode);
//    }
//
//    private void driveMecanum(double x, double y, double rx) {
//        double fl = y + x + rx;
//        double bl = y - x + rx;
//        double fr = y - x - rx;
//        double br = y + x - rx;
//
//        double max = Math.max(Math.max(Math.abs(fl), Math.abs(bl)), Math.max(Math.abs(fr), Math.abs(br)));
//        if (max > 1.0) {
//            fl /= max;
//            bl /= max;
//            fr /= max;
//            br /= max;
//        }
//
//        frontLeftMotor.setPower(fl);
//        backLeftMotor.setPower(bl);
//        frontRightMotor.setPower(fr);
//        backRightMotor.setPower(br);
//    }
//
//    private AprilTagDetection getTrackedTag() {
//        List<AprilTagDetection> detections = aprilTag.getDetections();
//        for (AprilTagDetection tag : detections) {
//            if (trackedTagID != null && tag.id == trackedTagID) {
//                return tag;
//            } else if (trackedTagID == null && isValidTag(tag.id)) {
//                trackedTagID = tag.id;
//                return tag;
//            }
//        }
//        return null;
//    }
//
//    private boolean isValidTag(int id) {
//        for (int valid : validTagIDs) {
//            if (id == valid) return true;
//        }
//        return false;
//    }
//}
