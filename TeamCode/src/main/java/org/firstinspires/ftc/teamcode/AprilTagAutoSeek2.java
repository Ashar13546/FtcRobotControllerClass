//package org.firstinspires.ftc.teamcode;
//
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
//@Disabled
//@TeleOp(name = "AprilTagAutoSeek2", group = "Vision")
//public class AprilTagAutoSeek2 extends LinearOpMode {
//
//    // --- Vision Components ---
//    private VisionPortal visionPortal;
//    private AprilTagProcessor aprilTag;
//
//    // --- Hardware ---
//    private Servo tilt_servo;
//    private Servo pan_servo;
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
//    // --- Tilt starting position ---
//    private final double TILT_START_POS = 0.33;
//
//    // --- Pan servo sweep settings ---
//    private final double PAN_MIN_POS = 0.2;
//    private final double PAN_MAX_POS = 0.8;
//    private final double PAN_SPEED = 0.01;  // Increment per cycle for pan servo
//
//    private double currentPanPos = PAN_MIN_POS;
//    private boolean panMovingRight = true;
//
//    // --- Tilt oscillation parameters ---
//    private double currentTiltPos = TILT_START_POS;
//    private final double TILT_MAX_OFFSET = 0.15;
//    private final double TILT_STEP = 0.01;
//    private boolean tiltGoingUp = true;
//
//    @Override
//    public void runOpMode() {
//        // --- Hardware Init ---
//        tilt_servo = hardwareMap.get(Servo.class, "tilt_servo");
//        pan_servo = hardwareMap.get(Servo.class, "pan_servo");
//
//        leftMotor = hardwareMap.get(DcMotor.class, "left_motor");
//        rightMotor = hardwareMap.get(DcMotor.class, "right_motor");
//
//        leftMotor.setDirection(DcMotor.Direction.FORWARD);
//        rightMotor.setDirection(DcMotor.Direction.REVERSE);
//
//        tilt_servo.setDirection(Servo.Direction.REVERSE);
//        pan_servo.setDirection(Servo.Direction.FORWARD);
//
//        tilt_servo.scaleRange(0.2, 0.8);
//        pan_servo.scaleRange(PAN_MIN_POS, PAN_MAX_POS);
//
//        // Initialize servo positions
//        tilt_servo.setPosition(TILT_START_POS);
//        pan_servo.setPosition(currentPanPos);
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
//                // Manual inputs for driving
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
//                double drive = manualDrive;  // default manual drive
//
//                if (targetDetection != null) {
//                    // Tag visible - track tag
//                    double panError = targetDetection.center.x - centerX;
//                    double tiltError = targetDetection.center.y - centerY;
//
//                    // Smooth tilt error
//                    smoothedTiltError = smoothingFactor * tiltError + (1 - smoothingFactor) * smoothedTiltError;
//
//                    // Pan correction (turn robot)
//                    if (Math.abs(panError) > deadZone) {
//                        double kP = 0.0015;
//                        turnCorrection = panError * kP;
//                    }
//
//                    if (gamepad1.a) {
//                        // Move forward fixed power, tilt fixed at start position
//                        drive = 0.5;
//                        tilt_servo.setPosition(TILT_START_POS);
//                        tiltIntegral = 0.0;
//                        tiltLastError = 0.0;
//
//                        // Keep pan centered while tracking
//                        pan_servo.setPosition((PAN_MIN_POS + PAN_MAX_POS) / 2);
//
//                        telemetry.addData("Mode", "A pressed - Moving Forward, Tilt at Start");
//                    } else {
//                        // PID control for tilt servo
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
//                        // Keep pan centered during tracking
//                        pan_servo.setPosition((PAN_MIN_POS + PAN_MAX_POS) / 2);
//
//                        telemetry.addData("Mode", "Normal Tracking");
//                    }
//
//                    // Distance check (avoid NPE)
//                    double distance = Double.POSITIVE_INFINITY;
//                    if (targetDetection.ftcPose != null) {
//                        distance = targetDetection.ftcPose.range;
//                    } else {
//                        telemetry.addLine("Warning: ftcPose is null");
//                    }
//
//                    if (gamepad1.a && distance != Double.POSITIVE_INFINITY) {
//                        drive = 0.5;
//                    }
//
//                    telemetry.addData("Tracking ID", targetDetection.id);
//                    telemetry.addData("Pan Error", "%.2f", panError);
//                    telemetry.addData("Turn Correction", "%.3f", turnCorrection);
//                    telemetry.addData("Distance", "%.2f", distance);
//                } else {
//                    telemetry.addData("Status", "No target visible");
//                    currentlyTrackedTagID = null;
//
//                    // --- PAN SWEEP logic ---
//                    if (panMovingRight) {
//                        currentPanPos += PAN_SPEED;
//                        if (currentPanPos >= PAN_MAX_POS) {
//                            currentPanPos = PAN_MAX_POS;
//                            panMovingRight = false;
//
//                            // Increment or decrement tilt after sweep
//                            if (tiltGoingUp) {
//                                currentTiltPos += TILT_STEP;
//                                if (currentTiltPos >= TILT_START_POS + TILT_MAX_OFFSET) {
//                                    currentTiltPos = TILT_START_POS + TILT_MAX_OFFSET;
//                                    tiltGoingUp = false;
//                                }
//                            } else {
//                                currentTiltPos -= TILT_STEP;
//                                if (currentTiltPos <= TILT_START_POS) {
//                                    currentTiltPos = TILT_START_POS;
//                                    tiltGoingUp = true;
//                                }
//                            }
//                        }
//                    } else {
//                        currentPanPos -= PAN_SPEED;
//                        if (currentPanPos <= PAN_MIN_POS) {
//                            currentPanPos = PAN_MIN_POS;
//                            panMovingRight = true;
//
//                            // Increment or decrement tilt after sweep
//                            if (tiltGoingUp) {
//                                currentTiltPos += TILT_STEP;
//                                if (currentTiltPos >= TILT_START_POS + TILT_MAX_OFFSET) {
//                                    currentTiltPos = TILT_START_POS + TILT_MAX_OFFSET;
//                                    tiltGoingUp = false;
//                                }
//                            } else {
//                                currentTiltPos -= TILT_STEP;
//                                if (currentTiltPos <= TILT_START_POS) {
//                                    currentTiltPos = TILT_START_POS;
//                                    tiltGoingUp = true;
//                                }
//                            }
//                        }
//                    }
//
//                    pan_servo.setPosition(currentPanPos);
//                    tilt_servo.setPosition(currentTiltPos);
//                }
//
//                // Combine drive + turn + manual turn inputs
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
