package me.davehummel.tredserver.services;

import me.davehummel.tredserver.command.*;
import me.davehummel.tredserver.serial.StandardLine;
import me.davehummel.tredserver.serial.TimeLine;
import me.davehummel.tredserver.services.alert.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by dmhum_000 on 9/20/2015.
 */

@Component
public class InitService extends CommandService {

    private static final int INITID = 1;
    private static final int TIMEID = 2;

    private static final ImmediateInstruction startPing = new ImmediateInstruction('Z' , TIMEID, new CmdBody("TICK"));

    private final List<CommandListener> listeners = new ArrayList<>();


    private long lastDatePing;

    @Autowired
    private AlertService alertService;
    @Autowired
    private SMSSender smsSender;


    public InitService() {


        listeners.add(new CommandListener() {

            @Override
            public boolean matches(StandardLine line) {
                if (line.getClass() == TimeLine.class){
                    return true;
                }
                return (line.module == 'Z' && line.instructionID == INITID) ;
            }


            @Override
            protected void processData(StandardLine line) {
                if (line.getClass() == TimeLine.class){
                    lastDatePing = System.currentTimeMillis();
                    return;
                }
                Runnable task = () -> {
                    triggerEmbeddedRestart();
                };
                (new Thread(task)).start();
            }
        });
    }



    @Override
    public void restartEmbedded() {
        bridge.writeInstruction(startPing);
    }

    @Override
    public List<CommandListener> getListeners() {
        return listeners;
    }


    public void start() {
        System.out.println("Embedded Init Service Started!");
        if (bridge.isSimulation())
            bridge.writeInstruction(new ImmediateInstruction(
                    'Z',INITID,new ReadBody(DataType.BYTE,"ZZZ")));

        Alert alert = new Alert(){

            private AlertStatus status = AlertStatus.Safe;


            @Override
            public Trigger getTimingTrigger() {
                return new PeriodicTrigger(1, TimeUnit.SECONDS);
            }

            @Override
            public void validate() {
                long delta = System.currentTimeMillis() - lastDatePing;
                if ( delta > 30000){
                    status = AlertStatus.Alerting;
                }else if (delta > 20000){
                    status = AlertStatus.Critical;
                } else {
                    status = AlertStatus.Safe;
                }
            }

            @Override
            public String getDescription() {
                return "The embedded cpu is responding on a regular basis";
            }

            @Override
            public String getName() {
                return "Embedded Ping";
            }

            @Override
            public AlertStatus getStatus() {
                return status;
            }

            @Override
            public String getStatusDetails() {
                return "Time from last ping = " +((System.currentTimeMillis() - lastDatePing)/1000l) ;
            }

            @Override
            public int getMinimumNotifyDurationMS() {
                return 100000;
            }

            @Override
            public List<NotifyAction> getNotifyActions() {
                List<NotifyAction> list = new ArrayList<>();
                list.add(new NotifyAction() {
                    @Override
                    public void alert(Alert parent) {
                        System.out.println("Embedded system is down!");
                        smsSender.sendSMS("Embedded system is down!");
                    }

                    @Override
                    public void endAlert(Alert parent) {
                        System.out.println("Embedded system is back!");
                        smsSender.sendSMS("Embedded system is back!");
                    }

                    @Override
                    public void critical(Alert parent) {
                        System.out.println("Restarting Service");
                        InitService.this.triggerEmbeddedRestart();
                        smsSender.sendSMS("Attempted embedded restart...");
                    }
                });

                return list;
            }

        };

        lastDatePing = System.currentTimeMillis();
       alertService.loadAlert(alert);

    }



    public void stop() {

    }

}
