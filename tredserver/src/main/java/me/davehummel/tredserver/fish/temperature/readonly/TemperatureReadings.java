package me.davehummel.tredserver.fish.temperature.readonly;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.ResourceSupport;

import java.util.Arrays;

/**
 * Created by dmhum_000 on 1/25/2017.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class TemperatureReadings extends ResourceSupport {

    @JsonCreator
    public TemperatureReadings(@JsonProperty("topTemp") float topTemp, @JsonProperty("bottomTemp") float bottomTemp, @JsonProperty("outTemp") float outTemp,@JsonProperty("topOneHour") float topOneHour, @JsonProperty("bottomOneHour") float bottomOneHour,@JsonProperty("outOneHour") float outOneHour) {
        this.topTemp = topTemp;
        this.bottomTemp = bottomTemp;
        this.outTemp = outTemp;
        this.topOneHour = topOneHour;
        this.bottomOneHour = bottomOneHour;
        this.outOneHour = outOneHour;
    }

    private float topTemp;
    private float bottomTemp;
    private float topOneHour;
    private float bottomOneHour;
    private float outTemp;
    private float outOneHour;


    public float getTopTemp() {
        return topTemp;
    }

    public void setTopTemp(float topTemp) {
        this.topTemp = topTemp;
    }

    public float getBottomTemp() {
        return bottomTemp;
    }

    public void setBottomTemp(float bottomTemp) {
        this.bottomTemp = bottomTemp;
    }

    public float getTopOneHour() {
        return topOneHour;
    }

    public void setTopOneHour(float topOneHour) {
        this.topOneHour = topOneHour;
    }

    public float getBottomOneHour() {
        return bottomOneHour;
    }

    public void setBottomOneHour(float bottomOneHour) {
        this.bottomOneHour = bottomOneHour;
    }

    public float getOutTemp() {
        return outTemp;
    }

    public void setOutTemp(float outTemp) {
        this.outTemp = outTemp;
    }

    public float getOutOneHour() {
        return outOneHour;
    }

    public void setOutOneHour(float outOneHour) {
        this.outOneHour = outOneHour;
    }

}
