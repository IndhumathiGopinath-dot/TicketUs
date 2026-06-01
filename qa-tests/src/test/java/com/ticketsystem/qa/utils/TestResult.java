package com.ticketsystem.qa.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * One row in the test-results Excel.
 *
 * Built fluently as the test progresses, then handed to ResultRecorder.
 * The {@code observed} field is meant to hold the *exact output displayed*
 * on screen / in the response body at verification time.
 */
public class TestResult {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String testId;
    private final String description;
    private String input = "";
    private String expected = "";
    private String actual = "";
    private String status = "";   // PASS | FAIL | ERROR
    private String observed = ""; // verbatim screen / response output
    private String error = "";
    private final String timestamp;

    private TestResult(String id, String desc) {
        this.testId = id == null ? "" : id;
        this.description = desc == null ? "" : desc;
        this.timestamp = LocalDateTime.now().format(TS);
    }

    public static TestResult of(String id, String desc) { return new TestResult(id, desc); }

    public TestResult input(String s)    { this.input = trunc(s); return this; }
    public TestResult expected(String s) { this.expected = trunc(s); return this; }
    public TestResult actual(String s)   { this.actual = trunc(s); return this; }
    public TestResult observed(String s) { this.observed = trunc(s); return this; }
    public TestResult error(String s)    { this.error = trunc(s); return this; }
    public TestResult pass(boolean ok)   { this.status = ok ? "PASS" : "FAIL"; return this; }
    public TestResult errored()          { this.status = "ERROR"; return this; }

    public String getTestId()      { return testId; }
    public String getDescription() { return description; }
    public String getInput()       { return input; }
    public String getExpected()    { return expected; }
    public String getActual()      { return actual; }
    public String getStatus()      { return status; }
    public String getObserved()    { return observed; }
    public String getError()       { return error; }
    public String getTimestamp()   { return timestamp; }

    private static String trunc(String s) {
        if (s == null) return "";
        return s.length() > 4000 ? s.substring(0, 3997) + "..." : s;
    }
}
