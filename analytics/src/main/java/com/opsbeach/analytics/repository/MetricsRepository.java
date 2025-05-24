package com.opsbeach.analytics.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opsbeach.analytics.core.BaseRepository;
import com.opsbeach.analytics.dto.SlaMeterDto;
import com.opsbeach.analytics.entity.Metrics;

public interface MetricsRepository extends BaseRepository<Metrics> {
    
    @Query(value = """
            select t1.first_reply_time as firstReplyTime, t1.time_to_acknowledge as timeToAcknowledge, 
            t1.time_to_resolve as timeToResolve, t2.ticket_sla_meter as ticketSlaMeter from analytics.metrics t1 
                cross join 
            (select (sum(case when m1.time_to_resolve > m2.sla_time then 0 else 1 end))
            /
            (select count(*) from analytics.metrics where client_id = :client_id and created_at >= :from and created_at <= :to) 
            as ticket_sla_meter from analytics.metrics m1 join analytics.sla m2 on m1.source = m2.type 
            where m1.client_id = :client_id and m2.client_id = :client_id and m1.created_at >= :from  and m1.created_at <= :to) t2 
            where t1.client_id = :client_id and t1.created_at >= :from  and t1.created_at <= :to """, nativeQuery = true)
    List<SlaMeterDto> findTicketSlaMeter(@Param("client_id") Long clientId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
