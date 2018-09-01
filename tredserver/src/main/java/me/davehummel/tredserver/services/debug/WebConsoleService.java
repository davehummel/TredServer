package me.davehummel.tredserver.services.debug;

import me.davehummel.tredserver.services.CommandListener;
import me.davehummel.tredserver.services.CommandService;
import me.davehummel.tredserver.serial.StandardLine;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmhum_000 on 3/22/2016.
 */
public class WebConsoleService extends CommandService {

    private final List<CommandListener> listeners = new ArrayList<>();

    public WebConsoleService(){
            // Error listener
            listeners.add(new CommandListener() {
                @Override
                public boolean matches(StandardLine line) {
                    return true;
                }


                @Override
                protected void processData(StandardLine line){

                }
            });


    }

    @Override
    public List<CommandListener> getListeners() {
        return listeners;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }


}
