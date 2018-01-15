package me.davehummel.tredserver.fish;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by dmhum_000 on 2/5/2017.
 */

@Controller
public class PageController {

    @RequestMapping(value = "/pump")
    public String pumpIndex() {
        return "pump";
    }

    @RequestMapping(value = "/temperature")
    public String tempIndex() {
        return "temperature";
    }

    @RequestMapping(value = "/overview")
    public String overviewIndex() {
        return "overview";
    }


}
