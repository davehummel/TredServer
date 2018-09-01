package me.davehummel.tredserver.fish.services;

import me.davehummel.tredserver.command.*;
import me.davehummel.tredserver.fish.history.HistorySeries;
import me.davehummel.tredserver.fish.lighting.persisted.lighttiming.LightTiming;
import me.davehummel.tredserver.fish.lighting.persisted.lighttiming.LightTimingRepository;
import me.davehummel.tredserver.services.CommandListener;
import me.davehummel.tredserver.services.CommandService;
import me.davehummel.tredserver.services.alert.Alert;
import me.davehummel.tredserver.services.alert.AlertService;
import me.davehummel.tredserver.services.alert.AlertStatus;
import me.davehummel.tredserver.services.alert.NotifyAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by dmhum_000 on 9/20/2015.
 */

@Controller
public class LightService extends CommandService {

    private static final int LIGHTINSTRUCTIONID = 83563;
    private static final String DAILY = "daily";


    private final ImmediateInstruction[] setupPWM = new ImmediateInstruction[]{

            new ImmediateInstruction('P',LIGHTINSTRUCTIONID, new CmdBody("DIN G 23")),
            new ImmediateInstruction('P',LIGHTINSTRUCTIONID, new CmdBody("DIN H 22")),

    };

    private final ImmediateInstruction lightOn = new ImmediateInstruction('P' , LIGHTINSTRUCTIONID , new WriteBody(DataType.BYTE,"GGG",1));
    private final ImmediateInstruction lightOff = new ImmediateInstruction('P' , LIGHTINSTRUCTIONID , new WriteBody(DataType.BYTE,"HHH",1));
    private final ScheduledInstruction lightOnEnd = new ScheduledInstruction('P' , LIGHTINSTRUCTIONID , 250,0,1, new WriteBody(DataType.BYTE,"GGG",0));
    private final ScheduledInstruction lightOffEnd = new ScheduledInstruction('P' , LIGHTINSTRUCTIONID , 250, 0 , 1 ,new WriteBody(DataType.BYTE,"HHH",0));

    Logger logger = LoggerFactory.getLogger(LightService.class);


    @Autowired
    LightTimingRepository lightTimingRepository;
    @Autowired
    private AlertService alertService;


    public LightService() {


    }


    @Override
    public void restartEmbedded() {
        for (ImmediateInstruction instruction:this.setupPWM){
            bridge.writeInstruction(instruction);
        }


        logger.info("Light Service Restarted!");
    }

    @Override
    public List<CommandListener> getListeners() {
        return Collections.emptyList();
    }


    public void start() {


        Alert alert = new Alert() {
            @Override
            public Trigger getTimingTrigger() {
                return new PeriodicTrigger(1, TimeUnit.MINUTES);
            }

            @Override
            public void validate() {
                LightTiming timing;

                timing = lightTimingRepository.findOne(DAILY);
                if (timing == null || !StringUtils.hasText(timing.getOnTime()) || !StringUtils.hasText(timing.getOffTime())) {
                    timing = new LightTiming(DAILY);
                    timing.setOnTime("6:30");
                    timing.setOffTime("20:00");
                    lightTimingRepository.save(timing);
                }

                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int minute = cal.get(Calendar.MINUTE);
                String parts[] = timing.getOnTime().split(":");
                int startHour = Integer.parseInt(parts[0]);
                int startMin = Integer.parseInt(parts[1]);

                parts = timing.getOffTime().split(":");

                int endHour = Integer.parseInt(parts[0]);
                int endMinute = Integer.parseInt(parts[1]);

                if (hour<startHour){
                    lightOff();
                    return;
                }

                if (hour==startHour){
                    if (minute<startMin) {
                        lightOff();
                        return;
                    }
                }

                if (hour>endHour){
                    lightOff();
                    return;
                }
                if (hour == endHour){
                    if (minute > endMinute){
                        lightOff();
                        return;
                    }
                }
                lightOn();
            }

            @Override
            public String getDescription() {
                return "";
            }

            @Override
            public String getName() {
                return "";
            }

            @Override
            public AlertStatus getStatus() {
                return AlertStatus.Safe;
            }

            @Override
            public String getStatusDetails() {
                return "";
            }

            @Override
            public int getMinimumNotifyDurationMS() {
                return 1;
            }

            @Override
            public List<NotifyAction> getNotifyActions() {
                return null;
            }
        };

        alertService.loadAlert(alert);
    }


    public void stop() {

    }

    public void lightOn(){
        bridge.writeInstruction(lightOn);
        bridge.writeInstruction(lightOnEnd);
    }

    public void lightOff(){
        bridge.writeInstruction(lightOff);
        bridge.writeInstruction(lightOffEnd);
    }
}
