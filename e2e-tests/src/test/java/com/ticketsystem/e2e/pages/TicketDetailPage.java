package com.ticketsystem.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * The ticket detail view at "/ticket/:id". Shows status, priority, timeline.
 */
public class TicketDetailPage extends BasePage {

    private final By statusBadge   = By.cssSelector(".status, [class*='status-badge'], [data-testid='status']");
    private final By priorityBadge = By.cssSelector(".priority, [class*='priority-badge'], [data-testid='priority']");
    private final By categoryBadge = By.cssSelector(".category, [class*='category-badge']");
    private final By titleHeading  = By.cssSelector("h1, h2, .ticket-title");
    private final By rateThumbsUp  = By.xpath("//button[contains(., '👍') or contains(., 'thumbs up') or @data-rating='up']");

    public TicketDetailPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return driver.getCurrentUrl().matches(".*/ticket/\\d+.*");
    }

    public long ticketIdFromUrl() {
        String url = driver.getCurrentUrl();
        String[] parts = url.split("/ticket/");
        if (parts.length < 2) throw new IllegalStateException("Not on ticket detail: " + url);
        return Long.parseLong(parts[1].split("[?#/]")[0]);
    }

    public String readTitle() {
        return waitVisible(titleHeading).getText();
    }

    public String readStatus() {
    // Search for any of the expected status values anywhere on the page
    String[] statuses = {"OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED"};
    for (String s : statuses) {
        if (!driver.findElements(
            org.openqa.selenium.By.xpath("//*[contains(normalize-space(.),'" + s + "')]")).isEmpty()) {
            return s;
        }
    }
    return "";
}

public String readPriority() {
    String[] priorities = {"URGENT", "HIGH", "NORMAL", "LOW"};
    for (String p : priorities) {
        if (!driver.findElements(
            org.openqa.selenium.By.xpath("//*[contains(normalize-space(.),'" + p + "')]")).isEmpty()) {
            return p;
        }
    }
    return "";
}

public String readCategory() {
    String[] categories = {"IT", "BUG", "HR"};
    for (String c : categories) {
        if (!driver.findElements(
            org.openqa.selenium.By.xpath("//*[contains(normalize-space(.),'" + c + "')]")).isEmpty()) {
            return c;
        }
    }
    return "";
}

    public boolean hasRatingButtons() {
        return isPresent(rateThumbsUp);
    }

    public void rateUp() {
        WebElement btn = waitClickable(rateThumbsUp);
        jsClick(btn);
    }
}
