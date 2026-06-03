package com.ticketsystem.e2e.journeys;

import com.ticketsystem.e2e.api.AuthApi;
import com.ticketsystem.e2e.api.TicketApi;
import com.ticketsystem.e2e.support.BaseJourneyTest;
import com.ticketsystem.e2e.support.ConfigReader;
import com.ticketsystem.e2e.support.TestDataFactory;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * ============================================================================
 * Journey 06 — Priority Routing Matrix
 * ============================================================================
 *
 * Exercises the auto-priority routing engine across a matrix of inputs to
 * verify the algorithm's behaviour. Uses the API directly (not the UI) so we
 * can rapidly test many combinations — the UI workflow is already covered
 * in J01-J04.
 *
 * Test matrix:
 *   - "outage"        in title → URGENT  (keyword)
 *   - "down"          in title → URGENT  (keyword)
 *   - "critical"      in description → URGENT (keyword)
 *   - "password reset" in title → LOW (low-impact keyword)
 *   - "documentation" in title → NORMAL (no special keyword)
 *   - HR confidential → URGENT (confidentiality always elevates)
 *
 * Verifies: the priority detection rules are correctly applied. This is a
 * pure backend behavioural test exposed as a journey because it's central
 * to the system's value proposition (smart routing).
 */
public class J06_PriorityRoutingMatrix extends BaseJourneyTest {

    private String token;

    public J06_PriorityRoutingMatrix() { super("J06"); }

    @Test(priority = 0, description = "Acquire employee token for ticket creation")
    public void step0_getToken() {
        stepBegin();
        try {
            token = AuthApi.login(ConfigReader.employeeEmail(), ConfigReader.employeePass());
            Assert.assertNotNull(token);
            stepPass("Get token", "Employee token acquired");
        } catch (Throwable t) { stepFail("Get token", t); throw t; }
    }

    @Test(priority = 1, description = "'outage' keyword → URGENT")
    public void step1_outageUrgent() {
        verifyPriority(
            "outage",
            TicketApi.buildItTicket(
                TestDataFactory.uniqueTitle("Network outage in office"),
                "Whole floor offline.",
                "ROUTER-01"),
            "URGENT");
    }

    @Test(priority = 2, description = "'down' keyword → URGENT")
    public void step2_downUrgent() {
        verifyPriority(
            "down",
            TicketApi.buildItTicket(
                TestDataFactory.uniqueTitle("VPN is down"),
                "Cannot reach internal resources.",
                "VPN-LAPTOP"),
            "URGENT");
    }

    @Test(priority = 3, description = "'password reset' → LOW")
    public void step3_passwordResetLow() {
        verifyPriority(
            "password reset",
            TicketApi.buildItTicket(
                TestDataFactory.uniqueTitle("Password reset request"),
                "Forgot my password after the long weekend.",
                null),
            "LOW");
    }

    @Test(priority = 4, description = "Neutral wording → NORMAL")
    public void step4_neutralNormal() {
        verifyPriority(
            "neutral",
            TicketApi.buildItTicket(
                TestDataFactory.uniqueTitle("Documentation update needed"),
                "Please review when you have a chance.",
                null),
            "NORMAL");
    }

   @Test(priority = 5, description = "BUG ticket with CRITICAL severity → URGENT regardless of wording")
public void step5_criticalBugUrgent() {
    verifyPriority(
        "critical bug",
        TicketApi.buildBugTicket(
            TestDataFactory.uniqueTitle("Routine display tweak"),
            "Just a small visual issue.",
            "CRITICAL",
            "2.4.1"),
        "URGENT");
}

    /** Helper: create a ticket and assert its priority matches expected. */
    private void verifyPriority(String label, Map<String, Object> body, String expected) {
        stepBegin();
        try {
            Response resp = TicketApi.createTicket(token, body);
            Assert.assertEquals(resp.statusCode(), 200,
                "Create ticket should succeed for: " + label);
            String actual = resp.jsonPath().getString("priority");
            Assert.assertEquals(actual, expected,
                "Priority mismatch for '" + label + "': expected " + expected + " got " + actual);
            stepPass("Routing: " + label, "Correctly assigned " + expected);
        } catch (Throwable t) { stepFail("Routing: " + label, t); throw t; }
    }
}
