package com.ticketsystem.qa.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object for the Create Ticket form.
 *
 * Strategy: this implementation drives the form through JavaScript instead of
 * native Selenium interactions, because Angular's debounced reactive form has
 * proven flaky with WebDriver's click+type model. We dispatch real input/change
 * events so Angular's [(ngModel)] picks up the values, and we trigger native
 * Angular click handlers on category buttons by calling .click() via JS.
 */
public class CreateTicketPage extends BasePage {

    // Locators kept for waiting purposes
    private final By categoryTabs    = By.cssSelector(".category-tabs");
    private final By titleInput      = By.cssSelector("input[placeholder*='Briefly describe']");
    private final By descriptionTA   = By.cssSelector("textarea[placeholder*='Provide details']");
    private final By submitBtn       = By.xpath("//button[contains(., 'Submit ticket')]");
    private final By submitAnywayBtn = By.xpath("//button[contains(., 'Submit anyway')]");

    // Verification anchors (one per category)
    private final By severityLabel    = By.xpath("//label[normalize-space()='Severity']");
    private final By requestTypeLabel = By.xpath("//label[normalize-space()='Request type']");
    private final By assetTagLabel    = By.xpath("//label[contains(normalize-space(),'Asset Tag')]");

    public CreateTicketPage(WebDriver driver) { super(driver); }

    public CreateTicketPage open(String baseUrl) {
        driver.get(baseUrl + "/create");
        waitVisible(categoryTabs);
        // Small settle so Angular finishes its initial render
        sleep(400);
        return this;
    }

    /**
     * Click the category button via JavaScript, then wait until the
     * category-specific anchor field appears in the DOM. This is the
     * key reliability fix — JS .click() invokes Angular's bound (click)
     * handler synchronously, and we then wait deterministically for
     * Angular to re-render the *ngIf block.
     */
    public CreateTicketPage selectCategory(String c) {
        String upper = c.toUpperCase();
        String containsText = switch (upper) {
            case "IT"  -> "IT";
            case "BUG" -> "Bug";
            case "HR"  -> "HR";
            default -> throw new IllegalArgumentException("Unknown category: " + c);
        };

        String js =
            "const btns = document.querySelectorAll('.category-tabs button');" +
            "for (const b of btns) {" +
            "  if (b.textContent.includes(arguments[0])) { b.click(); return true; }" +
            "}" +
            "return false;";
        Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(js, containsText);
        if (clicked == null || !clicked) {
            throw new RuntimeException("Could not find category button for: " + c);
        }

        // Wait for the category-specific section to render
        By anchor = switch (upper) {
            case "IT"  -> assetTagLabel;
            case "BUG" -> severityLabel;
            case "HR"  -> requestTypeLabel;
            default    -> titleInput;
        };
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(anchor));
        return this;
    }

    public CreateTicketPage enterTitle(String t)        { setNgModelInput(titleInput, t); return this; }
    public CreateTicketPage enterDescription(String d)  { setNgModelInput(descriptionTA, d); return this; }

    public CreateTicketPage enterSeverity(String s) {
        if (notBlank(s)) {
            WebElement sel = waitVisible(By.xpath(
                "//label[normalize-space()='Severity']/following::select[1]"));
            setSelectByValue(sel, s);
        }
        return this;
    }

    public CreateTicketPage enterAppVersion(String v) {
        if (notBlank(v)) setNgModelInput(By.cssSelector("input[placeholder*='2.4']"), v);
        return this;
    }

    public CreateTicketPage enterAssetTag(String t) {
        if (notBlank(t)) setNgModelInput(By.cssSelector("input[placeholder*='LAPTOP']"), t);
        return this;
    }

    public CreateTicketPage enterRequestType(String type) {
        if (!notBlank(type)) return this;
        WebElement sel = waitVisible(By.xpath(
            "//label[normalize-space()='Request type']/following::select[1]"));
        setSelectByText(sel, type);
        return this;
    }

    public CreateTicketPage checkConfidential() {
        WebElement chk = waitVisible(By.cssSelector("#conf"));
        if (!chk.isSelected()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", chk);
        }
        return this;
    }

    public void submit() {
        // Hide any autofill panel that might be overlaying
        ((JavascriptExecutor) driver).executeScript(
            "document.querySelectorAll('.suggestions').forEach(e => e.style.display='none');");

        // JS-click the Submit ticket button
        WebElement btn = waitVisible(submitBtn);
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", btn);

        // Wait for either review screen OR navigation away
        new WebDriverWait(driver, Duration.ofSeconds(20)).until(d ->
            !d.getCurrentUrl().contains("/create") || isPresent(submitAnywayBtn)
        );

        // If review screen appeared, JS-click Submit anyway
        if (isPresent(submitAnywayBtn)) {
            WebElement anyway = waitVisible(submitAnywayBtn);
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", anyway);
            new WebDriverWait(driver, Duration.ofSeconds(15)).until(d ->
                !d.getCurrentUrl().contains("/create")
            );
        }
    }

    // ============ helpers ============

    /**
     * Set an Angular [(ngModel)] input/textarea value via JS. We must dispatch
     * an 'input' event for Angular's change detection to pick up the value.
     */
    private void setNgModelInput(By locator, String value) {
        WebElement el = waitVisible(locator);
        String tag = el.getTagName();
        String script =
            "const el = arguments[0];" +
            "const val = arguments[1];" +
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

    /**
     * Set a <select> by value and dispatch change event so Angular sees it.
     */
    private void setSelectByValue(WebElement select, String value) {
        String script =
            "const sel = arguments[0];" +
            "const val = arguments[1];" +
            "for (let i = 0; i < sel.options.length; i++) {" +
            "  if (sel.options[i].value === val) {" +
            "    sel.selectedIndex = i;" +
            "    sel.dispatchEvent(new Event('change', { bubbles: true }));" +
            "    return true;" +
            "  }" +
            "}" +
            "return false;";
        ((JavascriptExecutor) driver).executeScript(script, select, value);
    }

    /**
     * Set a <select> by visible text and dispatch change event.
     */
    private void setSelectByText(WebElement select, String text) {
        String script =
            "const sel = arguments[0];" +
            "const txt = arguments[1];" +
            "for (let i = 0; i < sel.options.length; i++) {" +
            "  if (sel.options[i].text.trim() === txt) {" +
            "    sel.selectedIndex = i;" +
            "    sel.dispatchEvent(new Event('change', { bubbles: true }));" +
            "    return true;" +
            "  }" +
            "}" +
            "return false;";
        ((JavascriptExecutor) driver).executeScript(script, select, text);
    }

    private static boolean notBlank(String s) { return s != null && !s.isBlank(); }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }
}