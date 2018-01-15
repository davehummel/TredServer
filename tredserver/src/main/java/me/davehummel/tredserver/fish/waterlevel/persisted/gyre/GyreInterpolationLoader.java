package me.davehummel.tredserver.fish.waterlevel.persisted.gyre;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Created by dmhum_000 on 2/4/2017.
 */

@Component
public class GyreInterpolationLoader implements CommandLineRunner {

    private final GyreInterpolationRepository repository;

    @Autowired
    public GyreInterpolationLoader(GyreInterpolationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... strings) throws Exception {


        GyreInterpolation found = repository.findOne("pump");
        if (found == null || found.getLevels().size() == 0) {
            found = new GyreInterpolation("pump");

            found.getLevels().put("0", "1");
            found.getLevels().put("10000", "1");
            found.getLevels().put("20000", "3");
            found.getLevels().put("30000", "1");
            found.getLevels().put("40000", "0");
            found.getLevels().put("80000", "1");
            found.getLevels().put("90000", "2");
            found.getLevels().put("110000", "5");
            found.getLevels().put("120000", "0");
            found.getLevels().put("130000", "1");
            found.getLevels().put("150000", "1");
            found.getLevels().put("200000", "1");
            found.getLevels().put("220000", "3");
            found.getLevels().put("230000", "1");
            found.getLevels().put("240000", "2");
            found.getLevels().put("250000", "1");
            found.getLevels().put("260000", "3");
            found.getLevels().put("270000", "0");
            found.getLevels().put("280000", "0");
            found.getLevels().put("290000", "1");
            found.getLevels().put("300000", "1");


            repository.save(found);
        }
         found = repository.findOne("head");
        if (found == null || found.getLevels().size() == 0) {
            found = new GyreInterpolation("head");

            found.getLevels().put("0", "1");
            found.getLevels().put("500", "1");
            found.getLevels().put("20000", "0");
            found.getLevels().put("20500", "2");
            found.getLevels().put("21000", "3");
            found.getLevels().put("40000", "0");
            found.getLevels().put("40500", "4");
            found.getLevels().put("50000", "0");
            found.getLevels().put("50500", "1");
            found.getLevels().put("51000", "3");
            found.getLevels().put("60000", "0");
            found.getLevels().put("60500", "2");
            found.getLevels().put("61000", "4");
            found.getLevels().put("70000", "0");
            found.getLevels().put("70500", "1");
            found.getLevels().put("75000", "0");
            found.getLevels().put("75500", "3");
            found.getLevels().put("76000", "2");
            found.getLevels().put("79500", "0");
            found.getLevels().put("80000", "4");

            repository.save(found);
        }


    }
}
