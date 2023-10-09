package pi.quadrocopter.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pi.quadrocopter.model.i2c.BMP180;
import pi.quadrocopter.model.i2c.L3GD20;
import pi.quadrocopter.model.i2c.LSM303D;
import pi.quadrocopter.model.spi.NRF24;
import pi.quadrocopter.util.MadgwickAHRS;
import pi.quadrocopter.util.ThreeAngles;
import pi.quadrocopter.util.Vector3_16bit;

import javax.annotation.PostConstruct;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class SensorDataCollector {

    private final MadgwickAHRS ahrs;
    private final L3GD20 gyro;
    private final LSM303D accMag;
    private final BMP180 tempPress;
    private final NRF24 nrf;

    private final ArrayList<Vector3_16bit> averageData = new ArrayList<>(80);

    @SneakyThrows
    @PostConstruct
    void initSensors() {
        gyro.init();
        accMag.init();
        tempPress.init();
        setZToZero();
//        MagnetometerCalibration.start(accMag);
    }

    @SneakyThrows
    @Scheduled(cron = "*/1 * * * * *")
    void mainLoop() {
        tempPress.update();
//        System.out.println(tempPress);
        ThreeAngles q = ahrs.getEulerAngles();
        q.toDegrees();
        System.out.println("AHRS: " + q);

//        synchronized (averageData) {
//            double resx = (averageData.stream().mapToDouble(ax -> ax.x).sum()) / averageData.size();
//            double resy = (averageData.stream().mapToDouble(ax -> ax.y).sum()) / averageData.size();
//            double resz = (averageData.stream().mapToDouble(ax -> ax.z).sum()) / averageData.size();
//            System.out.println("X " + resx + " Y " + resy + " Z " + resz);
//            averageData.clear();
//        }
    }

    @SneakyThrows
    @Scheduled(fixedRateString = "#{@madgwickAHRS.getSamplePeriodInMs()}")
    void ahrsUpdate() {
        gyro.update();
        accMag.update();
        Vector3_16bit gyroAxes = gyro.getAxes();
        Vector3_16bit accAxes = accMag.getAccel();
        Vector3_16bit magAxes = accMag.getMag();

        ahrs.update(
                (float) Math.toRadians(gyroAxes.x),
                (float) Math.toRadians(gyroAxes.y),
                (float) Math.toRadians(gyroAxes.z),
                accAxes.x, accAxes.y, accAxes.z,
                magAxes.x, magAxes.y, magAxes.z
        );

//        synchronized (averageData) {
//            ArrayList<Float> a = new ArrayList<>(3);
//            averageData.add(new ThreeAxes(magAxes));
//        }
    }

    @SneakyThrows
    @Scheduled(fixedDelayString = "#{@nrf.getSampleMS()}")
    void radio() {
        if(nrf.available()) {
            System.out.println(nrf.read());
        } else {
            nrf.startListening();
        }
    }

    @SneakyThrows
    void setZToZero() {
        int count = (int) (2000 / ahrs.getSamplePeriodInMs());
        System.out.println(count);
        for(int i = 0; i < count; i++) {
            ahrsUpdate();
            Thread.sleep(ahrs.getSamplePeriodInMs());
            System.out.print(".");
        }
        ThreeAngles axes = ahrs.getEulerAngles();
        System.out.println("Yaw offset: " + axes.getYaw());
        ahrs.setZOffset((float) Math.toDegrees(axes.getYaw()));
    }
}
