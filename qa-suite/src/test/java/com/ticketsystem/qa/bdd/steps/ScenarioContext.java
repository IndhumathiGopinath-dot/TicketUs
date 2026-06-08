package com.ticketsystem.qa.bdd.steps;

import org.openqa.selenium.WebDriver;

/**
 * Picocontainer-managed bean shared across step classes in the same scenario.
 * Cucumber's picocontainer DI creates one instance per scenario and injects it
 * into any step class that has it as a constructor parameter.
 */
public class ScenarioContext {
    public WebDriver driver;
    public String currentUserEmail;
    public String currentUserToken;
    public Long lastTicketId;
}
