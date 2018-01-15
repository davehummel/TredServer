package me.davehummel.tredserver;

import me.davehummel.tredserver.command.*;
import me.davehummel.tredserver.serial.SerialBridgeException;
import me.davehummel.tredserver.serial.StandardLine;
import me.davehummel.tredserver.serial.jsscserial.JsscSerialBridge;



/**
 * Created by dmhum_000 on 9/13/2015.
 */
public class BridgeTest {

    private static final ImmediateInstruction READ = new ImmediateInstruction('V',101,
            new ReadBody(DataType.UINT_16,"CUR"));

    static public void main (String[] args){
        String portName = "/dev/ttyMFD1";
        int portSpeed = 3500000;
        if (args.length>0) {
            System.out.println(args[0]);
            String[] split = args[0].split("_");
            portName = split[0];
            portSpeed = Integer.parseInt(split[1]);
        }
            System.out.println("Serial using :"+portName+"_"+portSpeed);


        JsscSerialBridge p = new JsscSerialBridge(portName, portSpeed);
        try {
            p.start();
        } catch (SerialBridgeException e) {
            e.printStackTrace();
        }
        //  CommandBridge bridge = new CommandBridge(new JNISerialBridge());
      //  CommandBridge bridge = new CommandBridge(new MraaSerialBridge(0,3500000));
//        CommandBridge bridge = new CommandBridge(new JsscSerialBridge(/*"COM3"*/portName,portSpeed));
//        CommandListener listener = new CommandListener() {
//            @Override
//            public boolean matches(StandardLine line) {
//                //System.out.println(line.toString());
//                bridge.writeInstruction(READ);
//                return true;
//            }
//
//            @Override
//            protected void processData( StandardLine data) {
//            }
//        };
//        bridge.addCommandListener(listener);
//        bridge.start();
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        for (int i = 0 ; i < 10 ; i++){
//            bridge.writeInstruction(new ImmediateInstruction('P',100,
//                    new WriteBody(DataType.UINT_16,"PAN",(Integer)(i*100+200))));
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

    }
}
