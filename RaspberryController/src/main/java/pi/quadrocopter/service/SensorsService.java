package pi.quadrocopter.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pi.quadrocopter.model.i2c.BMP180;
import pi.quadrocopter.model.i2c.L3GD20;
import pi.quadrocopter.model.i2c.LSM303D;
import pi.quadrocopter.model.spi.NRF24;
import pi.quadrocopter.model.ahrs.MadgwickAHRS;
import pi.quadrocopter.util.ThreeAngles;
import pi.quadrocopter.util.Vector3_16bit;

import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class SensorsService {

    private final L3GD20 gyro;
    private final LSM303D accMag;
    private final BMP180 tempPress;

    @SneakyThrows
    @PostConstruct
    public void initSensors() {
        gyro.init();
        accMag.init();
        tempPress.init();
//        MagnetometerCalibration.start(accMag);
    }

    public synchronized void update() {
        tempPress.update();
        gyro.update();
        accMag.update();
    }

    public Vector3_16bit getAccel() {
        return this.accMag.getAccel();
    }

    public Vector3_16bit getMag() {
        return this.accMag.getMag();
    }

    public Vector3_16bit getGyro() {
        return this.gyro.getAxes();
    }

    public double getAltitude() {
        return this.tempPress.getAltitude();
    }

    public double getPressure() {
        return this.tempPress.getPressure();
    }

    public double getTemperature() {
        return this.tempPress.getTemperature();
    }

}
