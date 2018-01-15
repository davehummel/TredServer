package me.davehummel.tredserver;


import me.davehummel.tredserver.serial.jsscserial.JsscSerialBridge;
import me.davehummel.tredserver.services.CommandBridge;
import me.davehummel.tredserver.services.ServiceManager;
import me.davehummel.tredserver.web.WebSocketServer;

public class WebConsole
{


    public static void main1( String[] args )
    {
        ServiceManager serviceManager = new ServiceManager(new CommandBridge(new JsscSerialBridge("COM6",3500000)));

//        HeadingService headingService = new HeadingService();
//
//        serviceManager.addService(headingService);
////
////        MotorService motorService = new MotorService();
////
////        serviceManager.addService(motorService);
//        try {
//        serviceManager.start();
//        Thread.sleep(1000);
//        headingService.start();
// //       motorService.start();
//
//
//
//            Thread.sleep(100000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//
        WebSocketServer server = new WebSocketServer();

        Runnable launchServer = () -> {
            server.start();
        };
        new Thread(launchServer).start();
//
//        final LineReceiver lr = session.getLineReceiver();
//        LineReceiver.LineReceiverListener lrl = new LineReceiver.LineReceiverListener() {
//            public void onLine(String line) {
//                System.err.println(">"+line);
//                server.broadcast(line);
//            }
//        };
//        lr.setListener(lrl);
//
//        WebSocketServerListener wssl = text -> {System.err.println("<"+text);session.writeLine(text+'\n');};
//
//        server.addListener(wssl);

//        WebConsoleService wcs = new WebConsoleService();
//
//        serviceManager.addService(wcs);
    }
}
