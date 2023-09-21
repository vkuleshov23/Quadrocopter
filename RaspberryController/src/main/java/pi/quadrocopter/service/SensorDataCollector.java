package pi.quadrocopter.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pi.quadrocopter.model.i2c.QI2CDevice;
import pi.quadrocopter.model.spi.NRF24;
import pi.quadrocopter.util.ApplicationShutdownManager;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SensorDataCollector {

    private final List<QI2CDevice> devices;
    private final NRF24 nrf;
    private final ApplicationShutdownManager shutdownManager;

    @SneakyThrows
    @PostConstruct
    void initSensors() {
        devices.forEach(QI2CDevice::init);
    }

    @SneakyThrows
    @Scheduled(cron = "*/1 * * * * *")
    void collect() {
        devices.forEach(QI2CDevice::update);
        devices.forEach(System.out::println);
    }

    @SneakyThrows
    @Scheduled(fixedDelay = 10)
    void radioReceive() {
        if(nrf.available()) {
            byte[] data = nrf.read(nrf.getPayloadSize());
            System.out.println(Arrays.toString(data));
        }
    }
}
