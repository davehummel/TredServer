package me.davehummel.tredserver.services.alert;

import org.springframework.scheduling.Trigger;

import java.util.List;

/**
 * Created by dmhum_000 on 4/2/2017.
 */
public interface  Alert {

    Trigger getTimingTrigger();

    void validate();

    String getDescription();

    String getName();

    AlertStatus getStatus();

    String getStatusDetails();

    int getMinimumNotifyDurationMS();

    List<NotifyAction> getNotifyActions();
}
