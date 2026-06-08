package com.ticketsystem.qa.ui.pages;

import com.ticketsystem.qa.support.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Base class for all PageObjects. Wires up PageFactory.initElements() so the
 * @FindBy fields in subclasses get populated. Provides shared waits and JS helpers.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.explicitWait()));
        // PageFactory wires every @FindBy annotated WebElement in the subclass
        PageFactory.initElements(driver, this);
    }

    protected WebElement waitVisible(WebElement el) {
        return wait.until(ExpectedConditions.visibilityOf(el));
    }

    protected WebElement waitClickable(WebElement el) {
        return wait.until(ExpectedConditions.elementToBeClickable(el));
    }

    protected void waitUrlContains(String fragment) {
        wait.until(ExpectedConditions.urlContains(fragment));
    }

    protected void jsClick(WebElement el) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
    }

    /**
     * Sets an input value via native setter + dispatched events so Angular's
     * change detection picks it up. Use this for Reactive Forms.
     */
    protected void jsSet(WebElement input, String value) {
        ((JavascriptExecutor) driver).executeScript(
            "const el = arguments[0]; const v = arguments[1];" +
            "const setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype,'value').set;" +
            "setter.call(el, v);" +
            "el.dispatchEvent(new Event('input', {bubbles:true}));" +
            "el.dispatchEvent(new Event('change', {bubbles:true}));",
            input, value);
    }

    protected boolean pageContains(String text) {
        return !driver.findElements(
            By.xpath("//*[contains(normalize-space(.), '" + text + "')]")).isEmpty();
    }

    public String currentUrl() { return driver.getCurrentUrl(); }
}
