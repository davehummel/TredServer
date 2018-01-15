package me.davehummel.tredserver.fish.waterlevel.persisted.gyre;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dmhum on 3/27/2017.
 */


@Entity
public class GyreInterpolation {
    @Id
    @Column(unique = true)
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "GYRE_INTERPOLATION_MAP", joinColumns = @JoinColumn(name = "name"))
    @MapKeyColumn(name = "mapKey")
    @Column(name = "mapValue")
    private Map<String, String> levels = new HashMap<String, String>();

    public GyreInterpolation(String name) {
        this.name = name;
    }

    protected GyreInterpolation(){}

    @Override
    public String toString() {
        return "GyreInterpolation{" +
                "name='" + name + '\'' +
                ", levels=" + levels +
                '}';
    }

    public String getName(){
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getLevels() {
        return levels;
    }
}
