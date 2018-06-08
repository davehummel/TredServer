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
    private  double pressure;
    private  double humidity;
    private  double co2;
    private  double tvoc;
    private double temperature;
    private int loss;

    @JsonCreator
    public EnvironmentReadings(@JsonProperty("ph") double ph, @JsonProperty("orp") double orp, @JsonProperty("salinity") double salinity, @JsonProperty("do") double disolvedO,@JsonProperty("co2") double co2, @JsonProperty("tvoc") double tvoc, @JsonProperty("pressure") double pressure, @JsonProperty("humidity") double humidity,@JsonProperty("temperature") double temperature, @JsonProperty("loss") int loss) {
        this.ph = ph;
        this.orp = orp;
        this.salinity = salinity;
        this.dissolvedO = disolvedO;
        this.pressure = pressure;
        this.humidity = humidity;
        this.co2 = co2;
        this.tvoc = tvoc;
        this.temperature = temperature;
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

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getCo2() {
        return co2;
    }

    public void setCo2(double co2) {
        this.co2 = co2;
    }

    public double getTvoc() {
        return tvoc;
    }

    public void setTvoc(double tvoc) {
        this.tvoc = tvoc;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
}
