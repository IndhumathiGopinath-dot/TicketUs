package com.ticketsystem.e2e.support;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

/**
 * Builds a configured WebDriver instance for E2E tests. Chrome is used by
 * default; headless mode is toggled via the {@code headless} config key so
 * the suite can run in CI without a display.
 */
public final class DriverFactory {

    private DriverFactory() {}

    public static WebDriver create() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        if (ConfigReader.headless()) {
            options.addArguments("--headless=new");
        }
        options.addArguments(
            "--window-size=1400,900",
            "--disable-gpu",
            "--disable-extensions",
            "--no-sandbox",
            "--disable-dev-shm-usage",
            "--disable-popup-blocking",
            "--disable-notifications"
        );

        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts()
            .pageLoadTimeout(Duration.ofSeconds(ConfigReader.pageLoadTimeout()))
            .implicitlyWait(Duration.ofSeconds(0));  // explicit waits only — never mix

        return driver;
    }
}
