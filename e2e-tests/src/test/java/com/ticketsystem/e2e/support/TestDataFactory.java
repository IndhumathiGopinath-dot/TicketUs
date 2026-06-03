package com.ticketsystem.e2e.support;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Generates unique test data to avoid collisions across runs. Every run uses
 * fresh email addresses and ticket titles, so we never trip on the "duplicate
 * signup" rule or on similar-ticket suggestions from previous runs.
 */
public final class TestDataFactory {

    private static final String RUN_ID = LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

    private TestDataFactory() {}

    public static String uniqueEmail(String prefix) {
        String suffix = UUID.randomUUID().toString().substring(0, 6);
        return prefix + "_" + suffix + "@e2e.test";
    }

    public static String uniqueTitle(String prefix) {
        return prefix + " " + RUN_ID + "-" + shortId();
    }

    public static String shortId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public static String runId() {
        return RUN_ID;
    }
}
