package com.ticketsystem.qa.api.tests;

import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

/**
 * API_05 — Verify protected endpoints reject unauthenticated requests.
 *
 * Spring Security may respond with 401 (Unauthorized — no credentials) or
 * 403 (Forbidden — depends on filter configuration). Both are valid "denied"
 * responses; the important thing is that the request did not succeed.
 */
public class API05_UnauthorizedAccessTest extends ApiBase {

    @Test(groups = {"smoke", "api", "security"},
          description = "GET /tickets without a JWT is rejected with 401 or 403")
    public void unauthenticatedRequestRejected() {
        given()
            .spec(spec())
        .when()
            .get("/tickets")
        .then()
            .statusCode(anyOf(is(401), is(403)));
    }
}