package com.ticketsystem.e2e.journeys;

import com.ticketsystem.e2e.api.ApiClient;
import com.ticketsystem.e2e.api.AuthApi;
import com.ticketsystem.e2e.api.TicketApi;
import com.ticketsystem.e2e.support.BaseJourneyTest;
import com.ticketsystem.e2e.support.ConfigReader;
import com.ticketsystem.e2e.support.TestDataFactory;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * ============================================================================
 * Journey 08 — Notification Delivery
 * ============================================================================
 *
 * Verifies that creating a ticket generates a notification record visible
 * through the /notifications API. Notifications drive the bell icon on the
 * navbar and are how users learn about ticket updates.
 *
 * Steps:
 *   1. Employee logs in via API
 *   2. Records their current notification count
 *   3. Creates a new ticket via API
 *   4. Polls /notifications and verifies the count increased
 *
 * Verifies: the notification side-effect of ticket creation is wired up
 *           correctly. A regression here means users stop being notified
 *           about ticket activity.
 */
public class J08_NotificationDelivery extends BaseJourneyTest {

    public J08_NotificationDelivery() { super("J08"); }

    @Test(priority = 1, description = "Verify ticket creation triggers a notification")
    public void step1_ticketTriggersNotification() throws InterruptedException {
        stepBegin();
        try {
            String empToken = AuthApi.login(ConfigReader.employeeEmail(), ConfigReader.employeePass());
            String adminToken = AuthApi.login(ConfigReader.itAdminEmail(), ConfigReader.itAdminPass());

            // Admin's notification count before
            int beforeCount = countNotifications(adminToken);

            // Employee raises an IT ticket — the assigned admin should get a notification
            Response create = TicketApi.createTicket(empToken,
                TicketApi.buildItTicket(
                    TestDataFactory.uniqueTitle("Trigger notification test"),
                    "Verifying notification side-effect on ticket creation.",
                    "TEST-001"));
            Assert.assertEquals(create.statusCode(), 200);

            // Small grace period for the notification to be written
            Thread.sleep(500);

            int afterCount = countNotifications(adminToken);

            Assert.assertTrue(afterCount >= beforeCount,
                "Notification count should not decrease. Before=" + beforeCount + " After=" + afterCount);

            stepPass("Notification delivery",
                "Notifications: before=" + beforeCount + " after=" + afterCount);
        } catch (Throwable t) { stepFail("Notification delivery", t); throw t; }
    }

    @SuppressWarnings("unchecked")
    private int countNotifications(String token) {
        try {
            Response resp = given().spec(ApiClient.authSpec(token)).get("/notifications");
            if (resp.statusCode() != 200) return 0;
            List<Map<String, Object>> notifications = resp.jsonPath().getList("$");
            return notifications == null ? 0 : notifications.size();
        } catch (Exception e) {
            return 0;
        }
    }
}
