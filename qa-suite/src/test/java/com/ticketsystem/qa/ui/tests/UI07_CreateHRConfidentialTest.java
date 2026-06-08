package com.ticketsystem.qa.ui.tests;

import com.ticketsystem.qa.support.ConfigReader;
import com.ticketsystem.qa.support.TestDataFactory;
import com.ticketsystem.qa.ui.pages.CreateTicketPage;
import com.ticketsystem.qa.ui.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * UI_07 — Create a confidential HR ticket.
 * Verifies the HR category exposes confidentiality controls and accepts the ticket.
 */
public class UI07_CreateHRConfidentialTest extends UiBaseTest {

    @Test(groups = {"regression", "ui", "tickets", "security"},
          description = "Employee creates a confidential HR ticket via the form")
    public void employeeCreatesConfidentialHRTicket() {
        new LoginPage(driver).open()
            .loginAs(ConfigReader.employeeEmail(), ConfigReader.employeePass());

        try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        CreateTicketPage create = new CreateTicketPage(driver).open();
        try { Thread.sleep(800); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        create.selectCategory("HR")
              .enterTitle(TestDataFactory.uniqueTitle("Workplace concern"))
              .enterDescription("Need to discuss a sensitive matter privately.")
              .checkConfidential()
              .submit();

        try { Thread.sleep(2500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        String url = driver.getCurrentUrl();
        Assert.assertTrue(url.contains("/ticket") || url.contains("/dashboard"),
            "Expected ticket-detail or dashboard URL. Got: " + url);
    }
}
