package pi.quadrocopter.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import pi.quadrocopter.model.spi.NRF24;

import java.io.IOException;


@Configuration
public class NRFConfig {

    private static final byte[] writingAddress = {(byte)0xaa, (byte)0xaa, 0x00, 0x00, 0x00};
    private static final byte[] readingAddress = {(byte)0xff, (byte)0xff, 0x00, 0x00, 0x00};

    @Bean
    @Scope("singleton")
    public NRF24 getRadio() throws IOException {
        NRF24 nrf = new NRF24();
        nrf.begin();
        nrf.setPALevel(NRF24.RF24_PA_LOW);
        nrf.setPayloadSize((byte) 32);
        nrf.openWritingPipe(writingAddress);
        nrf.openReadingPipe(1, readingAddress);
        nrf.startListening();
        return nrf;
    }
}
