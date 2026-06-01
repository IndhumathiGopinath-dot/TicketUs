package com.ticketsystem.qa.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Thin REST client built on Java 17's {@code java.net.http.HttpClient}.
 * Intentionally minimal — the point is to show REST plumbing explicitly
 * (status, headers, body, JSON parsing) before contrasting with Rest Assured.
 */
public class HttpHelper {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static HttpResponse<String> get(String url, String bearerToken) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET();
        if (bearerToken != null) b.header("Authorization", "Bearer " + bearerToken);
        return CLIENT.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> postJson(String url, Object body, String bearerToken) throws Exception {
        String json = MAPPER.writeValueAsString(body);
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json));
        if (bearerToken != null) b.header("Authorization", "Bearer " + bearerToken);
        return CLIENT.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    public static JsonNode parse(String body) {
        try { return MAPPER.readTree(body); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    /** Convenience: log into the backend and return the bearer token. */
    public static String login(String apiBase, String email, String password) throws Exception {
        HttpResponse<String> resp = postJson(apiBase + "/auth/login",
                Map.of("email", email, "password", password), null);
        if (resp.statusCode() != 200) {
            throw new RuntimeException("Login failed: HTTP " + resp.statusCode() + " — " + resp.body());
        }
        return parse(resp.body()).get("token").asText();
    }
}
