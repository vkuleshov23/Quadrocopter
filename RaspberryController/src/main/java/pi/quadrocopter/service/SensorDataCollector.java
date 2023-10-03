package pi.quadrocopter.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pi.quadrocopter.model.i2c.BMP180;
import pi.quadrocopter.model.i2c.L3GD20;
import pi.quadrocopter.model.i2c.LSM303D;
import pi.quadrocopter.model.spi.NRF24;
import pi.quadrocopter.util.ApplicationShutdownManager;
import pi.quadrocopter.util.MadgwickAHRS;
import pi.quadrocopter.util.ThreeAxes;

import javax.annotation.PostConstruct;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class SensorDataCollector {

    private final MadgwickAHRS ahrs;
    private final L3GD20 gyro;
    private final LSM303D accMag;
    private final BMP180 tempPress;
    private final NRF24 nrf;

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
        System.out.println(tempPress);
        ThreeAxes q = ahrs.getEulerAngles();
        System.out.println("AHRS: " + q);
    }

    @SneakyThrows
    @Scheduled(fixedDelayString = "#{@madgwickAHRS.getSamplePeriodInMs()}")
    void ahrs() {
        gyro.update();
        accMag.update();
        ThreeAxes gyroAxes = gyro.getAxes();
        ThreeAxes accAxes = accMag.getAccel();
        ThreeAxes magAxes = accMag.getMag();
//        ahrs.update(gyroAxes.x, gyroAxes.y, gyroAxes.z, accAxes.x, accAxes.y, accAxes.z, magAxes.x, magAxes.y, magAxes.z);
        ahrs.update(gyroAxes.x, gyroAxes.y, gyroAxes.z, accAxes.x, accAxes.y, accAxes.z);
        System.out.println("GYRO: " + gyroAxes);
        System.out.println("ACC: " + accAxes);
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
