package com.ticketsystem.qa.api.tests;

import com.ticketsystem.qa.support.ConfigReader;
import com.ticketsystem.qa.support.TestDataFactory;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * API_04 — Verify priority engine routes "password reset" to LOW priority.
 */
public class API04_TicketLowTest extends ApiBase {

    @Test(groups = {"regression", "api", "priority-routing"},
          description = "IT ticket with 'password reset' keyword is routed to LOW priority")
    public void passwordResetKeywordYieldsLow() {
        String token = loginAndGetToken(ConfigReader.employeeEmail(),
                                        ConfigReader.employeePass());

        String body = String.format("""
            {
              "title": "%s",
              "description": "Please help me with a password reset for my account",
              "category": "IT"
            }
            """, TestDataFactory.uniqueTitle("Password reset request"));

        given()
            .spec(authSpec(token))
            .body(body)
        .when()
            .post("/tickets")
        .then()
            .statusCode(200)
            .body("priority", equalTo("LOW"));
    }
}
