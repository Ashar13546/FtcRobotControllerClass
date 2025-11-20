//package org.firstinspires.ftc.teamcode;
//
//import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
//import com.qualcomm.robotcore.eventloop.opmode.Disabled;
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
//import com.qualcomm.robotcore.hardware.DcMotor;
//import com.qualcomm.robotcore.hardware.DcMotorSimple;
//import com.qualcomm.robotcore.hardware.IMU;
//import com.qualcomm.robotcore.hardware.Servo;
//import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
//
//@Disabled
//@TeleOp(name = "DriveTrainShooter", group = "Linear OpMode")
//public class DriveTrainShooter extends LinearOpMode {
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
//    // Feed motor (placeholder)
//    private DcMotor placeholderMotor = null;
//
//    // IMU for field-oriented drive
//    private IMU imu = null;
//
//    // Servos
//    private Servo hold_servo = null;
//    private Servo push_servo = null;
//
//    // Constants
//    private static final double INTAKE_POWER = 1.0;
//    private static final double SHOOTER_POWER = 0.6;
//
//    // Shooter toggle
//    private boolean shooterActive = false;
//    private boolean rightBumperPressed = false;
//
//    // Combined intake/feed toggle
//    private boolean intakeFeedActive = false;
//    private boolean bButtonPressed = false;
//
//    @Override
//    public void runOpMode() {
//
//        // Hardware initialization
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
//        // Set motor directions
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
//        // Set run modes
//        setDriveMotorRunMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//        intakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//        shooterMotor1.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//        shooterMotor2.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//        placeholderMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//
//        // Set initial servo positions
//        hold_servo.setPosition(0.0); // Hold position
//        push_servo.setPosition(0.0); // Retracted
//
//        // IMU setup
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
//            // --- Drivetrain control (arcade style) ---
//            double drive = gamepad1.left_stick_y;  // forward/back
//            double turn  = gamepad1.right_stick_x;  // turn
//
//            double leftPower = drive + turn;
//            double rightPower = drive - turn;
//
//            // Normalize powers if any is outside [-1,1]
//            double maxPower = Math.max(Math.abs(leftPower), Math.abs(rightPower));
//            if (maxPower > 1.0) {
//                leftPower /= maxPower;
//                rightPower /= maxPower;
//            }
//
//            // Apply power to motors
//            frontLeftMotor.setPower(leftPower);
//            backLeftMotor.setPower(leftPower);
//            frontRightMotor.setPower(rightPower);
//            backRightMotor.setPower(rightPower);
//
//            // --- Shooter Toggle (Right Bumper) ---
//            if (gamepad1.right_bumper && !rightBumperPressed) {
//                shooterActive = !shooterActive;
//                rightBumperPressed = true;
//            } else if (!gamepad1.right_bumper) {
//                rightBumperPressed = false;
//            }
//
//            if (shooterActive) {
//                shooterMotor1.setPower(-SHOOTER_POWER); // Opposing directions
//                shooterMotor2.setPower(SHOOTER_POWER);
//            } else {
//                shooterMotor1.setPower(0);
//                shooterMotor2.setPower(0);
//            }
//
//            // --- Intake + Feed Toggle (B Button) ---
//            if (gamepad1.b && !bButtonPressed) {
//                intakeFeedActive = !intakeFeedActive;
//                bButtonPressed = true;
//            } else if (!gamepad1.b) {
//                bButtonPressed = false;
//            }
//
//            if (intakeFeedActive) {
//                intakeMotor.setPower(-INTAKE_POWER);         // Reverse
//                placeholderMotor.setPower(INTAKE_POWER);    // Reverse
//            } else {
//                intakeMotor.setPower(0);
//                placeholderMotor.setPower(0);
//            }
//
//            // --- Hold + Push Servos (A Button) ---
//            if (gamepad1.a) {
//                hold_servo.setPosition(0); // Release
//                push_servo.setPosition(0); // Push
//            } else {
//                hold_servo.setPosition(1.0); // Hold
//                push_servo.setPosition(1.0); // Retract
//            }
//
//            // --- Telemetry ---
//            telemetry.addData("Shooter Power", shooterMotor1.getPower());
//            telemetry.addData("Intake + Feed Power", intakeMotor.getPower());
//            telemetry.addData("Hold Servo Pos", hold_servo.getPosition());
//            telemetry.addData("Push Servo Pos", push_servo.getPosition());
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
