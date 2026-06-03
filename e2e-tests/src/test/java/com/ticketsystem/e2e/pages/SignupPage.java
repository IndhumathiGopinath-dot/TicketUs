package com.ticketsystem.e2e.pages;

import com.ticketsystem.e2e.support.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * The signup screen at "/signup". Creates a new account.
 */
public class SignupPage extends BasePage {

    private final By nameInput      = By.cssSelector("input[name='name'], input[formControlName='name']");
    private final By emailInput     = By.cssSelector("input[type='email'], input[name='email'], input[formControlName='email']");
    private final By passwordInput  = By.cssSelector("input[type='password'], input[formControlName='password']");
    private final By roleSelect     = By.cssSelector("select[name='role'], select[formControlName='role']");
    private final By departmentInput = By.cssSelector("input[name='department'], input[formControlName='department']");
    private final By submitButton   = By.xpath("//button[contains(text(),'Sign up') or contains(text(),'Create') or contains(text(),'Register')]");

    public SignupPage(WebDriver driver) {
        super(driver);
    }

    public SignupPage open() {
        driver.get(ConfigReader.baseUrl() + "/signup");
        waitVisible(emailInput);
        return this;
    }

    public SignupPage enterName(String name) {
        setNgInput(nameInput, name);
        return this;
    }

    public SignupPage enterEmail(String email) {
        setNgInput(emailInput, email);
        return this;
    }

    public SignupPage enterPassword(String password) {
        setNgInput(passwordInput, password);
        return this;
    }

    public SignupPage selectRole(String role) {
        if (!isPresent(roleSelect)) return this;
        WebElement el = waitVisible(roleSelect);
        String script =
            "const sel = arguments[0]; const val = arguments[1];" +
            "for (let i = 0; i < sel.options.length; i++) {" +
            "  if (sel.options[i].value === val || sel.options[i].text.trim() === val) {" +
            "    sel.selectedIndex = i;" +
            "    sel.dispatchEvent(new Event('change', { bubbles: true }));" +
            "    return;" +
            "  }" +
            "}";
        ((JavascriptExecutor) driver).executeScript(script, el, role);
        return this;
    }

    public SignupPage enterDepartment(String department) {
        if (isPresent(departmentInput)) {
            setNgInput(departmentInput, department);
        }
        return this;
    }

    public void submit() {
        jsClick(waitClickable(submitButton));
        wait.until(d -> d.getCurrentUrl().contains("/dashboard")
                     || d.getCurrentUrl().contains("/admin"));
    }
}
