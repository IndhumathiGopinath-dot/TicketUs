package com.ticketsystem.qa.rest;

import com.ticketsystem.qa.utils.ConfigReader;
import com.ticketsystem.qa.utils.ExcelReader;
import com.ticketsystem.qa.utils.ResultRecorder;
import com.ticketsystem.qa.utils.TestResult;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.http.HttpResponse;
import java.util.Map;

/**
 * REST tests using plain {@code java.net.http.HttpClient}.
 *
 * Verifies the /api/auth/login endpoint with multiple credential combinations
 * from login_data.xlsx. Each row gets a PASS/FAIL line in the REST-Auth sheet.
 */
public class RestAuthTest {

    private static final String SHEET = "REST-Auth";
    private final String apiBase = ConfigReader.get("api.base.url");

    @DataProvider(name = "loginData")
    public Object[][] loginData() {
        return ExcelReader.readSheet("testdata/login_data.xlsx", "LoginCases");
    }

    @Test(dataProvider = "loginData",
          description = "POST /auth/login — data-driven from Excel")
    public void loginViaRest(Map<String, String> row) {
        String testId   = row.get("testId");
        String desc     = row.get("description");
        String email    = row.get("email");
        String password = row.get("password");
        String expected = row.get("expected");

        TestResult tr = TestResult.of(testId, desc)
                .input("POST " + apiBase + "/auth/login  body={email='" + email + "', password='" + mask(password) + "'}");

        try {
            HttpResponse<String> resp = HttpHelper.postJson(
                    apiBase + "/auth/login",
                    Map.of("email", email, "password", password),
                    null);

            int code = resp.statusCode();
            String body = resp.body();
            boolean isSuccess = "SUCCESS".equalsIgnoreCase(expected);
            boolean passed;

            if (isSuccess) {
                String token = HttpHelper.parse(body).path("token").asText("");
                passed = code == 200 && !token.isEmpty();
                tr.expected("HTTP 200 with non-empty 'token' field")
                  .actual("HTTP " + code + " | token length=" + token.length())
                  .observed(truncate(body, 500));
            } else {
                passed = code >= 400 && code < 500;
                tr.expected("HTTP 4xx error response")
                  .actual("HTTP " + code)
                  .observed(truncate(body, 500));
            }
            tr.pass(passed);
            ResultRecorder.record(SHEET, tr);
            Assert.assertTrue(passed,
                    testId + " — expected " + expected + " but got HTTP " + code + ": " + truncate(body, 200));
        } catch (Throwable t) {
            tr.actual("Exception").error(t.getClass().getSimpleName() + ": " + t.getMessage()).errored();
            ResultRecorder.record(SHEET, tr);
            throw new RuntimeException(t);
        }
    }

    @Test(description = "GET /api/auth/me with a valid token returns the current user")
    public void getCurrentUserWithToken() throws Exception {
        TestResult tr = TestResult.of("R_ME", "GET /auth/me returns the bearer's profile");
        try {
            String token = HttpHelper.login(apiBase,
                    ConfigReader.get("employee.email"), ConfigReader.get("employee.password"));
            HttpResponse<String> resp = HttpHelper.get(apiBase + "/auth/me", token);

            String email = HttpHelper.parse(resp.body()).path("email").asText();
            boolean passed = resp.statusCode() == 200
                    && email.equalsIgnoreCase(ConfigReader.get("employee.email"));

            tr.input("GET " + apiBase + "/auth/me   Authorization: Bearer <token>")
              .expected("HTTP 200, response.email == employee.email")
              .actual("HTTP " + resp.statusCode() + ", email='" + email + "'")
              .observed(truncate(resp.body(), 500))
              .pass(passed);
            ResultRecorder.record(SHEET, tr);
            Assert.assertTrue(passed);
        } catch (Throwable t) {
            tr.actual("Exception").error(t.getClass().getSimpleName() + ": " + t.getMessage()).errored();
            ResultRecorder.record(SHEET, tr);
            throw new RuntimeException(t);
        }
    }

    @Test(description = "GET /auth/me without a token returns 401/403")
    public void getCurrentUserWithoutToken() throws Exception {
        TestResult tr = TestResult.of("R_ME_NO_AUTH", "GET /auth/me without Authorization header → 4xx");
        try {
            HttpResponse<String> resp = HttpHelper.get(apiBase + "/auth/me", null);
            int code = resp.statusCode();
            boolean passed = code == 401 || code == 403;

            tr.input("GET " + apiBase + "/auth/me  (no Authorization header)")
              .expected("HTTP 401 or 403")
              .actual("HTTP " + code)
              .observed(truncate(resp.body(), 300))
              .pass(passed);
            ResultRecorder.record(SHEET, tr);
            Assert.assertTrue(passed, "Unauthenticated /auth/me should return 401 or 403");
        } catch (Throwable t) {
            tr.actual("Exception").error(t.getClass().getSimpleName() + ": " + t.getMessage()).errored();
            ResultRecorder.record(SHEET, tr);
            throw new RuntimeException(t);
        }
    }

    private static String mask(String s) { return s == null || s.isEmpty() ? "" : "****"; }
    private static String truncate(String s, int n) {
        return s == null ? "" : (s.length() > n ? s.substring(0, n) + "..." : s);
    }
}
