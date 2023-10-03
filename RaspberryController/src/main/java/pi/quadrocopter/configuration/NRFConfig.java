package pi.quadrocopter.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import pi.quadrocopter.model.spi.NRF24;

import java.io.IOException;


@Configuration
public class NRFConfig {

    private static final byte[] writingAddress = {(byte)0xAA, (byte)0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA};
    private static final byte[] readingAddress = {(byte)0xFF, (byte)0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
    private final long SAMPLE_MS = 50;

    @Bean
    @Scope("singleton")
    public NRF24 nrf() throws IOException {
        NRF24 nrf = new NRF24();
        nrf.setSampleMS(SAMPLE_MS);
        nrf.begin();
        nrf.setChannel(76);
        nrf.setCRCLength(NRF24.RF24_CRC_16);
        nrf.setRetries((char) 5, (char) 5);
        nrf.setDataRate(NRF24.RF24_250KBPS);
        nrf.setPALevel(NRF24.RF24_PA_LOW);
        nrf.setPayloadSize((byte) 32);
//        nrf.openWritingPipe(writingAddress);
        nrf.openReadingPipe(1, readingAddress);
        nrf.startListening();
        return nrf;
    }
}
