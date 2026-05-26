package com.ticketsystem.controller;

import com.ticketsystem.model.Notification;
import com.ticketsystem.model.User;
import com.ticketsystem.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(toResponse(service.getForUser(user)));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Map<String, Object>>> unread(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(toResponse(service.getUnread(user)));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> read(@PathVariable Long id) {
        service.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> readAll(@AuthenticationPrincipal User user) {
        service.markAllAsRead(user);
        return ResponseEntity.ok().build();
    }

    private List<Map<String, Object>> toResponse(List<Notification> notifications) {
        return notifications.stream().map(n -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", n.getId());
            m.put("message", n.getMessage());
            m.put("ticketId", n.getTicketId());
            m.put("read", n.isRead());
            m.put("createdAt", n.getCreatedAt());
            return m;
        }).collect(Collectors.toList());
    }
}
