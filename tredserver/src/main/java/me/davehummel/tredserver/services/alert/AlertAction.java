package me.davehummel.tredserver.services.alert;

import java.util.List;

/**
 * Created by dmhum_000 on 4/2/2017.
 */
public class AlertAction implements Runnable {

    public Alert getParent() {
        return parent;
    }

    private final Alert parent;

    public boolean isTriggered() {
        return isTriggered;
    }

    public long getLastTriggerTime() {
        return lastTriggerTime;
    }

    public boolean isCritical() {
        return isCritical;
    }

    public long getLastCriticalTime() {
        return lastCriticalTime;
    }

    private boolean isTriggered;

    private long lastTriggerTime = 0;

    private boolean isCritical;

    private long lastCriticalTime = 0;

    public AlertAction(Alert parent) {
        this.parent = parent;
    }

    @Override
    public void run() {
        parent.validate();
        long time = System.currentTimeMillis();

        if (parent.getStatus() == AlertStatus.Alerting) {
            if (isTriggered && time - lastTriggerTime < parent.getMinimumNotifyDurationMS()) {
                return;
            }
            // Controls on how often you notify are further controlled by the notify action
            List<NotifyAction> actionList = parent.getNotifyActions();
            for (NotifyAction action : actionList) {
                action.alert(parent);
            }
            isTriggered = true;
            lastTriggerTime = time;
        } else if (isTriggered == true) {
            isTriggered = false;
            List<NotifyAction> actionList = parent.getNotifyActions();
            for (NotifyAction action : actionList) {
                action.endAlert(parent);
            }
        }
        if (parent.getStatus() == AlertStatus.Critical) {
            if (isCritical && time - lastCriticalTime < parent.getMinimumNotifyDurationMS()) {
                return;
            }
            // Controls on how often you notify are further controlled by the notify action
            List<NotifyAction> actionList = parent.getNotifyActions();
            for (NotifyAction action : actionList) {
                action.critical(parent);
            }
            isCritical = true;
            lastCriticalTime = time;
        }

    }
}

