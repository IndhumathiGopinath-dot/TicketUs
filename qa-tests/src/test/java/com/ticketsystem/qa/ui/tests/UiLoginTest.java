package com.ticketsystem.qa.ui.tests;

import com.ticketsystem.qa.ui.pages.DashboardPage;
import com.ticketsystem.qa.ui.pages.LoginPage;
import com.ticketsystem.qa.utils.ExcelReader;
import com.ticketsystem.qa.utils.ResultRecorder;
import com.ticketsystem.qa.utils.TestResult;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;

public class UiLoginTest extends UiBaseTest {

    private static final String SHEET = "UI-Login";

    @DataProvider(name = "loginData")
    public Object[][] loginData() {
        return ExcelReader.readSheet("testdata/login_data.xlsx", "LoginCases");
    }

    @Test(dataProvider = "loginData", description = "UI login — same Excel data as the REST tests")
    public void loginThroughUi(Map<String, String> row) {
        String testId   = row.get("testId");
        String desc     = row.get("description");
        String email    = row.get("email");
        String password = row.get("password");
        String expected = row.get("expected");

        TestResult tr = TestResult.of(testId, desc)
                .input("email='" + email + "', password='" + (password.isEmpty() ? "" : "****") + "'");

        try {
            LoginPage login = new LoginPage(driver).open(baseUrl);

            // Empty-field path: submit should be disabled
            if (email.isEmpty() || password.isEmpty()) {
                login.login(email, password); // no-op when disabled
                boolean stayed = login.isOnLoginPage();
                boolean disabled = !login.submitEnabled();
                boolean passed = stayed && disabled;
                tr.expected("Submit disabled and remain on /login")
                  .actual("Submit disabled: " + disabled + ", on /login: " + stayed)
                  .observed(login.observed())
                  .pass(passed);
                ResultRecorder.record(SHEET, tr);
                Assert.assertTrue(passed);
                return;
            }

            login.login(email, password);

            if ("SUCCESS".equalsIgnoreCase(expected)) {
                DashboardPage dash = new DashboardPage(driver);
                boolean landed = dash.isLoaded() || dash.isAdminLoaded();
                tr.expected("Land on /dashboard or /admin")
                  .actual(landed ? "Landed on " + driver.getCurrentUrl() : "Stayed at " + driver.getCurrentUrl())
                  .observed(landed ? dash.observed() : login.observed())
                  .pass(landed);
                ResultRecorder.record(SHEET, tr);
                Assert.assertTrue(landed);
            } else {
                boolean stayed = login.isOnLoginPage();
                tr.expected("Remain on /login (login rejected)")
                  .actual("URL: " + driver.getCurrentUrl())
                  .observed(login.observed())
                  .pass(stayed);
                ResultRecorder.record(SHEET, tr);
                Assert.assertTrue(stayed);
            }
        } catch (Throwable t) {
            tr.actual("Exception").error(t.getClass().getSimpleName() + ": " + t.getMessage()).errored();
            ResultRecorder.record(SHEET, tr);
            throw t;
        }
    }
}
