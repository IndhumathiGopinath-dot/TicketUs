package com.ticketsystem.qa.ui.pages;

import com.ticketsystem.qa.utils.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getInt("explicit.wait")));
    }

    protected WebElement waitVisible(By by) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }
    protected WebElement waitClickable(By by) {
        return wait.until(ExpectedConditions.elementToBeClickable(by));
    }
    protected void type(By by, String text) {
        WebElement el = waitVisible(by);
        el.clear();
        if (text != null) el.sendKeys(text);
    }
    protected void click(By by) { waitClickable(by).click(); }
    protected boolean isPresent(By by) { return !driver.findElements(by).isEmpty(); }
    protected String textOrEmpty(By by) {
        try { return driver.findElement(by).getText(); } catch (Exception e) { return ""; }
    }

    /** Override per-page to return a one-line snapshot of meaningful visible text. */
    public String observed() {
        return "URL: " + driver.getCurrentUrl() + " | H1: " + textOrEmpty(By.tagName("h1"));
    }
}
