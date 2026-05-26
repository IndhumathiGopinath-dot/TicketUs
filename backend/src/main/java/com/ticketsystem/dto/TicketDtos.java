package com.ticketsystem.dto;

import com.ticketsystem.model.enums.Category;
import com.ticketsystem.model.enums.Priority;
import com.ticketsystem.model.enums.Severity;
import com.ticketsystem.model.enums.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class TicketDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateTicketRequest {
        @NotBlank
        private String title;
        @NotBlank
        private String description;
        @NotNull
        private Category category;
        private boolean confidential;
        private String requestType;       // HR
        private String osInfo;             // BUG
        private String browserInfo;        // BUG
        private String appVersion;         // BUG
        private Severity severity;         // BUG
        private String assetTag;           // IT
        private List<Long> relatedTicketIds;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TicketResponse {
        private Long id;
        private String title;
        private String description;
        private Category category;
        private Priority priority;
        private Status status;
        private boolean confidential;
        private String requestType;
        private String osInfo;
        private String browserInfo;
        private String appVersion;
        private Severity severity;
        private String assetTag;
        private String createdByName;
        private Long createdById;
        private String assignedToName;
        private Long assignedToId;
        private Integer estimatedResolutionHours;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime resolvedAt;
        private Integer satisfactionRating;
        private boolean escalated;
        private List<Long> relatedTicketIds;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateStatusRequest {
        @NotNull
        private Status status;
        private String notes;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AssignRequest {
        @NotNull
        private Long agentId;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RatingRequest {
        @NotNull
        private Integer rating; // 1 or -1
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TimelineEntryDto {
        private Long id;
        private String action;
        private String notes;
        private String actorName;
        private LocalDateTime createdAt;
    }
}
