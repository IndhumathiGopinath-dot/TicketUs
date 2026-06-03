package com.ticketsystem.e2e.api;

import com.ticketsystem.e2e.support.ConfigReader;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/**
 * Shared Rest Assured configuration. API calls in E2E journeys are used for:
 *
 *   1. Test setup — creating users, seeding tickets faster than UI clicks
 *   2. Verification — confirming a UI action produced the right backend state
 *
 * The UI is still the primary actor in each journey — these helpers exist
 * to support the journey, not replace its main flow.
 */
public final class ApiClient {

    private ApiClient() {}

    public static RequestSpecification spec() {
        return new RequestSpecBuilder()
            .setBaseUri(ConfigReader.apiUrl())
            .setContentType(ContentType.JSON)
            .build();
    }

    public static RequestSpecification authSpec(String bearerToken) {
        return new RequestSpecBuilder()
            .setBaseUri(ConfigReader.apiUrl())
            .setContentType(ContentType.JSON)
            .addHeader("Authorization", "Bearer " + bearerToken)
            .build();
    }

    static {
        RestAssured.urlEncodingEnabled = false;
    }
}
