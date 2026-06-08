package com.ticketsystem.qa.ui.tests;

import com.ticketsystem.qa.support.ConfigReader;
import com.ticketsystem.qa.support.TestDataFactory;
import com.ticketsystem.qa.ui.pages.CreateTicketPage;
import com.ticketsystem.qa.ui.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * UI_05 — Create an IT ticket via the UI form.
 * Verifies the create-ticket workflow end-to-end through the browser.
 */
public class UI05_CreateITTicketTest extends UiBaseTest {

    @Test(groups = {"smoke", "ui", "tickets"},
          description = "Employee logs in and creates an IT ticket via the form")
    public void employeeCreatesITTicket() {
        // Login as employee
        new LoginPage(driver).open()
            .loginAs(ConfigReader.employeeEmail(), ConfigReader.employeePass());

        try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        // Navigate to create page
        CreateTicketPage create = new CreateTicketPage(driver).open();
        try { Thread.sleep(800); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        create.selectCategory("IT")
              .enterTitle(TestDataFactory.uniqueTitle("Need help with VPN access"))
              .enterDescription("Cannot connect to corporate VPN from home network.")
              .submit();

        try { Thread.sleep(2500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        // After submit we should be on either /dashboard or /ticket/:id
        String url = driver.getCurrentUrl();
        Assert.assertTrue(url.contains("/ticket") || url.contains("/dashboard"),
            "Expected redirect to ticket detail or dashboard. Got: " + url);
    }
}
