package me.davehummel.tredserver;

import me.davehummel.tredserver.serial.SerialBridgeException;
import me.davehummel.tredserver.serial.jsscserial.JsscSerialBridge;

/**
 * Created by dmhum_000 on 4/25/2016.
 */
public class Test {
    final static private   JsscSerialBridge bridge = new JsscSerialBridge("COM3",3000000);
    static private Thread commandMonitor;
    static private Runnable runner = () -> {
     for (int i = 0 ; i < 10 ; i ++ ) {
         try {
             System.out.println(bridge.readLine());
         } catch (Throwable t) {
             t.printStackTrace();
             try {
                 Thread.sleep(100);
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         }
     }
    };

    public static void main1(String[] args){

        try {
            bridge.start();
        } catch (SerialBridgeException e) {
            e.printStackTrace();
            return;
        }
        commandMonitor = new Thread(runner,"CommandMonitor");
        commandMonitor.setDaemon(true);

        commandMonitor.start();
        for (int i = 0 ; i < 10 ; i ++){
            try {
                StringBuilder builder = new StringBuilder("IW 100 C U PAN 200");
//                (new ImmediateInstruction('C',100,
//                        new WriteBody(DataType.UINT_16,"PAN",(Integer)(i*100+200)))).toString(builder);
                builder.append('\n');
                 System.out.println(">>"+builder.toString());
                bridge.write("IW 100 C U FFF \n");//builder.toString());
            } catch (SerialBridgeException e) {
                e.printStackTrace();
                return ;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
