package com.ticketsystem.e2e.support;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Shared lifecycle for every Journey test class.
 *
 * Journey tests use ONE driver instance across all their @Test methods because
 * a journey is a single user session — the test methods simulate consecutive
 * actions of the same user. Per-method browser teardown would break the flow.
 */
public abstract class BaseJourneyTest {

    protected WebDriver driver;
    protected long stepStart;
    protected final String journeyId;

    protected BaseJourneyTest(String journeyId) {
        this.journeyId = journeyId;
    }

    @BeforeClass(alwaysRun = true)
    public void beforeJourney() {
        driver = DriverFactory.create();
        System.out.println("===== START " + journeyId + " =====");
    }

    @AfterClass(alwaysRun = true)
    public void afterJourney() {
        if (driver != null) driver.quit();
        System.out.println("===== END   " + journeyId + " =====");
    }

    /** Mark the start of a step. Pair with {@link #stepPass}/{@link #stepFail}. */
    protected void stepBegin() {
        stepStart = System.currentTimeMillis();
    }

    protected void stepPass(String stepName, String note) {
        long dur = System.currentTimeMillis() - stepStart;
        JourneyReporter.record(journeyId, stepName, "PASS", dur, note);
        System.out.printf("  [PASS] %s — %s (%d ms)%n", stepName, note, dur);
    }

    protected void stepFail(String stepName, Throwable t) {
        long dur = System.currentTimeMillis() - stepStart;
        JourneyReporter.record(journeyId, stepName, "FAIL", dur, t.getMessage());
        System.out.printf("  [FAIL] %s — %s (%d ms)%n", stepName, t.getMessage(), dur);
        captureScreenshot(stepName);
    }

    private void captureScreenshot(String stepName) {
        if (!ConfigReader.getBool("screenshot.on.failure")) return;
        if (!(driver instanceof TakesScreenshot)) return;
        try {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String dir = ConfigReader.get("report.directory") + "/screenshots";
            new File(dir).mkdirs();
            String stamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("HHmmss"));
            Path dest = Path.of(dir, journeyId + "_" + stepName + "_" + stamp + ".png");
            Files.copy(src.toPath(), dest);
            System.out.println("  Screenshot: " + dest);
        } catch (Exception ignored) {}
    }
}
