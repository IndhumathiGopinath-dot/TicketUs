package com.ticketsystem.qa.bdd.steps;

import com.ticketsystem.qa.utils.DriverFactory;
import com.ticketsystem.qa.utils.ResultRecorder;
import com.ticketsystem.qa.utils.TestResult;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

public class Hooks {

    private final ScenarioContext ctx;

    public Hooks(ScenarioContext ctx) { this.ctx = ctx; }

    @Before
    public void beforeScenario(Scenario s) {
        ctx.driver = DriverFactory.create();
        ctx.currentScenarioName = s.getName();
        ctx.token = null;
        ctx.lastTicketId = null;
        ctx.lastTicketPriority = null;
        ctx.lastTicketStatus = null;
    }

    @After
    public void afterScenario(Scenario s) {
        // Record one BDD row per scenario into the Excel results.
        boolean passed = !s.isFailed();
        TestResult tr = TestResult.of("BDD-" + Math.abs(s.getId().hashCode() % 100000),
                                       s.getName())
                .input("Cucumber scenario: " + s.getName())
                .expected("All scenario steps pass")
                .actual(passed ? "All steps passed" : "One or more steps failed")
                .observed(ctx.driver != null
                        ? "Final URL: " + ctx.driver.getCurrentUrl()
                        : "(no driver)")
                .pass(passed);
        ResultRecorder.record("BDD", tr);

        if (ctx.driver != null) {
            try { ctx.driver.quit(); } catch (Throwable ignored) {}
            ctx.driver = null;
        }
    }
}
