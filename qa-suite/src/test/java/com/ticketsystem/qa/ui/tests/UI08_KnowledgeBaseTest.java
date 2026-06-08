package com.ticketsystem.qa.ui.tests;

import com.ticketsystem.qa.support.ConfigReader;
import com.ticketsystem.qa.ui.pages.KnowledgeBasePage;
import com.ticketsystem.qa.ui.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * UI_08 — Knowledge base browse and search.
 * Verifies the KB page loads and accepts a search query.
 */
public class UI08_KnowledgeBaseTest extends UiBaseTest {

    @Test(groups = {"regression", "ui", "knowledge"},
          description = "Logged-in user browses and searches knowledge base")
    public void userCanSearchKnowledgeBase() {
        new LoginPage(driver).open()
            .loginAs(ConfigReader.employeeEmail(), ConfigReader.employeePass());

        try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        KnowledgeBasePage kb = new KnowledgeBasePage(driver).open();
        try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        Assert.assertTrue(kb.isOnKnowledgeBase(),
            "Expected to be on /knowledge. URL: " + kb.currentUrl());

        kb.search("password");
        try { Thread.sleep(800); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        Assert.assertTrue(kb.isOnKnowledgeBase(),
            "Should still be on KB page after search. URL: " + kb.currentUrl());
    }
}
