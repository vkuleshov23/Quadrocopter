package pi.quadrocopter.util;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import pi.quadrocopter.model.spi.NRF24;

@Component
@RequiredArgsConstructor
public class ApplicationShutdownManager {
    private final NRF24 nrf;
    private final ApplicationContext appContext;

    public void shutdown() {
        this.shutdown(0);
    }

    public void shutdown(int returnCode) {
        SpringApplication.exit(appContext, () -> returnCode);
    }

}
