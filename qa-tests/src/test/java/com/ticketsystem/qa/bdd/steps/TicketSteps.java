package com.ticketsystem.qa.bdd.steps;

import com.ticketsystem.qa.ui.pages.CreateTicketPage;
import com.ticketsystem.qa.ui.pages.TicketDetailPage;
import com.ticketsystem.qa.utils.ConfigReader;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.testng.Assert.*;

public class TicketSteps {

    private final ScenarioContext ctx;
    private TicketDetailPage detail;

    public TicketSteps(ScenarioContext ctx) { this.ctx = ctx; }

    @When("I create an {string} ticket titled {string} with description {string}")
    public void createSimpleTicket(String category, String title, String description) {
        new CreateTicketPage(ctx.driver).open(ConfigReader.get("app.base.url"))
                .selectCategory(category)
                .enterTitle(title)
                .enterDescription(description)
                .submit();
        detail = new TicketDetailPage(ctx.driver);
        assertTrue(detail.isLoaded(), "Ticket detail page should load after create");
    }

    @When("I create a confidential {string} ticket titled {string} with description {string} of request type {string}")
    public void createConfidentialHrTicket(String category, String title, String description, String reqType) {
        new CreateTicketPage(ctx.driver).open(ConfigReader.get("app.base.url"))
                .selectCategory(category)
                .enterTitle(title)
                .enterDescription(description)
                .enterRequestType(reqType)
                .checkConfidential()
                .submit();
        detail = new TicketDetailPage(ctx.driver);
        assertTrue(detail.isLoaded(), "Ticket detail page should load after create");
    }

    @Then("the ticket should be created with status {string}")
    public void ticketStatusIs(String status) {
        assertTrue(detail.hasBadge(status),
                "Expected status badge '" + status + "', got badges=" + detail.badges());
    }

    @And("the ticket priority should be {string}")
    public void ticketPriorityIs(String priority) {
        assertTrue(detail.hasBadge(priority),
                "Expected priority badge '" + priority + "', got badges=" + detail.badges());
    }
}
