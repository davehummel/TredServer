package me.davehummel.tredserver.fish.history;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HistorySeries {

    private static final int MS_IN_TEN_MIN = 1000*60*10;

    String name;

    List<Object[]> data;

    int resolution = 1;

    public HistorySeries() {
        data = new ArrayList<>();
    }
    public HistorySeries(String name, List<Object[]> data) {
        this.name = name;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Object[]> getData() {
        return data;
    }

    public void setData(List<Object[]> data) {
        this.data = data;
    }

    public void addSummedData(long date, double value){
        if (data.isEmpty()){
            data.add(new Object[]{date,value});
            return;
        }
        Object[] prev = data.get(data.size()-1);
        if (date - ((Long)prev[0]) < resolution*MS_IN_TEN_MIN ){
            prev[1] = ((Double)prev[1]) + value;
        }else{
            data.add(new Object[]{date,value});
        }
    }

    public void setResolution(int resolution) {
        this.resolution = resolution;
    }

    public void reduceSummedData(){
        data.forEach(o -> o[1] = ((Double) o[1])/resolution);
    }
}
