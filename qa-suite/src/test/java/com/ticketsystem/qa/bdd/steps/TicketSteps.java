package com.ticketsystem.qa.bdd.steps;

import com.ticketsystem.qa.support.TestDataFactory;
import com.ticketsystem.qa.ui.pages.CreateTicketPage;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

/**
 * Step definitions for ticket-creation scenarios.
 */
public class TicketSteps {

    private final ScenarioContext ctx;

    public TicketSteps(ScenarioContext ctx) {
        this.ctx = ctx;
    }

    @When("the user creates an {string} ticket with title {string}")
    public void user_creates_ticket(String category, String title) {
        CreateTicketPage create = new CreateTicketPage(ctx.driver).open();
        sleep(800);
        create.selectCategory(category)
              .enterTitle(TestDataFactory.uniqueTitle(title))
              .enterDescription("Created by Cucumber scenario for " + category + " category.")
              .submit();
        sleep(2500);
    }

    @When("the user creates a confidential HR ticket with title {string}")
    public void user_creates_confidential_hr_ticket(String title) {
        CreateTicketPage create = new CreateTicketPage(ctx.driver).open();
        sleep(800);
        create.selectCategory("HR")
              .enterTitle(TestDataFactory.uniqueTitle(title))
              .enterDescription("Sensitive HR matter requiring privacy.")
              .checkConfidential()
              .submit();
        sleep(2500);
    }

    @Then("the ticket creation should redirect to detail or dashboard")
    public void redirected_after_creation() {
        String url = ctx.driver.getCurrentUrl();
        Assert.assertTrue(url.contains("/ticket") || url.contains("/dashboard"),
            "Expected redirect to ticket detail or dashboard. Got: " + url);
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
