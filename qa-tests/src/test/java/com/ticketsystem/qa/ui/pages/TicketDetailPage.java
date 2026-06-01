package com.ticketsystem.qa.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;
import java.util.stream.Collectors;

public class TicketDetailPage extends BasePage {

    private final By heading      = By.cssSelector("h1");
    private final By statusBadges = By.cssSelector("span.badge");

    public TicketDetailPage(WebDriver driver) { super(driver); }

    public boolean isLoaded() {
        try {
            wait.until(ExpectedConditions.urlContains("/ticket/"));
            waitVisible(heading);
            return true;
        } catch (Exception e) { return false; }
    }

    public String getTitle() { return textOrEmpty(heading); }
    public List<String> badges() {
        return driver.findElements(statusBadges).stream().map(WebElement::getText).collect(Collectors.toList());
    }
    public boolean hasBadge(String label) {
        return badges().stream().anyMatch(b -> b.equalsIgnoreCase(label));
    }

    @Override
    public String observed() {
        return "URL: " + driver.getCurrentUrl()
                + " | Title: " + textOrEmpty(heading)
                + " | Badges: " + badges();
    }
}
