package com.ticketsystem.repository;

import com.ticketsystem.model.Attachment;
import com.ticketsystem.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByTicket(Ticket ticket);

    /** Used during ticket-deletion cascade. */
    void deleteByTicket(Ticket ticket);
}
