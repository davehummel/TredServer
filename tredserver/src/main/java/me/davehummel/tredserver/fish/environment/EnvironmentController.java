package me.davehummel.tredserver.fish.environment;

import me.davehummel.tredserver.fish.environment.readonly.EnvironmentReadings;
import me.davehummel.tredserver.fish.services.EnvSensorService;
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
@RequestMapping("/environment")
public class EnvironmentController {

    @Autowired
    EnvSensorService envSensorService;

    @RequestMapping("/readings")
    public HttpEntity<EnvironmentReadings> getReadings(){
        EnvironmentReadings readings = envSensorService.getReadings();
        readings.add(linkTo(methodOn(EnvironmentController.class).getReadings()).withSelfRel());
        return new ResponseEntity<EnvironmentReadings>(readings, HttpStatus.OK);
    }


}
