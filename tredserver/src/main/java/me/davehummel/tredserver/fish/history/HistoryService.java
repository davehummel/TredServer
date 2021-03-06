package me.davehummel.tredserver.fish.history;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Created by dmhum_000 on 7/2/2017.
 */

@Controller
public class HistoryService {

    Logger logger = LoggerFactory.getLogger(HistorySeries.class);

    final private HashMap<String, ResettingSupplier> suppliers = new HashMap<String, ResettingSupplier>();

    final private Timer timer = new Timer("History Timer",true);

    @Autowired
    HistoryEventRepository historyEventRepository;

    public HistoryService(){
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Calendar calendar = Calendar.getInstance();
                calendar.setLenient(true);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                int unroundedMinutes = calendar.get(Calendar.MINUTE);
                int mod = unroundedMinutes % 10;
                calendar.set(Calendar.MINUTE, unroundedMinutes - mod);
                saveSources(calendar.getTime());
            }
        },1000*60*10,1000*60*10);

    }

    private void saveSources(Date date) {
        for (Map.Entry<String, ResettingSupplier> entry: suppliers.entrySet()){
            try{
                HistoryEvent event = new HistoryEvent(date,entry.getKey(),entry.getValue().get());
                entry.getValue().resetState();
                historyEventRepository.save(event);
            }catch (Exception e){
                logger.error("History failed to capture metric!",e);
            }
        }

    }

    public void addSupplier(String name, ResettingSupplier supplier){
        suppliers.put(name,supplier);
    }

    public List<HistoryEvent> getResult(LocalDateTime from, LocalDateTime to){
        Date fromDate = Date.from(from.atZone(ZoneId.systemDefault()).toInstant());
        Date toDate = Date.from(to.atZone(ZoneId.systemDefault()).toInstant());

        return historyEventRepository.findAllByIdDateBetween(fromDate,toDate);
    }

    public List<HistoryEvent> getResult(LocalDateTime from, LocalDateTime to, List<String> names){
        if (names == null || names.size()==0){
            return getResult(from,to);
        }

        Date fromDate = Date.from(from.atZone(ZoneId.systemDefault()).toInstant());
        Date toDate = Date.from(to.atZone(ZoneId.systemDefault()).toInstant());

        return historyEventRepository.findByList(fromDate,toDate,names);
    }

    public List<String> getNames(LocalDateTime from, LocalDateTime to){

        Date fromDate = Date.from(from.atZone(ZoneId.systemDefault()).toInstant());
        Date toDate = Date.from(to.atZone(ZoneId.systemDefault()).toInstant());

        return historyEventRepository.getEventNames(fromDate,toDate);
    }

    public void disarm() {
        timer.cancel();
    }
}
