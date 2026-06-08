package com.ticketsystem.qa.api.tests;

import com.ticketsystem.qa.support.ExcelDataReader;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

/**
 * API_01 — Data-driven login test.
 *
 * Test scenarios are read from src/test/resources/login-data.xlsx using
 * Apache POI. Each row in the spreadsheet becomes one test invocation,
 * demonstrating data-driven testing with TestNG's @DataProvider mechanism.
 *
 * The spreadsheet has columns: testId | email | password | expectedStatus | description
 * Backend may return either 200 (OK) for valid creds, or 400/401 for invalid —
 * tests use anyOf matchers to tolerate either rejection code.
 */
public class API01_LoginValidTest extends ApiBase {

    /**
     * Reads login-data.xlsx via Apache POI and exposes each row as a test case.
     * TestNG invokes the @Test method once per Object[] returned here.
     */
    @DataProvider(name = "loginScenarios")
    public Object[][] loginScenarios() {
        List<Map<String, String>> rows =
            ExcelDataReader.readSheet("login-data.xlsx", "logins");
        Object[][] data = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) {
            data[i][0] = rows.get(i);
        }
        return data;
    }

    @Test(dataProvider = "loginScenarios",
          groups = {"smoke", "api", "auth", "data-driven"},
          description = "Data-driven login — Excel scenarios cover valid and invalid credentials")
    public void loginScenariosFromExcel(Map<String, String> row) {
        String testId         = row.get("testId");
        String email          = row.get("email");
        String password       = row.get("password");
        int    expectedStatus = Integer.parseInt(row.get("expectedStatus"));
        String description    = row.get("description");

        System.out.println("[API_01:" + testId + "] " + description
            + " — email=" + email + " expected=" + expectedStatus);

        // Backend tolerance: invalid logins may come back as 400 (Bad Request from validator)
        // or 401 (Unauthorized from auth service). Both are valid "denied" responses.
        Object statusMatcher;
        if (expectedStatus == 200) {
            statusMatcher = is(200);
        } else {
            statusMatcher = anyOf(is(400), is(401));
        }

        given()
            .spec(spec())
            .body("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}")
        .when()
            .post("/auth/login")
        .then()
            .statusCode((org.hamcrest.Matcher<Integer>) statusMatcher);
    }
}
