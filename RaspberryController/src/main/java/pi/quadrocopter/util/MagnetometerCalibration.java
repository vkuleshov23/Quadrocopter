package pi.quadrocopter.util;

import lombok.experimental.UtilityClass;
import pi.quadrocopter.model.i2c.LSM303D;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@UtilityClass
public class MagnetometerCalibration {

    public static final float x_offset = 474.114891f;
    public static final float y_offset = -84.097065f;
    public static final float z_offset = 185.820849f;


    private static final int calibrationSamples = 2_000;
    private static final int timeOffset = 50;
    private static final String magnetoData = "data_for_magneto_calibration.txt";

    public static void start(LSM303D magneto)  {
        try (PrintWriter printWriter = new PrintWriter(magnetoData, StandardCharsets.UTF_8)) {
            System.out.println("Start Calibration");
            for (int i = 0; i < calibrationSamples; i++) {
                magneto.update();
                ThreeAxes axes = magneto.getMag();
                System.out.println(axes.x + " " + axes.y + " " + axes.z);
                printWriter.println(axes.x + " " + axes.y + " " + axes.z);
                Thread.sleep(timeOffset);
            }
            System.out.println("End Calibration");
        } catch (IOException | InterruptedException e) {
            System.out.println("[X] " + e.getMessage());
        }
    }
}
