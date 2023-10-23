package pi.quadrocopter.configuration.beans;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import pi.quadrocopter.model.ahrs.MadgwickAHRS;

@Configuration
public class MadgwickAHRSConfig {

    public static final float AHRS_FREQUENCY_Hz = 72.7f;
    public static final float AHRS_BETA = 1.0f;

    @Bean
    @Scope("singleton")
    public MadgwickAHRS madgwickAHRS() {
        return new MadgwickAHRS(AHRS_FREQUENCY_Hz, AHRS_BETA);
    }
}
