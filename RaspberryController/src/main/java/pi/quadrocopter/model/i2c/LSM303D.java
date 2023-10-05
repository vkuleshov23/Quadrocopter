package pi.quadrocopter.model.i2c;

import com.pi4j.io.i2c.I2CBus;
import lombok.Getter;
import org.springframework.stereotype.Component;
import pi.quadrocopter.util.MagnetometerCalibration;
import pi.quadrocopter.util.ThreeAxes;

import java.io.IOException;

@Component
public class LSM303D extends QI2CDevice {

    private static final int ADDRESS = 0x1D;

    private static final int LSM303D_TEMP_OUT_L = 0x05;
    private static final int LSM303D_TEMP_OUT_H = 0x06;

    private static final int LSM303D_STATUS_M = 0x07;

    private static final int LSM303D_INT_CTRL_M = 0x12;
    private static final int LSM303D_INT_SRC_M = 0x13;
    private static final int LSM303D_INT_THS_L_M = 0x14;
    private static final int LSM303D_INT_THS_H_M = 0x15;


    private static final int LSM303D_OFFSET_X_L_M = 0x16;
    private static final int LSM303D_OFFSET_X_H_M = 0x17;
    private static final int LSM303D_OFFSET_Y_L_M = 0x18;
    private static final int LSM303D_OFFSET_Y_H_M = 0x19;
    private static final int LSM303D_OFFSET_Z_L_M = 0x1A;
    private static final int LSM303D_OFFSET_Z_H_M = 0x1B;
    private static final int LSM303D_REFERENCE_X = 0x1C;
    private static final int LSM303D_REFERENCE_Y = 0x1D;
    private static final int LSM303D_REFERENCE_Z = 0x1E;

    private static final int LSM303D_CTRL0 = 0x1F;
    private static final int LSM303D_CTRL1 = 0x20;
    private static final int LSM303D_CTRL2 = 0x21;
    private static final int LSM303D_CTRL3 = 0x22;
    private static final int LSM303D_CTRL4 = 0x23;
    private static final int LSM303D_CTRL5 = 0x24;
    private static final int LSM303D_CTRL6 = 0x25;
    private static final int LSM303D_CTRL7 = 0x26;
    private static final int LSM303D_STATUS_A = 0x27;

    private static final int LSM303D_OUT_X_L_A = 0x28;
    private static final int LSM303D_OUT_X_H_A = 0x29;
    private static final int LSM303D_OUT_Y_L_A = 0x2A;
    private static final int LSM303D_OUT_Y_H_A = 0x2B;
    private static final int LSM303D_OUT_Z_L_A = 0x2C;
    private static final int LSM303D_OUT_Z_H_A = 0x2D;

    private static final int LSM303D_FIFO_CTRL = 0x2E;
    private static final int LSM303D_FIFO_SRC = 0x2F;

    private static final int LSM303D_IG_CFG1 = 0x30;
    private static final int LSM303D_IG_SRC1 = 0x31;
    private static final int LSM303D_IG_THS1 = 0x32;
    private static final int LSM303D_IG_DUR1 = 0x33;
    private static final int LSM303D_IG_CFG2 = 0x34;
    private static final int LSM303D_IG_SRC2 = 0x35;
    private static final int LSM303D_IG_THS2 = 0x36;
    private static final int LSM303D_IG_DUR2 = 0x37;

    private static final int LSM303D_CLICK_CFG = 0x38;
    private static final int LSM303D_CLICK_SRC = 0x39;
    private static final int LSM303D_CLICK_THS = 0x3A;
    private static final int LSM303D_TIME_LIMIT = 0x3B;
    private static final int LSM303D_TIME_LATENCY = 0x3C;
    private static final int LSM303D_TIME_WINDOW = 0x3D;

    private static final int LSM303D_Act_THS = 0x3E;
    private static final int LSM303D_Act_DUR = 0x3F;

    private static final int LSM303D_OUT_X_L_M = 0x08;
    private static final int LSM303D_OUT_X_H_M = 0x09;
    private static final int LSM303D_OUT_Y_L_M = 0x0A;
    private static final int LSM303D_OUT_Y_H_M = 0x0B;
    private static final int LSM303D_OUT_Z_L_M = 0x0C;
    private static final int LSM303D_OUT_Z_H_M = 0x0D;

    // Accelerometer
    // AFS = 0 (+/- 2 g full scale)
    private static final int AFS_2G = 0x00;

    // 0x57 = 0b01010111
    // AODR = 0101 (50 Hz ODR); AZEN = AYEN = AXEN = 1 (all axes enabled)
    private static final int AODR_50Hz__ALL_AXES_ENABLE = 0x57;

    @Getter
    private final ThreeAxes accel = new ThreeAxes();

    // Magnetometer
    // 0x64 = 0b01100100
    // M_RES = 11 (high resolution mode); M_ODR = 001 (6.25 Hz ODR)
    private static final int HIGH_RES_MODE__6_25Hz_ODR = 0x64;

    // 0x20 = 0b00100000
    // MFS = 01 (+/- 4 gauss full scale)
    private static final int MFS_4G = 0x20;

    // 0x00 = 0b00000000
    // MLP = 0 (low power mode off); MD = 00 (continuous-conversion mode)
    private static final int MLP_MD_CCM = 0x00;

    private static final float DEFAULT_G = 2.0f;

    @Getter
    private final ThreeAxes mag = new ThreeAxes();

    public LSM303D(I2CBus bus) throws IOException {
        super(bus, ADDRESS);
    }

    @Override
    public void init() {
        try {
            device.write(LSM303D_CTRL2, (byte) AFS_2G);
            device.write(LSM303D_CTRL1, (byte) AODR_50Hz__ALL_AXES_ENABLE);
            device.write(LSM303D_CTRL5, (byte) HIGH_RES_MODE__6_25Hz_ODR);
            device.write(LSM303D_CTRL6, (byte) MFS_4G);
            device.write(LSM303D_CTRL7, (byte) MLP_MD_CCM);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update() {
        readAcc();
        readMag();
    }

    private void readAcc() {
        try {
            int xla = device.read(LSM303D_OUT_X_L_A);
            int xha = device.read(LSM303D_OUT_X_H_A);
            int yla = device.read(LSM303D_OUT_Y_L_A);
            int yha = device.read(LSM303D_OUT_Y_H_A);
            int zla = device.read(LSM303D_OUT_Z_L_A);
            int zha = device.read(LSM303D_OUT_Z_H_A);
            accel.setX(xha, xla);
            accel.setY(yha, yla);
            accel.setZ(zha, zla);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void readMag() {
        try {
            int xlm = device.read(LSM303D_OUT_X_L_M);
            int xhm = device.read(LSM303D_OUT_X_H_M);
            int ylm = device.read(LSM303D_OUT_Y_L_M);
            int yhm = device.read(LSM303D_OUT_Y_H_M);
            int zlm = device.read(LSM303D_OUT_Z_L_M);
            int zhm = device.read(LSM303D_OUT_Z_H_M);
            mag.setX(xhm, xlm);
            mag.setY(yhm, ylm);
            mag.setZ(zhm, zlm);
            mag.x -= MagnetometerCalibration.x_offset;
            mag.y -= MagnetometerCalibration.y_offset;
            mag.z -= MagnetometerCalibration.z_offset;
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "LSM303D | " + accel.toString() + " | " + mag.toString();
    }
}
