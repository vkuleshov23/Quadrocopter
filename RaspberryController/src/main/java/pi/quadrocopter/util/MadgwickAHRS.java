package pi.quadrocopter.util;

import lombok.Getter;
import lombok.Setter;

public class MadgwickAHRS {

    @Getter
    @Setter
    private float samplePeriod;

    @Getter
    @Setter
    private float beta;

    @Getter
    private final float[] quaternion;


    public long getSamplePeriodInMs() {
        return ((long)((1.0/samplePeriod) * 1000.0));
    }

    public MadgwickAHRS(float samplePeriod) {
        this(samplePeriod, 1f);
    }

    public MadgwickAHRS(float samplePeriod, float beta) {
        this.samplePeriod = samplePeriod;
        this.beta = beta;
        this.quaternion = new float[] { 1f, 0f, 0f, 0f };
    }

    public void update(float gx, float gy, float gz, float ax, float ay,
                       float az, float mx, float my, float mz) {
        float q1 = quaternion[0], q2 = quaternion[1], q3 = quaternion[2], q4 = quaternion[3]; // short

        float norm;
        float hx, hy, _2bx, _2bz;
        float s1, s2, s3, s4;
        float qDot1, qDot2, qDot3, qDot4;

        // Auxiliary variables to avoid repeated arithmetic
        float _2q1mx;
        float _2q1my;
        float _2q1mz;
        float _2q2mx;
        float _4bx;
        float _4bz;
        float _2q1 = 2f * q1;
        float _2q2 = 2f * q2;
        float _2q3 = 2f * q3;
        float _2q4 = 2f * q4;
        float _2q1q3 = 2f * q1 * q3;
        float _2q3q4 = 2f * q3 * q4;
        float q1q1 = q1 * q1;
        float q1q2 = q1 * q2;
        float q1q3 = q1 * q3;
        float q1q4 = q1 * q4;
        float q2q2 = q2 * q2;
        float q2q3 = q2 * q3;
        float q2q4 = q2 * q4;
        float q3q3 = q3 * q3;
        float q3q4 = q3 * q4;
        float q4q4 = q4 * q4;

        // Normalise accelerometer measurement
        norm = (float) Math.sqrt(ax * ax + ay * ay + az * az);
        if (norm == 0f)
            return; // handle NaN
        norm = 1 / norm; // use reciprocal for division
        ax *= norm;
        ay *= norm;
        az *= norm;

        // Normalise magnetometer measurement
        norm = (float) Math.sqrt(mx * mx + my * my + mz * mz);
        if (norm == 0f)
            return; // handle NaN
        norm = 1 / norm; // use reciprocal for division
        mx *= norm;
        my *= norm;
        mz *= norm;

        // Reference direction of Earth's magnetic field
        _2q1mx = 2f * q1 * mx;
        _2q1my = 2f * q1 * my;
        _2q1mz = 2f * q1 * mz;
        _2q2mx = 2f * q2 * mx;
        hx = mx * q1q1 - _2q1my * q4 + _2q1mz * q3 + mx * q2q2 + _2q2 * my * q3
                + _2q2 * mz * q4 - mx * q3q3 - mx * q4q4;
        hy = _2q1mx * q4 + my * q1q1 - _2q1mz * q2 + _2q2mx * q3 - my * q2q2
                + my * q3q3 + _2q3 * mz * q4 - my * q4q4;
        _2bx = (float) Math.sqrt(hx * hx + hy * hy);
        _2bz = -_2q1mx * q3 + _2q1my * q2 + mz * q1q1 + _2q2mx * q4 - mz * q2q2
                + _2q3 * my * q4 - mz * q3q3 + mz * q4q4;
        _4bx = 2f * _2bx;
        _4bz = 2f * _2bz;

        // Gradient decent algorithm corrective step
        s1 = -_2q3 * (2f * q2q4 - _2q1q3 - ax) + _2q2
                * (2f * q1q2 + _2q3q4 - ay) - _2bz * q3
                * (_2bx * (0.5f - q3q3 - q4q4) + _2bz * (q2q4 - q1q3) - mx)
                + (-_2bx * q4 + _2bz * q2)
                * (_2bx * (q2q3 - q1q4) + _2bz * (q1q2 + q3q4) - my) + _2bx
                * q3
                * (_2bx * (q1q3 + q2q4) + _2bz * (0.5f - q2q2 - q3q3) - mz);
        s2 = _2q4 * (2f * q2q4 - _2q1q3 - ax) + _2q1
                * (2f * q1q2 + _2q3q4 - ay) - 4f * q2
                * (1 - 2f * q2q2 - 2f * q3q3 - az) + _2bz * q4
                * (_2bx * (0.5f - q3q3 - q4q4) + _2bz * (q2q4 - q1q3) - mx)
                + (_2bx * q3 + _2bz * q1)
                * (_2bx * (q2q3 - q1q4) + _2bz * (q1q2 + q3q4) - my)
                + (_2bx * q4 - _4bz * q2)
                * (_2bx * (q1q3 + q2q4) + _2bz * (0.5f - q2q2 - q3q3) - mz);
        s3 = -_2q1 * (2f * q2q4 - _2q1q3 - ax) + _2q4
                * (2f * q1q2 + _2q3q4 - ay) - 4f * q3
                * (1 - 2f * q2q2 - 2f * q3q3 - az) + (-_4bx * q3 - _2bz * q1)
                * (_2bx * (0.5f - q3q3 - q4q4) + _2bz * (q2q4 - q1q3) - mx)
                + (_2bx * q2 + _2bz * q4)
                * (_2bx * (q2q3 - q1q4) + _2bz * (q1q2 + q3q4) - my)
                + (_2bx * q1 - _4bz * q3)
                * (_2bx * (q1q3 + q2q4) + _2bz * (0.5f - q2q2 - q3q3) - mz);
        s4 = _2q2 * (2f * q2q4 - _2q1q3 - ax) + _2q3
                * (2f * q1q2 + _2q3q4 - ay) + (-_4bx * q4 + _2bz * q2)
                * (_2bx * (0.5f - q3q3 - q4q4) + _2bz * (q2q4 - q1q3) - mx)
                + (-_2bx * q1 + _2bz * q3)
                * (_2bx * (q2q3 - q1q4) + _2bz * (q1q2 + q3q4) - my) + _2bx
                * q2
                * (_2bx * (q1q3 + q2q4) + _2bz * (0.5f - q2q2 - q3q3) - mz);
        norm = 1f / (float) Math.sqrt(s1 * s1 + s2 * s2 + s3 * s3 + s4 * s4); // normalise
        // step
        // magnitude
        s1 *= norm;
        s2 *= norm;
        s3 *= norm;
        s4 *= norm;

        // Compute rate of change of quaternion
        qDot1 = 0.5f * (-q2 * gx - q3 * gy - q4 * gz) - beta * s1;
        qDot2 = 0.5f * (q1 * gx + q3 * gz - q4 * gy) - beta * s2;
        qDot3 = 0.5f * (q1 * gy - q2 * gz + q4 * gx) - beta * s3;
        qDot4 = 0.5f * (q1 * gz + q2 * gy - q3 * gx) - beta * s4;

        // Integrate to yield quaternion
        q1 += qDot1 * samplePeriod;
        q2 += qDot2 * samplePeriod;
        q3 += qDot3 * samplePeriod;
        q4 += qDot4 * samplePeriod;
        System.out.println("q1: " + q1);
        System.out.println("qDot1: " + qDot1);
        System.out.println("q3: " + q3);
        System.out.println("qDot3: " + qDot3);
        norm = 1f / (float) Math.sqrt(q1 * q1 + q2 * q2 + q3 * q3 + q4 * q4); // normalise
        // quaternion
        quaternion[0] = q1 * norm;
        quaternion[1] = q2 * norm;
        quaternion[2] = q3 * norm;
        quaternion[3] = q4 * norm;
    }

