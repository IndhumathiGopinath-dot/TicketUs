package com.ticketsystem.e2e.pages;

import com.ticketsystem.e2e.support.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * The knowledge base browser at "/knowledge".
 */
public class KnowledgeBasePage extends BasePage {

    private final By heading       = By.xpath("//h1[contains(text(),'Knowledge')] | //h2[contains(text(),'Knowledge')]");
    private final By searchInput   = By.cssSelector("input[placeholder*='Search'], input[type='search']");
    private final By articleCards  = By.cssSelector(".kb-article, .article-card, [data-testid='kb-article']");

    public KnowledgeBasePage(WebDriver driver) {
        super(driver);
    }

    public KnowledgeBasePage open() {
        driver.get(ConfigReader.baseUrl() + "/knowledge");
        waitVisible(heading);
        return this;
    }

    public KnowledgeBasePage search(String query) {
        if (isPresent(searchInput)) setNgInput(searchInput, query);
        return this;
    }

    public List<WebElement> articles() {
        return driver.findElements(articleCards);
    }

    public int articleCount() {
        return articles().size();
    }
}
