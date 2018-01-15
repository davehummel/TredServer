package me.davehummel.tredserver.services;

import java.util.List;
import java.util.Timer;

/**
 * Created by dmhum_000 on 9/20/2015.
 */
public abstract class CommandService {

    protected Timer timer;
    protected CommandBridge bridge;
    private ServiceManager manager;

    abstract public List<CommandListener> getListeners();
    abstract public void start();
    abstract public void stop();
    public void restartEmbedded(){};
    protected void innerSetup(){};

    public void setup(Timer timer, CommandBridge bridge){
        this.timer = timer;
        this.bridge = bridge;
        innerSetup();
    }


    public void setManager(ServiceManager manager) {
        this.manager = manager;
    }

    protected void triggerEmbeddedRestart(){
        manager.detectedEmbeddedRestart();
    }
}
