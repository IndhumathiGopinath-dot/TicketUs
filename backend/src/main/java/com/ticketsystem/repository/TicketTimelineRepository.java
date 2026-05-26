package com.ticketsystem.repository;

import com.ticketsystem.model.Ticket;
import com.ticketsystem.model.TicketTimeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketTimelineRepository extends JpaRepository<TicketTimeline, Long> {
    List<TicketTimeline> findByTicketOrderByCreatedAtAsc(Ticket ticket);
}
