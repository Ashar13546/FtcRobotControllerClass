import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;

import java.util.List;
@Disabled
@TeleOp(name = "AprilTagDriveTrainTest2", group = "Vision")
public class AprilTagDriveTrainTest2 extends LinearOpMode {

    // --- Vision Components ---
    private VisionPortal visionPortal;
    private AprilTagProcessor aprilTag;

    // --- Hardware ---
    private Servo tilt_servo;
    private DcMotor leftMotor;
    private DcMotor rightMotor;

    // --- Tilt PID Constants ---
    private double tiltPGain = 0.0001;
    private double tiltDGain = 0.00001;
    private double tiltIGain = 0.000;

    // --- Vision Settings ---
    private final int centerX = 320;
    private final int centerY = 240;
    private final int deadZone = 10;
    private final int[] validTagIDs = {21, 22, 23};

    // --- PID State ---
    private double tiltLastError = 0.0;
    private double tiltIntegral = 0.0;

    // --- Tracking State ---
    private Integer currentlyTrackedTagID = null;
    private boolean tagVisible = false;
    private ElapsedTime timer = new ElapsedTime();

    @Override
    public void runOpMode() {
        // --- Hardware Init ---
        tilt_servo = hardwareMap.get(Servo.class, "tilt_servo");
        leftMotor = hardwareMap.get(DcMotor.class, "left_motor");
        rightMotor = hardwareMap.get(DcMotor.class, "right_motor");

        leftMotor.setDirection(DcMotor.Direction.FORWARD);
        rightMotor.setDirection(DcMotor.Direction.REVERSE);

        tilt_servo.setDirection(Servo.Direction.REVERSE);
        tilt_servo.scaleRange(0.4, 0.6);
        tilt_servo.setPosition(0.5);

        // --- Vision Setup ---
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

        if (opModeIsActive()) {
            timer.reset();

            while (opModeIsActive()) {
                double deltaTime = timer.seconds();
                timer.reset();

                double drive = -gamepad1.left_stick_y;  // Manual forward/backward
                double manualTurn = gamepad1.right_stick_x;  // Manual turning

                // --- AprilTag Vision ---
                List<AprilTagDetection> detections = aprilTag.getDetections();
                tagVisible = false;
                AprilTagDetection targetDetection = null;

                for (AprilTagDetection detection : detections) {
                    int id = detection.id;
                    if (currentlyTrackedTagID != null && id == currentlyTrackedTagID) {
                        targetDetection = detection;
                        tagVisible = true;
                        break;
                    } else if (currentlyTrackedTagID == null && isValidTag(id)) {
                        currentlyTrackedTagID = id;
                        targetDetection = detection;
                        tagVisible = true;
                        break;
                    }
                }

                double turnCorrection = 0.0;

                if (targetDetection != null) {
                    double panError = targetDetection.center.x - centerX;
                    double tiltError = targetDetection.center.y - centerY;

                    // Only rotate if outside dead zone
                    if (Math.abs(panError) > deadZone) {
                        // Proportional control to rotate robot
                        double kP = 0.0015;  // Tune this value
                        turnCorrection = panError * kP;
                    }

                    // Tilt servo still used (optional)
                    if (Math.abs(tiltError) > deadZone) {
                        tiltIntegral += tiltError * deltaTime;
                        double tiltDerivative = (tiltError - tiltLastError) / deltaTime;
                        if (Math.signum(tiltError) != Math.signum(tiltLastError)) tiltIntegral = 0.0;

                        double tiltPosition = tilt_servo.getPosition();
                        tiltPosition += tiltError * tiltPGain + tiltIntegral * tiltIGain + tiltDerivative * tiltDGain;
                        tiltPosition = Math.max(0.0, Math.min(1.0, tiltPosition));
                        tilt_servo.setPosition(tiltPosition);

                        tiltLastError = tiltError;
                    } else {
                        tiltIntegral = 0.0;
                    }

                    telemetry.addData("Status", "Tracking AprilTag ID: %d", targetDetection.id);
                    telemetry.addData("Pan Error", "%.2f", panError);
                    telemetry.addData("Turn Correction", "%.3f", turnCorrection);
                } else {
                    telemetry.addData("Status", "No target visible.");
                    currentlyTrackedTagID = null;
                }

                // Combine manual drive with vision correction
                double leftPower = drive + turnCorrection + manualTurn;
                double rightPower = drive - turnCorrection - manualTurn;

                // Normalize if needed
                double max = Math.max(Math.abs(leftPower), Math.abs(rightPower));
                if (max > 1.0) {
                    leftPower /= max;
                    rightPower /= max;
                }

                leftMotor.setPower(leftPower * 0.7);   // Slow down
                rightMotor.setPower(rightPower * 0.7); // Slow down

                telemetry.update();
                sleep(10);
            }
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
