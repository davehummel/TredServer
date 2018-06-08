package me.davehummel.tredserver.fish.history;

import me.davehummel.tredserver.services.alert.AlertInfo;
import me.davehummel.tredserver.services.alert.AlertService;
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
import java.util.*;


/**
 * Created by dmhum_000 on 2/4/2017.
 */

@RestController
@RequestMapping("/history")
public class HistoryController {

    private static final String EMPTY_DATE = "2000-01-01T00:00:00";
    private static final LocalDateTime EMPTY_DATETIME = LocalDateTime.parse(EMPTY_DATE, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

    @Autowired
    HistoryService historyService;
    @Autowired
    AlertService alertService;

    @GetMapping("/dump")
    public HttpEntity<List<HistoryEvent>> getResults(@RequestParam(value = "from", defaultValue = EMPTY_DATE) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from, @RequestParam(value = "to", defaultValue = EMPTY_DATE) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        if (EMPTY_DATETIME.equals(from)) {
            from = LocalDateTime.now().minusDays(1);
        }

        if (EMPTY_DATETIME.equals(to)) {
            to = LocalDateTime.now();
        }

        List<HistoryEvent> result = historyService.getResult(from, to);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/series")
    public HttpEntity<List<HistorySeries>> getResults(@RequestParam(value = "filters", defaultValue = "") String filters, @RequestParam(value = "from", defaultValue = EMPTY_DATE) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from, @RequestParam(value = "to", defaultValue = EMPTY_DATE) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        if (EMPTY_DATETIME.equals(from)) {
            from = LocalDateTime.now().minusDays(1);
        }

        if (EMPTY_DATETIME.equals(to)) {
            to = LocalDateTime.now();
        }

        Set<String> seriesNames = new HashSet<>();
        if (!filters.isEmpty()) {
            filters = filters.replace(" ", "");
            filters = filters.toLowerCase();

            for (String filter : filters.split(",")) {
                seriesNames.add(filter);
            }
        }

        List<HistoryEvent> result = historyService.getResult(from, to);

        result.sort((o1, o2) -> o1.getId().getDate().compareTo(o2.getId().getDate()));

        Map<String, HistorySeries> seriesSet = new TreeMap<>();

        for (HistoryEvent event : result) {
            if (!filters.isEmpty()) {
                if (!seriesNames.contains(event.getId().getName().replace(" ", "").toLowerCase())) {
                    continue;
                }
            }
            HistorySeries series = seriesSet.get(event.getId().getName());
            if (series == null) {
                series = new HistorySeries();
                series.setName(event.getId().getName());
                seriesSet.put(series.getName(), series);
            }

            Object[] item = new Object[]{event.getId().getDate().getTime(), event.value};


            if (item[1].equals(Double.NaN)) continue;

            series.getData().add(item);
        }

        List<HistorySeries> outputList = new ArrayList<>(seriesSet.values());

        return new ResponseEntity<>(outputList, HttpStatus.OK);
    }

    @GetMapping("/alerts")
    public HttpEntity<List<AlertInfo>> getAlerts() {

        List<AlertInfo> alerts = alertService.getAlerts(1000 * 60 * 60 * 72);


        boolean isActive = false;

        AlertInfo newest = null;

        for (AlertInfo alert : alerts) {
            if (alert.isActive()) {
                isActive = true;
                break;
            }
            if (newest == null || alert.getAlertTime().after(newest.getAlertTime())) {
                newest = alert;
            }
        }

        if (isActive) {
            alerts.removeIf(alertInfo -> !alertInfo.isActive());
        } else {
            alerts = new ArrayList<>();
            alerts.add(newest);
        }

        return new ResponseEntity<>(alerts, HttpStatus.OK);
    }


}
