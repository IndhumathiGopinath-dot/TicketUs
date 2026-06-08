package com.ticketsystem.qa.ui.pages;

import com.ticketsystem.qa.support.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;

import java.util.List;

/**
 * Page Object for /knowledge. Allows browsing and searching KB articles.
 */
public class KnowledgeBasePage extends BasePage {

    @FindBy(how = How.CSS, using = "input[type='search'], input[placeholder*='Search'], input[placeholder*='search']")
    private WebElement searchField;

    @FindBy(how = How.XPATH,
            using = "//*[contains(@class,'article') or contains(@class,'kb-item') or contains(@class,'knowledge')]")
    private List<WebElement> articles;

    public KnowledgeBasePage(WebDriver driver) { super(driver); }

    public KnowledgeBasePage open() {
        driver.get(ConfigReader.uiBaseUrl() + "/knowledge");
        return this;
    }

    public int articleCount() {
        return articles.size();
    }

    public KnowledgeBasePage search(String query) {
        try {
            waitVisible(searchField);
            jsSet(searchField, query);
        } catch (Exception ignored) {}
        sleep(800);
        return this;
    }

    public boolean isOnKnowledgeBase() {
        return driver.getCurrentUrl().contains("/knowledge");
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
