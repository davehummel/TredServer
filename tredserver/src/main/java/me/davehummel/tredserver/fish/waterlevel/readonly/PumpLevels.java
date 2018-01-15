package me.davehummel.tredserver.fish.waterlevel.readonly;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.ResourceSupport;

import java.util.Arrays;
import java.util.Date;

/**
 * Created by dmhum_000 on 1/25/2017.
 */

public class PumpLevels extends ResourceSupport {

    public PumpLevels(float levels[], float powers[], float powerMods[], float depth, float depthFiveMin, int topOffCount, String heads[], Date headDates[], int[] connectionLoss) {
        this.levels = levels;
        this.powers = powers;
        this.powerMods = powerMods;
        this.depth = depth;
        this.depthFiveMin = depthFiveMin;
        this.heads = heads;
        this.headDates = headDates;
        this.topOffCount = topOffCount;
        this.connectionLoss = connectionLoss;
    }

    private float levels[];
    private float powers[];

    private int topOffCount;
    private float powerMods[];

    private String heads[];
    private Date headDates[];

    private float depth;
    private float depthFiveMin;


    private int[] connectionLoss;

    @Override
    public String toString() {
        return "PumpLevels{" +
                "levels=" + Arrays.toString(levels) +
                ", powers=" + Arrays.toString(powers) +
                ", powerMods=" + Arrays.toString(powerMods) +
                ", heads=" + Arrays.toString(heads) +
                ", topOffCount=" + topOffCount +
                ", headDates=" + Arrays.toString(headDates) +
                ", depth=" + depth +
                ", depthFiveMin=" + depthFiveMin +
                ", connectionLoss=" + connectionLoss +
                '}';
    }

    public float[] getPowerMods() {
        return powerMods;
    }

    public void setPowerMods(float[] powerMods) {
        this.powerMods = powerMods;
    }

    public float[] getLevels() {
        return levels;
    }

    public void setLevels(float[] levels) {
        this.levels = levels;
    }

    public float[] getPowers() {
        return powers;
    }

    public void setPowers(float[] powers) {
        this.powers = powers;
    }

    public float getDepth() {
        return depth;
    }

    public void setDepth(float depth) {
        this.depth = depth;
    }

    public float getDepthFiveMin() {
        return depthFiveMin;
    }

    public void setDepthFiveMin(float depthFiveMin) {
        this.depthFiveMin = depthFiveMin;
    }

    public String[] getHeads() {
        return heads;
    }

    public void setHeads(String[] heads) {
        this.heads = heads;
    }

    public Date[] getHeadDates() {
        return headDates;
    }

    public void setHeadDates(Date[] headDates) {
        this.headDates = headDates;
    }


    public int getTopOffCount() {
        return topOffCount;
    }

    public void setTopOffCount(int topOffCount) {
        this.topOffCount = topOffCount;
    }

    public int[] getConnectionLoss() {
        return connectionLoss;
    }

    public void setConnectionLoss(int[] connectionLoss) {
        this.connectionLoss = connectionLoss;
    }



}
