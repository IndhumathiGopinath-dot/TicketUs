package com.ticketsystem.model;

import com.ticketsystem.model.enums.Category;
import com.ticketsystem.model.enums.Priority;
import com.ticketsystem.model.enums.Severity;
import com.ticketsystem.model.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tickets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "is_confidential")
    private boolean confidential;

    // HR-specific predefined request type
    @Column(name = "request_type")
    private String requestType; // e.g., leave_request, payroll_query, policy_clarification

    // Bug-specific fields
    @Column(name = "os_info")
    private String osInfo;

    @Column(name = "browser_info")
    private String browserInfo;

    @Column(name = "app_version")
    private String appVersion;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    // IT-specific
    @Column(name = "asset_tag")
    private String assetTag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @Column(name = "estimated_resolution_hours")
    private Integer estimatedResolutionHours;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    // 1 = up, -1 = down, 0/null = not rated
    @Column(name = "satisfaction_rating")
    private Integer satisfactionRating;

    @Column(name = "is_escalated")
    private boolean escalated;

    // Linked related tickets (e.g., recurring bugs)
    @ManyToMany
    @JoinTable(
        name = "ticket_links",
        joinColumns = @JoinColumn(name = "ticket_id"),
        inverseJoinColumns = @JoinColumn(name = "related_ticket_id")
    )
    private Set<Ticket> relatedTickets = new HashSet<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
