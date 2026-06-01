package com.ticketsystem.qa.utils;

import org.testng.annotations.BeforeSuite;

public class SuiteSetup {
    @BeforeSuite(alwaysRun = true)
    public void generateData() throws Exception {
        TestDataGenerator.generateAll();
        System.out.println("Input data generated under target/test-classes/testdata/");
    }
}
