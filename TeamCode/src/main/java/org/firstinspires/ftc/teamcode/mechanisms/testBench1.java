package org.firstinspires.ftc.teamcode.mechanisms;

import com.qualcomm.hardware.motors.RevRobotics40HdHexMotor;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;

@Disabled
public class testBench1 {

    private DigitalChannel touchSensor;
    private DcMotor motor;

    private double tickspPerRev;

    public void init(HardwareMap hwMap) {

        //touch sensor code
//        touchSensor = hwMap.get(DigitalChannel.class, "touch_Sensor");
//        touchSensor.setMode(DigitalChannel.Mode.INPUT);

        //motor code
        motor = hwMap.get(DcMotor.class, "hex_Motor");
        motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        tickspPerRev = motor.getMotorType().getTicksPerRev();
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motor.setDirection(DcMotorSimple.Direction.REVERSE);
    }
    //touch Sensor

    public boolean isTouchSensePressed(){
        return !touchSensor.getState();
    }
    public boolean isTouchSensorReleased(){
        return touchSensor.getState();
    }

    //DC Motor

    public void setMotorSpeed(double speed) {

        motor.setPower(speed);
    }
    public double getMotorRevs() {
        return motor.getCurrentPosition() / tickspPerRev;
    }

    public void setMotorZeroBehaviour(DcMotor.ZeroPowerBehavior zeroBehaviour) {
        motor.setZeroPowerBehavior(zeroBehaviour);
    }
}
