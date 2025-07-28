package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;

import java.util.List;
@Disabled
@TeleOp(name = "AprilTag Servo Tracker", group = "TeleOp")
public class AprilTagServoTracker extends LinearOpMode {

    private Servo pan_servo = null;
    private Servo tilt_servo = null;
    private VisionPortal visionPortal;
    private AprilTagProcessor aprilTagProcessor;
    private IMU imu;

    private double panPosition = 0.5;
    private double tiltPosition = 0.5;

    // --- Tuning Variables for P-Control ---
    // These values control the sensitivity of the servos.
    // Adjust them to change how quickly and smoothly the servos respond.
    private static final double PAN_GAIN = 0.005; // Pan servo sensitivity
    private static final double TILT_GAIN = 0.005; // Tilt servo sensitivity

    @Override
    public void runOpMode() {

        // --- Hardware Initialization ---

        pan_servo = hardwareMap.get(Servo.class, "pan_servo");
        tilt_servo = hardwareMap.get(Servo.class, "tilt_servo");

        pan_servo.setPosition(panPosition);
        tilt_servo.setPosition(tiltPosition);

        imu = hardwareMap.get(IMU.class, "imu");
        RevHubOrientationOnRobot.LogoFacingDirection logoDirection = RevHubOrientationOnRobot.LogoFacingDirection.RIGHT;
        RevHubOrientationOnRobot.UsbFacingDirection usbDirection = RevHubOrientationOnRobot.UsbFacingDirection.UP;
        RevHubOrientationOnRobot orientationOnRobot = new RevHubOrientationOnRobot(logoDirection, usbDirection);
        imu.initialize(new IMU.Parameters(orientationOnRobot));
        imu.resetYaw();

        aprilTagProcessor = new AprilTagProcessor.Builder()
                .setTagFamily(AprilTagProcessor.TagFamily.TAG_36h11)
                .build();

        visionPortal = new VisionPortal.Builder()
                .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))
                .addProcessor(aprilTagProcessor)
                .build();

        telemetry.addData("Status", "Initialized and Ready to Track Any Tag");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {

            List<AprilTagDetection> currentDetections = aprilTagProcessor.getDetections();
            AprilTagDetection targetTag = null;

            if (!currentDetections.isEmpty()) {
                // Track the first detected tag
                targetTag = currentDetections.get(0);
            }

            if (targetTag != null) {
                // Found a tag, calculate the servo adjustments using P-control
                double panError = targetTag.ftcPose.bearing;
                double tiltError = targetTag.ftcPose.elevation;

                // Adjust the servo positions based on the error and gain
                panPosition -= panError * PAN_GAIN;
                tiltPosition += tiltError * TILT_GAIN;

                // Clip the positions to the valid servo range [0.0, 1.0]
                panPosition = Range.clip(panPosition, 0.0, 1.0);
                tiltPosition = Range.clip(tiltPosition, 0.0, 1.0);

                // Set the new servo positions
                pan_servo.setPosition(panPosition);
                tilt_servo.setPosition(tiltPosition);

                telemetry.addData("Tracking", "Tag ID: %d", targetTag.id);
                telemetry.addData("Pan Position", "%.2f", panPosition);
                telemetry.addData("Tilt Position", "%.2f", tiltPosition);
                telemetry.addData("Pan Error (bearing)", "%.2f", panError);
                telemetry.addData("Tilt Error (elevation)", "%.2f", tiltError);
            } else {
                telemetry.addData("Tracking", "No tag found");
            }

            telemetry.addData("Robot Heading (Yaw)", "%.2f", imu.getRobotYawPitchRollAngles().getYaw(org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.DEGREES));
            telemetry.update();
        }
    }
}