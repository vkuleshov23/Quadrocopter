package pi.quadrocopter.util;

import lombok.experimental.UtilityClass;
import pi.quadrocopter.model.i2c.LSM303D;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@UtilityClass
public class MagnetometerCalibration {

    private static final int calibrationSamples = 25_000;
    private static final int timeOffset = 20;
    private static final String magnetoData = "data_for_magneto_calibration.txt";

    public static void start(LSM303D magneto)  {
        try (PrintWriter printWriter = new PrintWriter(magnetoData, StandardCharsets.UTF_8)) {
            System.out.println("Start Calibration");
            for (int i = 0; i < calibrationSamples; i++) {
                ThreeAxes axes = magneto.getMag();
                printWriter.println(axes.x + " " + axes.y + " " + axes.z);
                Thread.sleep(timeOffset);
            }
            System.out.println("End Calibration");
        } catch (IOException | InterruptedException e) {
            System.out.println("[X] " + e.getMessage());
        }
    }
}
