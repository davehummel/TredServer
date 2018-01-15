package me.davehummel.tredserver.fish.lighting.persisted.lighttiming;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by dmhum_000 onTime 5/6/2017.
 */

@Entity
public class LightTiming {

    @Id
    @Column(name = "name", unique = true)
    private String name;

    @Column(name = "onTime")
    private String onTime;

    @Column(name = "offTime")
    private String offTime;

    public LightTiming(){}

    public LightTiming(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOnTime() {
        return onTime;
    }

    public void setOnTime(String onTime) {
        this.onTime = onTime;
    }

    public String getOffTime() {
        return offTime;
    }

    public void setOffTime(String offTime) {
        this.offTime = offTime;
    }
}
