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
//@TeleOp(name="ShooterTest4", group="Linear OpMode")
//public class ShooterMotor4 extends LinearOpMode {
//
//    // Drive motors
//    private DcMotor frontLeftMotor = null;
//    private DcMotor frontRightMotor = null;
//    private DcMotor backLeftMotor = null;
//    private DcMotor backRightMotor = null;
//
//    // Intake motor
//    private DcMotor intakeMotor = null;
//
//    // Shooter motors
//    private DcMotor shooterMotor1 = null;
//    private DcMotor shooterMotor2 = null;
//
//    // Placeholder motor
//    private DcMotor placeholderMotor = null;
//
//    // IMU
//    private IMU imu = null;
//
//    // Constants
//    private static final double INTAKE_POWER = 1.0;
//    private static final double SHOOTER_POWER = 0.2;
//
//    // Shooter toggle states
//    private boolean shooter1Active = false;
//    private boolean shooter2Active = false;
//    private boolean rightBumperPressed = false;
//    private boolean leftBumperPressed = false;
//
//    // Placeholder motor toggle
//    private boolean placeholderMotorActive = false;
//    private boolean aButtonPressed = false;
//
//    @Override
//    public void runOpMode() {
//
//        // Hardware Map
//        frontLeftMotor = hardwareMap.get(DcMotor.class, "front_left_motor");
//        frontRightMotor = hardwareMap.get(DcMotor.class, "front_right_motor");
//        backLeftMotor = hardwareMap.get(DcMotor.class, "back_left_motor");
//        backRightMotor = hardwareMap.get(DcMotor.class, "back_right_motor");
//
//        intakeMotor = hardwareMap.get(DcMotor.class, "intake_motor");
//        shooterMotor1 = hardwareMap.get(DcMotor.class, "shooter_motor_1");
//        shooterMotor2 = hardwareMap.get(DcMotor.class, "shooter_motor_2");
//
//        placeholderMotor = hardwareMap.get(DcMotor.class, "feed_motor");
//
//        // Set directions
//        frontLeftMotor.setDirection(DcMotorSimple.Direction.REVERSE);
//        backLeftMotor.setDirection(DcMotorSimple.Direction.REVERSE);
//        frontRightMotor.setDirection(DcMotorSimple.Direction.FORWARD);
//        backRightMotor.setDirection(DcMotorSimple.Direction.FORWARD);
//        shooterMotor1.setDirection(DcMotorSimple.Direction.REVERSE);
//        shooterMotor2.setDirection(DcMotorSimple.Direction.REVERSE);
//
//        // Set run modes
//        setDriveMotorRunMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//        intakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//        shooterMotor1.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//        shooterMotor2.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//        placeholderMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//
//        // IMU init
//        imu = hardwareMap.get(IMU.class, "imu");
//        RevHubOrientationOnRobot orientationOnRobot = new RevHubOrientationOnRobot(
//                RevHubOrientationOnRobot.LogoFacingDirection.UP,
//                RevHubOrientationOnRobot.UsbFacingDirection.LEFT
//        );
//        imu.initialize(new IMU.Parameters(orientationOnRobot));
//        imu.resetYaw();
//
//        telemetry.addData("Status", "Initialized");
//        telemetry.update();
//
//        waitForStart();
//
//        while (opModeIsActive()) {
//            // Field-oriented drive
//            double y = gamepad1.left_stick_y;
//            double x = -gamepad1.left_stick_x;
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
//            double maxPower = Math.max(Math.abs(frontLeftPower), Math.max(Math.abs(frontRightPower),
//                    Math.max(Math.abs(backLeftPower), Math.abs(backRightPower))));
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
//            // Intake Control
//            if (gamepad1.x) {
//                intakeMotor.setPower(INTAKE_POWER);
//            } else if (gamepad1.b) {
//                intakeMotor.setPower(-INTAKE_POWER);
//            } else {
//                intakeMotor.setPower(0);
//            }
//
//            // Shooter 1 (right bumper)
//            if (gamepad1.right_bumper && !rightBumperPressed) {
//                shooter1Active = !shooter1Active;
//                rightBumperPressed = true;
//            } else if (!gamepad1.right_bumper) {
//                rightBumperPressed = false;
//            }
//
//            // Shooter 2 (left bumper)
//            if (gamepad1.left_bumper && !leftBumperPressed) {
//                shooter2Active = !shooter2Active;
//                leftBumperPressed = true;
//            } else if (!gamepad1.left_bumper) {
//                leftBumperPressed = false;
//            }
//
//            shooterMotor1.setPower(shooter1Active ? -SHOOTER_POWER : 0);
//            shooterMotor2.setPower(shooter2Active ? SHOOTER_POWER : 0);
//
//            // Placeholder Motor (button A toggle)
//            if (gamepad1.a && !aButtonPressed) {
//                placeholderMotorActive = !placeholderMotorActive;
//                aButtonPressed = true;
//            } else if (!gamepad1.a) {
//                aButtonPressed = false;
//            }
//
//            placeholderMotor.setPower(placeholderMotorActive ? 1.0 : 0);
//
//            // Telemetry
//            telemetry.addData("Shooter 1", shooterMotor1.getPower());
//            telemetry.addData("Shooter 2", shooterMotor2.getPower());
//            telemetry.addData("Intake", intakeMotor.getPower());
//            telemetry.addData("Feed Motor", placeholderMotor.getPower());
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
