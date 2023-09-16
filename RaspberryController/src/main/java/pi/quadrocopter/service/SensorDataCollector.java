package pi.quadrocopter.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import pi.quadrocopter.model.QI2CDevice;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SensorDataCollector {

    private final List<QI2CDevice> devices;
    
    @PostConstruct
    @SneakyThrows
    void post() {
        for(var device : devices) {
            device.init();
            device.update();
            System.out.println(device);
        }
    }
}
