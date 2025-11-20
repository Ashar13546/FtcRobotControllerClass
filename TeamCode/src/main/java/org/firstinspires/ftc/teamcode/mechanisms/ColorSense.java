package org.firstinspires.ftc.teamcode; // Your team code package

import android.graphics.Color; // Android color utility for easy conversion
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

@TeleOp(name = "Dual_Color_V3_Telemetry", group = "Sensor")
public class ColorSense extends LinearOpMode {

    // 1. Declare both color sensor objects
    NormalizedColorSensor colorSensorLeft;
    NormalizedColorSensor colorSensorRight;

    // Define a constant for our target distance (e.g., in centimeters)
    private static final double DISTANCE_LIMIT_CM = 4.0;

    @Override
    public void runOpMode() {
        // 2. Map both sensors from the configuration
        colorSensorLeft = hardwareMap.get(NormalizedColorSensor.class, "sensor_color_left");
        colorSensorRight = hardwareMap.get(NormalizedColorSensor.class, "sensor_color_right");

        // Optional: Turn on the white LED to improve reliability.

        telemetry.addData("Status", "Color Sensors Initialized");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // Read and process data for the LEFT sensor
            processSensorData(colorSensorLeft, "LEFT");

            // Read and process data for the RIGHT sensor
            processSensorData(colorSensorRight, "RIGHT");

            telemetry.update();
        }
    }

    /**
     * Helper method to read and display telemetry for a single color sensor.
     * @param sensor The NormalizedColorSensor object.
     * @param label The label for telemetry (e.g., "LEFT" or "RIGHT").
     */
    private void processSensorData(NormalizedColorSensor sensor, String label) {
        // Get normalized color values (0.0 to 1.0)
        NormalizedRGBA colors = sensor.getNormalizedColors();

        // Get distance (V3 also functions as a distance sensor)
        double distanceCM = ((DistanceSensor) sensor).getDistance(DistanceUnit.CM);

        // Convert the NormalizedRGBA to a Hue/Saturation/Value (HSV)
        // HSV is often the best way to reliably classify colors.
        final float[] hsvValues = new float[3];
        Color.colorToHSV(colors.toColor(), hsvValues);

        // Determine the detected color based on Hue (H) value.
        // Hue is measured in degrees (0-360). This is where you tune for your environment.
        String detectedColor = "UNKNOWN";
        double hue = (double) hsvValues[0];

        // Check if object is close enough AND has a valid Hue
        if (distanceCM < DISTANCE_LIMIT_CM) {

            // --- Purple Check: Hue roughly between 270 and 320 degrees ---
            if (hue >= 200 && hue <= 300) {
                detectedColor = "**PURPLE**";
            }
            // --- Green Check: Hue roughly between 100 and 160 degrees ---
            else if (hue >= 100 && hue <= 190) {
                detectedColor = "**GREEN**";
            }
        } else {
            detectedColor = "OUT OF RANGE (> " + String.format("%.1f", DISTANCE_LIMIT_CM) + " cm)";
        }

        // --- Display Telemetry ---
        telemetry.addLine("*** " + label + " Sensor Data ***");
        telemetry.addData(label + " Color", detectedColor);
        telemetry.addData(label + " Distance (CM)", String.format("%.1f", distanceCM));
        telemetry.addData(label + " RGB (N)",
                String.format("%.2f, %.2f, %.2f", colors.red, colors.green, colors.blue));
        telemetry.addData(label + " Hue", String.format("%.0f", hue));
    }
}