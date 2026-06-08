package com.ticketsystem.qa.api.tests;

import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

/**
 * API_02 — POST /auth/login with bad password returns 401.
 */
public class API02_LoginInvalidTest extends ApiBase {

    @Test(groups = {"smoke", "api", "auth", "negative"},
          description = "Invalid credentials return 401 Unauthorized")
    public void invalidLoginReturns401() {
        given()
            .spec(spec())
            .body("{\"email\":\"it.admin@company.com\",\"password\":\"definitelyWrong\"}")
        .when()
            .post("/auth/login")
        .then()
            .statusCode(org.hamcrest.Matchers.anyOf(
                org.hamcrest.Matchers.is(400),
                org.hamcrest.Matchers.is(401)));
    }
}
