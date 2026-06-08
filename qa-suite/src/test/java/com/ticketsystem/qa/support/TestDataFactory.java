package com.ticketsystem.qa.support;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Generates unique-per-run test data. Uses run timestamp + short UUID so
 * tests can be re-executed against the same database without colliding.
 */
public final class TestDataFactory {

    private static final String RUN_STAMP =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));

    private TestDataFactory() {}

    public static String uniqueEmail(String prefix) {
        return prefix + "_" + RUN_STAMP + "_" + shortId() + "@qa.test";
    }

    public static String uniqueTitle(String prefix) {
        return prefix + " [" + RUN_STAMP + "-" + shortId() + "]";
    }

    private static String shortId() {
        return UUID.randomUUID().toString().substring(0, 6);
    }
}
