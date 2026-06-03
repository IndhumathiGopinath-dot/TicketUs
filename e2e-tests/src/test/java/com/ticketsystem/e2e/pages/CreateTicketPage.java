package com.ticketsystem.e2e.pages;

import com.ticketsystem.e2e.support.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * The ticket creation form at "/create".
 *
 * IMPORTANT: this page drives the Angular form via JavaScript executor calls
 * rather than native Selenium .click()/.sendKeys(). Native interactions race
 * against Angular's debounced reactive form updates and change detection;
 * dispatching native input/change events as Angular's own test utilities do
 * makes the form behave deterministically.
 */
public class CreateTicketPage extends BasePage {

    private final By titleInput      = By.cssSelector("input[placeholder*='Briefly describe'], input[name='title']");
    private final By descriptionTA   = By.cssSelector("textarea[placeholder*='Provide details'], textarea[name='description']");
    private final By submitBtn       = By.xpath("//button[contains(., 'Submit ticket') or contains(., 'Submit')]");
    private final By submitAnywayBtn = By.xpath("//button[contains(., 'Submit anyway')]");
    private final By severityLabel    = By.xpath("//label[normalize-space()='Severity']");
    private final By requestTypeLabel = By.xpath("//label[normalize-space()='Request type']");
    private final By assetTagLabel    = By.xpath("//label[contains(normalize-space(),'Asset Tag')]");

    public CreateTicketPage(WebDriver driver) {
        super(driver);
    }

    public CreateTicketPage open() {
        driver.get(ConfigReader.baseUrl() + "/create");
        waitVisible(titleInput);
        return this;
    }

    /**
     * Click a category button (IT / BUG / HR) via JS, then deterministically
     * wait for the category-specific anchor element to appear.
     */
    public CreateTicketPage selectCategory(String category) {
        String upper = category.toUpperCase();
        String label = switch (upper) {
            case "IT" -> "IT";
            case "BUG" -> "Bug";
            case "HR" -> "HR";
            default -> throw new IllegalArgumentException("Unknown category: " + category);
        };

        String js =
            "const btns = document.querySelectorAll('.category-tabs button, button');" +
            "for (const b of btns) {" +
            "  if (b.textContent.includes(arguments[0])) { b.click(); return true; }" +
            "}" +
            "return false;";
        ((JavascriptExecutor) driver).executeScript(js, label);

        By anchor = switch (upper) {
            case "IT" -> assetTagLabel;
            case "BUG" -> severityLabel;
            case "HR" -> requestTypeLabel;
            default -> titleInput;
        };
        waitVisible(anchor);
        return this;
    }

    public CreateTicketPage enterTitle(String t) {
        setNgInput(titleInput, t);
        return this;
    }

    public CreateTicketPage enterDescription(String d) {
        setNgInput(descriptionTA, d);
        return this;
    }

    public CreateTicketPage setSeverity(String sev) {
        WebElement sel = waitVisible(By.xpath("//label[normalize-space()='Severity']/following::select[1]"));
        selectByValue(sel, sev);
        return this;
    }

    public CreateTicketPage setRequestType(String type) {
        WebElement sel = waitVisible(By.xpath("//label[normalize-space()='Request type']/following::select[1]"));
        selectByText(sel, type);
        return this;
    }

    public CreateTicketPage setAssetTag(String tag) {
        setNgInput(By.cssSelector("input[placeholder*='LAPTOP'], input[name='assetTag']"), tag);
        return this;
    }

    public CreateTicketPage markConfidential() {
        WebElement chk = waitVisible(By.cssSelector("#conf, input[name='confidential']"));
        if (!chk.isSelected()) jsClick(chk);
        return this;
    }

    /** Submit the form and wait for either the review step or navigation. */
    public void submit() {
        ((JavascriptExecutor) driver).executeScript(
            "document.querySelectorAll('.suggestions').forEach(e => e.style.display='none');");

        WebElement btn = waitVisible(submitBtn);
        jsClick(btn);

        wait.until(d -> !d.getCurrentUrl().contains("/create") || isPresent(submitAnywayBtn));

        if (isPresent(submitAnywayBtn)) {
            jsClick(waitClickable(submitAnywayBtn));
            wait.until(d -> !d.getCurrentUrl().contains("/create"));
        }
    }

    private void selectByValue(WebElement select, String value) {
        ((JavascriptExecutor) driver).executeScript(
            "const sel = arguments[0]; const val = arguments[1];" +
            "for (let i=0;i<sel.options.length;i++) {" +
            "  if (sel.options[i].value === val) {" +
            "    sel.selectedIndex = i;" +
            "    sel.dispatchEvent(new Event('change', { bubbles: true }));" +
            "    return;" +
            "  }" +
            "}", select, value);
    }

    private void selectByText(WebElement select, String text) {
        ((JavascriptExecutor) driver).executeScript(
            "const sel = arguments[0]; const txt = arguments[1];" +
            "for (let i=0;i<sel.options.length;i++) {" +
            "  if (sel.options[i].text.trim() === txt) {" +
            "    sel.selectedIndex = i;" +
            "    sel.dispatchEvent(new Event('change', { bubbles: true }));" +
            "    return;" +
            "  }" +
            "}", select, text);
    }
}
