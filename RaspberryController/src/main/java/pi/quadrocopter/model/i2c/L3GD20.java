package pi.quadrocopter.model.i2c;

import com.pi4j.io.i2c.I2CBus;
import lombok.Getter;
import org.springframework.stereotype.Component;
import pi.quadrocopter.util.Vector3_16bit;

import java.io.IOException;

//Gyro
@Component
public class L3GD20 extends QI2CDevice {

    private static final int L3GD20_ADDRESS = 0x6B;
    private static final int L3GD20_CTRL_REG1 = 0x20;
    private static final int L3GD20_CTRL_REG2 = 0x21;
    private static final int L3GD20_CTRL_REG3 = 0x22;
    private static final int L3GD20_CTRL_REG4 = 0x23;
    private static final int L3GD20_CTRL_REG5 = 0x24;
    private static final int L3GD20_REFERENCE = 0x25;
    private static final int L3GD20_OUT_TEMP = 0x26;
    private static final int L3GD20_STATUS_REG = 0x27;

    private static final int L3GD20_OUT_X_L = 0x28;
    private static final int L3GD20_OUT_X_H = 0x29;
    private static final int L3GD20_OUT_Y_L = 0x2A;
    private static final int L3GD20_OUT_Y_H = 0x2B;
    private static final int L3GD20_OUT_Z_L = 0x2C;
    private static final int L3GD20_OUT_Z_H = 0x2D;

    private static final int L3GD20_FIFO_CTRL_REG = 0x2E;
    private static final int L3GD20_FIFO_SRC_REG = 0x2F;

    private static final int L3GD20_INT1_CFG = 0x30;
    private static final int L3GD20_INT1_SRC = 0x31;
    private static final int L3GD20_INT1_THS_XH = 0x32;
    private static final int L3GD20_INT1_THS_XL = 0x33;
    private static final int L3GD20_INT1_THS_YH = 0x34;
    private static final int L3GD20_INT1_THS_YL = 0x35;
    private static final int L3GD20_INT1_THS_ZH = 0x36;
    private static final int L3GD20_INT1_THS_ZL = 0x37;
    private static final int L3GD20_INT1_DURATION = 0x38;

    // 0x0F = 0b00001111
    // Normal power mode, all axes enabled
    private static final int NORMAL_POWER_MODE = 0x0F;


    // * FS_SEL | Full Scale Range   | LSB Sensitivity
    // * -------+--------------------+----------------
    // * 0      | +/- 250 degrees/s  | 131 LSB/deg/s
    // * 1      | +/- 500 degrees/s  | 65.5 LSB/deg/s
    // * 2      | +/- 1000 degrees/s | 32.8 LSB/deg/s
    // * 3      | +/- 2000 degrees/s | 16.4 LSB/deg/s
    private static final float DEFAULT_DPS = 250.0f;
    private static final float LSB_DEG_S = 131.0f;

    @Getter
    private final Vector3_16bit axes = new Vector3_16bit();

    public L3GD20(I2CBus bus) throws IOException {
        super(bus, L3GD20_ADDRESS);
    }

    @Override
    public synchronized void init() {
        try {
            device.write(L3GD20_CTRL_REG1, (byte) NORMAL_POWER_MODE);
            device.write(L3GD20_CTRL_REG4, (byte) L3GD20_INT1_CFG);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public synchronized void update() {
        try {
            int xl = device.read(L3GD20_OUT_X_L);
            int xh = device.read(L3GD20_OUT_X_H);
            int yl = device.read(L3GD20_OUT_Y_L);
            int yh = device.read(L3GD20_OUT_Y_H);
            int zl = device.read(L3GD20_OUT_Z_L);
            int zh = device.read(L3GD20_OUT_Z_H);
            axes.setX(xh, xl);
            axes.setY(yh, yl);
            axes.setZ(zh, zl);
            axes.div(LSB_DEG_S); // to degrees
        } catch (IOException  e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "L3GD20 | " + axes.toString();
    }
}
