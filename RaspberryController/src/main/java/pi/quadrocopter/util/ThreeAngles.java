package pi.quadrocopter.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class ThreeAngles {

    @Setter
    @Getter
    private float roll;
    @Setter
    @Getter
    private float pitch;
    @Setter
    @Getter
    private float yaw;

    public void toDegrees() {
        this.roll = (float) Math.toDegrees(this.roll);
        this.pitch = (float) Math.toDegrees(this.pitch);
        this.yaw = (float) Math.toDegrees(this.yaw);
    }

    public void toRadians() {
        this.roll = (float) Math.toRadians(this.roll);
        this.pitch = (float) Math.toRadians(this.pitch);
        this.yaw = (float) Math.toRadians(this.yaw);
    }

    public Quaternion toQuaternion() {
        float qw = (float) (Math.cos(roll/2) * Math.cos(pitch/2) * Math.cos(yaw/2) + Math.sin(roll/2) * Math.sin(pitch/2) * Math.sin(yaw/2));
        float qx = (float) (Math.sin(roll/2) * Math.cos(pitch/2) * Math.cos(yaw/2) - Math.cos(roll/2) * Math.sin(pitch/2) * Math.sin(yaw/2));
        float qy = (float) (Math.cos(roll/2) * Math.sin(pitch/2) * Math.cos(yaw/2) + Math.sin(roll/2) * Math.cos(pitch/2) * Math.sin(yaw/2));
        float qz = (float) (Math.cos(roll/2) * Math.cos(pitch/2) * Math.sin(yaw/2) + Math.sin(roll/2) * Math.sin(pitch/2) * Math.cos(yaw/2));
        return new Quaternion(qw, qx, qy, qz);
    }

    @Override
    public String toString() {
        return " ANGLES | ROLL: " + roll + " PITCH: " + pitch + " YAW: " + yaw;
    }
}
