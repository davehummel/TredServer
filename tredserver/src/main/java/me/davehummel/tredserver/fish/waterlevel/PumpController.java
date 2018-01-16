package me.davehummel.tredserver.fish.waterlevel;

import me.davehummel.tredserver.fish.services.PumpLevelService;
import me.davehummel.tredserver.fish.waterlevel.readonly.PumpLevels;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;


/**
 * Created by dmhum_000 on 2/4/2017.
 */

@RestController
@RequestMapping("/pump")
public class PumpController {

    @Autowired
    PumpLevelService pumpLevelService;

    @Value("${tank.activationkey}")
    private String activationKey;

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

    @PostMapping("/allOn")
    public HttpEntity<String> allPumpsOn(){
        pumpLevelService.allPumpsOn();
        return new ResponseEntity<String>("All Pumps on", HttpStatus.OK);
    }

    @PostMapping("/topoffOn")
    public HttpEntity<String> topOffOn(){
        pumpLevelService.topoffOn();
        return new ResponseEntity<String>("Topoff On (for 1 min)", HttpStatus.OK);
    }

    @PostMapping("/topoffOff")
    public HttpEntity<String> topOffOf(){
        pumpLevelService.topoffOff();
        return new ResponseEntity<String>("Topoff off", HttpStatus.OK);
    }

}
