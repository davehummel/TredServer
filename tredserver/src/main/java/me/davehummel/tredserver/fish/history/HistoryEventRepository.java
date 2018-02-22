package me.davehummel.tredserver.fish.history;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * Created by dmhum_000 on 7/2/2017.
 */
@Repository
public interface HistoryEventRepository extends CrudRepository<HistoryEvent,HistoryID> {

    List<HistoryEvent> findAllByIdDateBetween(Date from,Date to);

}
