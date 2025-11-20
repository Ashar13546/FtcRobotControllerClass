//package org.firstinspires.ftc.teamcode;
//
//import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
//import com.qualcomm.robotcore.hardware.DcMotor;
//import com.qualcomm.robotcore.hardware.DcMotorSimple;
//import com.qualcomm.robotcore.hardware.Gamepad;
//import com.qualcomm.robotcore.hardware.IMU;
//import com.qualcomm.robotcore.hardware.PIDCoefficients;
//import com.qualcomm.robotcore.util.ElapsedTime;
//import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
//import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
//import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
//
//@TeleOp(name="FieldOrientedShooter", group="Linear OpMode")
//public class FieldOrientedShooter extends LinearOpMode {
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
//    // IMU for field-oriented driving
//    private IMU imu = null;
//
//    // Constants for motor power
//    private static final double INTAKE_POWER = 0.2;
//    private static final double SHOOTER_POWER = 0.2;
//
//    // Variable for toggling the shooter motors
//    private boolean shooterActive = false;
//    private boolean rightBumperPressed = false;
//
//    @Override
//    public void runOpMode() {
//
//        // --- Hardware Initialization ---
//        frontLeftMotor = hardwareMap.get(DcMotor.class, "front_left_motor");
//        frontRightMotor = hardwareMap.get(DcMotor.class, "front_right_motor");
//        backLeftMotor = hardwareMap.get(DcMotor.class, "back_left_motor");
//        backRightMotor = hardwareMap.get(DcMotor.class, "back_right_motor");
//
//        intakeMotor = hardwareMap.get(DcMotor.class, "intake_motor");
//        shooterMotor1 = hardwareMap.get(DcMotor.class, "shooter_motor_1");
//        shooterMotor2 = hardwareMap.get(DcMotor.class, "shooter_motor_2");
//
//        // Set motor directions (may need to be reversed depending on your robot)
//        frontLeftMotor.setDirection(DcMotorSimple.Direction.REVERSE);
//        backLeftMotor.setDirection(DcMotorSimple.Direction.REVERSE);
//        frontRightMotor.setDirection(DcMotorSimple.Direction.FORWARD);
//        backRightMotor.setDirection(DcMotorSimple.Direction.FORWARD);
//
//        // Shooter motors spin in opposite directions to create launch force
//        shooterMotor1.setDirection(DcMotorSimple.Direction.REVERSE);
//        shooterMotor2.setDirection(DcMotorSimple.Direction.FORWARD);
//
//        // Set motor run modes
//        setDriveMotorRunMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//        intakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//        shooterMotor1.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//        shooterMotor2.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//
//        // --- IMU Initialization ---
//        imu = hardwareMap.get(IMU.class, "imu");
//        RevHubOrientationOnRobot.LogoFacingDirection logoDirection = RevHubOrientationOnRobot.LogoFacingDirection.UP;
//        RevHubOrientationOnRobot.UsbFacingDirection usbDirection = RevHubOrientationOnRobot.UsbFacingDirection.FORWARD;
//        RevHubOrientationOnRobot orientationOnRobot = new RevHubOrientationOnRobot(logoDirection, usbDirection);
//        imu.initialize(new IMU.Parameters(orientationOnRobot));
//        imu.resetYaw();
//
//        telemetry.addData("Status", "Initialized");
//        telemetry.update();
//
//        waitForStart();
//
//        // --- Main TeleOp Loop ---
//        while (opModeIsActive()) {
//            // --- Field-Oriented Mecanum Drive ---
//            double y = gamepad1.left_stick_y;
//            double x = -gamepad1.left_stick_x;
//            double rotation = gamepad1.right_stick_x;
//
//            // Get robot heading from IMU
//            double botHeading = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);
//
//            // Rotate the movement vector by the inverse of the robot's heading
//            double rotX = x * Math.cos(-botHeading) - y * Math.sin(-botHeading);
//            double rotY = x * Math.sin(-botHeading) + y * Math.cos(-botHeading);
//
//            // Calculate wheel powers
//            double frontLeftPower = rotY + rotX + rotation;
//            double frontRightPower = rotY - rotX - rotation;
//            double backLeftPower = rotY - rotX + rotation;
//            double backRightPower = rotY + rotX - rotation;
//
//            // Normalize the wheel powers to ensure no value exceeds 1.0
//            double maxPower = Math.max(Math.abs(frontLeftPower), Math.abs(frontRightPower));
//            maxPower = Math.max(maxPower, Math.abs(backLeftPower));
//            maxPower = Math.max(maxPower, Math.abs(backRightPower));
//
//            if (maxPower > 1.0) {
//                frontLeftPower /= maxPower;
//                frontRightPower /= maxPower;
//                backLeftPower /= maxPower;
//                backRightPower /= maxPower;
//            }
//
//            // Set motor powers
//            frontLeftMotor.setPower(frontLeftPower);
//            frontRightMotor.setPower(frontRightPower);
//            backLeftMotor.setPower(backLeftPower);
//            backRightMotor.setPower(backRightPower);
//
//            // --- Intake Control ---
//            if (gamepad1.x) {
//                intakeMotor.setPower(INTAKE_POWER);
//            } else if (gamepad1.b) { // Way to reverse the intake
//                intakeMotor.setPower(-INTAKE_POWER);
//            } else {
//                intakeMotor.setPower(0);
//            }
//
//            // --- Shooter Control ---
//            if (gamepad1.right_bumper && !rightBumperPressed) {
//                shooterActive = !shooterActive; // Toggle the shooter state
//                rightBumperPressed = true;
//            } else if (!gamepad1.right_bumper) {
//                rightBumperPressed = false;
//            }
//
//            if (shooterActive) {
//                shooterMotor1.setPower(SHOOTER_POWER);
//                shooterMotor2.setPower(SHOOTER_POWER);
//            } else {
//                shooterMotor1.setPower(0);
//                shooterMotor2.setPower(0);
//            }
//
//            telemetry.addData("Intake Power", intakeMotor.getPower());
//            telemetry.addData("Shooter Power", shooterMotor1.getPower());
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
