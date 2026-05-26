package com.ticketsystem.service;

import com.ticketsystem.dto.TicketDtos;
import com.ticketsystem.exception.AppException;
import com.ticketsystem.model.Ticket;
import com.ticketsystem.model.TicketTimeline;
import com.ticketsystem.model.User;
import com.ticketsystem.model.enums.Category;
import com.ticketsystem.model.enums.Priority;
import com.ticketsystem.model.enums.Role;
import com.ticketsystem.model.enums.Status;
import com.ticketsystem.repository.TicketRepository;
import com.ticketsystem.repository.TicketTimelineRepository;
import com.ticketsystem.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketTimelineRepository timelineRepository;
    private final UserRepository userRepository;
    private final PriorityService priorityService;
    private final RoutingService routingService;
    private final NotificationService notificationService;

    public TicketService(TicketRepository ticketRepository,
                         TicketTimelineRepository timelineRepository,
                         UserRepository userRepository,
                         PriorityService priorityService,
                         RoutingService routingService,
                         NotificationService notificationService) {
        this.ticketRepository = ticketRepository;
        this.timelineRepository = timelineRepository;
        this.userRepository = userRepository;
        this.priorityService = priorityService;
        this.routingService = routingService;
        this.notificationService = notificationService;
    }

    @Transactional
    public Ticket createTicket(TicketDtos.CreateTicketRequest req, User createdBy) {
        Priority priority = priorityService.computePriority(req.getTitle(), req.getDescription(),
                req.getCategory(), req.getSeverity());
        Integer eta = priorityService.estimateResolutionHours(priority, req.getCategory());

        Optional<User> assigned = routingService.routeTicket(req.getCategory());

        Set<Ticket> related = new HashSet<>();
        if (req.getRelatedTicketIds() != null && !req.getRelatedTicketIds().isEmpty()) {
            related.addAll(ticketRepository.findAllById(req.getRelatedTicketIds()));
        }

        Ticket ticket = Ticket.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .category(req.getCategory())
                .priority(priority)
                .status(Status.OPEN)
                .confidential(req.isConfidential())
                .requestType(req.getRequestType())
                .osInfo(req.getOsInfo())
                .browserInfo(req.getBrowserInfo())
                .appVersion(req.getAppVersion())
                .severity(req.getSeverity())
                .assetTag(req.getAssetTag())
                .createdBy(createdBy)
                .assignedTo(assigned.orElse(null))
                .estimatedResolutionHours(eta)
                .relatedTickets(related)
                .escalated(false)
                .build();

        ticket = ticketRepository.save(ticket);

        addTimeline(ticket, "CREATED",
                "Ticket created with priority " + priority + ". ETA: " + eta + "h", createdBy);

        if (assigned.isPresent()) {
            addTimeline(ticket, "ASSIGNED", "Assigned to " + assigned.get().getName(), null);
            notificationService.notify(assigned.get(),
                    "A new " + req.getCategory() + " ticket has been assigned: " + ticket.getTitle(),
                    ticket.getId());
        }

        return ticket;
    }

    public List<Ticket> getTicketsForUser(User user) {
        if (user.getRole() == Role.ADMIN) {
            return ticketRepository.findAll();
        }
        return ticketRepository.findByCreatedByOrderByCreatedAtDesc(user);
    }

    public List<Ticket> getAssignedTickets(User agent) {
        return ticketRepository.findByAssignedToOrderByCreatedAtDesc(agent);
    }

    public Ticket getTicket(Long id, User requester) {
        Ticket t = ticketRepository.findById(id)
                .orElseThrow(() -> new AppException("Ticket not found"));

        // Confidential check
        if (t.isConfidential()) {
            boolean isOwner = t.getCreatedBy().getId().equals(requester.getId());
            boolean isAssigned = t.getAssignedTo() != null && t.getAssignedTo().getId().equals(requester.getId());
            boolean isHrAdmin = requester.getRole() == Role.ADMIN && "HR".equalsIgnoreCase(requester.getDepartment());

            if (!isOwner && !isAssigned && !isHrAdmin) {
                throw new AppException("This ticket is confidential");
            }
        } else if (requester.getRole() != Role.ADMIN
                && !t.getCreatedBy().getId().equals(requester.getId())
                && (t.getAssignedTo() == null || !t.getAssignedTo().getId().equals(requester.getId()))) {
            throw new AppException("You don't have access to this ticket");
        }
        return t;
    }

    @Transactional
    public Ticket updateStatus(Long id, Status newStatus, String notes, User actor) {
        Ticket t = ticketRepository.findById(id)
                .orElseThrow(() -> new AppException("Ticket not found"));

        if (actor.getRole() != Role.ADMIN &&
                (t.getAssignedTo() == null || !t.getAssignedTo().getId().equals(actor.getId()))) {
            throw new AppException("Only the assigned agent or admin can change status");
        }

        Status oldStatus = t.getStatus();
        t.setStatus(newStatus);
        if (newStatus == Status.RESOLVED || newStatus == Status.CLOSED) {
            t.setResolvedAt(LocalDateTime.now());
        }
        ticketRepository.save(t);

        addTimeline(t, "STATUS_CHANGED", "From " + oldStatus + " to " + newStatus +
                (notes != null && !notes.isBlank() ? " | " + notes : ""), actor);

        notificationService.notify(t.getCreatedBy(),
                "Status of your ticket '" + t.getTitle() + "' changed to " + newStatus,
                t.getId());

        return t;
    }

    @Transactional
    public Ticket assignTicket(Long ticketId, Long agentId, User actor) {
        if (actor.getRole() != Role.ADMIN) {
            throw new AppException("Only admins can reassign tickets");
        }
        Ticket t = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new AppException("Ticket not found"));
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new AppException("Agent not found"));
        if (agent.getRole() != Role.ADMIN) {
            throw new AppException("Tickets can only be assigned to admin agents");
        }
        t.setAssignedTo(agent);
        ticketRepository.save(t);
        addTimeline(t, "ASSIGNED", "Reassigned to " + agent.getName(), actor);
        notificationService.notify(agent, "Ticket assigned to you: " + t.getTitle(), t.getId());
        return t;
    }

    @Transactional
    public Ticket rateTicket(Long ticketId, Integer rating, User user) {
        Ticket t = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new AppException("Ticket not found"));
        if (!t.getCreatedBy().getId().equals(user.getId())) {
            throw new AppException("Only the creator can rate this ticket");
        }
        if (t.getStatus() != Status.RESOLVED && t.getStatus() != Status.CLOSED) {
            throw new AppException("You can only rate resolved tickets");
        }
        if (rating != 1 && rating != -1) {
            throw new AppException("Rating must be 1 (up) or -1 (down)");
        }
        t.setSatisfactionRating(rating);
        ticketRepository.save(t);
        addTimeline(t, "RATED", "User rated: " + (rating == 1 ? "👍" : "👎"), user);
        return t;
    }

    public List<Ticket> findSimilarTickets(String title, Category category) {
        if (title == null || title.isBlank()) return new ArrayList<>();
        // very simple: search by significant words
        String[] words = title.toLowerCase().split("\\s+");
        Set<Ticket> results = new HashSet<>();
        for (String w : words) {
            if (w.length() < 4) continue;
            results.addAll(ticketRepository.findSimilarTickets(category, w));
        }
        return results.stream().limit(5).collect(Collectors.toList());
    }

    public List<TicketTimeline> getTimeline(Ticket ticket) {
        return timelineRepository.findByTicketOrderByCreatedAtAsc(ticket);
    }

    public void addTimeline(Ticket ticket, String action, String notes, User actor) {
        TicketTimeline tl = TicketTimeline.builder()
                .ticket(ticket)
                .action(action)
                .notes(notes)
                .actor(actor)
                .build();
        timelineRepository.save(tl);
    }

    public TicketDtos.TicketResponse toResponse(Ticket t) {
        return TicketDtos.TicketResponse.builder()
                .id(t.getId())
                .title(t.getTitle())
                .description(t.getDescription())
                .category(t.getCategory())
                .priority(t.getPriority())
                .status(t.getStatus())
                .confidential(t.isConfidential())
                .requestType(t.getRequestType())
                .osInfo(t.getOsInfo())
                .browserInfo(t.getBrowserInfo())
                .appVersion(t.getAppVersion())
                .severity(t.getSeverity())
                .assetTag(t.getAssetTag())
                .createdByName(t.getCreatedBy() != null ? t.getCreatedBy().getName() : null)
                .createdById(t.getCreatedBy() != null ? t.getCreatedBy().getId() : null)
                .assignedToName(t.getAssignedTo() != null ? t.getAssignedTo().getName() : null)
                .assignedToId(t.getAssignedTo() != null ? t.getAssignedTo().getId() : null)
                .estimatedResolutionHours(t.getEstimatedResolutionHours())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .resolvedAt(t.getResolvedAt())
                .satisfactionRating(t.getSatisfactionRating())
                .escalated(t.isEscalated())
                .relatedTicketIds(t.getRelatedTickets() == null ? new ArrayList<>() :
                        t.getRelatedTickets().stream().map(Ticket::getId).collect(Collectors.toList()))
                .build();
    }
}
