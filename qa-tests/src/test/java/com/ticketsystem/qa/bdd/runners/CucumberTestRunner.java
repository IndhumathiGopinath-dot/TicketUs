package com.ticketsystem.qa.bdd.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

/**
 * TestNG runner that picks up all .feature files under src/test/resources/features.
 *
 * Step definitions live in {@code com.ticketsystem.qa.bdd.steps} (glued via the
 * {@code glue} attribute). cucumber-testng turns each scenario into a TestNG
 * "test method" so it slots cleanly into the rest of the suite.
 */
@CucumberOptions(
    features = "src/test/resources/features",
    glue = { "com.ticketsystem.qa.bdd.steps" },
    plugin = { "pretty", "summary",
               "html:target/cucumber-reports/cucumber.html",
               "json:target/cucumber-reports/cucumber.json" },
    monochrome = true
)
public class CucumberTestRunner extends AbstractTestNGCucumberTests {
    // No @Test method needed — AbstractTestNGCucumberTests provides one
    // that drives every scenario as a separate TestNG invocation.
}
