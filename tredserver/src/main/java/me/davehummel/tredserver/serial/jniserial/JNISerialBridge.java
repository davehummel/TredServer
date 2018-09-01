package me.davehummel.tredserver.serial.jniserial;

import me.davehummel.tredserver.serial.*;

/**
 * Created by dmhum_000 on 9/13/2015.
 */
public class JNISerialBridge implements SerialBridge {

    private boolean isSimulation;
    @Override
    public void setSimulation(boolean isSimulation) {
        this.isSimulation = isSimulation;
    }

    @Override
    public boolean getSimulation() {
        return isSimulation;
    }

    @Override
    public boolean isOpen() {
        return true;
    }


    @Override
    public void start() throws SerialBridgeException {
        String result = JNISerial.setup();
        if (result!=null)
            throw new SerialBridgeException("Failed to connect with JNISerial:"+result);
    }

    @Override
    public void end() {
        JNISerial.close();
    }

    @Override
    public byte[] readLine() throws SerialBridgeException {
        if (1==1) throw new NullPointerException("MUST UPDATE TO CHECK for FFFF bytes");
       return JNISerial.readLine();

    }

    @Override
    public void write(String text) throws SerialBridgeException {
        String out = JNISerial.write(text);
        if (out != null){
            throw new SerialBridgeException("Failed Write:"+out);
        }
    }

    @Override
    public void setWriteResponseTimeout(long itmeout) {

    }
}
