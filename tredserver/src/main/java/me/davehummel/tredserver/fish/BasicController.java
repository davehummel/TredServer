package me.davehummel.tredserver.fish;

import me.davehummel.tredserver.command.DataType;
import me.davehummel.tredserver.gpio.TurbotGpio;
import me.davehummel.tredserver.services.ReadService;
import me.davehummel.tredserver.services.ServiceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class BasicController {

    @Autowired
    private ServiceManager serialServiceManager;

    @Autowired
    private ReadService readService;


    @RequestMapping(value = "/serial",method = RequestMethod.POST)
    String updateSerial(@RequestBody String command) {
        serialServiceManager.getBridge().writeDirect(command);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
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
        TurbotGpio.setPinValue(483,false);
        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        TurbotGpio.setPinValue(483,true);
        return "Reset";
    }
}