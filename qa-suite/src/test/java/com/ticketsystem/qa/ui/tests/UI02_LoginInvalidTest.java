package com.ticketsystem.qa.ui.tests;

import com.ticketsystem.qa.ui.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * UI_02 — Login negative case.
 * Verifies wrong credentials produce an error and do not navigate away from /login.
 */
public class UI02_LoginInvalidTest extends UiBaseTest {

    @Test(groups = {"smoke", "ui", "auth", "negative"},
          description = "Wrong password shows error message and stays on /login")
    public void wrongPasswordShowsError() {
        LoginPage login = new LoginPage(driver).open();
        login.loginAs("it.admin@company.com", "wrongPassword!");

        // Wait briefly for any redirect/error to surface
        try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        Assert.assertTrue(login.currentUrl().contains("/login"),
            "Should remain on /login after invalid creds. Got: " + login.currentUrl());
        Assert.assertTrue(login.hasErrorMessage(),
            "Expected an error message to be visible on the login page.");
    }
}
