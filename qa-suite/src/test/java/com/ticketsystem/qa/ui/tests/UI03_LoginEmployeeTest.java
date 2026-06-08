package com.ticketsystem.qa.ui.tests;

import com.ticketsystem.qa.support.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

/**
 * UI_03 — Employee login.
 *
 * Demonstrates RAW Selenium with driver.findElement(By.xpath(...)) — the
 * opposite of the PageFactory style used in other UI tests. Both styles are
 * valid; this one shows you can locate elements directly without a Page Object.
 *
 * Uses XPath locators throughout to demonstrate XPath specifically (other
 * pages use a mix of CSS and XPath).
 */
public class UI03_LoginEmployeeTest extends UiBaseTest {

    @Test(groups = {"regression", "ui", "auth", "xpath"},
          description = "Employee logs in using raw findElement(By.xpath()) — no Page Object")
    public void employeeLoginUsingRawXPath() {
        // Navigate to login page
        driver.get(ConfigReader.uiBaseUrl() + "/login");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // Locate the email input via XPath — matches type='email' or formcontrolname='email'
        WebElement emailField = driver.findElement(
            By.xpath("//input[@type='email' or @name='email' or @formcontrolname='email']"));
        wait.until(ExpectedConditions.visibilityOf(emailField));

        // Set value using JS executor (Angular reactive forms are picky about events)
        ((JavascriptExecutor) driver).executeScript(
            "const el = arguments[0]; const v = arguments[1];" +
            "const s = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype,'value').set;" +
            "s.call(el, v);" +
            "el.dispatchEvent(new Event('input',{bubbles:true}));" +
            "el.dispatchEvent(new Event('change',{bubbles:true}));",
            emailField, ConfigReader.employeeEmail());

        // Locate the password input via XPath
        WebElement passwordField = driver.findElement(
            By.xpath("//input[@type='password' or @name='password' or @formcontrolname='password']"));
        ((JavascriptExecutor) driver).executeScript(
            "const el = arguments[0]; const v = arguments[1];" +
            "const s = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype,'value').set;" +
            "s.call(el, v);" +
            "el.dispatchEvent(new Event('input',{bubbles:true}));" +
            "el.dispatchEvent(new Event('change',{bubbles:true}));",
            passwordField, ConfigReader.employeePass());

        // Locate the sign-in button via XPath — case-insensitive text match on "sign in" or "login"
        WebElement signInBtn = driver.findElement(By.xpath(
            "//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sign in') " +
            "or contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'login')]"));
        wait.until(ExpectedConditions.elementToBeClickable(signInBtn));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", signInBtn);

        // Wait for navigation away from /login
        wait.until(d -> !d.getCurrentUrl().contains("/login"));

        // Assert: employee should land on /dashboard (NOT /admin)
        String url = driver.getCurrentUrl();
        Assert.assertTrue(url.contains("/dashboard"),
            "Expected /dashboard after employee login. Got: " + url);
    }
}
