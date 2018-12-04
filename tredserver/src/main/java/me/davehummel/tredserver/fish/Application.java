package me.davehummel.tredserver.fish;

import me.davehummel.tredserver.fish.services.EnvSensorService;
import me.davehummel.tredserver.fish.services.PumpLevelService;
import me.davehummel.tredserver.fish.services.TempService;
import me.davehummel.tredserver.gpio.TurbotGpio;
import me.davehummel.tredserver.serial.NullSerialBridge;
import me.davehummel.tredserver.serial.SerialBridge;
import me.davehummel.tredserver.serial.jsscserial.JsscSerialBridge;
import me.davehummel.tredserver.serial.jsscserial.RXTXSerialBridge;
import me.davehummel.tredserver.services.CommandBridge;
import me.davehummel.tredserver.services.InitService;
import me.davehummel.tredserver.services.ReadService;
import me.davehummel.tredserver.services.ServiceManager;
import me.davehummel.tredserver.services.alert.AlertService;
import me.davehummel.tredserver.services.alert.SMSSender;
import me.davehummel.tredserver.fish.history.HistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.io.IOException;

@SpringBootApplication(scanBasePackages={"me.davehummel.tredserver.services"})
//@Configuration
@ComponentScan("me.davehummel.tredserver.services")
@ComponentScan("me.davehummel.tredserver.fish")
@ComponentScan("me.davehummel.tredserver.fish.history")
@ComponentScan("me.davehummel.tredserver.fish.waterlevel.persisted")
@ComponentScan("me.davehummel.tredserver.fish.lighting.persisted")

public class Application {

    Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ServiceManager serialServiceManager, InitService initService, ReadService readService, PumpLevelService pumpLevelService, TempService tempService, EnvSensorService envSensorService, HistoryService historyService) {
        return args -> {
            try {
                TurbotGpio.setPinValue(483, false);

                Thread.sleep(500);

                TurbotGpio.setPinValue(483, true);

                Thread.sleep(500);
            } catch (IOException e){
                logger.error("Failed to interact with GPIO",e);
            }

            logger.info("Setting up ports:");


            //String portName = "/dev/ttyS4"; // This is the uart 1 on the Turbot gpio
            String portName = "/dev/ttyACM0"; // This is the uart over usb port
            int portSpeed = 1000000;
            if (args.length > 0) {
                logger.info(args[0]);
                String[] split = args[0].split("_");
                portName = split[0];
                portSpeed = Integer.parseInt(split[1]);
            }
            logger.info("Serial using :" + portName + "_" + portSpeed);

            SerialBridge sbridge = null;

            try{
                sbridge = new RXTXSerialBridge(portName, portSpeed);
                sbridge.start();
                SMSSender.arm();
            } catch (Exception e){
                logger.error(e.getMessage());
                try{
                    SMSSender.disarm();
                    historyService.disarm();
                    logger.error("Trying windows port");
                    sbridge = new RXTXSerialBridge("COM12", portSpeed);
                    sbridge.start();
                    sbridge.setSimulation(true);
                } catch (Exception ex){
                    logger.error("Failed to setup windows port, using null bridge.",ex);
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

            serialServiceManager.start();



        };
    }

}