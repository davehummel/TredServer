package me.davehummel.tredserver.services.motor;

import me.davehummel.tredserver.command.*;
import me.davehummel.tredserver.serial.StandardLine;
import me.davehummel.tredserver.services.CommandListener;
import me.davehummel.tredserver.services.CommandService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmhum_000 on 10/3/2015.
 */
public class MotorService extends CommandService {

    private static final int ERRORPINGID = 100;

    private static final ScheduledInstruction PINGER = new ScheduledInstruction('M',ERRORPINGID,1000,1000,0,new ReadBody(DataType.BYTE,"EEE"));

    private long errorTime = 0;
    private MotorError error = new MotorError();

    private final List<CommandListener> listeners = new ArrayList<>();

    public MotorService(){
        // Error listener
        listeners.add(new CommandListener() {
            @Override
            public boolean matches(StandardLine line) {
                return  (line.module == 'M' && line.instructionID == ERRORPINGID);
            }

            @Override
            protected void processData(StandardLine line) {
                error.update(line.raw[7]);
            }
        });

    }

    @Override
    public List<CommandListener> getListeners() {
        return listeners;
    }

    public void start() {
        bridge.writeInstruction(PINGER);

    }

    public void stop() {
        bridge.writeKill(ERRORPINGID);
    }

    public void clearError(){
        error.update(0);
    }


}
