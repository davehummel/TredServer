package me.davehummel.tredserver.fish.temperature;

import me.davehummel.tredserver.fish.services.TempService;
import me.davehummel.tredserver.fish.temperature.readonly.TemperatureReadings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;


/**
 * Created by dmhum_000 on 2/4/2017.
 */

@RestController
@RequestMapping("/temperature")
public class TemperatureController {

    @Autowired
    TempService temperatureService;

    @RequestMapping("/readings")
    public HttpEntity<TemperatureReadings> getReadings(){
        TemperatureReadings readings = temperatureService.getReadings();
        readings.add(linkTo(methodOn(TemperatureController.class).getReadings()).withSelfRel());
        return new ResponseEntity<TemperatureReadings>(readings, HttpStatus.OK);
    }


}
