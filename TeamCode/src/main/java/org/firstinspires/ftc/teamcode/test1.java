//package org.firstinspires.ftc.teamcode;
//
//import com.qualcomm.hardware.dfrobot.HuskyLens;
//import com.qualcomm.robotcore.eventloop.opmode.Disabled;
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
//import com.qualcomm.robotcore.hardware.Servo;
//import com.qualcomm.robotcore.util.ElapsedTime;
//
//@Disabled
//// The @TeleOp annotation makes this OpMode appear in the list on the Driver Station.
//@TeleOp(name = "test1")
//public class test1 extends LinearOpMode {
//
//    // --- Hardware Members ---
//    private HuskyLens huskylens;
//    private Servo pan_servo;
//    private Servo tilt_servo;
//
//    // --- Control Variables ---
//    private double panPosition = 0.5;
//    private double tiltPosition = 0.5;
//
//    // --- PID Control Constants ---
//    // Use very small P-gain for fine, precise movements to stay within the 5x5 zone.
//    private final double panPGain = 0.00003;
//    private final double panDGain = 0.00003;
//    private final double panIGain = 0.000003;
//    private final double tiltPGain = 0.00003;
//    private final double tiltDGain = 0.00003;
//    private final double tiltIGain = 0.000003;
//
//    // --- Dead Zone ---
//    // The dead zone is the 5x5 pixel range in the center of the display.
//    private final int deadZone = 5;
//
//    // --- HuskyLens Display Center ---
//    // Center coordinates of the HuskyLens display (320x240 pixels).
//    private final int centerX = 160;
//    private final int centerY = 120;
//
//    // --- PID Internal State Variables ---
//    private double panLastError = 0.0;
//    private double tiltLastError = 0.0;
//    private double panIntegral = 0.0;
//    private double tiltIntegral = 0.0;
//
//    // --- Timer for PID calculations ---
//    private ElapsedTime timer = new ElapsedTime();
//
//    @Override
//    public void runOpMode() {
//        // Blocks detected by the HuskyLens.
//        HuskyLens.Block[] myHuskyLensBlocks;
//
//        // --- Hardware Initialization ---
//        huskylens = hardwareMap.get(HuskyLens.class, "huskylens");
//        pan_servo = hardwareMap.get(Servo.class, "pan_servo");
//        tilt_servo = hardwareMap.get(Servo.class, "tilt_servo");
//
//        // Set the servo directions based on your robot's physical setup.
//        // The tilt servo is reversed to move the camera up when the position decreases.
//        pan_servo.setDirection(Servo.Direction.FORWARD);
//        tilt_servo.setDirection(Servo.Direction.REVERSE);
//
//        // Limit the servo's motion range to a conservative value.
//        pan_servo.scaleRange(0.4, 0.6);
//        tilt_servo.scaleRange(0.4, 0.6);
//
//        // Set the initial position of the servos to the center.
//        pan_servo.setPosition(panPosition);
//        tilt_servo.setPosition(tiltPosition);
//
//        // --- HuskyLens Setup ---
//        // Check for HuskyLens communication.
//        telemetry.addData(">>", huskylens.knock() ? "Touch start to continue" : "Problem communicating with HuskyLens");
//        // Select the tag recognition algorithm.
//        huskylens.selectAlgorithm(HuskyLens.Algorithm.TAG_RECOGNITION);
//        telemetry.update();
//
//        // Wait for the game to start.
//        waitForStart();
//
//        // --- Main OpMode Loop ---
//        if (opModeIsActive()) {
//            timer.reset(); // Reset the timer at the start of the loop.
//
//            while (opModeIsActive()) {
//                // Get the blocks from the HuskyLens.
//                myHuskyLensBlocks = huskylens.blocks();
//                double deltaTime = timer.seconds();
//                timer.reset();
//
//                if (myHuskyLensBlocks.length > 0) {
//                    HuskyLens.Block firstBlock = myHuskyLensBlocks[0];
//
//                    // Calculate the error from the center of the display.
//                    double panError = firstBlock.x - centerX;
//                    double tiltError = firstBlock.y - centerY;
//
//                    // --- Pan Servo Logic ---
//                    if (Math.abs(panError) > deadZone) {
//                        panIntegral += panError * deltaTime;
//                        double panDerivative = (panError - panLastError) / deltaTime;
//                        panPosition += panError * panPGain + panIntegral * panIGain + panDerivative * panDGain;
//                    } else {
//                        panIntegral = 0.0; // Reset integral to prevent windup.
//                    }
//
//                    // --- Tilt Servo Logic ---
//                    if (Math.abs(tiltError) > deadZone) {
//                        tiltIntegral += tiltError * deltaTime;
//                        double tiltDerivative = (tiltError - tiltLastError) / deltaTime;
//                        // Use += for tilt position correction based on feedback.
//                        // When the tag is up (y < centerY), tiltError is negative.
//                        // Adding a negative value decreases the servo position, moving the camera up (correct behavior).
//                        // When the tag is down (y > centerY), tiltError is positive.
//                        // Adding a positive value increases the servo position, moving the camera down (correct behavior).
//                        tiltPosition += tiltError * tiltPGain + tiltIntegral * tiltIGain + tiltDerivative * tiltDGain;
//                    } else {
//                        tiltIntegral = 0.0; // Reset integral to prevent windup.
//                    }
//
//                    // Clamp the servo positions to stay within the scaled 0.0 to 1.0 range.
//                    panPosition = Math.max(0.0, Math.min(1.0, panPosition));
//                    tiltPosition = Math.max(0.0, Math.min(1.0, tiltPosition));
//
//                    // Set the new servo positions.
//                    pan_servo.setPosition(panPosition);
//                    tilt_servo.setPosition(tiltPosition);
//
//                    // Update the last error for derivative calculation.
//                    panLastError = panError;
//                    tiltLastError = tiltError;
//
//                    // --- Telemetry for Debugging ---
//                    telemetry.addData("Status", "Tracking AprilTag ID: " + firstBlock.id);
//                    telemetry.addData("Pan Error", "%.2f", panError);
//                    telemetry.addData("Tilt Error", "%.2f", tiltError);
//                    telemetry.addData("Pan Servo Pos", "%.2f", panPosition);
//                    telemetry.addData("Tilt Servo Pos", "%.2f", tiltPosition);
//                } else {
//                    // No tag detected, report status.
//                    telemetry.addData("Status", "No AprilTag detected");
//                }
//                telemetry.update();
//            }
//        }
//    }
//}
