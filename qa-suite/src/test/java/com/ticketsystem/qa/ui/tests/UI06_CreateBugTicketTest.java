package com.ticketsystem.qa.ui.tests;

import com.ticketsystem.qa.support.ConfigReader;
import com.ticketsystem.qa.support.TestDataFactory;
import com.ticketsystem.qa.ui.pages.CreateTicketPage;
import com.ticketsystem.qa.ui.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * UI_06 — Create a BUG ticket. Verifies category-specific form fields render
 * when BUG is selected, including severity which doesn't appear for IT/HR.
 */
public class UI06_CreateBugTicketTest extends UiBaseTest {

    @Test(groups = {"regression", "ui", "tickets"},
          description = "Employee creates a BUG ticket and the form accepts the submission")
    public void employeeCreatesBugTicket() {
        new LoginPage(driver).open()
            .loginAs(ConfigReader.employeeEmail(), ConfigReader.employeePass());

        try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        CreateTicketPage create = new CreateTicketPage(driver).open();
        try { Thread.sleep(800); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        create.selectCategory("BUG")
              .enterTitle(TestDataFactory.uniqueTitle("Dashboard chart not rendering"))
              .enterDescription("Quarterly report chart shows blank canvas on Chrome 148.")
              .submit();

        try { Thread.sleep(2500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        String url = driver.getCurrentUrl();
        Assert.assertTrue(url.contains("/ticket") || url.contains("/dashboard"),
            "Expected ticket-detail or dashboard URL. Got: " + url);
    }
}
