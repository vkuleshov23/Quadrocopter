package pi.quadrocopter.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import pi.quadrocopter.util.MadgwickAHRS;

@Configuration
public class MadgwickAHRSConfig {

    public static final float AHRS_FREQUENCY_Hz = 76.9f;

    @Bean
    @Scope("singleton")
    public MadgwickAHRS madgwickAHRS() {
        return new MadgwickAHRS(AHRS_FREQUENCY_Hz);
    }
}
