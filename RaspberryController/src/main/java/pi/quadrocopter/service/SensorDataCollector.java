package pi.quadrocopter.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pi.quadrocopter.model.i2c.L3GD20;
import pi.quadrocopter.model.i2c.QI2CDevice;
import pi.quadrocopter.model.spi.NRF24;
import pi.quadrocopter.util.ApplicationShutdownManager;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SensorDataCollector {

    private final List<QI2CDevice> devices;
    private final L3GD20 gyro;
    private final NRF24 nrf;
    private final ApplicationShutdownManager shutdownManager;

    @SneakyThrows
    @PostConstruct
    void initSensors() {
        devices.forEach(QI2CDevice::init);
    }

    @SneakyThrows
//    @Scheduled(cron = "*/10 * * * * *")
    void collect() {
        devices.forEach(QI2CDevice::update);
        devices.forEach(System.out::println);
    }

    @SneakyThrows
    @Scheduled(fixedDelay = 100)
    void l3g() {
        gyro.update();
        System.out.println(gyro);
    }

    @SneakyThrows
//    @Scheduled(fixedDelay = 50)
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
