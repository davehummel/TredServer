package me.davehummel.tredserver.fish.waterlevel.persisted.instructions;

import me.davehummel.tredserver.fish.services.PumpLevelService;
import me.davehummel.tredserver.services.SendResponse;
import me.davehummel.tredserver.services.ServiceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

/**
 * Created by dmhum on 2/14/2017.
 */

@Component
@RepositoryEventHandler(PumpInstruction.class)
public class BeforePumpInstructionSaveEventHandler {

    @Autowired
    private ServiceManager serialServiceManager;

    @HandleBeforeSave
    @HandleBeforeCreate
    protected void onBeforeSave(PumpInstruction entity) {
        if (PumpLevelService.RUNTINST.equals(entity.getName())){
            serialServiceManager.getBridge().writeDirect("K "+PumpLevelService.PUMPINSTRUCTIONID);
        }
        SendResponse response = new SendResponse();
        serialServiceManager.demandResponse(response);
        serialServiceManager.getBridge().writeDirect(entity.getValue());
        entity.setSuccess(response.isSuccess());
        entity.setError(response.getError());
    }
}
