package com.ticketsystem.repository;

import com.ticketsystem.model.Ticket;
import com.ticketsystem.model.TicketTimeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketTimelineRepository extends JpaRepository<TicketTimeline, Long> {
    List<TicketTimeline> findByTicketOrderByCreatedAtAsc(Ticket ticket);

    void deleteByTicket(Ticket ticket);

    @Modifying
    @Query("UPDATE TicketTimeline t SET t.actor = null WHERE t.actor.id = :userId")
    int nullifyActor(@Param("userId") Long userId);
}