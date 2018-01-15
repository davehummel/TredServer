package me.davehummel.tredserver.services.alert.readonly;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import me.davehummel.tredserver.services.alert.AlertStatus;
import org.springframework.hateoas.ResourceSupport;

import java.util.Date;

/**
 * Created by dmhum_000 on 4/14/2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlertReadings extends ResourceSupport {

    @JsonCreator
    public AlertReadings(@JsonProperty("lastAlertDates") Date[] lastAlertDates,@JsonProperty("lastCriticalDates") Date[] lastCriticalDates,
                         @JsonProperty("lastOkDates") Date[] lastOkDates, @JsonProperty("alertStatuses")  AlertStatus[] alertStatuses,
                         @JsonProperty("alertNames")  String[] alertNames,@JsonProperty("alertDescriptions")  String[] alertDescriptions,
                         @JsonProperty("alertDetails") String[] alertDetails) {
        this.lastAlertDates = lastAlertDates;
        this.lastCriticalDates = lastCriticalDates;
        this.lastOkDates = lastOkDates;
        this.alertStatuses = alertStatuses;
        this.alertNames = alertNames;
        this.alertDescriptions = alertDescriptions;
        this.alertDetails = alertDetails;
    }

    private Date lastAlertDates[];
    private Date lastCriticalDates[];
    private Date lastOkDates[];
    private AlertStatus alertStatuses[];

    private String alertNames[];
    private String alertDescriptions[];
    private String alertDetails[];

    public Date[] getLastAlertDates() {
        return lastAlertDates;
    }

    public void setLastAlertDates(Date[] lastAlertDates) {
        this.lastAlertDates = lastAlertDates;
    }

    public Date[] getLastCriticalDates() {
        return lastCriticalDates;
    }

    public void setLastCriticalDates(Date[] lastCriticalDates) {
        this.lastCriticalDates = lastCriticalDates;
    }

    public Date[] getLastOkDates() {
        return lastOkDates;
    }

    public void setLastOkDates(Date[] lastOkDates) {
        this.lastOkDates = lastOkDates;
    }

    public AlertStatus[] getAlertStatuses() {
        return alertStatuses;
    }

    public void setAlertStatuses(AlertStatus[] alertStatuses) {
        this.alertStatuses = alertStatuses;
    }

    public String[] getAlertNames() {
        return alertNames;
    }

    public void setAlertNames(String[] alertNames) {
        this.alertNames = alertNames;
    }

    public String[] getAlertDescriptions() {
        return alertDescriptions;
    }

    public void setAlertDescriptions(String[] alertDescriptions) {
        this.alertDescriptions = alertDescriptions;
    }

    public String[] getAlertDetails() {
        return alertDetails;
    }

    public void setAlertDetails(String[] alertDetails) {
        this.alertDetails = alertDetails;
    }
}
