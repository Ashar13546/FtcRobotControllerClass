package org.firstinspires.ftc.teamcode.rubik;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;

public class StickerColorPipeline extends OpenCvPipeline {

    public enum CubeColor {WHITE, RED, BLUE, YELLOW, GREEN, ORANGE, UNKNOWN}

    // ============================================================
    // COLOR DETECTION THRESHOLDS - ADJUST THESE VALUES AS NEEDED
    // ============================================================

    // WHITE thresholds (low saturation, high brightness)
    public double WHITE_MAX_SATURATION = 35;      // Max saturation for white
    public double WHITE_MIN_VALUE = 164;          // Min brightness for white

    // RED thresholds (hue wraps around at 0/180)
    public double RED_MAX_HUE_LOW = 125;           // Red hue upper bound (near 0)
    public double RED_MIN_HUE_HIGH = 118;         // Red hue lower bound (near 180)
    public double RED_MIN_SATURATION = 190;       // Min saturation for red
    public double RED_MIN_VALUE = 133;            // Min brightness for red

    // ORANGE thresholds
    public double ORANGE_MIN_HUE = 102;            // Orange hue lower bound
    public double ORANGE_MAX_HUE = 115;            // Orange hue upper bound
    public double ORANGE_MIN_SATURATION = 193;    // Min saturation for orange
    public double ORANGE_MIN_VALUE = 180;         // Min brightness for orange

    // YELLOW thresholds
    public double YELLOW_MIN_HUE = 76;            // Yellow hue lower bound
    public double YELLOW_MAX_HUE = 95;            // Yellow hue upper bound
    public double YELLOW_MIN_SATURATION = 150;    // Min saturation for yellow
    public double YELLOW_MIN_VALUE = 163;         // Min brightness for yellow

    // GREEN thresholds
    public double GREEN_MIN_HUE = 54;             // Green hue lower bound
    public double GREEN_MAX_HUE = 67;             // Green hue upper bound
    public double GREEN_MIN_SATURATION = 114;     // Min saturation for green
    public double GREEN_MIN_VALUE = 127;           // Min brightness for green

    // BLUE thresholds
    public double BLUE_MIN_HUE = 5;              // Blue hue lower bound
    public double BLUE_MAX_HUE = 20;             // Blue hue upper bound
    public double BLUE_MIN_SATURATION = 170;      // Min saturation for blue
    public double BLUE_MIN_VALUE = 135;            // Min brightness for blue

    // Sampling settings
    public int SAMPLE_REGION_SIZE = 10;           // Size of region to sample (10x10 pixels)
    public boolean USE_BLUR = true;               // Apply Gaussian blur to reduce noise

    // ============================================================

    // --- MATS FOR REUSING (FIX) ---
    private Mat blurredMat = new Mat();
    private Mat conversionMatRgb = new Mat(1, 1, CvType.CV_8UC3);
    private Mat conversionMatHsv = new Mat();
    // ------------------------------

    // --- lastFrame INITIALIZED (FIX) ---
    private Mat lastFrame = new Mat();

    // Adjustable grid
    public int offsetX = 0;
    public int offsetY = 0;
    public int spacing = 120;    // distance between centers (default for 640x480)

    private final int size = 60; // ROI box size 60

    public Point[] gridPoints = new Point[9];

    public StickerColorPipeline() {
        updateGrid();
    }

    public void updateGrid() {
        int cx = 320 + offsetX;   // center of screen
        int cy = 240 + offsetY;

        int half = spacing / 2;

        gridPoints[0] = new Point(cx - half, cy - half);
        gridPoints[1] = new Point(cx, cy - half);
        gridPoints[2] = new Point(cx + half, cy - half);

        gridPoints[3] = new Point(cx - half, cy);
        gridPoints[4] = new Point(cx, cy);
        gridPoints[5] = new Point(cx + half, cy);

        gridPoints[6] = new Point(cx - half, cy + half);
        gridPoints[7] = new Point(cx, cy + half);
        gridPoints[8] = new Point(cx + half, cy + half);
    }

    // Returns raw BGR values of a sticker (AVERAGED over a region)
    public double[] sampleIndexRaw(int index) {
        // --- Added .empty() check (FIX) ---
        if (lastFrame == null || lastFrame.empty()) return new double[]{0, 0, 0};
        Point p = gridPoints[index];

        // Sample a region around the point and average
        int halfSample = SAMPLE_REGION_SIZE / 2;

        double sumB = 0, sumG = 0, sumR = 0;
        int count = 0;

        for (int dy = -halfSample; dy <= halfSample; dy++) {
            for (int dx = -halfSample; dx <= halfSample; dx++) {
                int y = (int) p.y + dy;
                int x = (int) p.x + dx;

                // Check bounds
                if (y >= 0 && y < lastFrame.rows() && x >= 0 && x < lastFrame.cols()) {
                    double[] bgr = lastFrame.get(y, x);
                    if (bgr != null) {
                        sumB += bgr[0];
                        sumG += bgr[1];
                        sumR += bgr[2];
                        count++;
                    }
                }
            }
        }

        if (count == 0) return new double[]{0, 0, 0};
        return new double[]{sumB / count, sumG / count, sumR / count};
    }

