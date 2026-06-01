package com.ticketsystem.qa.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class LoginPage extends BasePage {

    private final By emailInput    = By.cssSelector("input[name='email']");
    private final By passwordInput = By.cssSelector("input[name='password']");
    private final By submitBtn     = By.cssSelector("button[type='submit']");
    private final By errorBanner   = By.cssSelector(".error");

    public LoginPage(WebDriver driver) { super(driver); }

    public LoginPage open(String baseUrl) {
        driver.get(baseUrl + "/login");
        waitVisible(emailInput);
        return this;
    }

    public LoginPage login(String email, String password) {
        type(emailInput, email);
        type(passwordInput, password);
        if (waitVisible(submitBtn).isEnabled()) click(submitBtn);
        return this;
    }

    public boolean submitEnabled() { return waitVisible(submitBtn).isEnabled(); }
    public boolean isOnLoginPage() { return driver.getCurrentUrl().contains("/login"); }

    public String getError() {
        try { return wait.until(ExpectedConditions.visibilityOfElementLocated(errorBanner)).getText(); }
        catch (Exception e) { return ""; }
    }

    @Override
    public String observed() {
        String err = textOrEmpty(errorBanner);
        return "URL: " + driver.getCurrentUrl()
                + " | H1: " + textOrEmpty(By.tagName("h1"))
                + (err.isBlank() ? "" : " | Error: " + err);
    }
}
