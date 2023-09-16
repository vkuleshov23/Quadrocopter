package pi.quadrocopter.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pi.quadrocopter.model.QI2CDevice;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SensorDataCollector {

    private final List<QI2CDevice> devices;

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
}
