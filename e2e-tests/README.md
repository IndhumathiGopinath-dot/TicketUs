# Ticketus E2E Test Suite

End-to-end test suite for the Ticketus ticket management system. Each test
walks a complete user journey through the running application, exercising
both the Angular frontend and the Spring Boot backend the same way a real
user would.

This module is **complementary to** the existing `qa-tests/` suite. Where
`qa-tests/` focuses on isolated technology coverage (REST, SOAP, UI, BDD)
with many small atomic tests, this suite walks **multi-step user journeys**
that span login → action → verification.

---

## Suite Composition

| ID  | Journey                         | Style          | What It Verifies                                                  |
|-----|---------------------------------|----------------|-------------------------------------------------------------------|
| J01 | New Employee Lifecycle          | UI + API       | Signup → login → create ticket → verify on dashboard and via API  |
| J02 | IT Support Full Cycle           | UI + API       | Employee creates urgent IT ticket, admin sees it on their console |
| J03 | Critical Bug Flow               | UI + API       | BUG-specific fields appear, CRITICAL severity escalates to URGENT |
| J04 | HR Confidential Boundary        | UI + API       | Security test — IT admin cannot see HR confidential tickets       |
| J05 | Knowledge Base Self-Service     | UI             | KB renders, search works, navigation without raising tickets      |
| J06 | Priority Routing Matrix         | API            | Five-row matrix verifying auto-priority rules                     |
| J07 | Admin Multi-Ticket Workflow     | API + UI       | Seed 3 tickets, admin dashboard reflects them                     |
| J08 | Notification Delivery           | API            | Creating a ticket triggers a notification for the assigned admin  |

**Total: 8 journeys, ~32 sequential test steps.**

---

## Prerequisites

- **Java 21+**
- **Maven 3.9+**
- **Chrome 120+** (the suite auto-downloads the matching ChromeDriver)
- **Ticketus backend running** on `http://localhost:8082/api`
- **Ticketus frontend running** on `http://localhost:4200`
- **MySQL running** on `localhost:3306` with the seeded users present

The seeded users that come from the backend's `DataSeeder`:

| Email                    | Password    | Role  | Department  |
|--------------------------|-------------|-------|-------------|
| `it.admin@company.com`   | `admin123`  | ADMIN | IT          |
| `hr.admin@company.com`   | `admin123`  | ADMIN | HR          |
| `john@company.com`       | `password123` | EMPLOYEE | Engineering |

---

## Running the Suite

```bash
cd e2e-tests
mvn clean test
```

**Headless mode** (for CI):

```bash
mvn test -Dheadless=true
```

**Override URLs** (e.g. against staging):

```bash
mvn test -Dbase.url=http://staging.example.com -Dapi.url=http://staging.example.com/api
```

---

## Outputs

| Artifact                                                  | Description                                                              |
|-----------------------------------------------------------|--------------------------------------------------------------------------|
| `target/e2e-reports/E2E_Results_<timestamp>.xlsx`         | Step-by-step Excel report — one row per step, green=PASS, red=FAIL       |
| `target/surefire-reports/index.html`                      | TestNG HTML report — auto-generated, includes timing and stack traces    |
| `target/e2e-reports/screenshots/`                         | Screenshots automatically captured on any step failure                   |

---

## Architecture

```
e2e-tests/
├── pom.xml
├── README.md
└── src/test/
    ├── java/com/ticketsystem/e2e/
    │   ├── journeys/        ← 8 journey test classes (the deliverable)
    │   ├── pages/           ← Page Object Model for UI screens
    │   ├── api/             ← Rest Assured helpers for setup/verification
    │   ├── support/         ← DriverFactory, ConfigReader, BaseJourneyTest, JourneyReporter
    │   └── listeners/       ← E2EListener (TestNG suite-level Excel report writer)
    └── resources/
        ├── config.properties
        └── testng.xml
```

### Design Principles

1. **Journey ≠ unit test.** Each journey is a sequence of `@Test` methods with
   `priority = 1, 2, 3 ...`. Methods within a journey share state (driver, IDs,
   tokens) because they simulate consecutive actions of the same user.

2. **One driver per journey, not per test.** The `BaseJourneyTest` opens the
   browser in `@BeforeClass` and closes it in `@AfterClass`. A new driver per
   `@Test` would break the session continuity that defines a journey.

3. **Page Object Model.** All UI locators live in `pages/`. Tests read as user
   actions (`loginPage.loginAs(...)`), not as Selenium calls.

4. **JS-driven Angular interactions.** `CreateTicketPage` drives the dynamic
   Angular form through JavaScript executor calls that dispatch native input
   and change events. This is the same technique Angular's own test utilities
   use internally and avoids the timing flakes that native Selenium clicks
   produce against debounced reactive forms.

5. **API for setup and verification, UI for the journey.** Test setup
   (seeding tickets, getting tokens) and post-action verification (confirming
   backend state matches UI state) are done through Rest Assured calls.
   The main flow of each journey still goes through the UI.

6. **No shared mutable state across journeys.** Each journey runs against a
   fresh browser and uses `TestDataFactory.uniqueTitle()` / `uniqueEmail()` to
   avoid colliding with data from previous runs.

---

## Test Pyramid Context

This suite sits at the top of the test pyramid:

```
                        ┌─────────────────┐
                        │   E2E Journeys  │   ← this module (8 journeys)
                        └─────────────────┘
                  ┌───────────────────────────┐
                  │   QA Suite (qa-tests/)    │   ← 49 atomic tests
                  └───────────────────────────┘
            ┌───────────────────────────────────────┐
            │  Backend unit tests (PriorityService) │
            └───────────────────────────────────────┘
```

E2E tests are the fewest but the most expensive (full browser, full backend,
real database). They catch integration bugs that unit and component tests
cannot — a frontend that posts the wrong JSON shape, a security boundary
that leaks confidential tickets, a priority routing rule that doesn't actually
make it from the service into the response.

If a journey fails, you should also expect the corresponding lower-level test
to fail — and that lower-level test should be your first stop for diagnosis.

---

## Known Considerations

- **Test data isolation**: Each run leaves real records in the database. For
  long-running environments you may want to reset the schema periodically:
  `mysql -uroot -p -e "DROP DATABASE ticket_system;"` then restart the
  backend. The DataSeeder will reseed users and KB articles.

- **Test ordering matters**: Journeys run sequentially (`parallel="false"` in
  testng.xml). Parallel execution would produce flakes because tickets created
  by one journey can affect similar-ticket suggestions in another.

- **Selenium-vs-Angular timing**: see CreateTicketPage's javadoc for the
  rationale behind the JS-executor approach. This was a hard-won lesson and
  is documented inline so future maintainers don't undo it.
