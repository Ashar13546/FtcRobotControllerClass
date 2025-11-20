//import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
//import com.qualcomm.robotcore.hardware.DcMotor;
//import com.qualcomm.robotcore.hardware.DcMotorSimple;
//import com.qualcomm.robotcore.hardware.IMU;
//import com.qualcomm.robotcore.hardware.Servo;
//
//import org.firstinspires.ftc.vision.VisionPortal;
//import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
//import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
//import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
//
//import java.util.List;
//
//@TeleOp(name = "MechamDriveShooter_AprilTagAlign", group = "Linear OpMode")
//public class MechamDriveShooter_AprilTagAlign extends LinearOpMode {
//
//    // Drive motors
//    private DcMotor frontLeftMotor, frontRightMotor, backLeftMotor, backRightMotor;
//
//    // Intake and shooter motors
//    private DcMotor intakeMotor, shooterMotor1, shooterMotor2;
//
//    // Feed motor
//    private DcMotor placeholderMotor;
//
//    // IMU for field-oriented drive
//    private IMU imu;
//
//    // Servos
//    private Servo hold_servo, push_servo;
//
//    // Constants
//    private static final double INTAKE_POWER = 1.0;
//    private static final double SHOOTER_POWER = 0.5;
//
//    // Toggles
//    private boolean shooterActive = false;
//    private boolean rightBumperPressed = false;
//    private boolean intakeFeedActive = false;
//    private boolean rightTriggerPressed = false;
//
//    // --- Vision ---
//    private VisionPortal visionPortal;
//    private AprilTagProcessor aprilTag;
//    private final int centerX = 320;
//    private final int centerY = 240;
//    private final int deadZone = 40;
//    private final int[] validTagIDs = {20, 21, 22, 23};
//    private Integer trackedTagID = null;
//
//    // Adjustable “perfect shooting distance” (forward/backward)
//    private final double TARGET_DISTANCE = 0.25; // tweak as needed for your robot/arena
//
//    @Override
//    public void runOpMode() {
//        // --- Hardware Init ---
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
//        intakeMotor.setDirection(DcMotorSimple.Direction.FORWARD);
//        shooterMotor1.setDirection(DcMotorSimple.Direction.REVERSE);
//        shooterMotor2.setDirection(DcMotorSimple.Direction.REVERSE);
//        placeholderMotor.setDirection(DcMotorSimple.Direction.REVERSE);
//
//        setDriveMotorRunMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//        intakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//        shooterMotor1.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//        shooterMotor2.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//        placeholderMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//
//        hold_servo.setPosition(0.0);
//        push_servo.setPosition(0.0);
//
//        // --- IMU Init ---
//        imu = hardwareMap.get(IMU.class, "imu");
//        RevHubOrientationOnRobot orientation = new RevHubOrientationOnRobot(
//                RevHubOrientationOnRobot.LogoFacingDirection.UP,
//                RevHubOrientationOnRobot.UsbFacingDirection.LEFT
//        );
//        imu.initialize(new IMU.Parameters(orientation));
//        imu.resetYaw();
//
//        // --- Vision Init ---
//        aprilTag = new AprilTagProcessor.Builder()
//                .setTagFamily(AprilTagProcessor.TagFamily.TAG_36h11)
//                .build();
//
//        visionPortal = new VisionPortal.Builder()
//                .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))
//                .addProcessor(aprilTag)
//                .build();
//
//        telemetry.addData("Status", "Initialized");
//        telemetry.update();
//        waitForStart();
//
//        while (opModeIsActive()) {
//            // ========== MECANUM DRIVE ==========
//            double y = gamepad1.left_stick_y; // Forward/backward
//            double x = -gamepad1.left_stick_x; // Strafe
//            double rx = gamepad1.right_stick_x; // Rotation
//
//            // --- Check AprilTag alignment button ---
//            if (gamepad1.b) {
//                List<AprilTagDetection> detections = aprilTag.getDetections();
//                AprilTagDetection target = null;
//
//                for (AprilTagDetection detection : detections) {
//                    if (trackedTagID != null && detection.id == trackedTagID) {
//                        target = detection;
//                        break;
//                    } else if (trackedTagID == null && isValidTag(detection.id)) {
//                        trackedTagID = detection.id;
//                        target = detection;
//                        break;
//                    }
//                }
//
//                if (target != null) {
//                    // Horizontal correction
//                    double errorX = target.center.x - centerX;
//                    double kP = 0.0015;
//                    double turnCorrection = Math.abs(errorX) > deadZone ? errorX * kP : 0.0;
//
//                    // Forward/backward correction (simple proportional to “target distance”)
//                    double forward = TARGET_DISTANCE; // For now, just fixed power
//                    y = -forward; // negative because joystick forward is negative
//
//                    // Strafe correction is optional, could be zero if no info
//                    x = 0;
//
//                    // Rotation correction
//                    rx = turnCorrection;
//
//                    telemetry.addData("Mode", "Auto Align Shooting");
//                    telemetry.addData("Target Tag", target.id);
//                    telemetry.addData("ErrorX", errorX);
//                } else {
//                    trackedTagID = null;
//                    telemetry.addLine("No tag detected for alignment");
//                }
//            }
//
//            // --- Mecanum motor power calculations ---
//            double frontLeftPower = y + x + rx;
//            double backLeftPower = y - x + rx;
//            double frontRightPower = y - x - rx;
//            double backRightPower = y + x - rx;
//
//            double maxPower = Math.max(Math.abs(frontLeftPower), Math.abs(backLeftPower));
//            maxPower = Math.max(maxPower, Math.abs(frontRightPower));
//            maxPower = Math.max(maxPower, Math.abs(backRightPower));
//
//            if (maxPower > 1.0) {
//                frontLeftPower /= maxPower;
//                backLeftPower /= maxPower;
//                frontRightPower /= maxPower;
//                backRightPower /= maxPower;
//            }
//
//            frontLeftMotor.setPower(frontLeftPower);
//            backLeftMotor.setPower(backLeftPower);
//            frontRightMotor.setPower(frontRightPower);
//            backRightMotor.setPower(backRightPower);
//
//            // ========== GAMEPAD 2: MECHANISMS ==========
//            // Shooter toggle
//            if (gamepad2.right_bumper && !rightBumperPressed) {
//                shooterActive = !shooterActive;
//                rightBumperPressed = true;
//            } else if (!gamepad2.right_bumper) rightBumperPressed = false;
//
//            if (shooterActive) {
//                shooterMotor1.setDirection(DcMotorSimple.Direction.REVERSE);
//                shooterMotor2.setDirection(DcMotorSimple.Direction.REVERSE);
//                shooterMotor1.setPower(-SHOOTER_POWER);
//                shooterMotor2.setPower(SHOOTER_POWER);
//            } else {
//                shooterMotor1.setDirection(DcMotorSimple.Direction.FORWARD);
//                shooterMotor2.setDirection(DcMotorSimple.Direction.FORWARD);
//                shooterMotor1.setPower(-0.1);
//                shooterMotor2.setPower(0.1);
//            }
//
//            // Intake + feed toggle
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
//            // Hold + Push servos
//            if (gamepad2.a) {
//                hold_servo.setPosition(0);
//                push_servo.setPosition(0);
//            } else {
//                hold_servo.setPosition(1.0);
//                push_servo.setPosition(1.0);
//            }
//
//            telemetry.update();
//        }
//    }
//
//    private void setDriveMotorRunMode(DcMotor.RunMode runMode) {
//        frontLeftMotor.setMode(runMode);
//        frontRightMotor.setMode(runMode);
//        backLeftMotor.setMode(runMode);
//        backRightMotor.setMode(runMode);
//    }
//
//    private boolean isValidTag(int id) {
//        for (int validID : validTagIDs) {
//            if (id == validID) return true;
//        }
//        return false;
//    }
//}