package me.davehummel.tredserver.fish.history;

import org.springframework.data.util.Pair;

import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.List;

public class HistorySeries {

    String name;

    List<Object[]> data;

    public HistorySeries(){
        data = new ArrayList<>();
    };

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
}
