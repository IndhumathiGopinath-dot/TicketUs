package com.ticketsystem.e2e.listeners;

import com.ticketsystem.e2e.support.JourneyReporter;
import org.testng.ISuite;
import org.testng.ISuiteListener;

/**
 * Hooks into TestNG's suite lifecycle to write the consolidated Excel report
 * once every journey has finished running.
 */
public class E2EListener implements ISuiteListener {

    @Override
    public void onStart(ISuite suite) {
        System.out.println("=== Ticketus E2E Suite STARTED: " + suite.getName() + " ===");
    }

    @Override
    public void onFinish(ISuite suite) {
        System.out.println("=== Ticketus E2E Suite FINISHED: " + suite.getName() + " ===");
        String path = JourneyReporter.flush();
        System.out.println("=== E2E Excel report: " + path + " ===");
    }
}
