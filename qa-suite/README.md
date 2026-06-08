# Ticketus QA Suite — 18 Consolidated Tests

A focused test suite covering the Ticketus application across three technologies. Replaces the previous `qa-tests/` and `e2e-tests/` modules with a single, lean module that demonstrates every requested feature.

## Test Inventory

| ID | Category | Test Name | Groups |
|---|---|---|---|
| UI_01 | Selenium UI | Admin login lands on admin console | smoke, ui, auth |
| UI_02 | Selenium UI | Wrong password shows error | smoke, ui, auth, negative |
| UI_03 | Selenium UI | Employee login lands on employee dashboard | regression, ui, auth |
| UI_04 | Selenium UI | Signup auto-logs in user | regression, ui, auth |
| UI_05 | Selenium UI | Employee creates IT ticket | smoke, ui, tickets |
| UI_06 | Selenium UI | Employee creates BUG ticket | regression, ui, tickets |
| UI_07 | Selenium UI | Employee creates confidential HR ticket | regression, ui, tickets, security |
| UI_08 | Selenium UI | Employee searches knowledge base | regression, ui, knowledge |
| API_01 | Rest Assured | Valid login returns token + role | smoke, api, auth |
| API_02 | Rest Assured | Invalid login returns 401 | smoke, api, auth, negative |
| API_03 | Rest Assured | "outage" keyword → URGENT priority | smoke, api, priority-routing |
| API_04 | Rest Assured | "password reset" → LOW priority | regression, api, priority-routing |
| API_05 | Rest Assured | No token → 401 Unauthorized | smoke, api, security |
| API_06 | Rest Assured | Employee can't access /admin/users | regression, api, security |
| BDD_01 | Cucumber | Employee login | smoke, bdd, auth |
| BDD_02 | Cucumber | Create IT ticket | regression, bdd, tickets |
| BDD_03 | Cucumber | Confidential HR ticket | regression, bdd, security, hr |
| BDD_04 | Cucumber | Search knowledge base | regression, bdd, knowledge |

## Features Demonstrated

- **Selenium WebDriver** with Cross-browser support (Chrome / Edge / Firefox via `-Dbrowser=`)
- **Page Object Model with PageFactory** — `@FindBy` annotations in every page object
- **TestNG** with full lifecycle, annotations, and assertions
- **TestNG Groups** — `smoke`, `regression`, `security`, `ui`, `api`, `bdd`, etc.
- **TestNG Listeners** — `ITestListener` (TestListener) + `ISuiteListener` (SuiteListener)
- **Rest Assured** with `given/when/then` DSL
- **API testing** — auth, ticket workflows, security boundaries
- **Cucumber BDD** with Gherkin features and TestNG runner
- **Reporting** — ExtentReports HTML + Excel results sheet + native Cucumber HTML

## Prerequisites

1. Backend running on `http://localhost:8082/api`
2. Frontend running on `http://localhost:4200`
3. MySQL with seeded data (handled by backend DataSeeder)
4. Chrome / Edge / Firefox installed

## Running

```bash
# Full suite (all 18 tests)
mvn clean test

# Smoke tests only (uses groups)
mvn clean test -DsuiteXmlFile=testng-smoke.xml

# Just UI tests
mvn clean test -Dtest='UI*Test'

# Just API tests
mvn clean test -Dtest='API*Test'

# Run on Edge instead of Chrome
mvn clean test -Dbrowser=edge

# Headless mode
mvn clean test -Dheadless=true
```

## Reports

After every run, three reports are generated:

| Report | Location |
|---|---|
| ExtentReports HTML | `target/extent-reports/QASuite_Report_<timestamp>.html` |
| Excel results | `target/test-results/QASuite_Results_<timestamp>.xlsx` |
| Cucumber HTML | `target/cucumber/cucumber-report.html` |
| TestNG default | `target/surefire-reports/index.html` |

## Project Structure

```
qa-suite/
├── pom.xml                                   # Maven build
├── testng.xml                                # Main suite — all 18 tests
├── testng-smoke.xml                          # Smoke-only — uses groups
├── src/test/java/com/ticketsystem/qa/
│   ├── support/                              # Shared infrastructure
│   │   ├── ConfigReader.java                 # Config loader
│   │   ├── DriverFactory.java                # Cross-browser WebDriver
│   │   ├── TestListener.java                 # ITestListener
│   │   ├── SuiteListener.java                # ISuiteListener
│   │   ├── ExtentReporter.java               # HTML reports
│   │   ├── ExcelReporter.java                # Excel reports
│   │   └── TestDataFactory.java              # Unique test data
│   ├── ui/
│   │   ├── pages/                            # Page Objects (PageFactory)
│   │   │   ├── BasePage.java
│   │   │   ├── LoginPage.java
│   │   │   ├── SignupPage.java
│   │   │   ├── DashboardPage.java
│   │   │   ├── CreateTicketPage.java
│   │   │   └── KnowledgeBasePage.java
│   │   └── tests/                            # 8 Selenium tests + base
│   │       ├── UiBaseTest.java
│   │       └── UI01–UI08
│   ├── api/
│   │   └── tests/                            # 6 Rest Assured tests + base
│   │       ├── ApiBase.java
│   │       └── API01–API06
│   └── bdd/
│       ├── runners/CucumberRunner.java       # TestNG ↔ Cucumber bridge
│       └── steps/                            # 4 step classes
│           ├── ScenarioContext.java
│           ├── Hooks.java
│           ├── LoginSteps.java
│           ├── TicketSteps.java
│           └── KbSteps.java
└── src/test/resources/
    ├── config.properties
    └── features/                             # 4 Gherkin feature files
        ├── login.feature
        ├── ticket-creation.feature
        ├── confidential-hr.feature
        └── knowledge-base.feature
```

## Test Pyramid

This suite reflects a healthy test pyramid:
- **8 UI tests** — verify user-visible behaviour (slowest, most important to users)
- **6 API tests** — verify backend contracts and security (fast, high value)
- **4 BDD scenarios** — express requirements as living documentation (stakeholder-readable)

## Replacing the old modules

This module replaces both `qa-tests/` (49 tests) and `e2e-tests/` (8 journeys, 35 steps).
Reduction from 84 atomic tests to 18 focused tests — same coverage of the application's
critical paths, dramatically faster CI runtime (~3 min vs ~10 min).
