package me.davehummel.tredserver.services.alert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by dmhum_000 on 4/2/2017.
 */

@Service
public class AlertService {
    @Autowired
    ThreadPoolTaskScheduler taskScheduler;

    List<AlertAction> alerts = new ArrayList<>();

    public void loadAlert(Alert alert){
        AlertAction action = new AlertAction(alert);
        Trigger trigger = alert.getTimingTrigger();

        taskScheduler.schedule(action,trigger);
        alerts.add(action);
    }


    public List<AlertInfo> getAlerts(long timeout) {
        List <AlertInfo> relevant = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        for (AlertAction action:alerts){
            if (action.isTriggered() || action.getLastTriggerTime() + timeout >= currentTime)
                relevant.add(new AlertInfo(new Date(action.getLastTriggerTime()),action.getParent().getStatusDetails(),action.getParent().getDescription(),action.isTriggered()));
        }
        return relevant;
    }

    public void clearAlerts() {
        alerts.clear();
        taskScheduler.shutdown();
        taskScheduler.initialize();

    }
}
