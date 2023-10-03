package pi.quadrocopter.util;

import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;

public class MadgwickAHRS {

    @Getter
    @Setter
    private float samplePeriod;

    @Getter
    @Setter
    private float beta;

    @Getter
    @Setter
    private float twoKp;			// 2 * proportional gain (Kp)

    @Getter
    @Setter
    private float twoKi;			// 2 * integral gain (Ki)

    private float integralFBx = 0.0f,  integralFBy = 0.0f, integralFBz = 0.0f;	// integral error terms scaled by Ki

    private final float[] quaternion;

    public synchronized float[] getQuaternion() {
        return new float[] {quaternion[0], quaternion[1], quaternion[2], quaternion[3]};
    }

    public long getSamplePeriodInMs() {
        return ((long)((1.0/samplePeriod) * 1000.0));
    }

    public MadgwickAHRS(float samplePeriod) {
        this(samplePeriod, 0.1f);
    }

    public MadgwickAHRS(float samplePeriod, float beta) {
        this.samplePeriod = samplePeriod;
        this.beta = beta;
        this.quaternion = new float[] { 1f, 0f, 0f, 0f };
    }

    public synchronized void update(float gx, float gy, float gz, float ax, float ay,
                       float az, float mx, float my, float mz) {
        float recipNorm;
        float q0q0, q0q1, q0q2, q0q3, q1q1, q1q2, q1q3, q2q2, q2q3, q3q3;
        float hx, hy, bx, bz;
        float halfvx, halfvy, halfvz, halfwx, halfwy, halfwz;
        float halfex, halfey, halfez;
        float qa, qb, qc;

        // Use IMU algorithm if magnetometer measurement invalid (avoids NaN in magnetometer normalisation)
        if((mx == 0.0f) && (my == 0.0f) && (mz == 0.0f)) {
            update(gx, gy, gz, ax, ay, az);
            return;
        }

        float q0 = quaternion[0], q1 = quaternion[1], q2 = quaternion[2], q3 = quaternion[3]; // short

        // Compute feedback only if accelerometer measurement valid (avoids NaN in accelerometer normalisation)
        if(!((ax == 0.0f) && (ay == 0.0f) && (az == 0.0f))) {

            // Normalise accelerometer measurement
            recipNorm = invSqrt(ax * ax + ay * ay + az * az);
            ax *= recipNorm;
            ay *= recipNorm;
            az *= recipNorm;

            // Normalise magnetometer measurement
            recipNorm = invSqrt(mx * mx + my * my + mz * mz);
            mx *= recipNorm;
            my *= recipNorm;
            mz *= recipNorm;

            // Auxiliary variables to avoid repeated arithmetic
            q0q0 = q0 * q0;
            q0q1 = q0 * q1;
            q0q2 = q0 * q2;
            q0q3 = q0 * q3;
            q1q1 = q1 * q1;
            q1q2 = q1 * q2;
            q1q3 = q1 * q3;
            q2q2 = q2 * q2;
            q2q3 = q2 * q3;
            q3q3 = q3 * q3;

            // Reference direction of Earth's magnetic field
            hx = 2.0f * (mx * (0.5f - q2q2 - q3q3) + my * (q1q2 - q0q3) + mz * (q1q3 + q0q2));
            hy = 2.0f * (mx * (q1q2 + q0q3) + my * (0.5f - q1q1 - q3q3) + mz * (q2q3 - q0q1));
            bx = (float )Math.sqrt(hx * hx + hy * hy);
            bz = 2.0f * (mx * (q1q3 - q0q2) + my * (q2q3 + q0q1) + mz * (0.5f - q1q1 - q2q2));

            // Estimated direction of gravity and magnetic field
            halfvx = q1q3 - q0q2;
            halfvy = q0q1 + q2q3;
            halfvz = q0q0 - 0.5f + q3q3;
            halfwx = bx * (0.5f - q2q2 - q3q3) + bz * (q1q3 - q0q2);
            halfwy = bx * (q1q2 - q0q3) + bz * (q0q1 + q2q3);
            halfwz = bx * (q0q2 + q1q3) + bz * (0.5f - q1q1 - q2q2);

            // Error is sum of cross product between estimated direction and measured direction of field vectors
            halfex = (ay * halfvz - az * halfvy) + (my * halfwz - mz * halfwy);
            halfey = (az * halfvx - ax * halfvz) + (mz * halfwx - mx * halfwz);
            halfez = (ax * halfvy - ay * halfvx) + (mx * halfwy - my * halfwx);

            // Compute and apply integral feedback if enabled
            if(twoKi > 0.0f) {
                integralFBx += twoKi * halfex * (1.0f / samplePeriod);	// integral error scaled by Ki
                integralFBy += twoKi * halfey * (1.0f / samplePeriod);
                integralFBz += twoKi * halfez * (1.0f / samplePeriod);
                gx += integralFBx;	// apply integral feedback
                gy += integralFBy;
                gz += integralFBz;
            }
            else {
                integralFBx = 0.0f;	// prevent integral windup
                integralFBy = 0.0f;
                integralFBz = 0.0f;
            }

            // Apply proportional feedback
            gx += twoKp * halfex;
            gy += twoKp * halfey;
            gz += twoKp * halfez;
        }

        // Integrate rate of change of quaternion
        gx *= (0.5f * (1.0f / samplePeriod));		// pre-multiply common factors
        gy *= (0.5f * (1.0f / samplePeriod));
        gz *= (0.5f * (1.0f / samplePeriod));
        qa = q0;
        qb = q1;
        qc = q2;
        q0 += (-qb * gx - qc * gy - q3 * gz);
        q1 += (qa * gx + qc * gz - q3 * gy);
        q2 += (qa * gy - qb * gz + q3 * gx);
        q3 += (qa * gz + qb * gy - qc * gx);

        // Normalise quaternion
        recipNorm = invSqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3);
        quaternion[0] = q0 * recipNorm;
        quaternion[1] = q1 * recipNorm;
        quaternion[2] = q2 * recipNorm;
        quaternion[3] =  q3 * recipNorm;
    }

    public synchronized void update(float gx, float gy, float gz, float ax, float ay,
                       float az) {
        float q0 = quaternion[0], q1 = quaternion[1], q2 = quaternion[2], q3 = quaternion[3]; // short

        float recipNorm;
        float halfvx, halfvy, halfvz;
        float halfex, halfey, halfez;
        float qa, qb, qc;

        // Compute feedback only if accelerometer measurement valid (avoids NaN in accelerometer normalisation)
        if(!((ax == 0.0f) && (ay == 0.0f) && (az == 0.0f))) {

            // Normalise accelerometer measurement
            recipNorm = invSqrt(ax * ax + ay * ay + az * az);
            ax *= recipNorm;
            ay *= recipNorm;
            az *= recipNorm;

            // Estimated direction of gravity and vector perpendicular to magnetic flux
            halfvx = q1 * q3 - q0 * q2;
            halfvy = q0 * q1 + q2 * q3;
            halfvz = q0 * q0 - 0.5f + q3 * q3;

            // Error is sum of cross product between estimated and measured direction of gravity
            halfex = (ay * halfvz - az * halfvy);
            halfey = (az * halfvx - ax * halfvz);
            halfez = (ax * halfvy - ay * halfvx);

            // Compute and apply integral feedback if enabled
            if(twoKi > 0.0f) {
                integralFBx += twoKi * halfex * (1.0f / samplePeriod);	// integral error scaled by Ki
                integralFBy += twoKi * halfey * (1.0f / samplePeriod);
                integralFBz += twoKi * halfez * (1.0f / samplePeriod);
                gx += integralFBx;	// apply integral feedback
                gy += integralFBy;
                gz += integralFBz;
            }
            else {
                integralFBx = 0.0f;	// prevent integral windup
                integralFBy = 0.0f;
                integralFBz = 0.0f;
            }

            // Apply proportional feedback
            gx += twoKp * halfex;
            gy += twoKp * halfey;
            gz += twoKp * halfez;
        }

        // Integrate rate of change of quaternion
        gx *= (0.5f * (1.0f / samplePeriod));		// pre-multiply common factors
        gy *= (0.5f * (1.0f / samplePeriod));
        gz *= (0.5f * (1.0f / samplePeriod));
        qa = q0;
        qb = q1;
        qc = q2;
        q0 += (-qb * gx - qc * gy - q3 * gz);
        q1 += (qa * gx + qc * gz - q3 * gy);
        q2 += (qa * gy - qb * gz + q3 * gx);
        q3 += (qa * gz + qb * gy - qc * gx);

        // Normalise quaternion
        recipNorm = invSqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3);
        quaternion[0] = q0 * recipNorm;
        quaternion[1] = q1 * recipNorm;
        quaternion[2] = q2 * recipNorm;
        quaternion[3] =  q3 * recipNorm;
    }

    private float invSqrt(float x) {
        float xhalf = 0.5f * x;
        int i = Float.floatToIntBits(x);
        i = 0x5f3759df - (i >> 1);
        x = Float.intBitsToFloat(i);
        x *= (1.5f - xhalf * x * x);
        return x;
    }

    public synchronized ThreeAxes getEulerAngles() {
        ThreeAxes axes = new ThreeAxes();
//        double sinr_cosp = 2 * (quaternion[0] * quaternion[1] + quaternion[2] * quaternion[3]);
//        double cosr_cosp = 1 - 2 * (quaternion[1] * quaternion[1] + quaternion[2] * quaternion[2]);
//        axes.x = (float) Math.atan2(sinr_cosp, cosr_cosp);
//
//        double sinp = Math.sqrt(1 + 2 * (quaternion[0] * quaternion[2] - quaternion[1] * quaternion[3]));
//        double cosp = Math.sqrt(1 - 2 * (quaternion[0] * quaternion[2] - quaternion[1] * quaternion[3]));
//        axes.y = (float) (Math.atan2(sinp, cosp) - (Math.PI /2));
//
//        double siny_cosp = 2 * (quaternion[0] * quaternion[3] + quaternion[1] * quaternion[2]);
//        double cosy_cosp = 1 - 2 * (quaternion[2] * quaternion[2] + quaternion[3] * quaternion[3]);
//        axes.z = (float) Math.atan2(siny_cosp, cosy_cosp);

        double q2sqr = quaternion[2] * quaternion[2];
        double t0 = -2.0 * (q2sqr + quaternion[3] * quaternion[3]) + 1.0;
        double t1 = +2.0 * (quaternion[1] * quaternion[2] + quaternion[0] * quaternion[3]);
        double t2 = -2.0 * (quaternion[1] * quaternion[3] - quaternion[0] * quaternion[2]);
        double t3 = +2.0 * (quaternion[2] * quaternion[3] + quaternion[0] * quaternion[1]);
        double t4 = -2.0 * (quaternion[1] * quaternion[1] + q2sqr) + 1.0;

        t2 = Math.min(t2, 1.0);
        t2 = Math.max(t2, -1.0);

        axes.x = (float) Math.atan2(t3, t4);
        axes.y = (float) Math.asin(t2);
        axes.z = (float) Math.atan2(t1, t0);
        return axes;
    }

}