package me.davehummel.tredserver;

import me.davehummel.tredserver.command.*;
import me.davehummel.tredserver.serial.jsscserial.JsscSerialBridge;
import me.davehummel.tredserver.services.CommandBridge;
import me.davehummel.tredserver.services.ServiceManager;
import me.davehummel.tredserver.services.heading.HeadingService;


/**
 * Created by dmhum_000 on 9/13/2015.
 */
public class HeadingTest {

    private static final ImmediateInstruction READ = new ImmediateInstruction('V', 101,
            new ReadBody(DataType.UINT_16, "CUR"));

    static public void main1(String[] args) {
        String portName = "/dev/ttyMFD1";
        int portSpeed = 3500000;
        if (args.length > 0) {
            System.out.println(args[0]);
            String[] split = args[0].split("_");
            portName = split[0];
            portSpeed = Integer.parseInt(split[1]);
        }
        System.out.println("Serial using :" + portName + "_" + portSpeed);

        //  CommandBridge bridge = new CommandBridge(new MraaSerialBridge(0,3500000));
        CommandBridge bridge = new CommandBridge(new JsscSerialBridge(portName, portSpeed));

        ServiceManager manager = new ServiceManager(bridge);

        HeadingService headingService = HeadingService.INSTANCE;

        manager.addService(headingService);

        manager.start();

        for (int i = 0; i < 10; i++) {
            bridge.writeInstruction(new ImmediateInstruction('P', 100,
                    new WriteBody(DataType.UINT_16, "PAN", (Integer) (i * 100 + 200))));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
