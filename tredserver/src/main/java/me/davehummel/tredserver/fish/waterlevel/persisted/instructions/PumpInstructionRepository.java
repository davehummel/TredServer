package me.davehummel.tredserver.fish.waterlevel.persisted.instructions;

import org.springframework.data.repository.CrudRepository;

/**
 * Created by dmhum on 2/14/2017.
 */
public interface PumpInstructionRepository extends CrudRepository<PumpInstruction,String>{

}
