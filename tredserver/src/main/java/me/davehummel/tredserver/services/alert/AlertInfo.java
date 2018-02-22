package me.davehummel.tredserver.services.alert;


import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.Entity;
import java.util.Date;

/**
 * Created by dmhum_000 on 7/2/2017.
 */
@Entity
public class AlertInfo {

    private Date alertTime;

    private String message;

    private String alert;

    private boolean isActive;

    protected AlertInfo(){

    }

    public AlertInfo(Date alertTime, String message, String alert, boolean isActive) {
        this.alertTime = alertTime;
        this.message = message;
        this.alert = alert;
        this.isActive = isActive;
    }

    public Date getAlertTime() {
        return alertTime;
    }

    public void setAlertTime(Date alertTime) {
        this.alertTime = alertTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAlert() {
        return alert;
    }

    public void setAlert(String alert) {
        this.alert = alert;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
