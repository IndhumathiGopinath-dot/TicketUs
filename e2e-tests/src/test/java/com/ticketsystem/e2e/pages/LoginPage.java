package com.ticketsystem.e2e.pages;

import com.ticketsystem.e2e.support.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * The login screen at "/login".
 */
public class LoginPage extends BasePage {

    private final By emailInput    = By.cssSelector("input[type='email'], input[name='email'], input[formControlName='email']");
    private final By passwordInput = By.cssSelector("input[type='password'], input[formControlName='password']");
    private final By signInButton  = By.xpath("//button[contains(text(),'Sign in') or contains(text(),'Log in') or contains(text(),'Login')]");
    private final By errorMessage  = By.cssSelector(".error, .alert-danger, [class*='error']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage open() {
        driver.get(ConfigReader.baseUrl() + "/login");
        waitVisible(emailInput);
        return this;
    }

    public LoginPage enterEmail(String email) {
        setNgInput(emailInput, email);
        return this;
    }

    public LoginPage enterPassword(String password) {
        setNgInput(passwordInput, password);
        return this;
    }

    public void submitExpectingDashboard() {
        jsClick(waitClickable(signInButton));
        wait.until(d -> d.getCurrentUrl().contains("/dashboard")
                     || d.getCurrentUrl().contains("/admin"));
    }

    public void submitExpectingError() {
        jsClick(waitClickable(signInButton));
        wait.until(ExpectedConditions.or(
            ExpectedConditions.visibilityOfElementLocated(errorMessage),
            ExpectedConditions.urlContains("/login")
        ));
    }

    /** Convenience: full login as the given user, asserting it succeeded. */
    public void loginAs(String email, String password) {
        enterEmail(email).enterPassword(password).submitExpectingDashboard();
    }
}
