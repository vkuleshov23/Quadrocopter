package pi.quadrocopter.util;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class Vector3_16bit {
    public float x;
    public float y;
    public float z;

    public Vector3_16bit(Vector3_16bit ta) {
        this.x = ta.x;
        this.y = ta.y;
        this.z = ta.z;
    }

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

    public void div(float num) {
        this.x /= num;
        this.y /= num;
        this.z /= num;
    }

    @Override
    public String toString() {
        return "X: " + x + " Y: " + y + " Z: " + z;
    }

    public int getXHigh() {
        return (int)this.x >> 8;
    }

    public int getXLow() {
        return (int)this.x & 255;
    }

    public float getYInt() {
        return (int)this.y >> 8;
    }

    public int getYLow() {
        return (int)this.y & 255;
    }

    public float getZInt() {
        return (int)this.z >> 8;
    }

    public int getZLow() {
        return (int)this.y & 255;
    }
}
