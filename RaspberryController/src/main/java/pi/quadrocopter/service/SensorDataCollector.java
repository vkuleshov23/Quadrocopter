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
    @Scheduled(cron = "*/5 * * * * *")
    void bmp() {
        tempPress.update();
        System.out.println(tempPress);
        float[] q = ahrs.getQuaternion();
        System.out.println("AHRS: " + Arrays.toString(q));
    }

    @SneakyThrows
//    @Scheduled(fixedDelayString = "#{@madgwickAHRS.getSamplePeriodInMs()}")
    @Scheduled(fixedDelay = 13)
    void ahrs() {
        gyro.update();
        accMag.update();
        ThreeAxes gyroAxes = gyro.getAxes();
        ThreeAxes accAxes = accMag.getAccel();
        ThreeAxes magAxes = accMag.getMag();
        ahrs.update(gyroAxes.x, gyroAxes.y, gyroAxes.z, accAxes.x, accAxes.y, accAxes.z, magAxes.x, magAxes.y, magAxes.z);
    }

    @SneakyThrows
    @Scheduled(fixedDelay = 50)
    void radioReceive() {
        if(nrf.available()) {
//            byte[] data = nrf.read(nrf.getPayloadSize());
            System.out.println(nrf.read());
        } else {
            nrf.startListening();
            if(Math.random() < 0.005) {
                System.out.println("Not get message | " + nrf.getDataRate());
            }
        }
    }
}
