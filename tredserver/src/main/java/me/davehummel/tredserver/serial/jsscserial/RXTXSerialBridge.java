package me.davehummel.tredserver.serial.jsscserial;


import purejavacomm.*;
import me.davehummel.tredserver.serial.SerialBridge;
import me.davehummel.tredserver.serial.SerialBridgeException;
import me.davehummel.tredserver.serial.SerialConversionUtil;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;


/**
 * Created by dmhum_000 on 4/3/2016.
 */
public class RXTXSerialBridge implements SerialBridge {


    private boolean isSimulation = false;
    private long timeout = 500;

    @Override
    public void setSimulation(boolean isSimulation) {
        this.isSimulation = isSimulation;
    }

    @Override
    public boolean getSimulation() {
        return isSimulation;
    }


    private final static byte SYNCBLOCK = -1;

    private SerialPort serialPort;
    private InputStream inputStream;
    private OutputStream outputStream;
    private PrintWriter printWriter;
    private final int rate;
    private final String port;

    public RXTXSerialBridge(String port, int rate) {
        this.port = port;
        this.rate = rate;
    }

    @Override
    public void start() throws SerialBridgeException {
        CommPortIdentifier portIdentifier = null;
        try {
            portIdentifier = CommPortIdentifier.getPortIdentifier(port);
        } catch (NoSuchPortException e) {
            throw new SerialBridgeException(e.getMessage());
        }
        if (portIdentifier.isCurrentlyOwned()) {
            throw new SerialBridgeException("Port " + port + " is in use:" + portIdentifier.getCurrentOwner());
        }

        try {
            serialPort = (SerialPort) portIdentifier.open("AquariumServer", 1000);
        } catch (PortInUseException e) {
            throw new SerialBridgeException("Port " + port + " is in use:" + e.getMessage());
        }

        try {
            serialPort.setSerialPortParams(rate,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            serialPort.setFlowControlMode(
                    SerialPort.FLOWCONTROL_NONE);

        } catch (UnsupportedCommOperationException e) {
            throw new SerialBridgeException(e.getMessage());
        }

        try {
            outputStream = serialPort.getOutputStream();
            printWriter = new PrintWriter(outputStream);
            inputStream = serialPort.getInputStream();
        } catch (IOException e) {
            throw new SerialBridgeException(e.getMessage());
        }

        System.out.println("RXTX UART started");
    }

    @Override
    public void end() {
//        try {
//            jsscPort.closePort();
//        } catch (SerialPortException e) {
//            e.printStackTrace();
//        }
    }

    private final byte[] headerBytes = new byte[4];

    @Override
    public byte[] readLine() throws SerialBridgeException {
        int length = 0;
        try {

            while (length != 4) {
                length+= inputStream.read(headerBytes, length, 4 - length);
            }

//        System.out.print("Got Header:");
//        for (byte b:temp){
//            System.out.print(0xff&b);
//            System.out.print(",");
//        }
            if (headerBytes[2] != SYNCBLOCK || headerBytes[3] != SYNCBLOCK) {
                System.out.print("Read bad length header:");
                for (byte b : headerBytes) {
                    System.out.print(0xff & b);
                    System.out.print(",");
                }
                System.out.println(" - Syncing....");
                while (headerBytes[2] != SYNCBLOCK || headerBytes[3] != SYNCBLOCK) {
                    headerBytes[0] = headerBytes[1];
                    headerBytes[1] = headerBytes[2];
                    headerBytes[2] = headerBytes[3];
                    inputStream.read(headerBytes,3,1);
                }
            }

            length = SerialConversionUtil.getU16Int(headerBytes, 0);
            if (length > 255) {
                System.out.println("Warning Length:" + length);
            }
            byte[] temp = new byte[length];
            int offset = 0;
            while (offset != length) {
                offset+= inputStream.read(temp, offset, length - offset);
            }

//            System.out.print("Data len(" + length + ")[");
//            for (byte b:temp){
//                System.out.print(0xff&b);
//                System.out.print(",");
//            } System.out.println("]");

            return temp;

        } catch (IOException e) {
            e.printStackTrace();
            throw new SerialBridgeException(e.getMessage());
        }

    }

    @Override
    synchronized public void write(String text) throws SerialBridgeException {
            System.out.println("CMD>>" + text + "<<");
            printWriter.write(text);
            printWriter.flush();
    }

    @Override
    public void setWriteResponseTimeout(long timemeout) {
        this.timeout = timemeout;
    }


}
