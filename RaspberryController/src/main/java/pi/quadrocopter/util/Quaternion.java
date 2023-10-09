package pi.quadrocopter.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class Quaternion {
    private float w;
    private float x;
    private float y;
    private float z;

    public Quaternion() {
        this.w = 1.0f;
        this.x = 0.0f;
        this.y = 0.0f;
        this.z = 0.0f;
    }

    public ThreeAngles toEulerAngles() {
        ThreeAngles angles = new ThreeAngles();

        double q2sqr = y * y;
        double t0 = -2.0 * (q2sqr + z * z) + 1.0;
        double t1 = +2.0 * (x * y + w * z);
        double t2 = -2.0 * (w * y - z * x);
        double t3 = +2.0 * (y * z + w * x);
        double t4 = -2.0 * (x * x + q2sqr) + 1.0;

        t2 = Math.min(t2, 1.0);
        t2 = Math.max(t2, -1.0);

        angles.setRoll((float) Math.atan2(t3, t4));
        angles.setPitch((float) Math.asin(t2));
        angles.setYaw((float) Math.atan2(t1, t0));
        return angles;
    }

    @Override
    public String toString() {
        return " QUATERNION | W: " + w + " X: " + x + " Y: " + y + " Z: " + z;
    }
}
