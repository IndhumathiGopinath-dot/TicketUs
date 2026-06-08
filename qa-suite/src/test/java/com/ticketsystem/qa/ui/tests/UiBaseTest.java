package com.ticketsystem.qa.ui.tests;

import com.ticketsystem.qa.support.DriverFactory;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * Shared lifecycle for UI tests. One browser per @Test method gives maximum
 * isolation — no leftover cookies, localStorage, or DOM state.
 */
public abstract class UiBaseTest {

    protected WebDriver driver;

    @BeforeMethod
    public void setUp() {
        driver = DriverFactory.create();
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            try { driver.quit(); } catch (Exception ignored) {}
        }
    }
}
