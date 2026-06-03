package com.ticketsystem.e2e.pages;

import com.ticketsystem.e2e.support.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Shared functionality for all page objects: waits, JS click, JS-driven
 * input for Angular-bound fields. Subclasses inherit these so each page
 * object stays focused on the locators and actions specific to its screen.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.explicitWait()));
    }

    protected WebElement waitVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected boolean isPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    protected void jsClick(WebElement el) {
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", el);
    }

    /**
     * Set an Angular [(ngModel)] input/textarea value via JS. Dispatches
     * native 'input'/'change' events so Angular's change detection picks
     * it up — the same technique Angular's own test utilities use.
     */
    protected void setNgInput(By locator, String value) {
        WebElement el = waitVisible(locator);
        String script =
            "const el = arguments[0]; const val = arguments[1];" +
            "const proto = el.tagName === 'TEXTAREA'" +
            "  ? window.HTMLTextAreaElement.prototype" +
            "  : window.HTMLInputElement.prototype;" +
            "const setter = Object.getOwnPropertyDescriptor(proto, 'value').set;" +
            "setter.call(el, val);" +
            "el.dispatchEvent(new Event('input', { bubbles: true }));" +
            "el.dispatchEvent(new Event('change', { bubbles: true }));" +
            "el.dispatchEvent(new Event('blur', { bubbles: true }));";
        ((JavascriptExecutor) driver).executeScript(script, el, value);
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }
}
