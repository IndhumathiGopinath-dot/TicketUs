package com.ticketsystem.e2e.journeys;

import com.ticketsystem.e2e.api.AuthApi;
import com.ticketsystem.e2e.api.TicketApi;
import com.ticketsystem.e2e.pages.*;
import com.ticketsystem.e2e.support.BaseJourneyTest;
import com.ticketsystem.e2e.support.TestDataFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 * Journey 01 — New Employee Lifecycle
 * ============================================================================
 *
 * Verifies that a brand-new user can self-onboard, raise their first ticket
 * through the UI, and see the ticket reflected both in their dashboard and
 * via the backend API. This is the canonical "happy path" for an employee.
 *
 * Steps:
 *   1. Visit the landing page
 *   2. Click "Sign Up" — fill the signup form — submit
 *   3. Verify redirect to employee dashboard
 *   4. Open the ticket creation form
 *   5. Select IT category, fill title/description/asset tag, submit
 *   6. Verify redirect to ticket detail page; confirm status=OPEN
 *   7. Return to dashboard — confirm the new ticket appears in the list
 *   8. Verify via REST API that the ticket exists in the backend
 *
 * Pass criteria: every step completes without exception and assertions match.
 */
public class J01_NewEmployeeLifecycle extends BaseJourneyTest {

    private final String journeyName = "J01";
    private final String name        = "E2E TestUser " + TestDataFactory.shortId();
    private final String email       = TestDataFactory.uniqueEmail("emp_j01");
    private final String password    = "TestPass123!";
    private final String ticketTitle = TestDataFactory.uniqueTitle("My laptop screen flickering");
    private final String ticketDesc  = "Screen has been flickering since this morning. Tried restarting twice.";
    private final String assetTag    = "LAPTOP-J01-" + TestDataFactory.shortId();

    private String userToken;
    private long createdTicketId;

    public J01_NewEmployeeLifecycle() { super("J01"); }

    @Test(priority = 1, description = "Visit landing page and verify it loads")
    public void step1_visitLandingPage() {
        stepBegin();
        try {
            LandingPage landing = new LandingPage(driver).open();
            Assert.assertTrue(landing.isDisplayed(), "Landing page should render");
            stepPass("Visit landing", "Landing page loaded at /");
        } catch (Throwable t) { stepFail("Visit landing", t); throw t; }
    }

    @Test(priority = 2, description = "Sign up as a new employee")
    public void step2_signUp() {
        stepBegin();
        try {
            new SignupPage(driver).open()
                .enterName(name)
                .enterEmail(email)
                .enterPassword(password)
                .selectRole("EMPLOYEE")
                .enterDepartment("Engineering")
                .submit();
            Assert.assertTrue(driver.getCurrentUrl().contains("/dashboard"),
                "Should land on /dashboard after signup, was: " + driver.getCurrentUrl());
            stepPass("Sign up", "Account created for " + email);
        } catch (Throwable t) { stepFail("Sign up", t); throw t; }
    }

    @Test(priority = 3, description = "Verify employee dashboard loaded")
    public void step3_verifyDashboard() {
        stepBegin();
        try {
            EmployeeDashboardPage dash = new EmployeeDashboardPage(driver);
            Assert.assertTrue(dash.isLoaded(), "Dashboard should be loaded");
            stepPass("Dashboard loaded", "Employee dashboard rendered");
        } catch (Throwable t) { stepFail("Dashboard loaded", t); throw t; }
    }

    @Test(priority = 4, description = "Create an IT ticket via the UI")
    public void step4_createTicket() {
        stepBegin();
        try {
            new EmployeeDashboardPage(driver).clickCreateTicket()
                .selectCategory("IT")
                .enterTitle(ticketTitle)
                .enterDescription(ticketDesc)
                .setAssetTag(assetTag)
                .submit();
            TicketDetailPage detail = new TicketDetailPage(driver);
            Assert.assertTrue(detail.isLoaded(),
                "Should redirect to /ticket/:id, got: " + driver.getCurrentUrl());
            createdTicketId = detail.ticketIdFromUrl();
            stepPass("Create ticket", "Ticket #" + createdTicketId + " created via UI");
        } catch (Throwable t) { stepFail("Create ticket", t); throw t; }
    }

    @Test(priority = 5, description = "Verify the new ticket starts in OPEN status")
    public void step5_verifyOpenStatus() {
        stepBegin();
        try {
            TicketDetailPage detail = new TicketDetailPage(driver);
            String status = detail.readStatus().toUpperCase();
            Assert.assertTrue(status.contains("OPEN"),
                "Expected status OPEN, got: " + status);
            stepPass("Status OPEN", "Newly-created ticket is OPEN");
        } catch (Throwable t) { stepFail("Status OPEN", t); throw t; }
    }

    @Test(priority = 6, description = "Return to dashboard and find the new ticket in the list")
    public void step6_findOnDashboard() {
        stepBegin();
        try {
            EmployeeDashboardPage dash = new EmployeeDashboardPage(driver).open();
            Assert.assertTrue(dash.hasTicketWithTitle(ticketTitle),
                "Ticket '" + ticketTitle + "' should appear on dashboard");
            stepPass("Find on dashboard", "Ticket visible in user's dashboard");
        } catch (Throwable t) { stepFail("Find on dashboard", t); throw t; }
    }

    @Test(priority = 7, description = "Verify the ticket exists via the REST API")
    public void step7_verifyViaApi() {
        stepBegin();
        try {
            userToken = AuthApi.login(email, password);
            List<Map<String, Object>> tickets = TicketApi.listMyTickets(userToken);
            boolean found = tickets.stream()
                .anyMatch(t -> ticketTitle.equals(t.get("title")));
            Assert.assertTrue(found,
                "API should return the ticket created via UI. Found " + tickets.size() + " tickets.");
            stepPass("Verify via API", "Ticket confirmed in /tickets API response");
        } catch (Throwable t) { stepFail("Verify via API", t); throw t; }
    }
}
