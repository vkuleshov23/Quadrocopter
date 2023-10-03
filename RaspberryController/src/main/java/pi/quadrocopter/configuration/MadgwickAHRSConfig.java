package pi.quadrocopter.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import pi.quadrocopter.util.MadgwickAHRS;

import java.time.Duration;

@Configuration
public class MadgwickAHRSConfig {

    public static final float AHRS_FREQUENCY_Hz = 76.9f;
    public static final float AHRS_BETA = 0.5f;

    @Bean
    @Scope("singleton")
    public MadgwickAHRS madgwickAHRS() {
        MadgwickAHRS ahrs = new MadgwickAHRS(AHRS_FREQUENCY_Hz, AHRS_BETA);
        ahrs.setTwoKi(1.0f);
        ahrs.setTwoKp(0.5f);
        return ahrs;
    }
}