    public void update(float gx, float gy, float gz, float ax, float ay,
                       float az) {
        float q1 = quaternion[0], q2 = quaternion[1], q3 = quaternion[2], q4 = quaternion[3]; // short
        float norm;
        float s1, s2, s3, s4;
        float qDot1, qDot2, qDot3, qDot4;

        // Auxiliary variables to avoid repeated arithmetic
        float _2q1 = 2f * q1;
        float _2q2 = 2f * q2;
        float _2q3 = 2f * q3;
        float _2q4 = 2f * q4;
        float _4q1 = 4f * q1;
        float _4q2 = 4f * q2;
        float _4q3 = 4f * q3;
        float _8q2 = 8f * q2;
        float _8q3 = 8f * q3;
        float q1q1 = q1 * q1;
        float q2q2 = q2 * q2;
        float q3q3 = q3 * q3;
        float q4q4 = q4 * q4;

        // Normalise accelerometer measurement
        norm = (float) Math.sqrt(ax * ax + ay * ay + az * az);
        if (norm == 0f)
            return; // handle NaN
        norm = 1 / norm; // use reciprocal for division
        ax *= norm;
        ay *= norm;
        az *= norm;

        // Gradient decent algorithm corrective step
        s1 = _4q1 * q3q3 + _2q3 * ax + _4q1 * q2q2 - _2q2 * ay;
        s2 = _4q2 * q4q4 - _2q4 * ax + 4f * q1q1 * q2 - _2q1 * ay - _4q2 + _8q2
                * q2q2 + _8q2 * q3q3 + _4q2 * az;
        s3 = 4f * q1q1 * q3 + _2q1 * ax + _4q3 * q4q4 - _2q4 * ay - _4q3 + _8q3
                * q2q2 + _8q3 * q3q3 + _4q3 * az;
        s4 = 4f * q2q2 * q4 - _2q2 * ax + 4f * q3q3 * q4 - _2q3 * ay;
        norm = 1f / (float) Math.sqrt(s1 * s1 + s2 * s2 + s3 * s3 + s4 * s4); // normalise
        // step
        // magnitude
        s1 *= norm;
        s2 *= norm;
        s3 *= norm;
        s4 *= norm;

        // Compute rate of change of quaternion
        qDot1 = 0.5f * (-q2 * gx - q3 * gy - q4 * gz) - beta * s1;
        qDot2 = 0.5f * (q1 * gx + q3 * gz - q4 * gy) - beta * s2;
        qDot3 = 0.5f * (q1 * gy - q2 * gz + q4 * gx) - beta * s3;
        qDot4 = 0.5f * (q1 * gz + q2 * gy - q3 * gx) - beta * s4;

        // Integrate to yield quaternion
        q1 += qDot1 * samplePeriod;
        q2 += qDot2 * samplePeriod;
        q3 += qDot3 * samplePeriod;
        q4 += qDot4 * samplePeriod;
        norm = 1f / (float) Math.sqrt(q1 * q1 + q2 * q2 + q3 * q3 + q4 * q4); // normalise
        // quaternion
        quaternion[0] = q1 * norm;
        quaternion[1] = q2 * norm;
        quaternion[2] = q3 * norm;
        quaternion[3] = q4 * norm;
    }

}