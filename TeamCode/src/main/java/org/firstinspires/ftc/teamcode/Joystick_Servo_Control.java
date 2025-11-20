//package org.firstinspires.ftc.teamcode;
//
//import com.qualcomm.robotcore.eventloop.opmode.Disabled;
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
//import com.qualcomm.robotcore.hardware.Servo;
//
//@Disabled
//@TeleOp(name = "Joystick_Servo_Control", group = "TeleOp")
//public class Joystick_Servo_Control extends LinearOpMode {
//
//    private Servo pan_servo;
//    private Servo tilt_servo;
//
//    @Override
//    public void runOpMode() {
//        // Initialize the servos from the hardware map
//        pan_servo = hardwareMap.get(Servo.class, "pan_servo");
//        tilt_servo = hardwareMap.get(Servo.class, "tilt_servo");
//
//
//        // Optional: Set servo directions if needed
//        // pan_servo.setDirection(Servo.Direction.REVERSE);
//        // tilt_servo.setDirection(Servo.Direction.REVERSE);
//
//        // Send telemetry to confirm hardware initialization
//        telemetry.addData("Status", "Hardware Initialized");
//        telemetry.update();
//
//        // Wait for the game to start (driver presses PLAY)
//        waitForStart();
//
//        // Run until the end of the match (driver presses STOP)
//        while (opModeIsActive()) {
//            // Read joystick inputs
//            double panInput = gamepad1.left_stick_x;
//            double tiltInput = gamepad1.left_stick_y;
//
//            // Map joystick input (-1.0 to 1.0) to servo position (0.0 to 1.0)
//            // A simple way to do this is to add 1 and divide by 2.
//            double panPosition = (panInput + 1.0) / 2.0;
//            double tiltPosition = (tiltInput + 1.0) / 2.0;
//
//            // Set the new servo positions
//            pan_servo.setPosition(panPosition);
//            tilt_servo.setPosition(tiltPosition);
//
//            // Telemetry for debugging
//            telemetry.addData("Pan Servo Pos", "%.2f", pan_servo.getPosition());
//            telemetry.addData("Tilt Servo Pos", "%.2f", tilt_servo.getPosition());
//            telemetry.addData("Gamepad Left Stick X", "%.2f", gamepad1.left_stick_x);
//            telemetry.addData("Gamepad Left Stick Y", "%.2f", gamepad1.left_stick_y);
//            telemetry.update();
//        }
//    }
//}