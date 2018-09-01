package me.davehummel.tredserver.services;


import me.davehummel.tredserver.gpio.TurbotGpio;
import me.davehummel.tredserver.serial.SerialBridgeException;
import me.davehummel.tredserver.services.alert.AlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * Created by dmhum_000 on 9/22/2015.
 */

@Service
public class ServiceManager {

    private CommandBridge bridge;
    private final Timer timer = new Timer(true);
    private final List<CommandService> services = new ArrayList<>();

    Logger logger = LoggerFactory.getLogger(ServiceManager.class);

    @Autowired
    private AlertService alertService;

    private SendResponse requiredResponse;

    public ServiceManager() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting  Down Services.");
            stop();
        }, "Shutdown-thread"));

    }

    public ServiceManager(CommandBridge commandBridge) {
        super();
        bridge = commandBridge;
    }

    public void addService(CommandService service) {
        service.setup(timer, bridge);
        service.setManager(this);

        service.getListeners().forEach(bridge::addCommandListener);
        services.add(service);
    }

    public void start() throws SerialBridgeException {
        bridge.start();
        services.forEach(CommandService::start);
    }

    public void detectedEmbeddedRestart() {
        logger.info("Embedded Restart Detected!");

        for (CommandService service : services) {
            try {
                bridge.togglePureInput();
                service.restartEmbedded();
                try { // Give time for the embedded device to parse all input
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    logger.error("Interrupted",e);
                }
            } finally {
                bridge.togglePureInput();
            }
        }

    }

    public void stop() {
        if (bridge != null) {
            //    bridge.writeKill(0);
            alertService.clearAlerts();
            services.forEach(CommandService::stop);
            bridge.stop();
        }
    }

    public CommandBridge getBridge() {
        return bridge;
    }

    public void setBridge(CommandBridge bridge) {
        this.bridge = bridge;
    }


    public void demandResponse(SendResponse response) {
        this.bridge.demandResponse(response);
    }

    public void forceEmbeddedRestart() {
        try {
            stop();
            Thread.currentThread().sleep(500);
            TurbotGpio.setPinValue(483,false);
            Thread.currentThread().sleep(2000);
            TurbotGpio.setPinValue(483,true);
            Thread.currentThread().sleep(500);
            start();
        } catch (InterruptedException e) {
            logger.error("Interrupted",e);
        } catch (SerialBridgeException e) {
            logger.error("Failed to stop or start",e);
        } catch (IOException e){
            logger.error("Failed to switch power control pin",e);
        }

    }
}
