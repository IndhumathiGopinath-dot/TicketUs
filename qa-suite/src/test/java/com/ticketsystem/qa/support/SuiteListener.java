package com.ticketsystem.qa.support;

import org.testng.ISuite;
import org.testng.ISuiteListener;

/**
 * Suite-level TestNG listener. Initialises and flushes both the ExtentReports
 * HTML and the Excel results spreadsheet at the boundaries of the test run.
 */
public class SuiteListener implements ISuiteListener {

    @Override
    public void onStart(ISuite suite) {
        ExtentReporter.init();
        System.out.println("##################################################");
        System.out.println("##  Ticketus QA Suite — STARTED: " + suite.getName());
        System.out.println("##  Browser: " + ConfigReader.browser()
            + "  Headless: " + ConfigReader.headless());
        System.out.println("##################################################");
    }

    @Override
    public void onFinish(ISuite suite) {
        System.out.println("##################################################");
        System.out.println("##  Ticketus QA Suite — FINISHED: " + suite.getName());
        ExtentReporter.flush();
        ExcelReporter.flush();
        System.out.println("##################################################");
    }
}
