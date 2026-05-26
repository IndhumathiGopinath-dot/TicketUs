package com.ticketsystem.controller;

import com.ticketsystem.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal User user) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", user.getId());
        m.put("name", user.getName());
        m.put("email", user.getEmail());
        m.put("role", user.getRole());
        m.put("department", user.getDepartment());
        return ResponseEntity.ok(m);
    }

    @GetMapping("/common-issues")
    public ResponseEntity<Map<String, List<String>>> commonIssues() {
        Map<String, List<String>> issues = new HashMap<>();
        issues.put("IT", List.of(
                "Cannot connect to VPN",
                "Password reset required",
                "Email not syncing",
                "Software installation request",
                "Printer not working"
        ));
        issues.put("BUG", List.of(
                "Application crashes on login",
                "Page does not load properly",
                "Data not saving",
                "Slow performance",
                "Layout broken on mobile"
        ));
        issues.put("HR", List.of(
                "Leave balance inquiry",
                "Payroll discrepancy",
                "Update personal info",
                "Policy clarification",
                "Reimbursement request"
        ));
        return ResponseEntity.ok(issues);
    }
}
