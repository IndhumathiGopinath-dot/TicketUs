package com.ticketsystem.qa.restassured;

import com.ticketsystem.qa.utils.ConfigReader;
import com.ticketsystem.qa.utils.ResultRecorder;
import com.ticketsystem.qa.utils.TestResult;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Same endpoints as RestAuthTest, but written with Rest Assured.
 * Compare side-by-side — Rest Assured's given/when/then chain compresses
 * request setup, body matching, and JSON path assertions into one block.
 */
public class RestAssuredAuthTest {

    private static final String SHEET = "RestAssured-Auth";

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = ConfigReader.get("api.base.url");
    }

    @Test(description = "RA-01 — Valid login returns 200 with token, role, name")
    public void validLoginReturnsTokenAndRole() {
        TestResult tr = TestResult.of("RA01", "Valid employee login returns 200 + token + role");
        try {
            Response resp = given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("email",    ConfigReader.get("employee.email"),
                                 "password", ConfigReader.get("employee.password")))
                .when()
                    .post("/auth/login")
                .then()
                    .statusCode(200)
                    .body("token", not(emptyOrNullString()))
                    .body("role",  equalTo("EMPLOYEE"))
                    .extract().response();

            String token = resp.jsonPath().getString("token");
            String role  = resp.jsonPath().getString("role");

            tr.input("POST /auth/login  body={email='" + ConfigReader.get("employee.email") + "', password='****'}")
              .expected("HTTP 200, token non-empty, role='EMPLOYEE'")
              .actual("HTTP " + resp.statusCode() + ", token length=" + token.length() + ", role='" + role + "'")
              .observed(truncate(resp.getBody().asPrettyString(), 500))
              .pass(true);
            ResultRecorder.record(SHEET, tr);
        } catch (Throwable t) {
            tr.actual("Exception").error(t.getClass().getSimpleName() + ": " + t.getMessage()).errored();
            ResultRecorder.record(SHEET, tr);
            throw t;
        }
    }

    @Test(description = "RA-02 — Wrong password returns 401")
    public void wrongPasswordReturns401() {
        TestResult tr = TestResult.of("RA02", "Login with wrong password returns 4xx");
        try {
            Response resp = given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("email", "john@company.com", "password", "wrong"))
                .when()
                    .post("/auth/login")
                .then()
                    .statusCode(anyOf(is(400), is(401), is(403)))
                    .extract().response();
            tr.input("POST /auth/login  body={email='john@company.com', password='****' (wrong)}")
              .expected("HTTP 400/401/403")
              .actual("HTTP " + resp.statusCode())
              .observed(truncate(resp.getBody().asPrettyString(), 500))
              .pass(true);
            ResultRecorder.record(SHEET, tr);
        } catch (Throwable t) {
            tr.actual("Exception").error(t.getClass().getSimpleName() + ": " + t.getMessage()).errored();
            ResultRecorder.record(SHEET, tr);
            throw t;
        }
    }

    @Test(description = "RA-03 — Signup with an existing email returns an error")
    public void duplicateSignupReturnsError() {
        TestResult tr = TestResult.of("RA03", "Signup with existing email fails");
        try {
            Response resp = given()
                    .contentType(ContentType.JSON)
                    .body(Map.of(
                            "name", "Dup",
                            "email", "john@company.com",
                            "password", "password123",
                            "role", "EMPLOYEE",
                            "department", "Engineering"))
                .when()
                    .post("/auth/signup")
                .then()
                    .statusCode(anyOf(is(400), is(409)))
                    .extract().response();
            tr.input("POST /auth/signup  body={email='john@company.com' (already exists)}")
              .expected("HTTP 400 or 409 (conflict)")
              .actual("HTTP " + resp.statusCode())
              .observed(truncate(resp.getBody().asPrettyString(), 500))
              .pass(true);
            ResultRecorder.record(SHEET, tr);
        } catch (Throwable t) {
            tr.actual("Exception").error(t.getClass().getSimpleName() + ": " + t.getMessage()).errored();
            ResultRecorder.record(SHEET, tr);
            throw t;
        }
    }

    @Test(description = "RA-04 — Successful signup returns token and configured role")
    public void successfulSignupReturnsToken() {
        String uniqueEmail = "ratest_" + System.currentTimeMillis() + "@test.com";
        TestResult tr = TestResult.of("RA04", "Brand-new signup returns 200 + token");
        try {
            Response resp = given()
                    .contentType(ContentType.JSON)
                    .body(Map.of(
                            "name", "RA Test User",
                            "email", uniqueEmail,
                            "password", "password123",
                            "role", "EMPLOYEE",
                            "department", "Engineering"))
                .when()
                    .post("/auth/signup")
                .then()
                    .statusCode(200)
                    .body("token", not(emptyOrNullString()))
                    .body("email", equalTo(uniqueEmail))
                    .body("role",  equalTo("EMPLOYEE"))
                    .extract().response();

            tr.input("POST /auth/signup  body={email='" + uniqueEmail + "', role='EMPLOYEE'}")
              .expected("HTTP 200, token non-empty, email matches, role='EMPLOYEE'")
              .actual("HTTP " + resp.statusCode() + ", email='" + resp.jsonPath().getString("email") + "'")
              .observed(truncate(resp.getBody().asPrettyString(), 500))
              .pass(true);
            ResultRecorder.record(SHEET, tr);
            Assert.assertEquals(resp.jsonPath().getString("email"), uniqueEmail);
        } catch (Throwable t) {
            tr.actual("Exception").error(t.getClass().getSimpleName() + ": " + t.getMessage()).errored();
            ResultRecorder.record(SHEET, tr);
            throw t;
        }
    }

    private static String truncate(String s, int n) {
        return s == null ? "" : (s.length() > n ? s.substring(0, n) + "..." : s);
    }
}
