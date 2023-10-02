package pi.quadrocopter.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import pi.quadrocopter.util.MadgwickAHRS;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@ConfigurationProperties("hzInMs")
public class AppProperties {
    private final MadgwickAHRS ahrs;

    @Getter
    @Setter
    private Duration delay = Duration.parse(ahrs.getSamplePeriodInMs());
}
