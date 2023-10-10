package pi.quadrocopter.model.pid;

import lombok.Getter;
import lombok.Setter;

public class QuickPID {
    private float dispKp = 0; // for defaults and display
    private float dispKi = 0;
    private float dispKd = 0;
    @Getter
    private float pTerm;
    @Getter
    private float iTerm;
    @Getter
    private float dTerm;
    @Setter
    @Getter
    private float kp = dispKp;
    @Setter
    @Getter
    private float ki = dispKi;
    @Setter
    @Getter
    private float kd = dispKd;
    @Setter
    @Getter
    private Control control = Control.manual;
    @Setter
    @Getter
    private Action action = Action.direct;
    @Setter
    @Getter
    private PMode pMode = PMode.pOnErrorMeas;
    @Setter
    @Getter
    private DMode dMode = DMode.dOnError;
    @Setter
    @Getter
    private IAwMode iAwMode = IAwMode.iAwCondition;
    @Getter
    private long sampleTimeUs;
    private long lastTime;

    @Getter
    private float outMin, outMax;
    private float  error, lastError, lastInput;
    @Setter
    @Getter
    private float outputSum;

    private float output = 0.0f;

    enum Control {manual(0), automatic(1), timer(2), toggle(3); private final int value; Control(int value) {this.value = value;} public int getValue() {return this.value;}} // controller mode
    enum Action {direct(0), reverse(1); private final int value; Action(int value) {this.value = value;} public int getValue() {return this.value;}} // controller action
    enum PMode {pOnError(0), pOnMeas(1), pOnErrorMeas(2); private final int value; PMode(int value) {this.value = value;} public int getValue() {return this.value;}} // proportional mode
    enum DMode {dOnError(0), dOnMeas(1); private final int value; DMode(int value) {this.value = value;} public int getValue() {return this.value;}} // derivative mode
    enum IAwMode {iAwCondition(0), iAwClamp(1), iAwOff(2); private final int value; IAwMode(int value) {this.value = value;} public int getValue() {return this.value;}} // integral anti-windup mode

    public QuickPID(float kp, float ki, float kd, PMode pMode, DMode dMode, IAwMode iAwMode, Action action, int min, int max, long sampleTimeMs) {
        this.setTunings(kp, ki, kd, pMode, dMode, iAwMode);
        this.action = action;
        this.setOutputLimits(min, max);
        this.sampleTimeUs = sampleTimeMs * 1000;
    }

    public QuickPID(float kp, float ki, float kd, int min, int max, long sampleTimeMs) {
        this(kp, ki, kd, PMode.pOnErrorMeas, DMode.dOnMeas, IAwMode.iAwCondition, Action.direct, min, max, sampleTimeMs);
    }

    public QuickPID(float kp, float ki, float kd) {
        this(kp, ki, kd, 0, 255, 100);
    }

    float compute(float input, float setpoint) {
        if(this.control == Control.manual) return output;
        long now = micros();
        long timeChange = (now - lastTime);
        if(control == Control.timer || timeChange >= sampleTimeUs) {
            float dInput = input - lastInput;
            if(action == Action.reverse) dInput = -dInput;

            error = setpoint - input;
            if(action == Action.reverse) error = -error;
            float dError = error - lastError;

            float peTerm = kp * error;
            float pmTerm = kp * dInput;
            if(pMode == PMode.pOnError) pmTerm = 0;
            else if(pMode == PMode.pOnMeas) peTerm = 0;
            else { peTerm *= 0.5f; pmTerm *= 0.5f;} //pOnErrorMeas
            pTerm = peTerm - pmTerm;
            iTerm = ki * error;
            if(dMode == DMode.dOnError) dTerm = kd * dError;
            else dTerm = -kd * dInput; // dOnMeas

            //condition anti-windup (default)
            if(iAwMode == IAwMode.iAwCondition) {
                boolean aw = false;
                float iTermOut = (pTerm) + ki * (iTerm + error);
                if (iTermOut > outMax && dError > 0) aw = true;
                else if (iTermOut < outMin && dError < 0) aw = true;
                if (aw && ki != 0.0f) iTerm = constrain(iTermOut, -outMax, outMax);
            }
            // by default, compute output as per PID_v1
            outputSum += iTerm;                                                 // include integral amount
            if (iAwMode == IAwMode.iAwOff) outputSum -= pmTerm;                // include pmTerm (no anti-windup)
            else outputSum = constrain(outputSum - pmTerm, outMin, outMax);     // include pmTerm and clamp
            output = constrain(outputSum + peTerm + dTerm, outMin, outMax);  // include dTerm, clamp and drive output

            lastError = error;
            lastInput = input;
            lastTime = now;
        }
        return output;
    }

    public void setTunings(float kp, float ki, float kd) {
        setTunings(kp, ki, kd, this.pMode, this.dMode, this.iAwMode);
    }

    public void setTunings(float kp, float ki, float kd, PMode pMode, DMode dMode, IAwMode iAwMode) {
        if(kp < 0 || ki < 0 || kd < 0) return;
        if(ki == 0) outputSum = 0;
        this.dispKp = kp; this.dispKi = ki; this.dispKd = kd;
        this.pMode = pMode; this.dMode = dMode; this.iAwMode = iAwMode;
        float sampleTimeSec = sampleTimeUs / 1000000.0f;
        this.kp = kp;
        this.ki = ki * sampleTimeSec;
        this.kd = kd / sampleTimeSec;
    }

    public void setSampleTimeUs(long newSampleTimeUs) {
        if(newSampleTimeUs > 0) {
            float ratio = (float) newSampleTimeUs / (float) sampleTimeUs;
            ki *= ratio;
            kd /= ratio;
            sampleTimeUs = newSampleTimeUs;
        }
    }

    public void setMode(Control control) {
        if (this.control == Control.manual && control != Control.manual) { // just went from manual to automatic, timer or toggle
            this.init();
        }
        if(control == Control.toggle) {
            this.control = (this.control == Control.manual) ? Control.automatic : Control.manual;
        } else {
            this.control = control;
        }
    }
    void setControllerDirection(Action action) {
        this.action = action;
    }

    Control getMode() {
        return this.control;
    }

    Action getDirection() {
        return this.action;
    }

    public void setOutputLimits(float Min, float Max) {
        if (Min < Max) {
            this.outMax = Max;
            this.outMin = Min;
        } else if(Min != Max) {
            this.outMax = Min;
            this.outMin = Max;
        }
        if (this.control != Control.manual) {
            output = constrain(output, outMin, outMax);
            outputSum = constrain(outputSum, outMin, outMax);
        }
    }

    public void init() {
        this.outputSum = output;
//        this.lastInput = input;
        outputSum = constrain(outputSum, outMin, outMax);
    }

    public void reset() {
        lastTime = micros() - sampleTimeUs;
        lastInput = 0;
        outputSum = 0;
        pTerm = 0;
        iTerm = 0;
        dTerm = 0;
    }

    private float constrain(float value, float min, float max) {
        return Math.min(Math.max(value, min), max);
    }

    private long micros() {
        return System.nanoTime()/1000;
    }
}
