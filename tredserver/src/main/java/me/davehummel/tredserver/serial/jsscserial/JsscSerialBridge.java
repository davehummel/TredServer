package me.davehummel.tredserver.serial.jsscserial;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import me.davehummel.tredserver.serial.*;

import java.io.IOException;
import java.util.Date;


/**
 * Created by dmhum_000 on 4/3/2016.
 */
public class JsscSerialBridge implements SerialBridge {


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
            ex.printStackTrace();
            ;
            throw new SerialBridgeException(ex.getMessage());
        }

        System.out.println("JSSC UART started");
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
                System.out.print("Read bad length header:");
                for (byte b : temp) {
                    System.out.print(0xff & b);
                    System.out.print(",");
                }
                System.out.println(" - Syncing....");
                while (temp[2] != SYNCBLOCK || temp[3] != SYNCBLOCK) {
                    temp[0] = temp[1];
                    temp[1] = temp[2];
                    temp[2] = temp[3];
                    temp[3] = jsscPort.readBytes(1)[0];
                }
            }

            length = SerialConversionUtil.getU16Int(temp, 0);
            if (length > 255) {
                System.out.println("Warning Length:" + length);
            }

            temp = jsscPort.readBytes(length);

//            System.out.print("Data len(" + length + ")[");
//            for (byte b:temp){
//                System.out.print(0xff&b);
//                System.out.print(",");
//            } System.out.println("]");

            return temp;

        } catch (SerialPortException e) {
            e.printStackTrace();
            throw new SerialBridgeException(e.getMessage());
        }

    }

    @Override
    synchronized public void write(String text) throws SerialBridgeException {
        try {
            System.out.println("CMD>>"+text+"<<");
                jsscPort.writeString(text);
        } catch (SerialPortException e) {
            e.printStackTrace();
            throw new SerialBridgeException(e.getMessage());
        }
    }

    @Override
    public void setWriteResponseTimeout(long timemeout) {
        this.timeout = timemeout;
    }

    public static void PRINTPORTS() {
        String[] portNames = SerialPortList.getPortNames();


        for (int i = 0; i < portNames.length; i++) {
            System.out.println(portNames[i]);
        }
    }
}
