//package org.firstinspires.ftc.teamcode;
//
//import com.qualcomm.robotcore.eventloop.opmode.Disabled;
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
//import com.qualcomm.robotcore.hardware.DcMotor;
//import com.qualcomm.robotcore.hardware.Servo;
//
//@Disabled
//@TeleOp(name = "ClassBot V2 TeleOp", group = "REV EDU")
//public class ClassBotV2TeleOp extends LinearOpMode {
//
//    private DcMotor leftDrive, rightDrive, armMotor;
//    private Servo gripper;
//
//    @Override
//    public void runOpMode() {
//        // Hardware mapping
//        leftDrive = hardwareMap.get(DcMotor.class, "left_drive");
//        rightDrive = hardwareMap.get(DcMotor.class, "right_drive");
//        armMotor = hardwareMap.get(DcMotor.class, "arm_motor");
//        gripper = hardwareMap.get(Servo.class, "gripper");
//
//        // Reverse one drive motor so robot moves forward
//        leftDrive.setDirection(DcMotor.Direction.REVERSE);
//
//        telemetry.addData("Status", "Initialized");
//        telemetry.update();
//
//        waitForStart();
//
//        while (opModeIsActive()) {
//            // Driving
//            double drive = -gamepad1.left_stick_y;
//            double turn = gamepad1.right_stick_x;
//            double leftPower = drive + turn;
//            double rightPower = drive - turn;
//
//            leftDrive.setPower(leftPower);
//            rightDrive.setPower(rightPower);
//
//            // Arm control
//            if (gamepad1.dpad_up) {
//                armMotor.setPower(0.5);
//            } else if (gamepad1.dpad_down) {
//                armMotor.setPower(-0.5);
//            } else {
//                armMotor.setPower(0);
//            }
//
//            // Gripper control
//            if (gamepad1.a) {
//                gripper.setPosition(1.0); // Close
//            } else if (gamepad1.b) {
//                gripper.setPosition(0.0); // Open
//            }
//
//            telemetry.addData("Left Power", leftPower);
//            telemetry.addData("Right Power", rightPower);
//            telemetry.update();
//        }
//    }
//}
