package me.davehummel.tredserver.fish.history;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * Created by dmhum_000 on 7/2/2017.
 */
@Repository
public interface HistoryEventRepository extends CrudRepository<HistoryEvent,Date> {
}
