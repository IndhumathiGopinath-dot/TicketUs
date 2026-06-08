package com.ticketsystem.qa.support;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.HashMap;
import java.util.Map;

/**
 * TestNG ITestListener hook. Fires on every test method's start/pass/fail/skip.
 * Responsibilities:
 *  - Create an ExtentTest node per @Test method
 *  - Record results to Excel
 *  - Log to console
 */
public class TestListener implements ITestListener {

    /** Thread-safe map of test method → ExtentTest node so subclasses can attach logs. */
    private static final Map<String, ExtentTest> extentMap = new HashMap<>();

    @Override
    public void onTestStart(ITestResult result) {
        String name = result.getMethod().getMethodName();
        String desc = result.getMethod().getDescription();
        ExtentTest et = ExtentReporter.startTest(name, desc == null ? "" : desc);

        // Attach group/category info if available
        String[] groups = result.getMethod().getGroups();
        for (String g : groups) et.assignCategory(g);

        extentMap.put(key(result), et);
        System.out.println("[START ] " + name + (desc != null ? " — " + desc : ""));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        long duration = result.getEndMillis() - result.getStartMillis();
        ExtentTest et = extentMap.get(key(result));
        if (et != null) et.log(Status.PASS, "Test passed in " + duration + " ms");
        recordExcel(result, "PASS", duration, "");
        System.out.println("[PASS  ] " + result.getMethod().getMethodName() + " (" + duration + " ms)");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        long duration = result.getEndMillis() - result.getStartMillis();
        String msg = result.getThrowable() == null ? "" : result.getThrowable().getMessage();
        ExtentTest et = extentMap.get(key(result));
        if (et != null) {
            et.log(Status.FAIL, "Test failed: " + msg);
            if (result.getThrowable() != null) et.fail(result.getThrowable());
        }
        recordExcel(result, "FAIL", duration, msg);
        System.out.println("[FAIL  ] " + result.getMethod().getMethodName() + " — " + msg);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest et = extentMap.get(key(result));
        if (et != null) et.log(Status.SKIP, "Test skipped");
        recordExcel(result, "SKIP", 0, "skipped");
        System.out.println("[SKIP  ] " + result.getMethod().getMethodName());
    }

    @Override
    public void onStart(ITestContext context) {
        System.out.println("\n========== <test> " + context.getName() + " started ==========\n");
    }

    @Override
    public void onFinish(ITestContext context) {
        System.out.println("\n========== <test> " + context.getName() + " finished — "
            + "Passed: " + context.getPassedTests().size()
            + ", Failed: " + context.getFailedTests().size()
            + ", Skipped: " + context.getSkippedTests().size() + " ==========\n");
    }

    private String key(ITestResult result) {
        return result.getMethod().getQualifiedName() + "#" + result.hashCode();
    }

    private void recordExcel(ITestResult result, String status, long duration, String msg) {
        String testName = result.getMethod().getMethodName();
        String desc = result.getMethod().getDescription();
        String className = result.getTestClass().getName();
        String shortClass = className.substring(className.lastIndexOf('.') + 1);
        String groups = String.join(",", result.getMethod().getGroups());
        ExcelReporter.record(new ExcelReporter.Row(
            shortClass, desc == null ? testName : desc,
            classCategory(className), groups, status, duration, msg
        ));
    }

    private String classCategory(String className) {
        if (className.contains(".ui.")) return "Selenium UI";
        if (className.contains(".api.")) return "Rest Assured API";
        if (className.contains(".bdd.")) return "Cucumber BDD";
        return "Other";
    }
}
