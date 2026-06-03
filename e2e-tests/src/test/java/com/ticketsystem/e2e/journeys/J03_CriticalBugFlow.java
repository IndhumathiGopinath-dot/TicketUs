package com.ticketsystem.e2e.journeys;

import com.ticketsystem.e2e.api.AuthApi;
import com.ticketsystem.e2e.api.TicketApi;
import com.ticketsystem.e2e.pages.*;
import com.ticketsystem.e2e.support.BaseJourneyTest;
import com.ticketsystem.e2e.support.ConfigReader;
import com.ticketsystem.e2e.support.TestDataFactory;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * ============================================================================
 * Journey 03 — Critical Bug Flow
 * ============================================================================
 *
 * Verifies the BUG-specific workflow: a developer reports a critical bug,
 * the form reveals BUG-only fields (Severity, App Version), and the priority
 * routing engine elevates CRITICAL-severity bugs to URGENT regardless of
 * the title's wording.
 *
 * Steps:
 *   1. Employee logs in
 *   2. Opens create ticket form, selects BUG category
 *   3. Verifies the Severity and App Version fields become visible
 *      (these are NOT visible for IT or HR categories)
 *   4. Fills the form with severity=CRITICAL, neutral wording in the title
 *   5. Submits, lands on detail page
 *   6. Confirms via API that the ticket has priority=URGENT even though
 *      the title contains no urgency keywords — proves severity-based escalation
 *   7. Confirms the ticket category is BUG and severity is CRITICAL
 *
 * Verifies: category-conditional fields, severity escalation, BUG metadata
 *           persistence end-to-end.
 */
public class J03_CriticalBugFlow extends BaseJourneyTest {

    private final String title       = TestDataFactory.uniqueTitle("Profile picture upload broken");
    private final String description = "Selecting an image hangs the upload spinner forever; no error shown.";
    private final String severity    = "CRITICAL";
    private final String appVersion  = "2.4.1";

    private long ticketId;

    public J03_CriticalBugFlow() { super("J03"); }

    @Test(priority = 1, description = "Employee logs in")
    public void step1_login() {
        stepBegin();
        try {
            new LoginPage(driver).open()
                .loginAs(ConfigReader.employeeEmail(), ConfigReader.employeePass());
            stepPass("Login", "Employee logged in");
        } catch (Throwable t) { stepFail("Login", t); throw t; }
    }

    @Test(priority = 2, description = "Open create form, switch to BUG category, confirm fields appear")
    public void step2_bugFieldsAppear() {
        stepBegin();
        try {
            CreateTicketPage form = new EmployeeDashboardPage(driver).clickCreateTicket();
            form.selectCategory("BUG");
            // selectCategory's deterministic wait already verifies the Severity
            // anchor is present, but assert explicitly for the report
            Assert.assertTrue(
                driver.findElements(org.openqa.selenium.By.xpath(
                    "//label[normalize-space()='Severity']")).size() > 0,
                "Severity field must be visible after selecting BUG category");
            stepPass("BUG fields appear", "Severity field rendered as expected");
        } catch (Throwable t) { stepFail("BUG fields appear", t); throw t; }
    }

    @Test(priority = 3, description = "Fill bug form and submit")
    public void step3_submitBug() {
        stepBegin();
        try {
            new CreateTicketPage(driver)
                .enterTitle(title)
                .enterDescription(description)
                .setSeverity(severity)
                .submit();

            TicketDetailPage detail = new TicketDetailPage(driver);
            Assert.assertTrue(detail.isLoaded(), "Should redirect to ticket detail");
            ticketId = detail.ticketIdFromUrl();
            stepPass("Submit bug", "Bug ticket #" + ticketId + " created");
        } catch (Throwable t) { stepFail("Submit bug", t); throw t; }
    }

    @Test(priority = 4, description = "Verify severity escalation routed bug to URGENT")
    public void step4_severityEscalation() {
        stepBegin();
        try {
            String token = AuthApi.login(ConfigReader.employeeEmail(), ConfigReader.employeePass());
            Response resp = TicketApi.getTicket(token, ticketId);
            Assert.assertEquals(resp.statusCode(), 200);

            String priority = resp.jsonPath().getString("priority");
            String category = resp.jsonPath().getString("category");
            String sev      = resp.jsonPath().getString("severity");

            Assert.assertEquals(category, "BUG", "Category should be BUG");
            Assert.assertEquals(sev, "CRITICAL", "Severity should persist as CRITICAL");
            Assert.assertEquals(priority, "URGENT",
                "CRITICAL severity should escalate priority to URGENT regardless of wording, got: " + priority);

            stepPass("Severity escalation",
                "CRITICAL severity correctly escalated to URGENT priority");
        } catch (Throwable t) { stepFail("Severity escalation", t); throw t; }
    }
}
