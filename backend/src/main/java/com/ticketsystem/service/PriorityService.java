package com.ticketsystem.service;

import com.ticketsystem.model.enums.Category;
import com.ticketsystem.model.enums.Priority;
import com.ticketsystem.model.enums.Severity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PriorityService {

    private static final List<String> URGENT_KEYWORDS = List.of(
            "outage", "down", "cannot access", "production", "critical",
            "data loss", "security breach", "payroll error", "broken"
    );

    private static final List<String> HIGH_KEYWORDS = List.of(
            "error", "failed", "not working", "urgent", "asap", "blocker"
    );

    private static final List<String> LOW_KEYWORDS = List.of(
            "password reset", "how to", "request", "question", "minor"
    );

    public Priority computePriority(String title, String description, Category category, Severity severity) {
        String text = (safe(title) + " " + safe(description)).toLowerCase();

        // Bug severity overrides text-based detection
        if (category == Category.BUG && severity != null) {
            if (severity == Severity.CRITICAL) return Priority.URGENT;
            if (severity == Severity.HIGH) return Priority.HIGH;
            if (severity == Severity.LOW) return Priority.LOW;
        }

        if (containsAny(text, URGENT_KEYWORDS)) return Priority.URGENT;
        if (containsAny(text, HIGH_KEYWORDS)) return Priority.HIGH;
        if (containsAny(text, LOW_KEYWORDS)) return Priority.LOW;
        return Priority.NORMAL;
    }

    public Integer estimateResolutionHours(Priority priority, Category category) {
        // simple heuristic
        return switch (priority) {
            case URGENT -> 4;
            case HIGH -> 12;
            case NORMAL -> 24;
            case LOW -> 72;
        };
    }

    private boolean containsAny(String text, List<String> keywords) {
        return keywords.stream().anyMatch(text::contains);
    }

    private String safe(String s) { return s == null ? "" : s; }
}
