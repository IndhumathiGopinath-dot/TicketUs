package com.ticketsystem.qa.support;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;

/**
 * Builds a configured WebDriver instance. Browser is selected via -Dbrowser=
 * system property (chrome/edge/firefox) or the value in config.properties.
 * WebDriverManager auto-downloads the matching driver binary.
 */
public final class DriverFactory {

    private DriverFactory() {}

    public static WebDriver create() {
        String browser = ConfigReader.browser().toLowerCase();
        WebDriver driver;

        switch (browser) {
            case "edge" -> {
                WebDriverManager.edgedriver().setup();
                driver = buildEdge();
            }
            case "firefox" -> {
                WebDriverManager.firefoxdriver().setup();
                driver = buildFirefox();
            }
            default -> {
                WebDriverManager.chromedriver().setup();
                driver = buildChrome();
            }
        }

        driver.manage().window().setSize(new Dimension(1400, 900));
        driver.manage().timeouts()
            .implicitlyWait(Duration.ofSeconds(ConfigReader.implicitWait()));
        return driver;
    }

    private static WebDriver buildChrome() {
        ChromeOptions o = new ChromeOptions();
        if (ConfigReader.headless()) o.addArguments("--headless=new");
        o.addArguments("--disable-gpu", "--disable-extensions",
                       "--no-sandbox", "--disable-dev-shm-usage",
                       "--disable-popup-blocking");
        return new ChromeDriver(o);
    }

    private static WebDriver buildEdge() {
        EdgeOptions o = new EdgeOptions();
        if (ConfigReader.headless()) o.addArguments("--headless=new");
        o.addArguments("--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage");
        return new EdgeDriver(o);
    }

    private static WebDriver buildFirefox() {
        FirefoxOptions o = new FirefoxOptions();
        if (ConfigReader.headless()) o.addArguments("-headless");
        return new FirefoxDriver(o);
    }
}
