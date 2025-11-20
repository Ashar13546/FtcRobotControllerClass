//import com.qualcomm.robotcore.eventloop.opmode.Disabled;
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
//import com.qualcomm.robotcore.hardware.DcMotor;
//import com.qualcomm.robotcore.hardware.Servo;
//import com.qualcomm.robotcore.util.ElapsedTime;
//
//import org.firstinspires.ftc.vision.VisionPortal;
//import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
//import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
//import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
//
//import java.util.List;
//
//
//@TeleOp(name = "AprilTagDriveTrainTest3", group = "Vision")
//public class AprilTagDriveTrainTest3 extends LinearOpMode {
//
//    // --- Vision Components ---
//    private VisionPortal visionPortal;
//    private AprilTagProcessor aprilTag;
//
//    // --- Hardware ---
//    private Servo tilt_servo;
//    private DcMotor leftMotor;
//    private DcMotor rightMotor;
//
//    // --- Tilt PID Constants ---
//    private double tiltPGain = 0.00003;    // Reduced proportional gain
//    private double tiltDGain = 0.0;        // Derivative gain disabled for stability
//    private double tiltIGain = 0.0;        // Integral gain disabled to avoid windup
//
//    // --- Vision Settings ---
//    private final int centerX = 320;
//    private final int centerY = 240;
//    private final int deadZone = 50;       // Increased deadZone to reduce jitter
//    private final int[] validTagIDs = {20, 21, 22, 23};
//
//    // --- PID State ---
//    private double tiltLastError = 0.0;
//    private double tiltIntegral = 0.0;
//
//    // --- Tilt error smoothing ---
//    private double smoothedTiltError = 0.0;
//    private final double smoothingFactor = 0.2; // Smoothing factor (0-1)
//
//    // --- Tracking State ---
//    private Integer currentlyTrackedTagID = null;
//    private boolean tagVisible = false;
//
//    private ElapsedTime timer = new ElapsedTime();
//
//    // New constant for tilt start position (about 45 degrees up)
//    private final double TILT_START_POS = 0.33;
//
//    @Override
//    public void runOpMode() {
//        // --- Hardware Init ---
//        tilt_servo = hardwareMap.get(Servo.class, "tilt_servo");
//        leftMotor = hardwareMap.get(DcMotor.class, "left_motor");
//        rightMotor = hardwareMap.get(DcMotor.class, "right_motor");
//
//        leftMotor.setDirection(DcMotor.Direction.FORWARD);
//        rightMotor.setDirection(DcMotor.Direction.REVERSE);
//
//        tilt_servo.setDirection(Servo.Direction.REVERSE);
//
//        // Widen scale range for bigger tilt movement
//        tilt_servo.scaleRange(0.2, 0.8);
//
//        // Start position: tilted about 45 degrees up
//        tilt_servo.setPosition(TILT_START_POS);
//
//        // --- Vision Setup ---
//        aprilTag = new AprilTagProcessor.Builder()
//                .setTagFamily(AprilTagProcessor.TagFamily.TAG_36h11)
//                .build();
//
//        visionPortal = new VisionPortal.Builder()
//                .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))
//                .addProcessor(aprilTag)
//                .build();
//
//        telemetry.addLine("Initialized. Waiting for start...");
//        telemetry.update();
//        waitForStart();
//
//        if (opModeIsActive()) {
//            timer.reset();
//
//            while (opModeIsActive()) {
//                double deltaTime = timer.seconds();
//                timer.reset();
//
//                // Manual inputs (for non-A pressed cases)
//                double manualDrive = -gamepad1.left_stick_y;
//                double manualTurn = gamepad1.right_stick_x;
//
//                // Get AprilTag detections
//                List<AprilTagDetection> detections = aprilTag.getDetections();
//                tagVisible = false;
//                AprilTagDetection targetDetection = null;
//
//                for (AprilTagDetection detection : detections) {
//                    int id = detection.id;
//                    if (currentlyTrackedTagID != null && id == currentlyTrackedTagID) {
//                        targetDetection = detection;
//                        tagVisible = true;
//                        break;
//                    } else if (currentlyTrackedTagID == null && isValidTag(id)) {
//                        currentlyTrackedTagID = id;
//                        targetDetection = detection;
//                        tagVisible = true;
//                        break;
//                    }
//                }
//
//                double turnCorrection = 0.0;
//                double drive = manualDrive;  // default drive is manual
//
//                if (targetDetection != null) {
//                    double panError = targetDetection.center.x - centerX;
//                    double tiltError = targetDetection.center.y - centerY;
//
//                    // Smooth tilt error to reduce jitter
//                    smoothedTiltError = smoothingFactor * tiltError + (1 - smoothingFactor) * smoothedTiltError;
//
//                    // Pan correction (always use to turn robot towards tag)
//                    if (Math.abs(panError) > deadZone) {
//                        double kP = 0.0015;  // Tune this value
//                        turnCorrection = panError * kP;
//                    }
//
//                    if (gamepad1.a) {
//                        // A button pressed and tag visible - move forward, keep tilt at start angle
//                        drive = 0.5;  // Fixed forward power, adjust as needed
//
//                        // Keep tilt servo at start tilt angle (do not update it)
//                        tilt_servo.setPosition(TILT_START_POS);
//
//                        // Reset tilt PID state to avoid integral windup when released
//                        tiltIntegral = 0.0;
//                        tiltLastError = 0.0;
//
//                        telemetry.addData("Mode", "A pressed - Moving Forward, Tilt at Start Position");
//                    } else {
//                        // A NOT pressed, do normal tilt PID adjustment
//                        if (Math.abs(smoothedTiltError) > deadZone) {
//                            tiltIntegral += smoothedTiltError * deltaTime;
//                            double tiltDerivative = (smoothedTiltError - tiltLastError) / deltaTime;
//                            if (Math.signum(smoothedTiltError) != Math.signum(tiltLastError)) tiltIntegral = 0.0;
//
//                            double tiltPosition = tilt_servo.getPosition();
//                            tiltPosition += smoothedTiltError * tiltPGain + tiltIntegral * tiltIGain + tiltDerivative * tiltDGain;
//                            tiltPosition = Math.max(0.0, Math.min(1.0, tiltPosition));
//                            tilt_servo.setPosition(tiltPosition);
//
//                            tiltLastError = smoothedTiltError;
//                        } else {
//                            tiltIntegral = 0.0;
//                        }
//
//                        telemetry.addData("Mode", "Normal Tracking");
//                    }
//
//                    telemetry.addData("Status", "Tracking AprilTag ID: %d", targetDetection.id);
//                    telemetry.addData("Pan Error", "%.2f", panError);
//                    telemetry.addData("Turn Correction", "%.3f", turnCorrection);
//                } else {
//                    telemetry.addData("Status", "No target visible.");
//                    currentlyTrackedTagID = null;
//
//                    // Reset tilt servo to start tilt position if no tag visible and A not pressed
//                    if (!gamepad1.a) {
//                        tilt_servo.setPosition(TILT_START_POS);
//                    }
//                }
//
//                // Combine drive and turn corrections
//                double leftPower = drive + turnCorrection + manualTurn;
//                double rightPower = drive - turnCorrection - manualTurn;
//
//                // Normalize motor powers
//                double max = Math.max(Math.abs(leftPower), Math.abs(rightPower));
//                if (max > 1.0) {
//                    leftPower /= max;
//                    rightPower /= max;
//                }
//
//                // Set motor powers with a speed scale
//                leftMotor.setPower(leftPower * 0.7);
//                rightMotor.setPower(rightPower * 0.7);
//
//                telemetry.update();
//                sleep(10);
//            }
//        }
//
//        visionPortal.close();
//    }
//
//    private boolean isValidTag(int id) {
//        for (int validID : validTagIDs) {
//            if (id == validID) return true;
//        }
//        return false;
//    }
//}
