package com.ticketsystem.service;

import com.ticketsystem.model.enums.Status;
import com.ticketsystem.repository.TicketRepository;
import org.springframework.stereotype.Component;

@Component
public class TicketLoadCounter {

    private final TicketRepository ticketRepository;

    public TicketLoadCounter(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public long countOpen(Long agentId) {
        return ticketRepository.findAll().stream()
                .filter(t -> t.getAssignedTo() != null && t.getAssignedTo().getId().equals(agentId))
                .filter(t -> t.getStatus() != Status.RESOLVED && t.getStatus() != Status.CLOSED)
                .count();
    }
}
