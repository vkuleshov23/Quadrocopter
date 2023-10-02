package pi.quadrocopter.util;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ThreeAxes {
    public float x;
    public float y;
    public float z;

    public void setX(int high, int low) {
        this.x = hltoi(high, low);
    }

    public void setY(int high, int low) {
        this.y = hltoi(high, low);
    }

    public void setZ(int high, int low) {
        this.z = hltoi(high, low);
    }

    private float hltoi(int high, int low) {
        int i = ((high & 0xFF) << 8) | (low & 0xFF);
        i = ((i > 32767) ? (i - 65536) : i);
        return (float) i;
    }

    public void mull(float num) {
        this.x *= num;
        this.y *= num;
        this.z *= num;
    }

    private float normalize(int data) {
        return (float) data / 32768;
    }

    @Override
    public String toString() {
        return "X: " + x + " Y: " + y + " Z: " + z;
    }
}