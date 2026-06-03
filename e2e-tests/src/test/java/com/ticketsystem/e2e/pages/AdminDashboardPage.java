package com.ticketsystem.e2e.pages;

import com.ticketsystem.e2e.support.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * The admin console at "/admin" — admins see all (or department-filtered)
 * tickets and can assign, change status, manage users.
 */
public class AdminDashboardPage extends BasePage {

    private final By adminHeading   = By.xpath("//h1[contains(text(),'Admin')] | //h2[contains(text(),'Admin')]");
    private final By ticketsTab     = By.xpath("//*[self::a or self::button][contains(text(),'Tickets')]");
    private final By usersTab       = By.xpath("//*[self::a or self::button][contains(text(),'Users')]");
    private final By ticketRows     = By.cssSelector("tr[data-ticket-id], .admin-ticket-row, [data-testid='admin-ticket']");
    private final By ticketTitles   = By.cssSelector(".ticket-title, td.title, [data-col='title']");

    public AdminDashboardPage(WebDriver driver) {
        super(driver);
    }

    public AdminDashboardPage open() {
        driver.get(ConfigReader.baseUrl() + "/admin");
        waitVisible(adminHeading);
        return this;
    }

    public boolean isLoaded() {
        return driver.getCurrentUrl().contains("/admin");
    }

    public List<WebElement> visibleTicketRows() {
        return driver.findElements(ticketRows);
    }

    public int ticketCount() {
        return visibleTicketRows().size();
    }

    public boolean hasTicketContaining(String text) {
        return driver.findElements(By.xpath("//*[contains(text(),'" + text + "')]")).size() > 0;
    }
}
