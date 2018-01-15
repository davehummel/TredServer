package me.davehummel.tredserver.fish.history;

import javax.persistence.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dmhum_000 on 7/2/2017.
 */
@Entity
public class HistoryEvent {

    @Id
    @Column(unique = true)
    private Date time;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "HISTORY_MAP", joinColumns = @JoinColumn(name = "time"))
    @MapKeyColumn(name = "mapKey")
    @Column(name = "mapValue")
    private Map<String, Double> values = new HashMap<String, Double>();

    protected HistoryEvent(){};

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Map<String, Double> getValues() {
        return values;
    }

    public void setValues(Map<String, Double> values) {
        this.values = values;
    }
}
