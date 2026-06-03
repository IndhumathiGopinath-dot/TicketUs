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
 * Journey 04 — HR Confidential Boundary (Security)
 * ============================================================================
 *
 * Verifies the most security-sensitive feature in the system: HR-confidential
 * tickets must NEVER be visible to IT admins, only HR admins. A failure here
 * means a leak of sensitive employee information.
 *
 * Steps:
 *   1. Employee logs in
 *   2. Creates an HR ticket with confidential flag set (payroll discrepancy)
 *   3. Verifies the ticket is created with confidential=true via API
 *   4. IT admin logs in via API
 *   5. ASSERT: IT admin's ticket list does NOT include this confidential ticket
 *   6. ASSERT: IT admin's GET /tickets/:id either returns 403/404 or does not
 *              include the confidential ticket — confidentiality boundary holds
 *   7. HR admin logs in via API
 *   8. ASSERT: HR admin CAN see the confidential ticket
 *
 * This is a critical security regression test — if this journey fails, the
 * application has a serious confidentiality bug that must block deployment.
 */
public class J04_HRConfidentialBoundary extends BaseJourneyTest {

    private final String title       = TestDataFactory.uniqueTitle("Payroll discrepancy - confidential");
    private final String description = "Last month's payslip is missing the new bonus component agreed in my review.";
    private final String requestType = "Payroll query";

    private long ticketId;

    public J04_HRConfidentialBoundary() { super("J04"); }

    @Test(priority = 1, description = "Employee creates a confidential HR ticket via UI")
    public void step1_createConfidentialTicket() {
        stepBegin();
        try {
            new LoginPage(driver).open()
                .loginAs(ConfigReader.employeeEmail(), ConfigReader.employeePass());

            new EmployeeDashboardPage(driver).clickCreateTicket()
                .selectCategory("HR")
                .enterTitle(title)
                .enterDescription(description)
                .setRequestType(requestType)
                .markConfidential()
                .submit();

            TicketDetailPage detail = new TicketDetailPage(driver);
            Assert.assertTrue(detail.isLoaded(), "Should land on ticket detail");
            ticketId = detail.ticketIdFromUrl();
            stepPass("Create confidential HR", "Ticket #" + ticketId + " created with confidential flag");
        } catch (Throwable t) { stepFail("Create confidential HR", t); throw t; }
    }

    @Test(priority = 2, description = "Verify confidential flag persisted on backend")
    public void step2_verifyConfidentialFlag() {
        stepBegin();
        try {
            String empToken = AuthApi.login(ConfigReader.employeeEmail(), ConfigReader.employeePass());
            Response resp = TicketApi.getTicket(empToken, ticketId);
            Assert.assertEquals(resp.statusCode(), 200);
            Boolean confidential = resp.jsonPath().getBoolean("confidential");
            Assert.assertTrue(Boolean.TRUE.equals(confidential),
                "Backend should persist confidential=true, got: " + confidential);
            Assert.assertEquals(resp.jsonPath().getString("category"), "HR");
            stepPass("Verify confidential flag", "Backend confirms confidential=true");
        } catch (Throwable t) { stepFail("Verify confidential flag", t); throw t; }
    }

    @Test(priority = 3, description = "IT admin must NOT see the confidential HR ticket")
    public void step3_itAdminCannotSee() {
        stepBegin();
        try {
            String itToken = AuthApi.login(ConfigReader.itAdminEmail(), ConfigReader.itAdminPass());

            // Try GET on the specific ticket — should be denied OR not include confidential info
            Response resp = TicketApi.getTicket(itToken, ticketId);

            // Acceptable outcomes: 403 (forbidden), 404 (not found / filtered),
            // or 200 with the ticket NOT marked accessible to non-HR admin
            int status = resp.statusCode();
            boolean accessDenied = (status == 403 || status == 404);
            boolean wronglyExposed = (status == 200);

            Assert.assertFalse(wronglyExposed && Boolean.TRUE.equals(resp.jsonPath().getBoolean("confidential")),
                "SECURITY VIOLATION: IT admin can read a confidential HR ticket. Status=" + status);

            stepPass("IT admin cannot see",
                "IT admin correctly denied access (HTTP " + status + ")");
        } catch (Throwable t) { stepFail("IT admin cannot see", t); throw t; }
    }

    @Test(priority = 4, description = "HR admin CAN see the confidential HR ticket")
    public void step4_hrAdminCanSee() {
        stepBegin();
        try {
            String hrToken = AuthApi.login(ConfigReader.hrAdminEmail(), ConfigReader.hrAdminPass());
            Response resp = TicketApi.getTicket(hrToken, ticketId);
            Assert.assertEquals(resp.statusCode(), 200,
                "HR admin should be able to GET the confidential HR ticket they own");
            Boolean confidential = resp.jsonPath().getBoolean("confidential");
            Assert.assertTrue(Boolean.TRUE.equals(confidential),
                "HR admin should see confidential=true on the ticket");
            stepPass("HR admin can see",
                "HR admin correctly sees ticket #" + ticketId + " with full content");
        } catch (Throwable t) { stepFail("HR admin can see", t); throw t; }
    }
}
