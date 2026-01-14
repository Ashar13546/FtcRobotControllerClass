package org.firstinspires.ftc.teamcode.rubik;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.openftc.easyopencv.*;

@TeleOp(name = "Basic Visual Indicator", group = "Test")
public class IsometricView extends LinearOpMode {
    OpenCvCamera webcam;
    SimpleStickerPipeline pipeline;

    @Override
    public void runOpMode() {
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());

        webcam = OpenCvCameraFactory.getInstance().createWebcam(
                hardwareMap.get(WebcamName.class, "Webcam 1"), cameraMonitorViewId);

        pipeline = new SimpleStickerPipeline();
        webcam.setPipeline(pipeline);

        webcam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                webcam.startStreaming(640, 480, OpenCvCameraRotation.UPRIGHT);
            }
            @Override
            public void onError(int errorCode) {}
        });

        waitForStart();

        while (opModeIsActive()) {
            telemetry.addLine("Check the Robot Controller/Driver Station screen");
            telemetry.addLine("to see the green diagonal grid.");
            telemetry.update();
            sleep(100);
        }
    }
}