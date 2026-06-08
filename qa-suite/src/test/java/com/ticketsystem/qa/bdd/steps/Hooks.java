package com.ticketsystem.qa.bdd.steps;

import com.ticketsystem.qa.support.DriverFactory;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

/**
 * Cucumber lifecycle hooks. Creates a fresh WebDriver per scenario (max isolation)
 * and ensures it gets quit no matter how the scenario ends.
 */
public class Hooks {

    private final ScenarioContext context;

    /** Picocontainer injects the shared ScenarioContext. */
    public Hooks(ScenarioContext context) {
        this.context = context;
    }

    @Before
    public void beforeScenario(Scenario scenario) {
        System.out.println("=== Cucumber scenario START: " + scenario.getName() + " ===");
        context.driver = DriverFactory.create();
    }

    @After
    public void afterScenario(Scenario scenario) {
        System.out.println("=== Cucumber scenario END: " + scenario.getName()
            + " — " + scenario.getStatus() + " ===");
        if (context.driver != null) {
            try { context.driver.quit(); } catch (Exception ignored) {}
        }
    }
}
