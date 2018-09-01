package me.davehummel.tredserver.serial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dmhum_000 on 2/4/2017.
 */
public class NullSerialBridge implements SerialBridge {

    Logger logger = LoggerFactory.getLogger(NullSerialBridge.class);

    private boolean isSimulation;
    @Override
    public void setSimulation(boolean isSimulation) {
        this.isSimulation = isSimulation;
    }

    @Override
    public boolean getSimulation() {
        return true;
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void start() throws SerialBridgeException {

    }

    @Override
    public void end() {

    }

    @Override
    public byte[] readLine() throws SerialBridgeException {
        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            logger.error("read timeout",e);
        }
        return new byte[0];
    }

    @Override
    public void write(String text) throws SerialBridgeException {
        logger.error("NSB:"+text);
    }

    @Override
    public void setWriteResponseTimeout(long itmeout) {

    }
}
