package me.davehummel.tredserver.fish.waterlevel.persisted.gyre;

import me.davehummel.tredserver.command.ImmediateInstruction;
import me.davehummel.tredserver.command.math.InterpType;
import me.davehummel.tredserver.fish.waterlevel.LevelInterpUtility;
import me.davehummel.tredserver.services.ServiceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

/**
 * Created by dmhum_000 on 2/11/2017.
 */
@Component
@RepositoryEventHandler(GyreInterpolation.class)
public class AfterGyreInterpolationSaveEventHandler {

    @Autowired
    private ServiceManager serialServiceManager;



    @HandleAfterSave
    protected void onAfterSave(GyreInterpolation entity) {
        if (entity.getName().equals("pump")) {
            ImmediateInstruction inst = LevelInterpUtility.createGyreInterpInstruction(2, entity.getLevels(), InterpType.LINEAR);
            serialServiceManager.getBridge().writeInstruction(inst);
        }
        if (entity.getName().equals("head")) {
            ImmediateInstruction inst = LevelInterpUtility.createGyreInterpInstruction(3, entity.getLevels(), InterpType.STEP);
            serialServiceManager.getBridge().writeInstruction(inst);
        }
    }

}