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
import pi.quadrocopter.util.ThreeAxes;

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

    private final ArrayList<ThreeAxes> averageData = new ArrayList<>(80);

    @SneakyThrows
    @PostConstruct
    void initSensors() {
        gyro.init();
        accMag.init();
        tempPress.init();
    }

    @SneakyThrows
    @Scheduled(cron = "*/1 * * * * *")
    void mainLoop() {
        tempPress.update();
//        System.out.println(tempPress);
        ThreeAxes q = ahrs.getEulerAngles();
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
    void ahrs() {
        gyro.update();
        accMag.update();
        ThreeAxes gyroAxes = gyro.getAxes();
        ThreeAxes accAxes = accMag.getAccel();
        ThreeAxes magAxes = accMag.getMag();

//        ahrs.update(
//                (float) Math.toRadians(gyroAxes.x),
//                (float) Math.toRadians(gyroAxes.x),
//                (float) Math.toRadians(gyroAxes.x),
//                accAxes.x, accAxes.y, accAxes.z, magAxes.x, magAxes.y, magAxes.z
//        );
        ahrs.update((float) Math.toRadians(gyroAxes.x),
                (float) Math.toRadians(gyroAxes.x),
                (float) Math.toRadians(gyroAxes.x),
                accAxes.x, accAxes.y, accAxes.z
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
}
