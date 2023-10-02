package pi.quadrocopter.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import pi.quadrocopter.util.MadgwickAHRS;

import java.time.Duration;

@Configuration
@ConfigurationProperties("hzInMs")
public class MadgwickAHRSConfig {

    public static final float AHRS_FREQUENCY_Hz = 76.9f;
    private final MadgwickAHRS ahrs = new MadgwickAHRS(AHRS_FREQUENCY_Hz);

    @Getter
    @Setter
    private Duration delay = Duration.parse(ahrs.getSamplePeriodInMs());

    @Bean
    @Scope("singleton")
    public MadgwickAHRS madgwickAHRS() {
        return ahrs;
    }
}
