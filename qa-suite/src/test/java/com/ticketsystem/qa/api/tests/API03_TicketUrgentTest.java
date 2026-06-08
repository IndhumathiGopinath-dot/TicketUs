package com.ticketsystem.qa.api.tests;

import com.ticketsystem.qa.support.ConfigReader;
import com.ticketsystem.qa.support.TestDataFactory;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * API_03 — Verify priority engine routes "outage" keyword to URGENT.
 */
public class API03_TicketUrgentTest extends ApiBase {

    @Test(groups = {"smoke", "api", "priority-routing"},
          description = "IT ticket with 'outage' keyword is routed to URGENT priority")
    public void outageKeywordYieldsUrgent() {
        String token = loginAndGetToken(ConfigReader.employeeEmail(),
                                        ConfigReader.employeePass());

        String body = String.format("""
            {
              "title": "%s",
              "description": "Email server outage — nobody can send mail",
              "category": "IT"
            }
            """, TestDataFactory.uniqueTitle("Mail server outage"));

        given()
            .spec(authSpec(token))
            .body(body)
        .when()
            .post("/tickets")
        .then()
            .statusCode(200)
            .body("priority", equalTo("URGENT"))
            .body("status", equalTo("OPEN"));
    }
}
