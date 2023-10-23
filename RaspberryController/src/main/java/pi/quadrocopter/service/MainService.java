package pi.quadrocopter.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MainService {
    private final SensorsService sensorsService;
    private final AHRSService ahrsService;
    private final RadioService radioService;
//    private final MotionControlService;

    @SneakyThrows
    @Scheduled(fixedDelay = 1000)
    public void mainF() {
        System.out.println("altitude: " + ahrsService.getAltitude());
        System.out.println("position: " + ahrsService.getAngles());
        System.out.println("temperature: " + ahrsService.getTemperature() + "C");
    }

    @SneakyThrows
    @Scheduled(fixedDelayString = "#{@radioService.getSampleInMs()}")
    public void radioUpdate() {
       radioService.update();
    }

    @SneakyThrows
    @Scheduled(fixedDelayString = "#{@AHRSService.getSampleInMs()}")
    public void ahrsUpdate() {
        ahrsService.update();
    }
    
}
