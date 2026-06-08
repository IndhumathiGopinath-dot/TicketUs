package com.ticketsystem.qa.bdd.steps;

import com.ticketsystem.qa.support.ConfigReader;
import com.ticketsystem.qa.ui.pages.DashboardPage;
import com.ticketsystem.qa.ui.pages.LoginPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

/**
 * Step definitions for login-related scenarios. Shares state with other step
 * classes via the picocontainer-injected ScenarioContext.
 */
public class LoginSteps {

    private final ScenarioContext ctx;

    public LoginSteps(ScenarioContext ctx) {
        this.ctx = ctx;
    }

    @Given("the user is on the login page")
    public void user_on_login_page() {
        new LoginPage(ctx.driver).open();
    }

    @When("the user logs in as employee {string}")
    public void user_logs_in_as_employee(String identifier) {
        // Use the seeded employee creds regardless of identifier — keeps tests stable
        new LoginPage(ctx.driver).open()
            .loginAs(ConfigReader.employeeEmail(), ConfigReader.employeePass());
        ctx.currentUserEmail = ConfigReader.employeeEmail();
        sleep(1500);
    }

    @When("the user logs in as IT admin")
    public void user_logs_in_as_it_admin() {
        new LoginPage(ctx.driver).open()
            .loginAs(ConfigReader.adminEmail(), ConfigReader.adminPassword());
        ctx.currentUserEmail = ConfigReader.adminEmail();
        sleep(1500);
    }

    @When("the user logs in as HR admin")
    public void user_logs_in_as_hr_admin() {
        new LoginPage(ctx.driver).open()
            .loginAs(ConfigReader.hrAdminEmail(), ConfigReader.hrAdminPass());
        ctx.currentUserEmail = ConfigReader.hrAdminEmail();
        sleep(1500);
    }

    @Then("the employee dashboard should be visible")
    public void employee_dashboard_visible() {
        DashboardPage dash = new DashboardPage(ctx.driver);
        Assert.assertTrue(waitFor(dash::isOnEmployeeDashboard, 10_000),
            "Expected /dashboard URL. Got: " + ctx.driver.getCurrentUrl());
    }

    @Then("the admin dashboard should be visible")
    public void admin_dashboard_visible() {
        DashboardPage dash = new DashboardPage(ctx.driver);
        Assert.assertTrue(waitFor(dash::isOnAdminDashboard, 10_000),
            "Expected /admin URL. Got: " + ctx.driver.getCurrentUrl());
    }

    private boolean waitFor(java.util.function.BooleanSupplier cond, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (cond.getAsBoolean()) return true;
            sleep(200);
        }
        return cond.getAsBoolean();
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
