package org.firstinspires.ftc.teamcode;

//import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.hardware.bosch.BNO055IMU;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

@Disabled
@TeleOp(name="FieldOrientedBasicMecham", group="Iterative OpMode")

public class FieldOrientedBasicMecham extends OpMode
{
    // Declare OpMode members.
    private ElapsedTime runtime = new ElapsedTime();
    private DcMotor frontLeft = null;
    private DcMotor backLeft = null;
    private DcMotor frontRight = null;
    private DcMotor backRight = null;
    private DcMotor sliderMotor;
    private DcMotor leftMotor;
    private DcMotor rightMotor;
    private DcMotor shootMotor;

    private BNO055IMU imu;
    private Orientation angles;

    // Slider movement limits in encoder ticks
    private static final int SLIDER_MIN_TICKS = 0;     // Minimum position
    private static final int SLIDER_MAX_TICKS = 1000;  // Maximum position
    private static final double SLIDER_POWER = 1;    // Slider movement power
    // PID control parameters (Proportional control only for simplicity)
    private static final double Kp = 0.01;  // Proportional gain
    private static final double HOLD_POWER = 0.05; // Small power for maintaining position
    private int targetPosition = 0;
    // Direction constants
    private static final int CW = 1;   // Clockwise direction
    private static final int CCW = -1; // Counterclockwise direction

    /*
     * Code to run ONCE when the driver hits INIT
     */
    @Override
    public void init() {

        leftMotor = hardwareMap.get(DcMotor.class, "left_motor");
        rightMotor = hardwareMap.get(DcMotor.class, "right_motor");
        shootMotor = hardwareMap.get(DcMotor.class, "shoot_motor");

        // Optionally reverse one motor depending on orientation
        leftMotor.setDirection(DcMotor.Direction.FORWARD);
        rightMotor.setDirection(DcMotor.Direction.FORWARD);
        shootMotor.setDirection(DcMotor.Direction.FORWARD);

        sliderMotor = hardwareMap.get(DcMotor.class, "motor_rotate");
        sliderMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        sliderMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        sliderMotor.setDirection(DcMotorSimple.Direction.REVERSE);

        frontLeft  = hardwareMap.get(DcMotor.class, "leftFront");//0
        backLeft  = hardwareMap.get(DcMotor.class, "leftBack");//1

        frontRight = hardwareMap.get(DcMotor.class, "rightFront");//2
        backRight = hardwareMap.get(DcMotor.class, "rightBack");//3

        //since the motors are 180 out one side needs to be reversed
        //!!!frontRight.setDirection(DcMotorSimple.Direction.REVERSE);
        // the front right gearbox is mounted wrong. Only way to get going same direction
        frontRight.setDirection(DcMotorSimple.Direction.REVERSE);
        backRight.setDirection(DcMotorSimple.Direction.REVERSE);

        imu = hardwareMap.get(BNO055IMU.class, "imu");
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.RADIANS;
        imu.initialize(parameters);

        // Tell the driver that initialization is complete.
        telemetry.addData("Status", "Initialized");
    }

    /*
     * Code to run REPEATEDLY after the driver hits INIT, but before they hit PLAY
     */

    @Override
    public void init_loop() {
    }

    /*
     * Code to run ONCE when the driver hits PLAY
     */
    @Override
    public void start() {
        runtime.reset();
    }

    /*
     * Code to run REPEATEDLY after the driver hits PLAY but before they hit STOP
     */
    @Override
    public void loop() {

        double basePower;

        // Default: same power
        double leftPower = 0;
        double rightPower = 0;
        double shootPower  = 0;

        // If bumpers are pressed, adjust power
        if (gamepad2.left_bumper) {
            leftPower = 1.0;
            rightPower = -1.0;

        }
        if (gamepad2.right_bumper) {
            rightPower = 1.0;
            leftPower = -1.0;

        }
        if (gamepad2.x) {

            shootPower = -1.0;
        }
        if (gamepad2.y) {

            shootPower = 1.0;
        }
        if(gamepad2.options){
            rightPower = 0;
            leftPower = 0;
            shootPower = 0;
        }

        leftMotor.setPower(leftPower);
        rightMotor.setPower(rightPower);
        shootMotor.setPower(shootPower);

        // Control the slider motor for holding position
        if (gamepad2.dpad_up) {
            // Move up if below the maximum limit
            targetPosition = Math.min(sliderMotor.getCurrentPosition() + 10, SLIDER_MAX_TICKS);
        } else if (gamepad2.dpad_down) {
            // Move down if above the minimum limit
            targetPosition = Math.max(sliderMotor.getCurrentPosition() -10, SLIDER_MIN_TICKS);
        }

        // P-Control: Calculate error between current and target position
        int currentPosition = sliderMotor.getCurrentPosition();
        int positionError = targetPosition - currentPosition;
        // Apply P-control to resist external movement and hold position
        double power = Kp * positionError;  // Proportional control
        power = Math.max(Math.min(power, 0.5), -0.5);  // Limit power between -1 and 1
        // Apply calculated power to the motor
        sliderMotor.setPower(power);

        // Setup a variable for each drive wheel to save power level for telemetry
        double frontLeftPower;
        double backLeftPower;

        double frontRightPower;
        double backRightPower;

        double y = -gamepad1.left_stick_y;
        double x = gamepad1.left_stick_x;
        double turn  =  -gamepad1.right_stick_x;

        double heading = imu.getAngularOrientation().firstAngle - Math.PI / 2;
        double cosA = Math.cos(-heading);
        double sinA = Math.sin(-heading);

        double fieldX = x * cosA - y * sinA;
        double fieldY = x * sinA + y * cosA;

        double denominator = Math.max(Math.abs(fieldY) + Math.abs(fieldX) + Math.abs(turn), 1);

        frontLeftPower = (fieldY + fieldX + turn) / denominator;
        backLeftPower  = (fieldY - fieldX + turn) / denominator;

        frontRightPower = (fieldY - fieldX - turn) / denominator;
        backRightPower  = (fieldY + fieldX - turn) / denominator;

        frontLeft.setPower(frontLeftPower);
        backLeft.setPower(backLeftPower);

        frontRight.setPower(frontRightPower);
        backRight.setPower(backRightPower);

        // Show the elapsed game time and wheel power.
        telemetry.addData("Status", "Run Time: " + runtime.toString());
        telemetry.addData("Left", "front (%.2f), back (%.2f)", frontLeftPower, backLeftPower);
        telemetry.addData("Right", "front (%.2f), back (%.2f)", frontRightPower, backRightPower );
        telemetry.addData("Left Power", leftPower);
        telemetry.addData("Right Power", rightPower);
    }

    /*
     * Code to run ONCE after the driver hits STOP
     */
    @Override
    public void stop() {
    }

}
