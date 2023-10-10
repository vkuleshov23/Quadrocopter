package pi.quadrocopter.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pi.quadrocopter.model.ahrs.MadgwickAHRS;
import pi.quadrocopter.util.ThreeAngles;
import pi.quadrocopter.util.Vector3_16bit;

import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class AHRSService {
    private final MadgwickAHRS ahrs;
    private final SensorsService sensorsService;

    @SneakyThrows
    @PostConstruct
    public void initAHRS() {
        this.setZToZero();
    }

    public ThreeAngles getAngles() {
        return ahrs.getEulerAnglesInDegrees();
    }

    public double getTemperature() {
        return sensorsService.getTemperature();
    }

    public double getPressure() {
        return sensorsService.getPressure();
    }

    public double getAltitude() {
        return sensorsService.getAltitude();
    }

    @SneakyThrows
    @Scheduled(fixedRateString = "#{@madgwickAHRS.getSamplePeriodInMs()}")
    public void update() {
        sensorsService.update();
        Vector3_16bit gyroAxes = sensorsService.getGyro();
        Vector3_16bit accAxes = sensorsService.getAccel();
        Vector3_16bit magAxes = sensorsService.getMag();
        ahrs.update(
                (float) Math.toRadians(gyroAxes.x),
                (float) Math.toRadians(gyroAxes.y),
                (float) Math.toRadians(gyroAxes.z),
                accAxes.x, accAxes.y, accAxes.z,
                magAxes.x, magAxes.y, magAxes.z);
    }

    @SneakyThrows
    public void setZToZero() {
        int count = (int) (5000 / ahrs.getSamplePeriodInMs());
        System.out.println(count);
        for(int i = 0; i < count; i++) {
            update();
            Thread.sleep(ahrs.getSamplePeriodInMs());
            System.out.print(".");
        }
        ThreeAngles axes = ahrs.getEulerAngles();
        System.out.println("Yaw offset: " + axes.getYaw());
        ahrs.setZOffset((float) Math.toDegrees(axes.getYaw()));
    }

    public long getSampleInMs() {
        return ahrs.getSamplePeriodInMs();
    }
}
