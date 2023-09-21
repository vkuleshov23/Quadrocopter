package pi.quadrocopter.model.i2c;

import com.pi4j.io.i2c.I2CBus;
import org.springframework.stereotype.Component;
import pi.quadrocopter.util.TreeAxes;

import java.io.IOException;

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

    private static final TreeAxes axes = new TreeAxes();

    public L3GD20(I2CBus bus) throws IOException {
        super(bus, L3GD20_ADDRESS);
    }

    @Override
    public void init() {
        try {
            device.write(L3GD20_CTRL_REG1, (byte) 0x0F);
            device.write(L3GD20_CTRL_REG4, (byte) L3GD20_INT1_CFG);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update() {
        try {
//            device.write((byte) (L3GD20_OUT_X_L | (1 << 7)));
            int xl = device.read(L3GD20_OUT_X_L);
            int xh = device.read(L3GD20_OUT_X_H);
            int yl = device.read(L3GD20_OUT_Y_L);
            int yh = device.read(L3GD20_OUT_Y_H);
            int zl = device.read(L3GD20_OUT_Z_L);
            int zh = device.read(L3GD20_OUT_Z_H);

            axes.setX(xh, xl);
            axes.setY(yh, yl);
            axes.setZ(zh, zl);

        } catch (IOException  e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public String toString() {
        return axes.toString();
    }
}
