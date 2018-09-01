package me.davehummel.tredserver.fish;

import me.davehummel.tredserver.command.DataType;
import me.davehummel.tredserver.services.ReadService;
import me.davehummel.tredserver.services.ServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class BasicController {

    @Autowired
    private ServiceManager serialServiceManager;

    @Autowired
    private ReadService readService;

    Logger logger = LoggerFactory.getLogger(BasicController.class);


    @RequestMapping(value = "/serial",method = RequestMethod.POST)
    String updateSerial(@RequestBody String command) {
        serialServiceManager.getBridge().writeDirect(command);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            logger.error("Interrupted",e);
        }
        boolean failed = serialServiceManager.getBridge().isLastInstrunctionFailed();
        return failed?"Failed ":"Ran " + command;
    }

    @RequestMapping(value = "/read",method = RequestMethod.POST)
    String readValue(@RequestParam(value="type") String rawType,@RequestParam(value="module") String module, @RequestParam(value="address") String address) {
        DataType type = DataType.valueOf(rawType);
        ReadService.ReadValue out = readService.getValueNow(type, module.charAt(0), address, 500);
        return "Read "+out;
    }

    @RequestMapping(value = "/reset",method = RequestMethod.POST)
    String readValue() {
        serialServiceManager.forceEmbeddedRestart();
        return "Reset";
    }
}