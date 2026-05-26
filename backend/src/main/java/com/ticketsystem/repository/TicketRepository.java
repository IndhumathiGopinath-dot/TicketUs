package com.ticketsystem.repository;

import com.ticketsystem.model.Ticket;
import com.ticketsystem.model.User;
import com.ticketsystem.model.enums.Category;
import com.ticketsystem.model.enums.Priority;
import com.ticketsystem.model.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByCreatedByOrderByCreatedAtDesc(User createdBy);

    List<Ticket> findByAssignedToOrderByCreatedAtDesc(User assignedTo);

    List<Ticket> findByCategory(Category category);

    List<Ticket> findByStatus(Status status);

    List<Ticket> findByPriority(Priority priority);

    @Query("SELECT t FROM Ticket t WHERE t.category = :category AND LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Ticket> findSimilarTickets(@Param("category") Category category, @Param("keyword") String keyword);

    @Query("SELECT t FROM Ticket t WHERE t.status <> 'RESOLVED' AND t.status <> 'CLOSED' AND t.createdAt < :threshold AND t.escalated = false")
    List<Ticket> findTicketsForEscalation(@Param("threshold") LocalDateTime threshold);

    @Query("SELECT t.category, COUNT(t) FROM Ticket t GROUP BY t.category")
    List<Object[]> countByCategory();

    @Query("SELECT t.status, COUNT(t) FROM Ticket t GROUP BY t.status")
    List<Object[]> countByStatus();

    @Query("SELECT t.assignedTo.name, COUNT(t) FROM Ticket t WHERE t.assignedTo IS NOT NULL GROUP BY t.assignedTo.name")
    List<Object[]> agentWorkload();

    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, t.createdAt, t.resolvedAt)) FROM Ticket t WHERE t.resolvedAt IS NOT NULL")
    Double averageResolutionHours();

    long countByStatus(Status status);
}
