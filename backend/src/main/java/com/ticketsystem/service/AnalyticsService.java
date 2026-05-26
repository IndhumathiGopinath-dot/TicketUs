package com.ticketsystem.service;

import com.ticketsystem.model.enums.Status;
import com.ticketsystem.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    private final TicketRepository ticketRepository;

    public AnalyticsService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public Map<String, Object> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        // ticket counts by category
        Map<String, Long> byCategory = new HashMap<>();
        for (Object[] row : ticketRepository.countByCategory()) {
            byCategory.put(row[0].toString(), ((Number) row[1]).longValue());
        }
        dashboard.put("byCategory", byCategory);

        // by status
        Map<String, Long> byStatus = new HashMap<>();
        for (Object[] row : ticketRepository.countByStatus()) {
            byStatus.put(row[0].toString(), ((Number) row[1]).longValue());
        }
        dashboard.put("byStatus", byStatus);

        // agent workload
        Map<String, Long> agentLoad = new HashMap<>();
        for (Object[] row : ticketRepository.agentWorkload()) {
            agentLoad.put(row[0].toString(), ((Number) row[1]).longValue());
        }
        dashboard.put("agentWorkload", agentLoad);

        // average resolution
        Double avgHours = ticketRepository.averageResolutionHours();
        dashboard.put("avgResolutionHours", avgHours == null ? 0 : avgHours);

        dashboard.put("totalTickets", ticketRepository.count());
        dashboard.put("openTickets", ticketRepository.countByStatus(Status.OPEN));
        dashboard.put("inProgressTickets", ticketRepository.countByStatus(Status.IN_PROGRESS));
        dashboard.put("resolvedTickets", ticketRepository.countByStatus(Status.RESOLVED));
        dashboard.put("escalatedTickets", ticketRepository.findAll().stream()
                .filter(t -> t.isEscalated()).count());

        // satisfaction ratings
        List<Integer> ratings = ticketRepository.findAll().stream()
                .map(t -> t.getSatisfactionRating())
                .filter(r -> r != null)
                .toList();
        long ups = ratings.stream().filter(r -> r == 1).count();
        long downs = ratings.stream().filter(r -> r == -1).count();
        Map<String, Long> sat = new HashMap<>();
        sat.put("up", ups);
        sat.put("down", downs);
        dashboard.put("satisfaction", sat);

        return dashboard;
    }
}
