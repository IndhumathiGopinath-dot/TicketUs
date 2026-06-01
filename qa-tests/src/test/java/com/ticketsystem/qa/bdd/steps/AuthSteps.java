package com.ticketsystem.qa.bdd.steps;

import com.ticketsystem.qa.ui.pages.DashboardPage;
import com.ticketsystem.qa.ui.pages.LoginPage;
import com.ticketsystem.qa.utils.ConfigReader;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.java.en.And;

import static org.testng.Assert.*;

public class AuthSteps {

    private final ScenarioContext ctx;
    private LoginPage loginPage;

    public AuthSteps(ScenarioContext ctx) { this.ctx = ctx; }

    @Given("the application is running")
    public void appIsRunning() {
        ctx.driver.get(ConfigReader.get("app.base.url"));
    }

    @Given("I am on the login page")
    public void onLoginPage() {
        loginPage = new LoginPage(ctx.driver).open(ConfigReader.get("app.base.url"));
    }

    @When("I login as {string} with password {string}")
    public void iLoginAs(String email, String password) {
        if (loginPage == null) loginPage = new LoginPage(ctx.driver).open(ConfigReader.get("app.base.url"));
        loginPage.login(email, password);
    }

    @Then("I should land on the employee dashboard")
    public void onEmployeeDashboard() {
        assertTrue(new DashboardPage(ctx.driver).isLoaded(),
                "Expected to be on /dashboard, was on " + ctx.driver.getCurrentUrl());
    }

    @Then("I should land on the admin console")
    public void onAdminConsole() {
        assertTrue(new DashboardPage(ctx.driver).isAdminLoaded(),
                "Expected to be on /admin, was on " + ctx.driver.getCurrentUrl());
    }

    @Then("I should remain on the login page")
    public void onLogin() {
        assertTrue(new LoginPage(ctx.driver).isOnLoginPage(),
                "Expected to stay on /login, was on " + ctx.driver.getCurrentUrl());
    }

    @And("I should see at least {int} stat card")
    public void seeAtLeastNStatCards(int n) {
        int count = new DashboardPage(ctx.driver).statCount();
        assertTrue(count >= n, "Expected ≥ " + n + " stat cards, found " + count);
    }

    @Given("I am logged in as an employee")
    public void loggedInAsEmployee() {
        new LoginPage(ctx.driver).open(ConfigReader.get("app.base.url"))
                .login(ConfigReader.get("employee.email"), ConfigReader.get("employee.password"));
        assertTrue(new DashboardPage(ctx.driver).isLoaded(),
                "Employee login should land on /dashboard");
    }
}
