package com.ticketsystem.qa.ui.tests;

import com.ticketsystem.qa.support.TestDataFactory;
import com.ticketsystem.qa.ui.pages.DashboardPage;
import com.ticketsystem.qa.ui.pages.SignupPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * UI_04 — Signup workflow.
 * Verifies a new user can create an account and is auto-logged in.
 */
public class UI04_SignupTest extends UiBaseTest {

    @Test(groups = {"regression", "ui", "auth"},
          description = "New user signs up and is auto-logged in to employee dashboard")
    public void signupAutoLogsInUser() {
        String name = "QA User";
        String email = TestDataFactory.uniqueEmail("qa");
        String password = "Pa55w0rd!";

        SignupPage signup = new SignupPage(driver).open();
        signup.signupAs(name, email, password);

        DashboardPage dash = new DashboardPage(driver);
        long deadline = System.currentTimeMillis() + 10_000;
        while (!dash.isLoaded() && System.currentTimeMillis() < deadline) {
            try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }

        Assert.assertTrue(dash.isLoaded(),
            "Expected to land on a dashboard after signup. URL: " + dash.currentUrl());
    }
}
