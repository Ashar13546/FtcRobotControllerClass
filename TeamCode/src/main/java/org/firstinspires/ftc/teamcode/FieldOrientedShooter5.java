//package org.firstinspires.ftc.teamcode;
//
//import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
//import com.qualcomm.robotcore.eventloop.opmode.Disabled;
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
//import com.qualcomm.robotcore.hardware.DcMotor;
//import com.qualcomm.robotcore.hardware.DcMotorSimple;
//import com.qualcomm.robotcore.hardware.IMU;
//import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
//
//@Disabled
//@TeleOp(name = "FieldOrientedShooter5", group = "Linear OpMode")
//public class FieldOrientedShooter5 extends LinearOpMode {
//
//    // Drive motors
//    private DcMotor frontLeftMotor = null;
//    private DcMotor frontRightMotor = null;
//    private DcMotor backLeftMotor = null;
//    private DcMotor backRightMotor = null;
//
//    // Intake and shooter motors
//    private DcMotor intakeMotor = null;
//    private DcMotor shooterMotor1 = null;
//    private DcMotor shooterMotor2 = null;
//
//    // Feed motor
//    private DcMotor placeholderMotor = null;
//
//    // IMU for field-oriented drive
//    private IMU imu = null;
//
//    // Constants
//    private static final double INTAKE_POWER = 1.0;
//    private static final double SHOOTER_POWER = 0.6;
//
//    // Toggle state variables
//    private boolean shooterActive = false;
//    private boolean rightBumperPressed = false;
//
//    private boolean intakeFeedActive = false;
//    private boolean bButtonPressed = false;
//
//    @Override
//    public void runOpMode() {
//
//        // --- Hardware Mapping ---
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
//        // --- Motor Directions ---
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
//        // --- Set Run Modes ---
//        setDriveMotorRunMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//        intakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//        shooterMotor1.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//        shooterMotor2.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//        placeholderMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//
//        // --- IMU Initialization ---
//        imu = hardwareMap.get(IMU.class, "imu");
//        RevHubOrientationOnRobot orientation = new RevHubOrientationOnRobot(
//                RevHubOrientationOnRobot.LogoFacingDirection.UP,
//                RevHubOrientationOnRobot.UsbFacingDirection.LEFT
//        );
//        imu.initialize(new IMU.Parameters(orientation));
//        imu.resetYaw();
//
//        telemetry.addData("Status", "Initialized");
//        telemetry.update();
//
//        waitForStart();
//
//        while (opModeIsActive()) {
//
//            // =======================
//            // === Gamepad 1: Driving
//            // =======================
//
//            double y = -gamepad1.left_stick_y;  // Forward = push up
//            double x = gamepad1.left_stick_x;   // Right = push right
//            double rotation = gamepad1.right_stick_x;
//
//            double botHeading = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);
//
//            double rotX = x * Math.cos(-botHeading) - y * Math.sin(-botHeading);
//            double rotY = x * Math.sin(-botHeading) + y * Math.cos(-botHeading);
//
//            double frontLeftPower = rotY + rotX + rotation;
//            double frontRightPower = rotY - rotX - rotation;
//            double backLeftPower = rotY - rotX + rotation;
//            double backRightPower = rotY + rotX - rotation;
//
//            double maxPower = Math.max(
//                    Math.max(Math.abs(frontLeftPower), Math.abs(frontRightPower)),
//                    Math.max(Math.abs(backLeftPower), Math.abs(backRightPower))
//            );
//
//            if (maxPower > 1.0) {
//                frontLeftPower /= maxPower;
//                frontRightPower /= maxPower;
//                backLeftPower /= maxPower;
//                backRightPower /= maxPower;
//            }
//
//            frontLeftMotor.setPower(frontLeftPower);
//            frontRightMotor.setPower(frontRightPower);
//            backLeftMotor.setPower(backLeftPower);
//            backRightMotor.setPower(backRightPower);
//
//            // =========================
//            // === Gamepad 2: Shooter
//            // =========================
//
//            // Toggle shooter with right bumper
//            if (gamepad2.right_bumper && !rightBumperPressed) {
//                shooterActive = !shooterActive;
//                rightBumperPressed = true;
//            } else if (!gamepad2.right_bumper) {
//                rightBumperPressed = false;
//            }
//
//            if (shooterActive) {
//                shooterMotor1.setPower(-SHOOTER_POWER);
//                shooterMotor2.setPower(SHOOTER_POWER);
//            } else {
//                shooterMotor1.setPower(0);
//                shooterMotor2.setPower(0);
//            }
//
//            // =========================
//            // === Gamepad 2: Intake + Feed
//            // =========================
//
//            // Toggle intake + feed with B
//            if (gamepad2.b && !bButtonPressed) {
//                intakeFeedActive = !intakeFeedActive;
//                bButtonPressed = true;
//            } else if (!gamepad2.b) {
//                bButtonPressed = false;
//            }
//
//            if (intakeFeedActive) {
//                intakeMotor.setPower(-INTAKE_POWER);          // Intake reverse
//                placeholderMotor.setPower(INTAKE_POWER);      // Feed forward
//            } else {
//                intakeMotor.setPower(0);
//                placeholderMotor.setPower(0);
//            }
//
//            // =======================
//            // === Telemetry
//            // =======================
//
//            telemetry.addData("Shooter Active", shooterActive);
//            telemetry.addData("Shooter Motor 1", shooterMotor1.getPower());
//            telemetry.addData("Shooter Motor 2", shooterMotor2.getPower());
//            telemetry.addData("Intake/Feed Active", intakeFeedActive);
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
//}
