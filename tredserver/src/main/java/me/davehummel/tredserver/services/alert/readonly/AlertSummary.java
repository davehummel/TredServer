package me.davehummel.tredserver.services.alert.readonly;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.ResourceSupport;

import java.util.Date;

/**
 * Created by dmhum_000 on 4/14/2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlertSummary  extends ResourceSupport {

    @JsonCreator
    public AlertSummary (@JsonProperty("currentAlert") String currentAlert, @JsonProperty("lastAlert") String lastAlert, @JsonProperty("lastAlertTime") Date lastAlertTime){

    }
}
