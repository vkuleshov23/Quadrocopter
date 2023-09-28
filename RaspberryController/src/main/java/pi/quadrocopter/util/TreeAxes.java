package pi.quadrocopter.util;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TreeAxes {
    public double x;
    public double y;
    public double z;

    public void setX(int high, int low) {
        this.x = hltoi(high, low);
    }

    public void setY(int high, int low) {
        this.y = hltoi(high, low);
    }

    public void setZ(int high, int low) {
        this.z = hltoi(high, low);
    }

    private double hltoi(int high, int low) {
        int i = ((high & 0xFF) << 8) + (low & 0xFF);
        i = ((i > 32767) ? (i - 65536) : i);
        return i;
    }

    private double normalize(int data) {
        return (double) data / 32768;
    }

    @Override
    public String toString() {
        return "\nX: " + x + "\nY: " + y + "\nZ: " + z;
    }
}
