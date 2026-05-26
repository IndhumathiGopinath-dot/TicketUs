package com.ticketsystem.controller;

import com.ticketsystem.dto.TicketDtos;
import com.ticketsystem.exception.AppException;
import com.ticketsystem.model.Ticket;
import com.ticketsystem.model.User;
import com.ticketsystem.model.enums.Role;
import com.ticketsystem.repository.UserRepository;
import com.ticketsystem.service.AnalyticsService;
import com.ticketsystem.service.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final TicketService ticketService;
    private final AnalyticsService analyticsService;

    public AdminController(UserRepository userRepository,
                           TicketService ticketService,
                           AnalyticsService analyticsService) {
        this.userRepository = userRepository;
        this.ticketService = ticketService;
        this.analyticsService = analyticsService;
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> analytics() {
        return ResponseEntity.ok(analyticsService.getDashboard());
    }

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> users() {
        List<Map<String, Object>> users = userRepository.findAll().stream()
                .map(u -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", u.getId());
                    m.put("name", u.getName());
                    m.put("email", u.getEmail());
                    m.put("role", u.getRole());
                    m.put("department", u.getDepartment());
                    return m;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/agents")
    public ResponseEntity<List<Map<String, Object>>> agents() {
        List<Map<String, Object>> agents = userRepository.findByRole(Role.ADMIN).stream()
                .map(u -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", u.getId());
                    m.put("name", u.getName());
                    m.put("email", u.getEmail());
                    m.put("department", u.getDepartment());
                    return m;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(agents);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal User actor) {
        if (actor.getId().equals(id)) {
            throw new AppException("Cannot delete yourself");
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }

    @PutMapping("/tickets/{id}/assign")
    public ResponseEntity<TicketDtos.TicketResponse> assign(
            @PathVariable Long id,
            @RequestBody TicketDtos.AssignRequest req,
            @AuthenticationPrincipal User actor) {
        Ticket t = ticketService.assignTicket(id, req.getAgentId(), actor);
        return ResponseEntity.ok(ticketService.toResponse(t));
    }
}
