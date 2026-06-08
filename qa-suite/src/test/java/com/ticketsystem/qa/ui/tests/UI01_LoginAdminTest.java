package com.ticketsystem.qa.ui.tests;

import com.ticketsystem.qa.support.ConfigReader;
import com.ticketsystem.qa.ui.pages.DashboardPage;
import com.ticketsystem.qa.ui.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * UI_01 — Admin login happy path.
 * Verifies an IT admin can log in via the UI and land on the admin console.
 */
public class UI01_LoginAdminTest extends UiBaseTest {

    @Test(groups = {"smoke", "ui", "auth"},
          description = "IT admin logs in with valid credentials and lands on admin dashboard")
    public void adminLoginLandsOnAdminConsole() {
        LoginPage login = new LoginPage(driver).open();
        login.loginAs(ConfigReader.adminEmail(), ConfigReader.adminPassword());

        DashboardPage dash = new DashboardPage(driver);
        // Allow Angular routing to settle
        long deadline = System.currentTimeMillis() + 10_000;
        while (!dash.isLoaded() && System.currentTimeMillis() < deadline) {
            try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }

        Assert.assertTrue(dash.isOnAdminDashboard(),
            "Expected to land on /admin after IT admin login. Current URL: " + dash.currentUrl());
    }
}
