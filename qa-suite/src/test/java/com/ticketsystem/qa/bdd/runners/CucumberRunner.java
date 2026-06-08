package com.ticketsystem.qa.bdd.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

/**
 * Bridges Cucumber to TestNG. Each Gherkin scenario in the features directory
 * runs as one TestNG @Test method, listed under the "Cucumber-BDD" <test> block.
 *
 * Reports: native Cucumber HTML + JSON in target/cucumber/.
 */
@CucumberOptions(
    features = "src/test/resources/features",
    glue = "com.ticketsystem.qa.bdd.steps",
    plugin = {
        "pretty",
        "html:target/cucumber/cucumber-report.html",
        "json:target/cucumber/cucumber-report.json"
    },
    monochrome = true
)
public class CucumberRunner extends AbstractTestNGCucumberTests {}
