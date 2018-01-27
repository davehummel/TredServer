package me.davehummel.tredserver.fish.history;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


/**
 * Created by dmhum_000 on 2/4/2017.
 */

@RestController
@RequestMapping("/history")
public class HistoryController {

    private static final String EMPTY_DATE = "2000-01-01T00:00:00";
    private static final LocalDateTime EMPTY_DATETIME = LocalDateTime.parse(EMPTY_DATE,DateTimeFormatter.ISO_LOCAL_DATE_TIME);

    @Autowired
    HistoryService historyService;

    @GetMapping("/values")
    public HttpEntity<List<HistoryEvent>> getResults(@RequestParam(value="from",defaultValue = EMPTY_DATE) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)  LocalDateTime from, @RequestParam(value="to",defaultValue = EMPTY_DATE) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)  LocalDateTime to){

        if (EMPTY_DATETIME.equals(from)){
            from = LocalDateTime.now().minusDays(1);
        }

        if (EMPTY_DATETIME.equals(to)){
            to = LocalDateTime.now();
        }

        List<HistoryEvent> result = historyService.getResult(from,to);

        return new ResponseEntity<List<HistoryEvent>>(result, HttpStatus.OK);
    }


}
