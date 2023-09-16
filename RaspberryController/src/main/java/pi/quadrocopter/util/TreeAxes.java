package pi.quadrocopter.util;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TreeAxes {
    public int x;
    public int y;
    public int z;

    public void setX(int high, int low) {
        this.x = hltoi(high, low);
    }

    public void setY(int high, int low) {
        this.y = hltoi(high, low);
    }

    public void setZ(int high, int low) {
        this.z = hltoi(high, low);
    }

    private int hltoi(int high, int low) {
        int i = ((high & 0xFF)*256) + (low & 256);
        return (i > 32767) ? (i - 65536) : i;
    }

    @Override
    public String toString() {
        return "X: " + x + " Y: " + y + " Z: " + z;
    }
}
