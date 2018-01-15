package me.davehummel.tredserver.fish.lighting;

import me.davehummel.tredserver.fish.services.LightService;
import me.davehummel.tredserver.fish.services.TempService;
import me.davehummel.tredserver.fish.temperature.readonly.TemperatureReadings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;


/**
 * Created by dmhum_000 on 2/4/2017.
 */

@RestController
@RequestMapping("/light")
public class LightController {

    @Autowired
    LightService lightService;

    @RequestMapping("/bottomOn")
    public HttpEntity<String> bottomOn(){
        lightService.lightOn();
        return new ResponseEntity<String>("Bottom On", HttpStatus.OK);
    }

    @RequestMapping("/bottomOff")
    public HttpEntity<String> bottomOff(){
        lightService.lightOff();
        return new ResponseEntity<String>("Bottom Off", HttpStatus.OK);
    }

}
