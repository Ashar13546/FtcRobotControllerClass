package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.dfrobot.HuskyLens;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;
import java.util.List;
import org.firstinspires.ftc.robotcore.external.JavaUtil;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;

@Disabled
@TeleOp(name = "TET (Blocks to Java)")
public class TET extends LinearOpMode {

    private HuskyLens huskylens;

    @Override
    public void runOpMode() {
        ElapsedTime myElapsedTime;
        // Corrected declaration: use an array instead of a list
        HuskyLens.Block[] myHuskyLensBlocks;
        HuskyLens.Block myHuskyLensBlock;

        huskylens = hardwareMap.get(HuskyLens.class, "huskylens");

        telemetry.addData(">>", huskylens.knock() ? "Touch start to continue" : "Problem communicating with HuskyLens");
        huskylens.selectAlgorithm(HuskyLens.Algorithm.TAG_RECOGNITION);
        telemetry.update();
        myElapsedTime = new ElapsedTime();
        waitForStart();
        if (opModeIsActive()) {
            while (opModeIsActive()) {
                if (myElapsedTime.seconds() >= 1) {
                    myElapsedTime.reset();
                    myHuskyLensBlocks = huskylens.blocks();
                    // Change the line below to use array length
                    telemetry.addData("Block count", myHuskyLensBlocks.length);
                    for (HuskyLens.Block myHuskyLensBlock_item : myHuskyLensBlocks) {
                        myHuskyLensBlock = myHuskyLensBlock_item;
                        telemetry.addData("Block", "id=" + myHuskyLensBlock.id + " size: " + myHuskyLensBlock.width + "x" + myHuskyLensBlock.height + " position: " + myHuskyLensBlock.x + "," + myHuskyLensBlock.y);
                    }
                    telemetry.update();
                }
            }
        }
    }
}
