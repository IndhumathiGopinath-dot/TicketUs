package com.ticketsystem.qa.ui.tests;

import com.ticketsystem.qa.utils.ConfigReader;
import com.ticketsystem.qa.utils.DriverFactory;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class UiBaseTest {
    protected WebDriver driver;
    protected String baseUrl;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        baseUrl = ConfigReader.get("app.base.url");
        driver = DriverFactory.create();
        driver.get(baseUrl);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driver == null) return;
        try { driver.quit(); } catch (Throwable ignored) { }
        driver = null;
    }
}
