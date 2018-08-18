package me.davehummel.tredserver.fish;

import me.davehummel.tredserver.fish.services.EnvSensorService;
import me.davehummel.tredserver.fish.services.PumpLevelService;
import me.davehummel.tredserver.fish.services.TempService;
import me.davehummel.tredserver.gpio.TurbotGpio;
import me.davehummel.tredserver.serial.NullSerialBridge;
import me.davehummel.tredserver.serial.SerialBridge;
import me.davehummel.tredserver.serial.jsscserial.RXTXSerialBridge;
import me.davehummel.tredserver.services.CommandBridge;
import me.davehummel.tredserver.services.InitService;
import me.davehummel.tredserver.services.ReadService;
import me.davehummel.tredserver.services.ServiceManager;
import me.davehummel.tredserver.services.alert.AlertService;
import me.davehummel.tredserver.services.alert.SMSSender;
import me.davehummel.tredserver.fish.history.HistoryService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages={"me.davehummel.tredserver.services"})
//@Configuration
@ComponentScan("me.davehummel.tredserver.services")
@ComponentScan("me.davehummel.tredserver.fish")
@ComponentScan("me.davehummel.tredserver.fish.history")
@ComponentScan("me.davehummel.tredserver.fish.waterlevel.persisted")
@ComponentScan("me.davehummel.tredserver.fish.lighting.persisted")

public class Application {


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ServiceManager serialServiceManager, InitService initService, ReadService readService, PumpLevelService pumpLevelService, TempService tempService, EnvSensorService envSensorService) {
        return args -> {


            TurbotGpio.setPinValue(483,false);

            System.out.println("Setting up ports:");


            //String portName = "/dev/ttyS4"; // This is the uart 1 on the Turbot gpio
            String portName = "/dev/ttyACM0"; // This is the uart over usb port
            int portSpeed = 1000000;
            if (args.length > 0) {
                System.out.println(args[0]);
                String[] split = args[0].split("_");
                portName = split[0];
                portSpeed = Integer.parseInt(split[1]);
            }
            System.out.println("Serial using :" + portName + "_" + portSpeed);

            //  CommandBridge bridge = new CommandBridge(new MraaSerialBridge(0,3500000));
            SerialBridge sbridge = null;

            try{
                sbridge = new RXTXSerialBridge(portName, portSpeed);
                sbridge.start();
                SMSSender.arm();
            } catch (Exception e){
                e.printStackTrace();
                try{
                    SMSSender.disarm();
                    System.err.println("Trying windows port");
                    sbridge = new RXTXSerialBridge("COM6", portSpeed);
                    sbridge.start();
                    sbridge.setSimulation(true);
                } catch (Exception ex){
                    ex.printStackTrace();
                    sbridge = new NullSerialBridge();
                }
             }

            CommandBridge bridge = new CommandBridge(sbridge);

            serialServiceManager.setBridge(bridge);

            serialServiceManager.addService(readService);

            serialServiceManager.addService(initService);

            serialServiceManager.addService(tempService);

            serialServiceManager.addService(pumpLevelService);

            serialServiceManager.addService(envSensorService);

     //       serialServiceManager.addService(lightService);  // Light is now on a simple timer

            serialServiceManager.start();

            TurbotGpio.setPinValue(483,true);

        };
    }

}