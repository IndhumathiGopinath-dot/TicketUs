package com.ticketsystem.controller;

import com.ticketsystem.dto.TicketDtos;
import com.ticketsystem.model.Attachment;
import com.ticketsystem.model.Ticket;
import com.ticketsystem.model.TicketTimeline;
import com.ticketsystem.model.User;
import com.ticketsystem.model.enums.Category;
import com.ticketsystem.service.FileStorageService;
import com.ticketsystem.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final FileStorageService fileStorageService;

    public TicketController(TicketService ticketService, FileStorageService fileStorageService) {
        this.ticketService = ticketService;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping
    public ResponseEntity<TicketDtos.TicketResponse> create(
            @Valid @RequestBody TicketDtos.CreateTicketRequest req,
            @AuthenticationPrincipal User user) {
        Ticket t = ticketService.createTicket(req, user);
        return ResponseEntity.ok(ticketService.toResponse(t));
    }

    @GetMapping
    public ResponseEntity<List<TicketDtos.TicketResponse>> list(@AuthenticationPrincipal User user) {
        List<TicketDtos.TicketResponse> tickets = ticketService.getTicketsForUser(user).stream()
                .map(ticketService::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/assigned")
    public ResponseEntity<List<TicketDtos.TicketResponse>> assigned(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ticketService.getAssignedTickets(user).stream()
                .map(ticketService::toResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketDtos.TicketResponse> get(@PathVariable Long id, @AuthenticationPrincipal User user) {
        Ticket t = ticketService.getTicket(id, user);
        return ResponseEntity.ok(ticketService.toResponse(t));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<TicketDtos.TicketResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody TicketDtos.UpdateStatusRequest req,
            @AuthenticationPrincipal User user) {
        Ticket t = ticketService.updateStatus(id, req.getStatus(), req.getNotes(), user);
        return ResponseEntity.ok(ticketService.toResponse(t));
    }

    @PutMapping("/{id}/rate")
    public ResponseEntity<TicketDtos.TicketResponse> rate(
            @PathVariable Long id,
            @Valid @RequestBody TicketDtos.RatingRequest req,
            @AuthenticationPrincipal User user) {
        Ticket t = ticketService.rateTicket(id, req.getRating(), user);
        return ResponseEntity.ok(ticketService.toResponse(t));
    }

    @GetMapping("/{id}/timeline")
    public ResponseEntity<List<TicketDtos.TimelineEntryDto>> timeline(
            @PathVariable Long id, @AuthenticationPrincipal User user) {
        Ticket t = ticketService.getTicket(id, user);
        List<TicketDtos.TimelineEntryDto> result = ticketService.getTimeline(t).stream()
                .map(this::toTimelineDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/similar")
    public ResponseEntity<List<TicketDtos.TicketResponse>> similar(
            @RequestParam String title,
            @RequestParam Category category) {
        return ResponseEntity.ok(ticketService.findSimilarTickets(title, category).stream()
                .map(ticketService::toResponse)
                .collect(Collectors.toList()));
    }

    @PostMapping("/{id}/attachments")
    public ResponseEntity<Map<String, Object>> upload(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user) {
        Ticket t = ticketService.getTicket(id, user);
        Attachment a = fileStorageService.store(file, t);
        return ResponseEntity.ok(Map.of(
                "id", a.getId(),
                "fileName", a.getFileName(),
                "filePath", a.getFilePath(),
                "fileSize", a.getFileSize()
        ));
    }

    @GetMapping("/{id}/attachments")
    public ResponseEntity<List<Map<String, Object>>> listAttachments(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        Ticket t = ticketService.getTicket(id, user);
        List<Map<String, Object>> result = fileStorageService.getByTicket(t).stream()
                .map(a -> {
                    Map<String, Object> m = new java.util.HashMap<>();
                    m.put("id", a.getId());
                    m.put("fileName", a.getFileName());
                    m.put("filePath", a.getFilePath());
                    m.put("fileSize", a.getFileSize());
                    return m;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/attachments/download/{storedName}")
    public ResponseEntity<Resource> download(@PathVariable String storedName) throws Exception {
        Path path = fileStorageService.load(storedName);
        Resource resource = new UrlResource(path.toUri());
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    private TicketDtos.TimelineEntryDto toTimelineDto(TicketTimeline t) {
        return TicketDtos.TimelineEntryDto.builder()
                .id(t.getId())
                .action(t.getAction())
                .notes(t.getNotes())
                .actorName(t.getActor() != null ? t.getActor().getName() : "system")
                .createdAt(t.getCreatedAt())
                .build();
    }
}
