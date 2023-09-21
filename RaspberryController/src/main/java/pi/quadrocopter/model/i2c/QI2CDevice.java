package pi.quadrocopter.model.i2c;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import lombok.Getter;

import java.io.IOException;

public abstract class QI2CDevice {

    @Getter
    protected final int address;
    protected final I2CBus bus;
    protected final I2CDevice device;

    public QI2CDevice(I2CBus bus, int address) throws IOException {
        this.bus = bus;
        this.address = address;
        device = bus.getDevice(address);
    }

    abstract public void init();

    abstract public void update();

}
