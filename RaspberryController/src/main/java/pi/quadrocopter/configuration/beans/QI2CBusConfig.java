package pi.quadrocopter.configuration.beans;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.io.IOException;

@Configuration
public class QI2CBusConfig {

    @Bean
    @Scope("singleton")
    public I2CBus getBus() throws IOException, I2CFactory.UnsupportedBusNumberException {
        return I2CFactory.getInstance(I2CBus.BUS_1);
    }

}
