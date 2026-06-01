package com.ticketsystem.qa.bdd.steps;

import org.openqa.selenium.WebDriver;

/**
 * Holds per-scenario state shared between step definition classes
 * (driver, last ticket created, etc.). Reset by Hooks @After.
 */
public class ScenarioContext {
    public WebDriver driver;
    public String token;
    public String lastTicketId;
    public String lastTicketPriority;
    public String lastTicketStatus;
    public String currentScenarioName;
}
