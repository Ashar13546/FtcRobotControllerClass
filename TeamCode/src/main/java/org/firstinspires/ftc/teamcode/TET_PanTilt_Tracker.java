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
//@TeleOp(name = "TET_PanTilt_Tracker")
//public class TET_PanTilt_Tracker extends LinearOpMode {
//
//    private HuskyLens huskylens;
//    private Servo pan_servo;
//    private Servo tilt_servo;
//
//    private double panPosition = 0.5;
//    private double tiltPosition = 0.5;
//
//    // Proportional (P) and Derivative (D) control constants.
//    private final double panPGain = 0.002;
//    private final double panDGain = 0.001;
//    private final double tiltPGain = 0.002;
//    private final double tiltDGain = 0.001;
//
//    // Define the dead zone (in pixels)
//    private final int deadZone = 5;
//
//    // Define the center coordinates of the HuskyLens display
//    private final int centerX = 160;
//    private final int centerY = 120;
//
//    // Variables for derivative control
//    private double panLastError = 0.0;
//    private double tiltLastError = 0.0;
//    private ElapsedTime timer = new ElapsedTime();
//
//    @Override
//    public void runOpMode() {
//        HuskyLens.Block[] myHuskyLensBlocks;
//
//        huskylens = hardwareMap.get(HuskyLens.class, "huskylens");
//        pan_servo = hardwareMap.get(Servo.class, "pan_servo");
//        tilt_servo = hardwareMap.get(Servo.class, "tilt_servo");
//
//        // Set the servo directions as needed for your physical setup
//        pan_servo.setDirection(Servo.Direction.FORWARD);
//        tilt_servo.setDirection(Servo.Direction.REVERSE);
//
//        // *** Adjusted scaleRange() to limit the servo's motion ***
//        // Replace with your calibrated min and max values.
//        // Use a smaller range to restrict the total motion.
//        pan_servo.scaleRange(0.2, 0.8); // 0.2 to 0.8
//        tilt_servo.scaleRange(0.25, 0.85); // 0.25 to 0.85
//
//        pan_servo.setPosition(panPosition);
//        tilt_servo.setPosition(tiltPosition);
//
//        telemetry.addData(">>", huskylens.knock() ? "Touch start to continue" : "Problem communicating with HuskyLens");
//        huskylens.selectAlgorithm(HuskyLens.Algorithm.TAG_RECOGNITION);
//        telemetry.update();
//
//        waitForStart();
//        if (opModeIsActive()) {
//            timer.reset();
//            while (opModeIsActive()) {
//                myHuskyLensBlocks = huskylens.blocks();
//                double deltaTime = timer.seconds();
//                timer.reset();
//
//                if (myHuskyLensBlocks.length > 0) {
//                    HuskyLens.Block firstBlock = myHuskyLensBlocks[0];
//
//                    // INVERTED LOGIC: Add the error instead of subtracting for proportional control
//                    // If the block is to the right (x > centerX), the error is positive.
//                    // Adding a positive value will increase panPosition, moving the servo right.
//                    // If the block is below the center (y > centerY), the error is positive.
//                    // For a reversed tilt servo, adding a positive value will move it up.
//                    double panError = firstBlock.x - centerX;
//                    double tiltError = firstBlock.y - centerY;
//
//                    if (Math.abs(panError) > deadZone) {
//                        double panDerivative = (panError - panLastError) / deltaTime;
//                        panPosition += panError * panPGain + panDerivative * panDGain;
//                    }
//                    if (Math.abs(tiltError) > deadZone) {
//                        double tiltDerivative = (tiltError - tiltLastError) / deltaTime;
//                        tiltPosition -= tiltError * tiltPGain + tiltDerivative * tiltDGain; // Changed from += to -=
//                    }
//
//                    // Clamp servo positions to the 0.0 to 1.0 range of the *scaled* range
//                    panPosition = Math.max(0.0, Math.min(1.0, panPosition));
//                    tiltPosition = Math.max(0.0, Math.min(1.0, tiltPosition));
//
//                    pan_servo.setPosition(panPosition);
//                    tilt_servo.setPosition(tiltPosition);
//
//                    panLastError = panError;
//                    tiltLastError = tiltError;
//
//                    telemetry.addData("Status", "Tracking AprilTag ID: " + firstBlock.id);
//                    telemetry.addData("Pan Error", "%.2f", panError);
//                    telemetry.addData("Tilt Error", "%.2f", tiltError);
//                    telemetry.addData("Pan Servo Pos", "%.2f", panPosition);
//                    telemetry.addData("Tilt Servo Pos", "%.2f", tiltPosition);
//                } else {
//                    telemetry.addData("Status", "No AprilTag detected");
//                }
//                telemetry.update();
//            }
//        }
//    }
//}