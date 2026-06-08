package com.ticketsystem.qa.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;

/**
 * Page Object for /dashboard (employee) and /admin (admin). Both share the
 * same brand bar and ticket-list pattern so one class covers both views.
 */
public class DashboardPage extends BasePage {

    @FindBy(how = How.XPATH, using = "//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'create') or contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'new ticket')]")
    private WebElement createTicketButton;

    @FindBy(how = How.XPATH, using = "//nav | //*[contains(@class,'navbar')]")
    private WebElement navbar;

    public DashboardPage(WebDriver driver) { super(driver); }

    public boolean isOnEmployeeDashboard() {
        return driver.getCurrentUrl().contains("/dashboard");
    }

    public boolean isOnAdminDashboard() {
        return driver.getCurrentUrl().contains("/admin");
    }

    public boolean isLoaded() {
        return isOnEmployeeDashboard() || isOnAdminDashboard();
    }

    public int ticketRowCount() {
        return driver.findElements(By.xpath(
            "//*[contains(@class,'ticket') and (self::div or self::tr or self::li)]"
        )).size();
    }

    public CreateTicketPage clickCreate() {
        waitClickable(createTicketButton);
        jsClick(createTicketButton);
        waitUrlContains("/create");
        return new CreateTicketPage(driver);
    }
}
