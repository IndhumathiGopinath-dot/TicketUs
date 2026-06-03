package com.ticketsystem.e2e.api;

import io.restassured.response.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Backend admin helpers. Useful in E2E journeys for verifying that confidential
 * HR tickets are correctly filtered per-admin-department, and for inspecting
 * the full ticket list that an admin would see.
 */
public final class AdminApi {

    private AdminApi() {}

    public static List<Map<String, Object>> listAllTickets(String adminToken) {
        Response resp = given().spec(ApiClient.authSpec(adminToken)).get("/admin/tickets");
        return resp.jsonPath().getList("$");
    }

    public static Response assignTicket(String adminToken, long ticketId, long adminUserId) {
        Map<String, Object> body = new HashMap<>();
        body.put("assigneeId", adminUserId);
        return given().spec(ApiClient.authSpec(adminToken))
            .body(body)
            .put("/admin/tickets/" + ticketId + "/assign");
    }

    public static Response updateStatus(String adminToken, long ticketId, String status) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status);
        return given().spec(ApiClient.authSpec(adminToken))
            .body(body)
            .put("/admin/tickets/" + ticketId + "/status");
    }
}
