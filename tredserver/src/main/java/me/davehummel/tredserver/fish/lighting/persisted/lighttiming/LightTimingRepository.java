package me.davehummel.tredserver.fish.lighting.persisted.lighttiming;

import org.springframework.data.repository.CrudRepository;

/**
 * Created by dmhum_000 on 5/6/2017.
 */
public interface LightTimingRepository extends CrudRepository<LightTiming,String> {
}
