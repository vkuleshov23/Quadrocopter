package pi.quadrocopter.model.i2c;

import com.pi4j.io.i2c.I2CBus;
import upm_lsm303d.LSM303D_M_RES_T;

import java.io.IOException;

public class LSM303D extends QI2CDevice {

    private final upm_lsm303d.LSM303D lsm = new upm_lsm303d.LSM303D(1, 0x1D);

    public LSM303D(I2CBus bus, int address) throws IOException {
        super(bus, address);
    }

    @Override
    public void init() {
        lsm.init(LSM303D_M_RES_T.LSM303D_M_RES_HIGH);
    }

    @Override
    public void update() {
        lsm.update();
    }

    @Override
    public String toString() {
        return "acc: " +  lsm.getAccelerometer().toString() + " mag: " + lsm.getMagnetometer().toString();
    }
}
