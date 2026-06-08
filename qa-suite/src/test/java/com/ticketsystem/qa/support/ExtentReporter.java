package com.ticketsystem.qa.support;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Wrapper around ExtentReports. Produces a polished HTML report at
 * target/extent-reports/QASuite_Report_<timestamp>.html with collapsible
 * test entries, color-coded pass/fail, and step-level logs.
 */
public final class ExtentReporter {

    private static ExtentReports extent;
    private static String reportPath;

    private ExtentReporter() {}

    public static synchronized void init() {
        if (extent != null) return;

        String timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File dir = new File("target/extent-reports");
        if (!dir.exists()) dir.mkdirs();

        reportPath = dir.getAbsolutePath() + "/QASuite_Report_" + timestamp + ".html";

        ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
        spark.config().setTheme(Theme.STANDARD);
        spark.config().setDocumentTitle("Ticketus QA Suite Report");
        spark.config().setReportName("Ticketus — 18 Test Consolidated Suite");

        extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("Application", "Ticketus");
        extent.setSystemInfo("Browser", ConfigReader.browser());
        extent.setSystemInfo("OS", System.getProperty("os.name"));
        extent.setSystemInfo("Java", System.getProperty("java.version"));
    }

    public static ExtentTest startTest(String name, String description) {
        if (extent == null) init();
        return extent.createTest(name, description);
    }

    public static void flush() {
        if (extent != null) {
            extent.flush();
            System.out.println("==> Extent HTML report: " + reportPath);
        }
    }

    public static String reportPath() {
        return reportPath;
    }
}
