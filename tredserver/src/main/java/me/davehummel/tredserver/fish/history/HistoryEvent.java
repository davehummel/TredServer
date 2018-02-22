package me.davehummel.tredserver.fish.history;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by dmhum_000 on 7/2/2017.
 */
@Entity
@Table( name = "history")
public class HistoryEvent {

    @EmbeddedId
    private HistoryID id;

    Double value;

    public HistoryEvent(){}

    public HistoryEvent (Date time,String name, Double value){
        this.id = new HistoryID();
        id.setDate(time);
        id.setName(name);
        this.value = value;
    }

    public HistoryID getId() {
        return id;
    }

    public void setId(HistoryID id) {
        this.id = id;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
