package me.davehummel.tredserver.fish.history;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by dmhum_000 on 7/2/2017.
 */

@Service
public class HistoryService {

    final private HashMap<String, ResetingSupplier> suppliers = new HashMap<String, ResetingSupplier>();

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
                calendar.set(Calendar.MINUTE, unroundedMinutes + mod);
                saveSources(calendar.getTime());
            }
        },1000*60*10,1000*60^10);

    }

    private void saveSources(Date date) {
        HistoryEvent event = new HistoryEvent();
        event.setTime(date);
        for (Map.Entry<String, ResetingSupplier> entry: suppliers.entrySet()){
            try{
                event.getValues().put(entry.getKey(),entry.getValue().get());
                entry.getValue().resetState();
            }catch (Exception e){
                System.err.println("History failed to capture metric");
                e.printStackTrace();
            }
        }
        historyEventRepository.save(event);
    }

    public void addSupplier(String name, ResetingSupplier supplier){
        suppliers.put(name,supplier);
    }
}
