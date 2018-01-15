package me.davehummel.tredserver.services;


import org.springframework.stereotype.Service;


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
    private SendResponse requiredResponse;

    public ServiceManager() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting  Down Services.");
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

    public void start() {
        bridge.start();
        services.forEach(CommandService::start);
    }

    public void detectedEmbeddedRestart() {
        System.out.println("Embedded Restart Detected!");


        for (CommandService service : services) {
            try {
                bridge.togglePureInput();
                service.restartEmbedded();
                try { // Give time for the embedded device to parse all input
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } finally {
                bridge.togglePureInput();
            }
        }

    }

    public void stop() {
        if (bridge != null) {
            //    bridge.writeKill(0);
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
}
