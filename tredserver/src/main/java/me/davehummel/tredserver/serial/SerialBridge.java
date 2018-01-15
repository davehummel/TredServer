package me.davehummel.tredserver.serial;

import me.davehummel.tredserver.services.SendResponse;

/**
 * Created by dmhum_000 on 9/5/2015.
 */
public interface SerialBridge {
    void start() throws SerialBridgeException;
    void end();
    byte[] readLine() throws SerialBridgeException;
    void write(String text) throws SerialBridgeException;

    void setWriteResponseTimeout(long itmeout);

    void setSimulation(boolean isSimulation);
    boolean getSimulation();
}
