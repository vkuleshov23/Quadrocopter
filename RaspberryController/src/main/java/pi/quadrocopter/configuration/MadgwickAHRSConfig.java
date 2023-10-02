package pi.quadrocopter.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pi.quadrocopter.util.MadgwickAHRS;

@Configuration
public class MadgwickAHRSConfig {

    public static final float AHRS_FREQUENCY_Hz = 76.9f;

    @Bean
    public MadgwickAHRS madgwickAHRS() {
        MadgwickAHRS ahrs = new MadgwickAHRS(AHRS_FREQUENCY_Hz);
        return ahrs;
    }
}
