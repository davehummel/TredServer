package me.davehummel.tredserver.fish.history;

import org.springframework.hateoas.ResourceSupport;

import java.util.List;

public class HistoryResult extends ResourceSupport {

    private final List<HistoryEvent> results;

    public HistoryResult (List<HistoryEvent> historyEntryList){
        this.results = historyEntryList;
    }


}
