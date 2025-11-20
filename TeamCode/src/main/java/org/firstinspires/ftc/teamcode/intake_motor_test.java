//package org.firstinspires.ftc.teamcode;
//
//import com.qualcomm.robotcore.eventloop.opmode.Disabled;
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
//import com.qualcomm.robotcore.hardware.DcMotor;
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
//
//@Disabled
//@TeleOp(name = "Simple Intake TeleOp", group = "Test")
//public class intake_motor_test extends LinearOpMode {
//
//    private DcMotor intake_motor;
//
//    @Override
//    public void runOpMode() {
//        // Get the motor from hardware map
//        intake_motor = hardwareMap.get(DcMotor.class, "intake_motor");
//
//        waitForStart();
//
//        // Set motor power to full when TeleOp starts
//        intake_motor.setPower(1.0);
//
//        // Keep the OpMode alive until stopped
//        while (opModeIsActive()) {
//            // You can add control here if needed
//            idle();
//        }
//
//        // Stop motor when OpMode ends
//        intake_motor.setPower(0);
//    }
//}
