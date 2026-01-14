package org.firstinspires.ftc.teamcode.rubik;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;

public class SimpleStickerPipeline extends OpenCvPipeline {
    // Adjust these via the OpMode if needed
    public int offsetX = 320;
    public int offsetY = 240;
    public int spacing = 40;

    @Override
    public Mat processFrame(Mat input) {
        // Draw 9 circles in a diagonal/isometric grid
        public SimpleStickerPipeline() {
            // Manually setting [x, y] for all 9 points (0 through 8)
            points[0] = new Point(280, 200);
            points[1] = new Point(320, 220);
            points[2] = new Point(360, 240);

            points[3] = new Point(240, 230);
            points[4] = new Point(280, 250);
            points[5] = new Point(320, 270);

            points[6] = new Point(200, 260);
            points[7] = new Point(240, 280);/
            points[8] = new Point(280, 300);
        }
        return input;
    }

    public Point getStickerPoint(int index) {
        int row = index / 3;
        int col = index % 3;

        // Diagonal/Isometric Math:
        // X shifts based on (col - row)
        // Y shifts based on (col + row)
        int x = offsetX + (int)((col - row) * (spacing * 1.2));
        int y = offsetY + (int)((col + row) * (spacing * 0.6));

        return new Point(x, y);
    }
}