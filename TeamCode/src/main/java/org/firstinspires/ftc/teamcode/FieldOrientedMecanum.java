package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

@Disabled
@TeleOp(name="Field Oriented Mecanum", group="Drive")
public class FieldOrientedMecanum extends LinearOpMode {

    // Declare drive motors
    DcMotor leftFront, leftRear, rightFront, rightRear;
    IMU imu;

    @Override
    public void runOpMode() {

        // Initialize drive motors
        leftFront = hardwareMap.get(DcMotor.class, "front_Left");
        leftRear = hardwareMap.get(DcMotor.class, "back_Left");
        rightFront = hardwareMap.get(DcMotor.class, "front_Right");
        rightRear = hardwareMap.get(DcMotor.class, "back_Right");

        // Adjust motor directions based on your robot's configuration
        // Common setup for mecanum drives:
        leftFront.setDirection(DcMotor.Direction.REVERSE);
        leftRear.setDirection(DcMotor.Direction.REVERSE);
        rightFront.setDirection(DcMotor.Direction.FORWARD);
        rightRear.setDirection(DcMotor.Direction.FORWARD);

        // Set motors to brake on zero power
        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftRear.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightRear.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Initialize IMU
        imu = hardwareMap.get(IMU.class, "imu");
        RevHubOrientationOnRobot.LogoFacingDirection logoDirection = RevHubOrientationOnRobot.LogoFacingDirection.RIGHT;
        RevHubOrientationOnRobot.UsbFacingDirection usbDirection = RevHubOrientationOnRobot.UsbFacingDirection.UP;
        RevHubOrientationOnRobot orientationOnRobot = new RevHubOrientationOnRobot(logoDirection, usbDirection);
        imu.initialize(new IMU.Parameters(orientationOnRobot));
        imu.resetYaw();

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        waitForStart();

        // Reset yaw again at the beginning of the OpMode run
        imu.resetYaw();

        while (opModeIsActive()) {
            // Read gamepad inputs
            double y = -gamepad1.left_stick_y;  // Y-axis is typically inverted
            double x = gamepad1.left_stick_x;
            double rx = -gamepad1.right_stick_x; // Rotation

            // Read the robot's current heading from the IMU in radians
            double botHeading = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);

            // Rotate the movement inputs by the inverse of the robot's heading
            double rotX = x * Math.cos(-botHeading) - y * Math.sin(-botHeading);
            double rotY = x * Math.sin(-botHeading) + y * Math.cos(-botHeading);

            // Calculate the power for each mecanum wheel
            double frontLeftPower = rotY + rotX + rx;
            double backLeftPower = rotY - rotX + rx;
            double frontRightPower = rotY - rotX - rx;
            double backRightPower = rotY + rotX - rx;

            // Normalize the power values to prevent them from exceeding 1.0
            double max = Math.max(Math.abs(frontLeftPower), Math.abs(backLeftPower));
            max = Math.max(max, Math.abs(frontRightPower));
            max = Math.max(max, Math.abs(backRightPower));

            if (max > 1.0) {
                frontLeftPower /= max;
                backLeftPower /= max;
                frontRightPower /= max;
                backRightPower /= max;
            }

            // Set power to the motors
            leftFront.setPower(frontLeftPower);
            leftRear.setPower(backLeftPower);
            rightFront.setPower(frontRightPower);
            rightRear.setPower(backRightPower);

            // Add a button to reset the yaw
            if (gamepad1.options) {
                imu.resetYaw();
            }

            // Add telemetry for debugging
            telemetry.addData("Status", "Running");
            telemetry.addData("Heading", imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES));
            telemetry.addData("Left Stick Y", y);
            telemetry.addData("Left Stick X", x);
            telemetry.addData("Right Stick X", rx);
            telemetry.addData("Front Left Power", frontLeftPower);
            telemetry.addData("Front Right Power", frontRightPower);
            telemetry.addData("Back Left Power", backLeftPower);
            telemetry.addData("Back Right Power", backRightPower);
            telemetry.update();
        }
    }
}