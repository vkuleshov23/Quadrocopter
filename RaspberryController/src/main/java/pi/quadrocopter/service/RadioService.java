package pi.quadrocopter.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pi.quadrocopter.exceptions.NoMessagesReceived;
import pi.quadrocopter.model.spi.NRF24;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@RequiredArgsConstructor
public class RadioService {
    private final NRF24 nrf;

    private final ConcurrentLinkedQueue<String> messages = new ConcurrentLinkedQueue<>();


    @SneakyThrows
    @Scheduled(fixedDelayString = "#{@nrf.getSampleMS()}")
    public void update() {
        if(nrf.available()) {
            String message = nrf.read();
            System.out.println(message);
            messages.add(message);
        } else {
            nrf.startListening();
        }
    }

    public String getMessage() throws NoMessagesReceived {
        Optional<String> message = Optional.ofNullable(messages.poll());
        return message.orElseThrow(NoMessagesReceived::new);
    }

    public long getSampleInMs() {
        return nrf.getSampleMS();
    }

}
