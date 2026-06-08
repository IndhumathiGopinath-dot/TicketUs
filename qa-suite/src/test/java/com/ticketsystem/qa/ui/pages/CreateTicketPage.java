package com.ticketsystem.qa.ui.pages;

import com.ticketsystem.qa.support.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;

import java.util.List;

/**
 * Page Object for /create.
 *
 * The form uses Angular template-driven forms (ngModel) without name/id/formcontrolname
 * attributes — fields are identified by their placeholder text. The selectors below match
 * the actual placeholders used in the application:
 *   - Title    : "Briefly describe the issue"
 *   - Description: typically a textarea, found by tag + visibility
 */
public class CreateTicketPage extends BasePage {

    // Title input — identified by its placeholder
    @FindBy(how = How.CSS, using = "input[placeholder='Briefly describe the issue']")
    private WebElement titleField;

    // Submit button — wide match for any "submit" or "create" button
    @FindBy(how = How.XPATH, using =
        "//button[@type='submit'] | " +
        "//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'submit') " +
        "or contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'create ticket') " +
        "or contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'create')]")
    private WebElement submitButton;

    public CreateTicketPage(WebDriver driver) { super(driver); }

    public CreateTicketPage open() {
        driver.get(ConfigReader.uiBaseUrl() + "/create");
        sleep(1500);  // bootstrap time for Angular
        return this;
    }

    /**
     * Click the category tab (IT, BUG, or HR). Tries text-matching across button/div/card/tab patterns.
     */
    public CreateTicketPage selectCategory(String category) {
        String lower = category.toLowerCase();
        List<WebElement> candidates = driver.findElements(By.xpath(
            "//button[translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='" + lower + "'] | " +
            "//*[contains(@class,'category')]" +
              "[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + lower + "')] | " +
            "//div[contains(@class,'tab') or contains(@class,'option') or contains(@class,'card') or contains(@class,'btn')]" +
              "[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + lower + "')] | " +
            "//label[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + lower + "')]"
        ));
        if (!candidates.isEmpty()) {
            jsClick(candidates.get(0));
            sleep(800);
        }
        return this;
    }

    public CreateTicketPage enterTitle(String title) {
        // Wait for the title field via its placeholder
        waitVisible(titleField);
        jsSet(titleField, title);
        return this;
    }

    public CreateTicketPage enterDescription(String desc) {
        // First try to find textarea by placeholder containing 'detail' or 'describ'
        WebElement el = findDescriptionField();
        if (el == null) return this;

        if ("textarea".equalsIgnoreCase(el.getTagName())) {
            ((JavascriptExecutor) driver).executeScript(
                "const el = arguments[0]; const v = arguments[1];" +
                "const setter = Object.getOwnPropertyDescriptor(window.HTMLTextAreaElement.prototype,'value').set;" +
                "setter.call(el, v);" +
                "el.dispatchEvent(new Event('input', {bubbles:true}));" +
                "el.dispatchEvent(new Event('change', {bubbles:true}));",
                el, desc);
        } else {
            jsSet(el, desc);
        }
        return this;
    }

    public CreateTicketPage selectRequestType(String value) {
        try {
            List<WebElement> selects = driver.findElements(By.cssSelector("select"));
            for (WebElement sel : selects) {
                if (sel.isDisplayed()) {
                    ((JavascriptExecutor) driver).executeScript(
                        "const el = arguments[0]; el.value = arguments[1];" +
                        "el.dispatchEvent(new Event('change', {bubbles:true}));",
                        sel, value);
                    return this;
                }
            }
        } catch (Exception ignored) {}
        return this;
    }

    public CreateTicketPage checkConfidential() {
        try {
            List<WebElement> boxes = driver.findElements(By.cssSelector("input[type='checkbox']"));
            for (WebElement b : boxes) {
                if (b.isDisplayed() && !b.isSelected()) {
                    jsClick(b);
                    return this;
                }
            }
        } catch (Exception ignored) {}
        return this;
    }

    public void submit() {
        sleep(400);
        try {
            waitClickable(submitButton);
            jsClick(submitButton);
        } catch (Exception e) {
            // Fallback: find any submit-looking button
            List<WebElement> btns = driver.findElements(By.xpath(
                "//button[@type='submit'] | //button[contains(.,'Submit') or contains(.,'Create')]"
            ));
            if (!btns.isEmpty()) jsClick(btns.get(0));
        }
        sleep(1200);
        // Handle optional "Submit anyway" / "Continue" review step
        List<WebElement> anyway = driver.findElements(By.xpath(
            "//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'submit anyway') " +
            "or contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'continue anyway') " +
            "or contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'create anyway')]"
        ));
        if (!anyway.isEmpty()) jsClick(anyway.get(0));
    }

    /** Find description field: try textarea first (most common), then input by placeholder. */
    private WebElement findDescriptionField() {
        // First: any visible textarea
        for (WebElement el : driver.findElements(By.tagName("textarea"))) {
            if (el.isDisplayed()) return el;
        }
        // Second: input with placeholder hinting at description
        List<WebElement> candidates = driver.findElements(By.cssSelector(
            "input[placeholder*='detail' i]," +
            "input[placeholder*='describ' i]," +
            "input[placeholder*='Description']"
        ));
        for (WebElement el : candidates) {
            if (el.isDisplayed()) return el;
        }
        return null;
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}