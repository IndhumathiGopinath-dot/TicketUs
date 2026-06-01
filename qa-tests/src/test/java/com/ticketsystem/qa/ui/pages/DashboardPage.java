package com.ticketsystem.qa.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class DashboardPage extends BasePage {

    private final By heading    = By.cssSelector("h1");
    private final By statCards  = By.cssSelector(".stat-card");
    private final By ticketRows = By.cssSelector("table tbody tr");
    private final By logoutBtn  = By.xpath("//button[contains(text(),'Logout')]");

    public DashboardPage(WebDriver driver) { super(driver); }

    public boolean isLoaded() {
        try {
            wait.until(ExpectedConditions.urlContains("/dashboard"));
            waitVisible(heading);
            return true;
        } catch (Exception e) { return false; }
    }
    public boolean isAdminLoaded() {
        try {
            wait.until(ExpectedConditions.urlContains("/admin"));
            waitVisible(heading);
            return true;
        } catch (Exception e) { return false; }
    }

    public int ticketCount() { return driver.findElements(ticketRows).size(); }
    public int statCount() { return driver.findElements(statCards).size(); }

    public void logout() { click(logoutBtn); }

    @Override
    public String observed() {
        return "URL: " + driver.getCurrentUrl()
                + " | H1: " + textOrEmpty(heading)
                + " | Stats: " + statCount()
                + " | Tickets: " + ticketCount();
    }
}
