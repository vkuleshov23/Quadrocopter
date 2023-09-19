package pi.quadrocopter.model.nrf;

public interface NRFReceiveListener {

    /**
     * @param data data bytes arrived
     */
    void dataReceived(int[] data);

}