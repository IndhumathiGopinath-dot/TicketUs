package com.ticketsystem.qa.ui.pages;

import com.ticketsystem.qa.support.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;

/**
 * Page Object for /signup. Uses PageFactory @FindBy.
 */
public class SignupPage extends BasePage {

    @FindBy(how = How.CSS, using = "input[name='name'], input[formcontrolname='name']")
    private WebElement nameField;

    @FindBy(how = How.CSS, using = "input[type='email'], input[name='email'], input[formcontrolname='email']")
    private WebElement emailField;

    @FindBy(how = How.CSS, using = "input[type='password'], input[name='password'], input[formcontrolname='password']")
    private WebElement passwordField;

    @FindBy(how = How.XPATH, using = "//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sign up') or contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'create')]")
    private WebElement signUpButton;

    public SignupPage(WebDriver driver) { super(driver); }

    public SignupPage open() {
        driver.get(ConfigReader.uiBaseUrl() + "/signup");
        waitVisible(nameField);
        return this;
    }

    public void signupAs(String name, String email, String password) {
        waitVisible(nameField);
        jsSet(nameField, name);
        jsSet(emailField, email);
        jsSet(passwordField, password);
        waitClickable(signUpButton);
        jsClick(signUpButton);
    }
}
