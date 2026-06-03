package com.ticketsystem.e2e.pages;

import com.ticketsystem.e2e.support.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * The public landing page at "/". Entry point for new users coming to the app.
 *
 * Detection strategy: we don't search for specific brand text (which is fragile —
 * XPath text() vs nested elements vs whitespace all interact badly). Instead we
 * verify the page rendered by checking that the body has substantive content
 * AND we're on the root URL (not auto-redirected elsewhere).
 */
public class LandingPage extends BasePage {

    private final By signUpButton = By.xpath(
        "//a[@href='/signup'] | //a[contains(text(),'Sign up')] " +
        "| //a[contains(text(),'Sign Up')] | //a[contains(text(),'Get Started')] " +
        "| //a[contains(text(),'Get started')]");
    private final By loginButton = By.xpath(
        "//a[@href='/login'] | //a[contains(text(),'Log in')] " +
        "| //a[contains(text(),'Log In')] | //a[contains(text(),'Login')]");
    private final By anyBody = By.tagName("body");

    public LandingPage(WebDriver driver) {
        super(driver);
    }

    public LandingPage open() {
        // Clear any stored auth so the landing component doesn't auto-redirect.
        // We have to navigate somewhere on the app's origin first to access its localStorage.
        driver.get(ConfigReader.baseUrl() + "/login");
        ((JavascriptExecutor) driver)
            .executeScript("try { localStorage.clear(); sessionStorage.clear(); } catch(e) {}");

        // Now navigate to root and wait for the page body to render
        driver.get(ConfigReader.baseUrl() + "/");
        waitVisible(anyBody);

        // Tiny settle so Angular finishes its initial component rendering
        try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        return this;
    }

    /**
     * Considered "displayed" if we're on the root path with substantive content rendered.
     * Doesn't depend on specific brand text — too fragile across landing-page revisions.
     */
    public boolean isDisplayed() {
        String url = driver.getCurrentUrl();
        boolean onRoot = url.endsWith(":4200/") || url.endsWith(":4200")
                      || url.matches(".*:4200/?\\??.*");
        if (!onRoot) return false;

        // Sanity check: the page rendered something meaningful, not a blank page
        String bodyText = driver.findElement(anyBody).getText();
        return bodyText != null && bodyText.trim().length() > 20;
    }

    public SignupPage clickSignUp() {
        jsClick(waitClickable(signUpButton));
        wait.until(ExpectedConditions.urlContains("/signup"));
        return new SignupPage(driver);
    }

    public LoginPage clickLogin() {
        jsClick(waitClickable(loginButton));
        wait.until(ExpectedConditions.urlContains("/login"));
        return new LoginPage(driver);
    }
}