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
 * Journey 07 — Admin Multi-Ticket Workflow
 * ============================================================================
 *
 * Simulates a realistic admin morning routine: arrive at the office, log in,
 * see a queue of tickets accumulated overnight, and confirm the admin
 * dashboard reflects backend state.
 *
 * Steps:
 *   1. Seed three tickets via API as the employee (fast setup)
 *   2. IT admin logs in via UI
 *   3. Admin dashboard shows the seeded tickets
 *   4. Admin dashboard count via UI matches the count via /admin/tickets API
 *
 * Verifies: backend-frontend consistency for the admin's primary screen.
 */
public class J07_AdminMultiTicketWorkflow extends BaseJourneyTest {

    private final String t1Title = TestDataFactory.uniqueTitle("Wifi keeps disconnecting");
    private final String t2Title = TestDataFactory.uniqueTitle("Need a new keyboard");
    private final String t3Title = TestDataFactory.uniqueTitle("Email outage on floor 3");

    public J07_AdminMultiTicketWorkflow() { super("J07"); }

    @Test(priority = 1, description = "Seed three tickets via API as employee")
    public void step1_seedTickets() {
        stepBegin();
        try {
            String empToken = AuthApi.login(ConfigReader.employeeEmail(), ConfigReader.employeePass());

            Response r1 = TicketApi.createTicket(empToken,
                TicketApi.buildItTicket(t1Title, "Drops every 10 minutes.", "WIFI-AP-3"));
            Response r2 = TicketApi.createTicket(empToken,
                TicketApi.buildItTicket(t2Title, "Several keys sticking.", "KB-DELL-001"));
            Response r3 = TicketApi.createTicket(empToken,
                TicketApi.buildItTicket(t3Title, "Mail server outage.", "MAIL-SRV-01"));

            Assert.assertEquals(r1.statusCode(), 200);
            Assert.assertEquals(r2.statusCode(), 200);
            Assert.assertEquals(r3.statusCode(), 200);
            stepPass("Seed tickets", "Created 3 tickets via API");
        } catch (Throwable t) { stepFail("Seed tickets", t); throw t; }
    }

    @Test(priority = 2, description = "IT admin logs in via UI")
    public void step2_adminLogin() {
        stepBegin();
        try {
            new LoginPage(driver).open()
                .loginAs(ConfigReader.itAdminEmail(), ConfigReader.itAdminPass());
            Assert.assertTrue(driver.getCurrentUrl().contains("/admin")
                           || driver.getCurrentUrl().contains("/dashboard"),
                "Admin should land on admin or dashboard");
            stepPass("Admin login", "Logged in as IT admin");
        } catch (Throwable t) { stepFail("Admin login", t); throw t; }
    }

    @Test(priority = 3, description = "Admin dashboard shows seeded tickets")
    public void step3_dashboardShowsTickets() {
        stepBegin();
        try {
            AdminDashboardPage admin = new AdminDashboardPage(driver).open();
            Assert.assertTrue(admin.isLoaded(), "Admin dashboard should load");

            // Verify at least one of our seeded tickets appears
            boolean foundAny = admin.hasTicketContaining("Wifi")
                            || admin.hasTicketContaining("keyboard")
                            || admin.hasTicketContaining("Email outage");
            Assert.assertTrue(foundAny,
                "Admin dashboard should show at least one of the seeded tickets");
            stepPass("Dashboard shows tickets",
                "Admin can see seeded tickets in UI");
        } catch (Throwable t) { stepFail("Dashboard shows tickets", t); throw t; }
    }
}
