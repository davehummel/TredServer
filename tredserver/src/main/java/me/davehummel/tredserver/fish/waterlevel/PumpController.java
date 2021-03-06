package me.davehummel.tredserver.fish.waterlevel;

import me.davehummel.tredserver.fish.services.PumpLevelService;
import me.davehummel.tredserver.fish.waterlevel.readonly.PumpLevels;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;


/**
 * Created by dmhum_000 on 2/4/2017.
 */

@RestController
@RequestMapping("/pump")
public class PumpController {

    @Autowired
    PumpLevelService pumpLevelService;

    @RequestMapping("/levels")
    public HttpEntity<PumpLevels> getPumpLevel(){
        PumpLevels levels = pumpLevelService.getLevels();
        levels.add(linkTo(methodOn(PumpController.class).getPumpLevel()).withSelfRel());
        return new ResponseEntity<PumpLevels>(levels, HttpStatus.OK);
    }

    @PostMapping("/allOff")
    public HttpEntity<String> allPumpsOff(){
        pumpLevelService.allPumpsOff();
        return new ResponseEntity<String>("All Pumps off (for 5 min)", HttpStatus.OK);
    }

    @PostMapping("/setTargetDepth")
    public HttpEntity<String> setTargetDepth(@RequestParam(value = "depth", defaultValue = "12.0")float depth){
        pumpLevelService.setTargetDepth(depth);
        return new ResponseEntity<String>("Setting target depth to :"+depth, HttpStatus.OK);
    }

    @PostMapping("/allOn")
    public HttpEntity<String> allPumpsOn(){
        pumpLevelService.allPumpsOn();
        return new ResponseEntity<String>("All Pumps on", HttpStatus.OK);
    }

    @PostMapping("/topoffOn")
    public HttpEntity<String> topOffOn(){
        pumpLevelService.disableTopOff(0); // remove disable override
        pumpLevelService.topoffOn();
        return new ResponseEntity<String>("Topoff On (for 1 min)", HttpStatus.OK);
    }

    @PostMapping("/topoffOff")
    public HttpEntity<String> topOffOff(){
        pumpLevelService.topoffOff();
        return new ResponseEntity<String>("Topoff off", HttpStatus.OK);
    }

    @PostMapping("/topoffDisable")
    public HttpEntity<String> topOffDisable(){
        pumpLevelService.disableTopOff(24*60*60*1000); // 24 hours
        return new ResponseEntity<String>("Topoff Disabled", HttpStatus.OK);
    }

    @PostMapping("/restartDevices")
    public HttpEntity<String> restartDevices(){
        pumpLevelService.restartRadioDevice();
        return new ResponseEntity<String>("Restarted Wireless", HttpStatus.OK);
    }

}
