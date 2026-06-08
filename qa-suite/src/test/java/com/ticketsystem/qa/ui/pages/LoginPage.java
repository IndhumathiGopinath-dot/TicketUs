package com.ticketsystem.qa.ui.pages;

import com.ticketsystem.qa.support.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;

/**
 * Page Object for /login. Uses PageFactory @FindBy annotations.
 * Selectors are deliberately tolerant — multiple matchers per element
 * so cosmetic frontend changes don't break tests.
 */
public class LoginPage extends BasePage {

    @FindBy(how = How.CSS, using = "input[type='email'], input[name='email'], input[formcontrolname='email']")
    private WebElement emailField;

    @FindBy(how = How.CSS, using = "input[type='password'], input[name='password'], input[formcontrolname='password']")
    private WebElement passwordField;

    @FindBy(how = How.XPATH, using = "//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sign in') or contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'login')]")
    private WebElement signInButton;

    @FindBy(how = How.XPATH, using = "//*[contains(@class,'error') or contains(@class,'alert')]")
    private WebElement errorMessage;

    public LoginPage(WebDriver driver) { super(driver); }

    public LoginPage open() {
        driver.get(ConfigReader.uiBaseUrl() + "/login");
        waitVisible(emailField);
        return this;
    }

    public LoginPage enterEmail(String email) {
        waitVisible(emailField);
        emailField.clear();
        jsSet(emailField, email);
        return this;
    }

    public LoginPage enterPassword(String password) {
        passwordField.clear();
        jsSet(passwordField, password);
        return this;
    }

    public void clickSignIn() {
        waitClickable(signInButton);
        jsClick(signInButton);
    }

    public void loginAs(String email, String password) {
        enterEmail(email).enterPassword(password).clickSignIn();
    }

    public boolean hasErrorMessage() {
        try {
            return errorMessage.isDisplayed();
        } catch (Exception e) {
            return pageContains("Invalid") || pageContains("Incorrect") || pageContains("Bad");
        }
    }
}
