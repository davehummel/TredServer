package me.davehummel.tredserver.fish.waterlevel.persisted.levelinterpolation;

import me.davehummel.tredserver.command.ImmediateInstruction;
import me.davehummel.tredserver.fish.waterlevel.LevelInterpUtility;
import me.davehummel.tredserver.services.ServiceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import java.util.TreeMap;

/**
 * Created by dmhum_000 on 2/11/2017.
 */
@Component
@RepositoryEventHandler(LevelInterpolation.class)
public class AfterLevelInterpolationSaveEventHandler  {

    @Autowired
    private ServiceManager serialServiceManager;



    @HandleAfterSave
    protected void onAfterSave(LevelInterpolation entity) {
        ImmediateInstruction inst;
        inst = LevelInterpUtility.createInterpInstruction(entity.getSide(),new TreeMap<>(entity.getLevels()));
        serialServiceManager.getBridge().writeInstruction(inst);
    }

}