package me.davehummel.tredserver.fish.environment.readonly;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.ResourceSupport;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EnvironmentReadings extends ResourceSupport {

    private  double dissolvedO;
    private  double salinity;
    private  double orp;
    private  double ph;
    private int loss;

    @JsonCreator
    public EnvironmentReadings(@JsonProperty("ph") double ph, @JsonProperty("orp") double orp, @JsonProperty("salinity") double salinity, @JsonProperty("do") double disolvedO, @JsonProperty("loss") int loss) {
        this.ph = ph;
        this.orp = orp;
        this.salinity = salinity;
        this.dissolvedO = disolvedO;
        this.loss = loss;
    }

    public double getDissolvedO() {
        return dissolvedO;
    }

    public double getSalinity() {
        return salinity;
    }

    public double getOrp() {
        return orp;
    }

    public double getPh() {
        return ph;
    }

    public void setDissolvedO(double dissolvedO) {
        this.dissolvedO = dissolvedO;
    }

    public void setSalinity(double salinity) {
        this.salinity = salinity;
    }

    public void setOrp(double orp) {
        this.orp = orp;
    }

    public void setPh(double ph) {
        this.ph = ph;
    }

    public int getLoss() {
        return loss;
    }

    public void setLoss(int loss) {
        this.loss = loss;
    }
}
