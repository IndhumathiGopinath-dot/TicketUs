package com.ticketsystem.qa.bdd.steps;

import com.ticketsystem.qa.ui.pages.KnowledgeBasePage;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

/**
 * Step definitions for knowledge-base scenarios.
 */
public class KbSteps {

    private final ScenarioContext ctx;

    public KbSteps(ScenarioContext ctx) { this.ctx = ctx; }

    @When("the user opens the knowledge base")
    public void user_opens_kb() {
        new KnowledgeBasePage(ctx.driver).open();
        sleep(1000);
    }

    @When("the user searches knowledge base for {string}")
    public void user_searches_kb(String query) {
        new KnowledgeBasePage(ctx.driver).search(query);
        sleep(800);
    }

    @Then("the knowledge base page should remain visible")
    public void kb_visible() {
        KnowledgeBasePage kb = new KnowledgeBasePage(ctx.driver);
        Assert.assertTrue(kb.isOnKnowledgeBase(),
            "Expected to be on /knowledge. URL: " + ctx.driver.getCurrentUrl());
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
