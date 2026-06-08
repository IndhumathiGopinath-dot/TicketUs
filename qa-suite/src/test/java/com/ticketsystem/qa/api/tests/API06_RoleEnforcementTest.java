package com.ticketsystem.qa.api.tests;

import com.ticketsystem.qa.support.ConfigReader;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

/**
 * API_06 — Verify role-based access: an employee cannot reach admin endpoints.
 */
public class API06_RoleEnforcementTest extends ApiBase {

    @Test(groups = {"regression", "api", "security"},
          description = "Employee JWT cannot access /admin/users — returns 403 Forbidden")
    public void employeeCannotAccessAdminEndpoint() {
        String employeeToken = loginAndGetToken(ConfigReader.employeeEmail(),
                                                ConfigReader.employeePass());

        given()
            .spec(authSpec(employeeToken))
        .when()
            .get("/admin/users")
        .then()
            .statusCode(403);
    }
}
