package me.davehummel.tredserver.serial.jsscserial;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import me.davehummel.tredserver.serial.SerialBridge;
import me.davehummel.tredserver.serial.SerialBridgeException;
import me.davehummel.tredserver.serial.SerialConversionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by dmhum_000 on 4/3/2016.
 */
public class JsscSerialBridge implements SerialBridge {

    Logger logger = LoggerFactory.getLogger(JsscSerialBridge.class);


    private boolean isSimulation = false;
    private long timeout = 2000;

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
        return jsscPort.isOpened();
    }


    private final static byte SYNCBLOCK = -1;

    private SerialPort jsscPort;
    private final int rate;
    private final String port;

    public JsscSerialBridge(String port, int rate) {
        this.port = port;
        this.rate = rate;
    }

    @Override
    public void start() throws SerialBridgeException {
        String[] portNames = SerialPortList.getPortNames();

        jsscPort = new SerialPort(port);

        try {

            if (jsscPort.isOpened())
                jsscPort.closePort();

            jsscPort.openPort();

            jsscPort.setParams(rate,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

        } catch (SerialPortException ex) {
            logger.error("Failed to start serial:",ex);
            throw new SerialBridgeException(ex.getMessage());
        }

        logger.info("JSSC UART started");
    }

    @Override
    public void end() {
//        try {
//            jsscPort.closePort();
//        } catch (SerialPortException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public byte[] readLine() throws SerialBridgeException {
        int length = 0;
        try {

            byte[] temp = jsscPort.readBytes(4);

//        System.out.print("Got Header:");
//        for (byte b:temp){
//            System.out.print(0xff&b);
//            System.out.print(",");
//        }
            if (temp[2] != SYNCBLOCK || temp[3] != SYNCBLOCK) {
                StringBuilder sb = new StringBuilder("Read bad length header:");
                for (byte b : temp) {
                    sb.append(0xff & b);
                    sb.append(",");
                }
                logger.error(sb.toString());
                logger.error(" - Syncing....");
                while (temp[2] != SYNCBLOCK || temp[3] != SYNCBLOCK) {
                    temp[0] = temp[1];
                    temp[1] = temp[2];
                    temp[2] = temp[3];
                    temp[3] = jsscPort.readBytes(1)[0];
                }
                logger.error(" - Resynced!");
            }

            length = SerialConversionUtil.getU16Int(temp, 0);
            if (length > 255) {
                logger.warn("Warning Length:" + length);
            }

            temp = jsscPort.readBytes(length);

            if (logger.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder("Data len(" + length + ")[");
                for (byte b : temp) {
                    sb.append(0xff & b);
                    sb.append(",");
                }
                sb.append("]");
                logger.debug(sb.toString());
            }

            return temp;

        } catch (SerialPortException e) {
            logger.error("Failed to read",e);
            throw new SerialBridgeException(e.getMessage());
        }

    }

    @Override
    synchronized public void write(String text) throws SerialBridgeException {
        try {
            logger.debug("CMD>>"+text+"<<");
                jsscPort.writeString(text);
        } catch (SerialPortException e) {
            logger.error("Failed to write",e);
            throw new SerialBridgeException(e.getMessage());
        }
    }

    @Override
    public void setWriteResponseTimeout(long timemeout) {
        this.timeout = timemeout;
    }


}
