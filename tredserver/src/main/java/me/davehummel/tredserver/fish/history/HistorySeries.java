package me.davehummel.tredserver.fish.history;

import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.List;

public class HistorySeries {

    String name;

    List<Double[]> data;

    public HistorySeries(){
        data = new ArrayList<>();
    };

    public HistorySeries(String name, List<Double[]> data) {
        this.name = name;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Double[]> getData() {
        return data;
    }

    public void setData(List<Double[]> data) {
        this.data = data;
    }
}
