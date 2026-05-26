package com.ticketsystem.service;

import com.ticketsystem.model.Ticket;
import com.ticketsystem.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EscalationService {

    private static final Logger log = LoggerFactory.getLogger(EscalationService.class);

    private final TicketRepository ticketRepository;
    private final NotificationService notificationService;
    private final TicketService ticketService;

    public EscalationService(TicketRepository ticketRepository,
                             NotificationService notificationService,
                             TicketService ticketService) {
        this.ticketRepository = ticketRepository;
        this.notificationService = notificationService;
        this.ticketService = ticketService;
    }

    /**
     * Run every 15 minutes; escalate tickets that have exceeded their estimated resolution time.
     */
    @Scheduled(fixedDelay = 900000)
    public void checkEscalations() {
        log.info("Running escalation check...");
        // tickets older than 24h that are still unresolved get flagged
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        List<Ticket> candidates = ticketRepository.findTicketsForEscalation(threshold);
        for (Ticket t : candidates) {
            t.setEscalated(true);
            ticketRepository.save(t);
            ticketService.addTimeline(t, "ESCALATED",
                    "Ticket escalated: exceeded resolution window", null);
            if (t.getAssignedTo() != null) {
                notificationService.notify(t.getAssignedTo(),
                        "⚠ Ticket escalated: " + t.getTitle(), t.getId());
            }
            notificationService.notify(t.getCreatedBy(),
                    "Your ticket has been escalated for faster resolution: " + t.getTitle(), t.getId());
        }
        log.info("Escalation check complete. Escalated {} tickets.", candidates.size());
    }
}
