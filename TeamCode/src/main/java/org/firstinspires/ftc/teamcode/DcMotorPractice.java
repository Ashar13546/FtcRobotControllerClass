package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.mechanisms.testBench1;

@Disabled
@TeleOp
public class DcMotorPractice extends OpMode {
    testBench1 bench = new testBench1();

    @Override
    public void init() {
        bench.init(hardwareMap);

    }

    @Override
    public void loop() {
        double motorSpeed = gamepad1.left_stick_y;

        bench.setMotorSpeed(motorSpeed);

        if (gamepad1.a) {
            bench.setMotorZeroBehaviour(DcMotor.ZeroPowerBehavior.BRAKE);
        } else if (gamepad1.b) {
            bench.setMotorZeroBehaviour(DcMotor.ZeroPowerBehavior.FLOAT);

        }


        telemetry.addData("Motor Revs", bench.getMotorRevs());

    }
}
