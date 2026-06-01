package com.ticketsystem.qa.restassured;

import com.ticketsystem.qa.rest.HttpHelper;
import com.ticketsystem.qa.utils.ConfigReader;
import com.ticketsystem.qa.utils.ResultRecorder;
import com.ticketsystem.qa.utils.TestResult;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class RestAssuredTicketTest {

    private static final String SHEET = "RestAssured-Tickets";
    private String token;

    @BeforeClass
    public void setup() throws Exception {
        RestAssured.baseURI = ConfigReader.get("api.base.url");
        token = HttpHelper.login(RestAssured.baseURI,
                ConfigReader.get("employee.email"), ConfigReader.get("employee.password"));
    }

    @Test(description = "RA-T01 — Create IT ticket with urgent keyword → priority URGENT")
    public void itTicketUrgent() {
        record("RA-T01", "IT ticket with 'outage' keyword routes to URGENT",
                "IT", "Email server outage in production", "Cannot access mail at all", "URGENT");
    }

    @Test(description = "RA-T02 — Create IT ticket about password → priority LOW")
    public void itTicketLow() {
        record("RA-T02", "IT ticket about 'password reset' routes to LOW",
                "IT", "password reset request", "Forgot my password", "LOW");
    }

    @Test(description = "RA-T03 — HR confidential ticket")
    public void hrConfidential() {
        TestResult tr = TestResult.of("RA-T03", "HR confidential payroll ticket → URGENT priority");
        try {
            Response resp = given()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(Map.of(
                            "category", "HR",
                            "title", "Payroll error in last salary",
                            "description", "Amount credited is incorrect",
                            "requestType", "Payroll query",
                            "confidential", true))
                .when()
                    .post("/tickets")
                .then()
                    .statusCode(200)
                    .body("priority", equalTo("URGENT"))
                    .body("category", equalTo("HR"))
                    .body("confidential", equalTo(true))
                    .body("status", equalTo("OPEN"))
                    .extract().response();

            tr.input("POST /tickets  HR confidential payroll")
              .expected("HTTP 200, priority=URGENT, category=HR, confidential=true, status=OPEN")
              .actual("HTTP " + resp.statusCode() +
                      ", priority=" + resp.jsonPath().getString("priority") +
                      ", confidential=" + resp.jsonPath().getBoolean("confidential"))
              .observed(truncate(resp.getBody().asPrettyString(), 600))
              .pass(true);
            ResultRecorder.record(SHEET, tr);
        } catch (Throwable t) {
            tr.actual("Exception").error(t.getClass().getSimpleName() + ": " + t.getMessage()).errored();
            ResultRecorder.record(SHEET, tr);
            throw t;
        }
    }

    @Test(description = "RA-T04 — GET /tickets returns array with size >= 0")
    public void listMyTickets() {
        TestResult tr = TestResult.of("RA-T04", "GET /tickets returns a JSON array");
        try {
            Response resp = given()
                    .header("Authorization", "Bearer " + token)
                .when()
                    .get("/tickets")
                .then()
                    .statusCode(200)
                    .body("$", isA(java.util.List.class))
                    .extract().response();

            int size = resp.jsonPath().getList("$").size();
            tr.input("GET /tickets  with employee token")
              .expected("HTTP 200, response is a JSON array")
              .actual("HTTP " + resp.statusCode() + ", array length=" + size)
              .observed("Array of " + size + " ticket(s)")
              .pass(true);
            ResultRecorder.record(SHEET, tr);
        } catch (Throwable t) {
            tr.actual("Exception").error(t.getClass().getSimpleName() + ": " + t.getMessage()).errored();
            ResultRecorder.record(SHEET, tr);
            throw t;
        }
    }

    @Test(description = "RA-T05 — Creating a ticket without auth returns 401/403")
    public void unauthorizedCreateBlocked() {
        TestResult tr = TestResult.of("RA-T05", "POST /tickets without token → 401/403");
        try {
            Response resp = given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("category", "IT", "title", "x", "description", "y"))
                .when()
                    .post("/tickets")
                .then()
                    .statusCode(anyOf(is(401), is(403)))
                    .extract().response();

            tr.input("POST /tickets without Authorization header")
              .expected("HTTP 401 or 403")
              .actual("HTTP " + resp.statusCode())
              .observed(truncate(resp.getBody().asString(), 300))
              .pass(true);
            ResultRecorder.record(SHEET, tr);
        } catch (Throwable t) {
            tr.actual("Exception").error(t.getClass().getSimpleName() + ": " + t.getMessage()).errored();
            ResultRecorder.record(SHEET, tr);
            throw t;
        }
    }

    // Helper for the simple "create ticket, verify priority" pattern.
    private void record(String id, String desc, String category, String title,
                         String body, String expectedPriority) {
        TestResult tr = TestResult.of(id, desc);
        try {
            Response resp = given()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(Map.of("category", category, "title", title, "description", body))
                .when()
                    .post("/tickets")
                .then()
                    .statusCode(200)
                    .body("priority", equalTo(expectedPriority))
                    .body("status",   equalTo("OPEN"))
                    .extract().response();

            tr.input("POST /tickets  category=" + category + ", title='" + title + "'")
              .expected("HTTP 200, priority=" + expectedPriority + ", status=OPEN")
              .actual("HTTP " + resp.statusCode() +
                      ", priority=" + resp.jsonPath().getString("priority") +
                      ", status=" + resp.jsonPath().getString("status"))
              .observed(truncate(resp.getBody().asPrettyString(), 600))
              .pass(true);
            ResultRecorder.record(SHEET, tr);
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
