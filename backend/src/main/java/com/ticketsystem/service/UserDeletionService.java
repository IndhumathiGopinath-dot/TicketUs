package com.ticketsystem.service;

import com.ticketsystem.exception.AppException;
import com.ticketsystem.model.Ticket;
import com.ticketsystem.model.User;
import com.ticketsystem.repository.AttachmentRepository;
import com.ticketsystem.repository.NotificationRepository;
import com.ticketsystem.repository.TicketRepository;
import com.ticketsystem.repository.TicketTimelineRepository;
import com.ticketsystem.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserDeletionService {

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final TicketTimelineRepository timelineRepository;
    private final AttachmentRepository attachmentRepository;
    private final NotificationRepository notificationRepository;

    public UserDeletionService(UserRepository userRepository,
                                TicketRepository ticketRepository,
                                TicketTimelineRepository timelineRepository,
                                AttachmentRepository attachmentRepository,
                                NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
        this.timelineRepository = timelineRepository;
        this.attachmentRepository = attachmentRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void deleteUserCascade(Long userId, Long actingAdminId) {
        if (userId.equals(actingAdminId)) {
            throw new AppException("Cannot delete yourself");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found"));

        // 1. Delete tickets the user raised, with their timeline + attachments
        List<Ticket> created = ticketRepository.findByCreatedByOrderByCreatedAtDesc(user);
        for (Ticket t : created) {
            timelineRepository.deleteByTicket(t);
            attachmentRepository.deleteByTicket(t);
            ticketRepository.delete(t);
        }

        // 2. Unassign tickets where this user is the assignee
        List<Ticket> assigned = ticketRepository.findByAssignedToOrderByCreatedAtDesc(user);
        for (Ticket t : assigned) {
            t.setAssignedTo(null);
            ticketRepository.save(t);
        }

        // 3. Nullify actor in timeline entries by this user (history rows survive)
        timelineRepository.nullifyActor(userId);

        // 4. Delete notifications addressed to this user
        notificationRepository.deleteByUser(user);

        // 5. Finally delete the user
        userRepository.delete(user);
    }
}