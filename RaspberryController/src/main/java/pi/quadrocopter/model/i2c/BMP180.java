package pi.quadrocopter.model.i2c;

import com.pi4j.io.i2c.I2CBus;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;

import static java.lang.Math.pow;

@Component
public class BMP180 extends QI2CDevice {

    private static final int OVER_SAMPLING_RATE = 3;
    private static final int BMP180_ADDRESS = 0x77;
    private static final int BMP180_REG_CONTROL = 0xF4;
    private static final int BMP180_REG_RESULT = 0xF6;

    private static final int BMP180_COMMAND_TEMPERATURE = 0x2E;
    private static final int BMP180_COMMAND_PRESSURE = 0x34;

    private int AC1, AC2, AC3, AC4, AC5, AC6, VB1, VB2, MB, MC, MD;
    private double X2, B3, B4, B5, B6, B7;
    private long X1, X3;
    private final byte[] data = new byte[22];

    @Getter
    private double T;
    @Getter
    private double P;

    private double startP;
    @Getter
    private double A;

    public BMP180(I2CBus bus) throws IOException {
        super(bus, BMP180_ADDRESS);
    }

    @Override
    public void init() {
        try {
            device.read(0xAA, data, 0, 22);
            AC1 = ByteBuffer.wrap(new byte[]{data[0], data[1]}).getShort();
            AC2 = ByteBuffer.wrap(new byte[]{data[2], data[3]}).getShort();
            AC3 = ByteBuffer.wrap(new byte[]{data[4], data[5]}).getShort();
            AC4 = ByteBuffer.wrap(new byte[]{0, 0, data[6], data[7]}).getInt();
            AC5 = ByteBuffer.wrap(new byte[]{0, 0, data[8], data[9]}).getInt();
            AC6 = ByteBuffer.wrap(new byte[]{0, 0, data[10], data[11]}).getInt();
            VB1 = ByteBuffer.wrap(new byte[]{data[12], data[13]}).getShort();
            VB2 = ByteBuffer.wrap(new byte[]{data[14], data[15]}).getShort();
            MB = ByteBuffer.wrap(new byte[]{data[16], data[17]}).getShort();
            MC = ByteBuffer.wrap(new byte[]{data[18], data[19]}).getShort();
            MD = ByteBuffer.wrap(new byte[]{data[20], data[21]}).getShort();
            Thread.sleep(50);
            this.update();
            this.startP = this.P;
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update() {
        try {
            readTemperature();
            readPressure();
            readAltitude();
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    private void readTemperature() throws IOException, InterruptedException {
        device.write(BMP180_REG_CONTROL, (byte) BMP180_COMMAND_TEMPERATURE);
        Thread.sleep(5);
        device.read(BMP180_REG_RESULT, data, 0, 2);
        int t = ByteBuffer.wrap(new byte[]{0, 0, data[0], data[1]}).getInt();
        X1 = (long) ((long) (t - AC6) * AC5 / 32768.0);
        X2 = (MC * 2048.0) / (X1 + MD);
        B5 = X1 + X2;
        this.T = ((B5 + 8.0) / 16.0) / 10.0; //Celsius
    }

    private void readPressure() throws IOException, InterruptedException {
        device.write(BMP180_REG_CONTROL, (byte) (BMP180_COMMAND_PRESSURE + (OVER_SAMPLING_RATE << 6)));
        Thread.sleep((3 * ((long) pow(2, OVER_SAMPLING_RATE))) + 2);
        device.read(BMP180_REG_RESULT, data, 0, 3);
        int p = ByteBuffer.wrap(new byte[]{0, data[0], data[1], data[2]}).getInt() >> (8-OVER_SAMPLING_RATE);
        B6 = B5 - 4000;
        X1 = (long) ((VB2 * (B6 * B6 / 4096.0)) / 2048.0);
        X2 = AC2 * B6 / 2048.0;
        X3 = (long) (X1 + X2);
        B3 = (((AC1 * 4 + X3) << OVER_SAMPLING_RATE) + 2) / 4.0;
        X1 = (long) (AC3 * B6 / 8192.0);
        X2 = (VB1 * (B6 * B6 / 2048.0)) / 65536.0;
        X3 = (long) (((X1 + X2) + 2) / 4.0);
        B4 = AC4 * (X3 + 32768) / 32768.0;
        B7 = ((p - B3) * (50000 >> OVER_SAMPLING_RATE));
        this.P = B7 < 2147483648L ? ((B7 * 2) / B4) : ((B7 / B4) * 2);
        X1 = (long) ((this.P / 256.0) * (this.P / 256.0));
        X1 = (long) ((X1 * 3038.0) / 65536.0);
        X2 = ((-7357) * this.P) / 65536.0;
        this.P = (this.P + (X1 + X2 + 3791) / 16.0) / 100; //hPa
    }

    private void readSeaAltitude() {
        double seaPressure = 1013.25;
        this.A = 44330 * (1 - pow((this.P / seaPressure), 0.1903));
    }

    private void readAltitude() {
        this.A = 44330 * (1 - pow((this.P / this.startP), 0.1903));
    }

    @Override
    public String toString() {
        return "BMP180 | Temperature: " + this.T + " Pressure: " + this.P + " Altitude: " + this.A;
    }
}