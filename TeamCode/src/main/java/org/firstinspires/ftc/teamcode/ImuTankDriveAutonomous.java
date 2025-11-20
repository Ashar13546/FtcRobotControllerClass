//package org.firstinspires.ftc.teamcode;
//
//import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
//import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
//import com.qualcomm.robotcore.eventloop.opmode.Disabled;
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
//import com.qualcomm.robotcore.hardware.DcMotor;
//import com.qualcomm.robotcore.hardware.IMU;
//import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
//import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
//import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
//
//@Disabled
//@Autonomous
//public class ImuTankDriveAutonomous extends LinearOpMode {
//
//    DcMotor leftMotor, rightMotor;
//    IMU imu;
//
//    static final double DRIVE_SPEED = 0.5;
//    static final double TURN_SPEED = 0.3;
//
//    @Override
//    public void runOpMode() {
//
//        // Initialize motors
//        leftMotor = hardwareMap.get(DcMotor.class, "motor_Left");
//        rightMotor = hardwareMap.get(DcMotor.class, "motor_Right");
//
//        leftMotor.setDirection(DcMotor.Direction.REVERSE);
//        rightMotor.setDirection(DcMotor.Direction.FORWARD);
//
//        // --- CORRECTED IMU INITIALIZATION ---
//        imu = hardwareMap.get(IMU.class, "imu");
//
//        // Set up the parameters for the IMU, specifying the hub's orientation
//        // Replace UP and FORWARD with the actual orientation of your hub
//        RevHubOrientationOnRobot.LogoFacingDirection logoDirection = RevHubOrientationOnRobot.LogoFacingDirection.FORWARD;
//        RevHubOrientationOnRobot.UsbFacingDirection usbDirection = RevHubOrientationOnRobot.UsbFacingDirection.UP; // Assumes USB ports are pointing upwards
//        RevHubOrientationOnRobot orientationOnRobot = new RevHubOrientationOnRobot(logoDirection, usbDirection);
//
//        // Pass the orientation parameters to the IMU's initialize method
//        imu.initialize(new IMU.Parameters(orientationOnRobot));
//        imu.resetYaw(); // Reset the heading to 0 degrees at the start
//
//        telemetry.addData("Status", "Initialized");
//        telemetry.update();
//
//        waitForStart();
//
//        imu.resetYaw(); // Reset again at the beginning of the OpMode run
//
//        // Example 1: Drive straight for 5 seconds
//        driveStraight(DRIVE_SPEED, 5.0);
//
//        // Example 2: Turn 90 degrees to the right
//        turn(90.0, TURN_SPEED);
//
//        // Example 3: Drive straight again for 2 seconds
//        driveStraight(DRIVE_SPEED, 2.0);
//
//        // Stop all motors
//        leftMotor.setPower(0);
//        rightMotor.setPower(0);
//    }
//
//    /**
//     * Drives the robot straight for a specified amount of time.
//     * @param speed The power to apply to the motors.
//     * @param seconds The time to drive, in seconds.
//     */
//    public void driveStraight(double speed, double seconds) {
//        double startTime = getRuntime();
//        double targetHeading = getHeading();
//
//        while (opModeIsActive() && getRuntime() < startTime + seconds) {
//            double currentHeading = getHeading();
//            double steeringAdjustment = (targetHeading - currentHeading) * 0.05; // P-Controller
//
//            leftMotor.setPower(speed + steeringAdjustment);
//            rightMotor.setPower(speed - steeringAdjustment);
//
//            telemetry.addData("Heading", currentHeading);
//            telemetry.update();
//        }
//        leftMotor.setPower(0);
//        rightMotor.setPower(0);
//    }
//
//    /**
//     * Turns the robot to a specific heading.
//     * @param targetAngle The target angle in degrees.
//     * @param speed The power to apply to the motors for turning.
//     */
//    public void turn(double targetAngle, double speed) {
//        double currentAngle = getHeading();
//
//        if (targetAngle > 180) targetAngle -= 360;
//        else if (targetAngle < -180) targetAngle += 360;
//
//        while (opModeIsActive() && Math.abs(targetAngle - currentAngle) > 2.0) {
//            double angleDifference = targetAngle - currentAngle;
//
//            if (angleDifference > 180) angleDifference -= 360;
//            else if (angleDifference < -180) angleDifference += 360;
//
//            double turnPower = angleDifference > 0 ? speed : -speed;
//
//            leftMotor.setPower(-turnPower);
//            rightMotor.setPower(turnPower);
//
//            currentAngle = getHeading();
//            telemetry.addData("Target Angle", targetAngle);
//            telemetry.addData("Current Angle", currentAngle);
//            telemetry.update();
//        }
//        leftMotor.setPower(0);
//        rightMotor.setPower(0);
//    }
//
//    /**
//     * Helper method to get the current Z-axis (yaw) angle from the IMU.
//     * @return The yaw angle in degrees.
//     */
//    private double getHeading() {
//        return imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
//    }
//}
