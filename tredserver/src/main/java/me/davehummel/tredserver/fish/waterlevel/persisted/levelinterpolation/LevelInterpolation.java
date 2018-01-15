package me.davehummel.tredserver.fish.waterlevel.persisted.levelinterpolation;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dmhum_000 on 1/25/2017.
 */

@Entity
public class LevelInterpolation {


    @Id
    @Column(unique = true)
    private Integer side;



    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "LEVEL_INTERPOLATION_MAP", joinColumns = @JoinColumn(name = "side"))
    @MapKeyColumn(name = "mapKey")
    @Column(name = "mapValue")
    private Map<String, String> levels = new HashMap<String, String>();

    public LevelInterpolation(Integer side) {
        this.side = side;
    }

    protected LevelInterpolation() {
    }

    @Override
    public String toString() {
        return "LevelInterpolation{" +
                "side='" + side + '\'' +
                ", levels=" + levels +
                '}';
    }


    public Integer getSide() {
        return side;
    }

    public void setSide(Integer side) {
        this.side = side;
    }

    public Map<String, String> getLevels() {
        return levels;
    }
}
