package me.davehummel.tredserver.serial;

/**
 * Created by dmhum_000 on 2/4/2017.
 */
public class NullSerialBridge implements SerialBridge {

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
            e.printStackTrace();
        }
        return new byte[0];
    }

    @Override
    public void write(String text) throws SerialBridgeException {
        System.out.println("NSB:"+text);
    }

    @Override
    public void setWriteResponseTimeout(long itmeout) {

    }
}
