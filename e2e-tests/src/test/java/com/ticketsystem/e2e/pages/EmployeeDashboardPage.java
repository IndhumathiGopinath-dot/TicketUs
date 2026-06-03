package com.ticketsystem.e2e.pages;

import com.ticketsystem.e2e.support.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * The employee dashboard at "/dashboard". Shows the current user's tickets.
 */
public class EmployeeDashboardPage extends BasePage {

    private final By createButton      = By.xpath("//a[contains(@href,'/create')] | //button[contains(text(),'Create') or contains(text(),'New ticket')]");
    private final By ticketRows        = By.cssSelector(".ticket-row, .ticket-card, [data-testid='ticket-row']");
    private final By dashboardHeading  = By.xpath("//h1[contains(text(),'Dashboard')] | //h1[contains(text(),'My Tickets')] | //*[contains(@class,'dashboard')]");
    private final By statusFilter      = By.cssSelector("select[name='statusFilter'], [data-filter='status']");

    public EmployeeDashboardPage(WebDriver driver) {
        super(driver);
    }

    public EmployeeDashboardPage open() {
        driver.get(ConfigReader.baseUrl() + "/dashboard");
        wait.until(ExpectedConditions.or(
            ExpectedConditions.visibilityOfElementLocated(dashboardHeading),
            ExpectedConditions.visibilityOfElementLocated(createButton)
        ));
        return this;
    }

    public boolean isLoaded() {
        return driver.getCurrentUrl().contains("/dashboard");
    }

    public CreateTicketPage clickCreateTicket() {
        jsClick(waitClickable(createButton));
        wait.until(ExpectedConditions.urlContains("/create"));
        return new CreateTicketPage(driver);
    }

    public List<WebElement> visibleTickets() {
        return driver.findElements(ticketRows);
    }

    public boolean hasTicketWithTitle(String title) {
        return driver.findElements(By.xpath("//*[contains(text(),'" + title + "')]")).size() > 0;
    }

    public int ticketCount() {
        return visibleTickets().size();
    }
}
