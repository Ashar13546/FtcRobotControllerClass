import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

@Disabled
@TeleOp(name="REV_TankDrive", group="Iterative OpMode")
public class REV_TankDrive extends OpMode {

    // Define motors
    private DcMotor leftDrive = null;
    private DcMotor rightDrive = null;

    // This method runs once when the INIT button is pressed
    @Override
    public void init() {
        // Initialize motors from the hardware map
        leftDrive  = hardwareMap.get(DcMotor.class, "motor_Left");
        rightDrive = hardwareMap.get(DcMotor.class, "motor_Right");

        // Set motor direction
        // NOTE: You may need to reverse one motor depending on your setup
        leftDrive.setDirection(DcMotor.Direction.REVERSE);
        rightDrive.setDirection(DcMotor.Direction.FORWARD);

        telemetry.addData("Status", "Initialized");
        telemetry.update();
    }

    // This method runs repeatedly after the INIT button is pressed
    @Override
    public void loop() {
        // Get joystick inputs
        double drive = -gamepad1.left_stick_y;
        double turn = -gamepad1.right_stick_x;
        double leftPower = drive + turn;
        double rightPower = drive - turn;

        //

        // Set motor power
        leftDrive.setPower(leftPower);
        rightDrive.setPower(rightPower);


        // Display power levels via telemetry
        telemetry.addData("Left Power", "%.2f", leftPower);
        telemetry.addData("Right Power", "%.2f", rightPower);
        telemetry.update();
    }
}
