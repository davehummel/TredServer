package me.davehummel.tredserver.fish.waterlevel.persisted.levelinterpolation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Created by dmhum_000 on 2/4/2017.
 */

@Component
public class LevelInterpolationLoader implements CommandLineRunner {

    private final LevelInterpolationRepository repository;

    @Autowired
    public LevelInterpolationLoader(LevelInterpolationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... strings) throws Exception {
        Iterable<LevelInterpolation> level = repository.findAll();
        boolean[] hasSides = new boolean[3];
        level.forEach(levelInterpolation -> {
            hasSides[levelInterpolation.getSide()]=true;
        });

        if (!hasSides[0]) {
            LevelInterpolation leftLevel = new LevelInterpolation(0);

            leftLevel.getLevels().put( "0","25");
            leftLevel.getLevels().put("2","61");
            leftLevel.getLevels().put("4","114");
            leftLevel.getLevels().put("6","168");
            leftLevel.getLevels().put("8","218");
            leftLevel.getLevels().put("10","270");
            leftLevel.getLevels().put("12","315");
            leftLevel.getLevels().put("14","362");

            repository.save(leftLevel);
        }

        if (!hasSides[1]) {
            LevelInterpolation rightLevel = new LevelInterpolation(1);
            rightLevel.getLevels().put( "0","20");
            rightLevel.getLevels().put("2","52");
            rightLevel.getLevels().put("4","96");
            rightLevel.getLevels().put("6","158");
            rightLevel.getLevels().put("8","212");
            rightLevel.getLevels().put("10","264");
            rightLevel.getLevels().put("12","308");
            rightLevel.getLevels().put("14","360");


            repository.save(rightLevel);
        }

        if (!hasSides[2]) {
            LevelInterpolation topOffLevel = new LevelInterpolation(2);
            topOffLevel.getLevels().put( "1","100");
            topOffLevel.getLevels().put( "0","460");
            repository.save(topOffLevel);
        }

    }
}
