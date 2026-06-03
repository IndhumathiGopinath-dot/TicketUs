package com.ticketsystem.e2e.api;

import io.restassured.response.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Backend ticket helpers for E2E journeys — used primarily for verification
 * (e.g. confirming a UI submit produced the correct backend state) and
 * occasionally for setup (seeding tickets without slow UI clicks).
 */
public final class TicketApi {

    private TicketApi() {}

    public static Response createTicket(String token, Map<String, Object> ticket) {
        return given().spec(ApiClient.authSpec(token))
            .body(ticket)
            .post("/tickets");
    }

    public static Response getTicket(String token, long id) {
        return given().spec(ApiClient.authSpec(token)).get("/tickets/" + id);
    }

    public static List<Map<String, Object>> listMyTickets(String token) {
        Response resp = given().spec(ApiClient.authSpec(token)).get("/tickets");
        return resp.jsonPath().getList("$");
    }

    public static Map<String, Object> buildItTicket(String title, String description,
                                                    String assetTag) {
        Map<String, Object> t = new HashMap<>();
        t.put("title", title);
        t.put("description", description);
        t.put("category", "IT");
        if (assetTag != null) t.put("assetTag", assetTag);
        return t;
    }

    public static Map<String, Object> buildBugTicket(String title, String description,
                                                     String severity, String appVersion) {
        Map<String, Object> t = new HashMap<>();
        t.put("title", title);
        t.put("description", description);
        t.put("category", "BUG");
        t.put("severity", severity);
        if (appVersion != null) t.put("appVersion", appVersion);
        return t;
    }

    public static Map<String, Object> buildHrTicket(String title, String description,
                                                    String requestType, boolean confidential) {
        Map<String, Object> t = new HashMap<>();
        t.put("title", title);
        t.put("description", description);
        t.put("category", "HR");
        t.put("requestType", requestType);
        t.put("confidential", confidential);
        return t;
    }
}
