package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@Disabled
@Autonomous
public class VariablePractice extends OpMode {
    @Override
    public void init() {
        int teamNumber = 2;
        double motorSpeed = 0.75;
        boolean clawClosed = true;

        telemetry.addData("Team Number", teamNumber);
        telemetry.addData("Motor Speed", motorSpeed);
        telemetry.addData("Claw Closed", clawClosed);

    }

    @Override
    public void loop() {

    }
}

