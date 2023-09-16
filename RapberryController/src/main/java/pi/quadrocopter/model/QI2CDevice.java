package pi.quadrocopter.model;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;

import java.io.IOException;

public abstract class QI2CDevice {
    protected final I2CBus bus;
    protected final I2CDevice device;

    public QI2CDevice(I2CBus bus, int address) throws IOException {
        this.bus = bus;
        device = bus.getDevice(address);
    }

    abstract public void init() throws IOException, InterruptedException;

    abstract public void update() throws IOException, InterruptedException;

}
