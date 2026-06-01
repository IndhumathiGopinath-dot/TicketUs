package com.ticketsystem.qa.utils;

import org.testng.ISuite;
import org.testng.ISuiteListener;

/** Flushes the in-memory results to Excel when the whole suite finishes. */
public class SuiteListener implements ISuiteListener {
    @Override
    public void onStart(ISuite suite) {
        ResultRecorder.clear();
        System.out.println("=== QA suite started: " + suite.getName() + " ===");
    }
    @Override
    public void onFinish(ISuite suite) {
        System.out.println();
        System.out.println("=== QA suite finished: " + suite.getName() + " ===");
        ResultRecorder.flushToExcel();
    }
}
