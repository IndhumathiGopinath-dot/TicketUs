package com.ticketsystem.qa.api.tests;

import com.ticketsystem.qa.support.ConfigReader;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;

/**
 * Shared helpers for Rest Assured tests. Centralises base URI and provides
 * spec builders for both authenticated and anonymous requests.
 */
public abstract class ApiBase {

    @BeforeClass
    public void initBase() {
        RestAssured.baseURI = ConfigReader.apiBaseUrl();
    }

    /** Anonymous spec — no Authorization header. */
    protected RequestSpecification spec() {
        return new RequestSpecBuilder()
            .setBaseUri(ConfigReader.apiBaseUrl())
            .setContentType(ContentType.JSON)
            .build();
    }

    /** Authenticated spec — adds Bearer token. */
    protected RequestSpecification authSpec(String token) {
        return new RequestSpecBuilder()
            .setBaseUri(ConfigReader.apiBaseUrl())
            .setContentType(ContentType.JSON)
            .addHeader("Authorization", "Bearer " + token)
            .build();
    }

    /** Convenience: log in and return the JWT token. */
    protected String loginAndGetToken(String email, String password) {
        Response resp = io.restassured.RestAssured.given(spec())
            .body("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}")
            .when().post("/auth/login");
        if (resp.statusCode() != 200) {
            throw new IllegalStateException("Login failed: " + resp.statusCode() + " " + resp.body().asString());
        }
        return resp.jsonPath().getString("token");
    }
}
