package me.davehummel.tredserver;

import me.davehummel.tredserver.command.*;
import me.davehummel.tredserver.serial.jsscserial.JsscSerialBridge;
import me.davehummel.tredserver.services.CommandBridge;
import me.davehummel.tredserver.services.ServiceManager;
import me.davehummel.tredserver.services.heading.HeadingService;
import me.davehummel.tredserver.services.lidar.LidarService;


/**
 * Created by dmhum_000 on 9/13/2015.
 */
public class LidarTest {

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

        LidarService lidarService = new LidarService();

        manager.addService(lidarService);

        manager.start();

        lidarService.scan(20000,4,250,400,850,550,70);
  //      lidarService.scan(2000,100,512,512,512,512,1);

        try {
            Thread.sleep(500000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }




    }
}
