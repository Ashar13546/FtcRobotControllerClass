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
@TeleOp(name = "AprilTagDriveTrainTest1", group = "Vision")
public class AprilTagDriveTrainTest1 extends LinearOpMode {

    // --- Vision Components ---
    private VisionPortal visionPortal;
    private AprilTagProcessor aprilTag;

    // --- Hardware ---
    private Servo pan_servo;
    private Servo tilt_servo;
    private DcMotor leftMotor;
    private DcMotor rightMotor;

    // --- Control Variables ---
    private double panPosition = 0.5;
    private double tiltPosition = 0.5;

    // --- PID Constants ---
    private double panPGain = 0.0001;
    private double panDGain = 0.00001;
    private double panIGain = 0.000;
    private double tiltPGain = 0.0001;
    private double tiltDGain = 0.00001;
    private double tiltIGain = 0.000;

    // --- Vision Settings ---
    private final int centerX = 320;
    private final int centerY = 240;
    private final int deadZone = 2;
    private final int[] validTagIDs = {21, 22, 23};

    // --- PID State ---
    private double panLastError = 0.0;
    private double tiltLastError = 0.0;
    private double panIntegral = 0.0;
    private double tiltIntegral = 0.0;

    // --- Tracking State ---
    private Integer currentlyTrackedTagID = null;
    private boolean tagVisible = false;

    private ElapsedTime timer = new ElapsedTime();

    @Override
    public void runOpMode() {
        // --- Hardware Init ---
        pan_servo = hardwareMap.get(Servo.class, "pan_servo");
        tilt_servo = hardwareMap.get(Servo.class, "tilt_servo");

        leftMotor = hardwareMap.get(DcMotor.class, "left_motor");
        rightMotor = hardwareMap.get(DcMotor.class, "right_motor");

        // Optional: Reverse one motor if needed
        leftMotor.setDirection(DcMotor.Direction.FORWARD);
        rightMotor.setDirection(DcMotor.Direction.REVERSE);

        pan_servo.setDirection(Servo.Direction.FORWARD);
        tilt_servo.setDirection(Servo.Direction.REVERSE);
        pan_servo.scaleRange(0.4, 0.6);
        tilt_servo.scaleRange(0.4, 0.6);

        pan_servo.setPosition(panPosition);
        tilt_servo.setPosition(tiltPosition);

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

                // --- Drivetrain control using gamepad1 sticks ---
                double drive = -gamepad1.left_stick_y;  // Forward/backward
                double turn = gamepad1.right_stick_x;   // Turning

                double leftPower = drive + turn;
                double rightPower = drive - turn;

                // Normalize powers if any exceeds 1.0
                double max = Math.max(Math.abs(leftPower), Math.abs(rightPower));
                if (max > 1.0) {
                    leftPower /= max;
                    rightPower /= max;
                }

                leftMotor.setPower(leftPower/3);
                rightMotor.setPower(rightPower/3);

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

                if (targetDetection != null) {
                    double panError = targetDetection.center.x - centerX;
                    double tiltError = targetDetection.center.y - centerY;

                    if (Math.abs(panError) > deadZone) {
                        panIntegral += panError * deltaTime;
                        double panDerivative = (panError - panLastError) / deltaTime;
                        if (Math.signum(panError) != Math.signum(panLastError)) panIntegral = 0.0;
                        panPosition += panError * panPGain + panIntegral * panIGain + panDerivative * panDGain;
                    } else {
                        panIntegral = 0.0;
                    }

                    if (Math.abs(tiltError) > deadZone) {
                        tiltIntegral += tiltError * deltaTime;
                        double tiltDerivative = (tiltError - tiltLastError) / deltaTime;
                        if (Math.signum(tiltError) != Math.signum(tiltLastError)) tiltIntegral = 0.0;
                        tiltPosition += tiltError * tiltPGain + tiltIntegral * tiltIGain + tiltDerivative * tiltDGain;
                    } else {
                        tiltIntegral = 0.0;
                    }

                    panPosition = Math.max(0.0, Math.min(1.0, panPosition));
                    tiltPosition = Math.max(0.0, Math.min(1.0, tiltPosition));

                    pan_servo.setPosition(panPosition);
                    tilt_servo.setPosition(tiltPosition);

                    panLastError = panError;
                    tiltLastError = tiltError;

                    telemetry.addData("Status", "Tracking AprilTag ID: %d", targetDetection.id);
                    telemetry.addData("Pan Error", "%.2f", panError);
                    telemetry.addData("Tilt Error", "%.2f", tiltError);
                    telemetry.addData("Pan Servo Pos", "%.2f", panPosition);
                    telemetry.addData("Tilt Servo Pos", "%.2f", tiltPosition);
                } else {
                    telemetry.addData("Status", "No target visible.");
                }

                if (!tagVisible && currentlyTrackedTagID != null) {
                    telemetry.addData("Lost tag", currentlyTrackedTagID);
                    currentlyTrackedTagID = null;
                }

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
