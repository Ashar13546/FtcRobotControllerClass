import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;

import java.util.List;

@TeleOp(name = "AprilTagAlignment", group = "Vision")
public class AprilTagAlignment extends LinearOpMode {

    // --- Vision ---
    private VisionPortal visionPortal;
    private AprilTagProcessor aprilTag;

    // --- Hardware ---
    private DcMotor left_motor;
    private DcMotor right_motor;
    private Servo tilt_servo;

    // --- Vision Settings ---
    private final int centerX = 320;
    private final int deadZone = 40;
    private final int[] validTagIDs = {20, 21, 22, 23};

    // --- Tracking ---
    private Integer trackedTagID = null;
    private final double TILT_START_POS = 0.33;

    @Override
    public void runOpMode() {
        // --- Hardware Init ---
        tilt_servo = hardwareMap.get(Servo.class, "tilt_servo");
        left_motor = hardwareMap.get(DcMotor.class, "left_motor");
        right_motor = hardwareMap.get(DcMotor.class, "right_motor");

        left_motor.setDirection(DcMotor.Direction.FORWARD);
        right_motor.setDirection(DcMotor.Direction.REVERSE);

        tilt_servo.setDirection(Servo.Direction.REVERSE);
        tilt_servo.scaleRange(0.2, 0.8);
        tilt_servo.setPosition(TILT_START_POS);

        // --- Vision Init ---
        aprilTag = new AprilTagProcessor.Builder()
                .setTagFamily(AprilTagProcessor.TagFamily.TAG_36h11)
                .build();

        visionPortal = new VisionPortal.Builder()
                .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))
                .addProcessor(aprilTag)
                .build();

        telemetry.addLine("Initialized. Waiting for start...");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {
            // --- Manual Inputs ---
            double drive = -gamepad1.left_stick_y;
            double turn = gamepad1.right_stick_x;

            // --- AprilTag Detection ---
            List<AprilTagDetection> detections = aprilTag.getDetections();
            AprilTagDetection target = null;

            for (AprilTagDetection detection : detections) {
                if (trackedTagID != null && detection.id == trackedTagID) {
                    target = detection;
                    break;
                } else if (trackedTagID == null && isValidTag(detection.id)) {
                    trackedTagID = detection.id;
                    target = detection;
                    break;
                }
            }

            double turnCorrection = 0.0;

            if (target != null) {
                double errorX = target.center.x - centerX;

                if (Math.abs(errorX) > deadZone) {
                    double kP = 0.0015;
                    turnCorrection = errorX * kP;
                }

                if (gamepad1.a) {
                    drive = 0.4; // forward power while A pressed
                    tilt_servo.setPosition(TILT_START_POS);
                    telemetry.addData("Mode", "Align + Drive");
                } else {
                    telemetry.addData("Mode", "Align Only");
                }

                telemetry.addData("Tracking", "Tag ID: %d", target.id);
                telemetry.addData("ErrorX", "%.2f", errorX);
            } else {
                trackedTagID = null;
                telemetry.addLine("No tag detected");
                if (!gamepad1.a) {
                    tilt_servo.setPosition(TILT_START_POS);
                }
            }

            // --- Motor Control ---
            double leftPower = drive + turnCorrection + turn;
            double rightPower = drive - turnCorrection - turn;

            double max = Math.max(Math.abs(leftPower), Math.abs(rightPower));
            if (max > 1.0) {
                leftPower /= max;
                rightPower /= max;
            }

            left_motor.setPower(leftPower * 0.7);
            right_motor.setPower(rightPower * 0.7);

            telemetry.update();
        }

        visionPortal.close();
    }

    private boolean isValidTag(int id) {
        for (int validID : validTagIDs) {
            if (id == validID) return true;
        }
        return false;
    }
}