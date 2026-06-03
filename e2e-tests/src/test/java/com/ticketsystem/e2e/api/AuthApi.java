package com.ticketsystem.e2e.api;

import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Backend authentication helpers. Used by journeys that need to verify
 * post-conditions through the API after a UI action, or to seed a user
 * faster than going through the signup screen.
 */
public final class AuthApi {

    private AuthApi() {}

    /** Login and return the JWT token. */
    public static String login(String email, String password) {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        Response resp = given().spec(ApiClient.spec())
            .body(body)
            .post("/auth/login");

        if (resp.statusCode() != 200) {
            throw new RuntimeException("Login failed for " + email + ": " + resp.statusCode());
        }
        return resp.jsonPath().getString("token");
    }

    /** Signup and return the JWT token. */
    public static String signup(String name, String email, String password,
                                String role, String department) {
        Map<String, String> body = new HashMap<>();
        body.put("name", name);
        body.put("email", email);
        body.put("password", password);
        body.put("role", role);
        if (department != null) body.put("department", department);

        Response resp = given().spec(ApiClient.spec())
            .body(body)
            .post("/auth/signup");

        if (resp.statusCode() != 200) {
            throw new RuntimeException("Signup failed for " + email
                + ": status=" + resp.statusCode() + " body=" + resp.asString());
        }
        return resp.jsonPath().getString("token");
    }

    /** Fetch the currently-authenticated user. */
    public static Response me(String token) {
        return given().spec(ApiClient.authSpec(token)).get("/auth/me");
    }
}