    // --- THIS METHOD IS NOW FIXED (FIX) ---
    @Override
    public Mat processFrame(Mat input) {
        // Apply Gaussian blur to reduce noise (optional)
        if (USE_BLUR) {
            // REUSE blurredMat
            Imgproc.GaussianBlur(input, blurredMat, new Size(5, 5), 0);
            // SAFELY COPY the result to lastFrame
            blurredMat.copyTo(lastFrame);
        } else {
            // SAFELY COPY the input to lastFrame
            input.copyTo(lastFrame);
        }

        updateGrid();

        int halfSize = size / 2;

        // Draw overlay
        for (Point p : gridPoints) {
            Imgproc.rectangle(
                    input,
                    new Point(p.x - halfSize, p.y - halfSize),
                    new Point(p.x + halfSize, p.y + halfSize),
                    new Scalar(0, 255, 0),
                    2
            );
        }
        return input;
    }

    // Sample a given index – returns classified cube color
    public CubeColor sampleIndex(int index) {
        // --- Added .empty() check (FIX) ---
        if (lastFrame == null || lastFrame.empty()) return CubeColor.UNKNOWN;

        // Get averaged BGR values from the region
        double[] bgr = sampleIndexRaw(index);

        return classify(bgr[2], bgr[1], bgr[0]); // r, g, b
    }

    // NEW: Sample S and V directly (HSV) for a given sticker index
    // --- THIS METHOD IS NOW FIXED (FIX) ---
    public double[] sampleIndexSV(int index) {
        // --- Added .empty() check (FIX) ---
        if (lastFrame == null || lastFrame.empty()) return new double[]{0, 0};

        // Get averaged BGR values from the region
        double[] bgr = sampleIndexRaw(index);

        // Convert BGR pixel to HSV
        // REUSE conversionMatRgb
        conversionMatRgb.put(0, 0, new double[]{bgr[0], bgr[1], bgr[2]});

        // REUSE conversionMatHsv
        Imgproc.cvtColor(conversionMatRgb, conversionMatHsv, Imgproc.COLOR_BGR2HSV);

        double S = conversionMatHsv.get(0, 0)[1];
        double V = conversionMatHsv.get(0, 0)[2];

        return new double[]{S, V};
    }

    // HSV classify -> cube color
    // --- THIS METHOD IS NOW FIXED (FIX) ---
    // HSV classify -> cube color
    // --- FINAL CORRECTED LOGIC ---
    private CubeColor classify(double r, double g, double b) {
        // REUSE conversionMatRgb
        conversionMatRgb.put(0, 0, new double[]{b, g, r});

        // REUSE conversionMatHsv
        Imgproc.cvtColor(conversionMatRgb, conversionMatHsv, Imgproc.COLOR_BGR2HSV);

        double H = conversionMatHsv.get(0, 0)[0];
        double S = conversionMatHsv.get(0, 0)[1];
        double V = conversionMatHsv.get(0, 0)[2];

        // ---------- WHITE ----------
        if (S <= WHITE_MAX_SATURATION && V >= WHITE_MIN_VALUE) {
            return CubeColor.WHITE;
        }

        // ---------- BLUE (Priority Check) ----------
        // Blue's Hue is 6-17 (as measured by your system)
        else if (H >= BLUE_MIN_HUE && H < BLUE_MAX_HUE &&
                S >= BLUE_MIN_SATURATION &&
                V >= BLUE_MIN_VALUE) {
            return CubeColor.BLUE;
        }

        // ---------- ORANGE (Priority Check) ----------
        // Orange's Hue is 103-114 (as measured by your system)
        else if (H > ORANGE_MIN_HUE && H < ORANGE_MAX_HUE &&
                S >= ORANGE_MIN_SATURATION &&
                V >= ORANGE_MIN_VALUE) {
            return CubeColor.ORANGE;
        }

        // ---------- RED (The logic was wrong here!) ----------
        // (FIXED: Changed OR (||) to AND (&&) to limit the check to the 121-123 range)
        else if ((H >= RED_MIN_HUE_HIGH && H <= RED_MAX_HUE_LOW) &&
                S >= RED_MIN_SATURATION &&
                V >= RED_MIN_VALUE) {
            return CubeColor.RED;
        }

        // ---------- YELLOW ----------
        else if (H >= YELLOW_MIN_HUE && H < YELLOW_MAX_HUE &&
                S >= YELLOW_MIN_SATURATION &&
                V >= YELLOW_MIN_VALUE) {
            return CubeColor.YELLOW;
        }

        // ---------- GREEN ----------
        else if (H >= GREEN_MIN_HUE && H < GREEN_MAX_HUE &&
                S >= GREEN_MIN_SATURATION &&
                V >= GREEN_MIN_VALUE) {
            return CubeColor.GREEN;
        }

        return CubeColor.UNKNOWN;
    }
}