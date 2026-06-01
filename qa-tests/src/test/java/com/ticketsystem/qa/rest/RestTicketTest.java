package com.ticketsystem.qa.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.ticketsystem.qa.utils.ConfigReader;
import com.ticketsystem.qa.utils.ExcelReader;
import com.ticketsystem.qa.utils.ResultRecorder;
import com.ticketsystem.qa.utils.TestResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * REST tests for ticket endpoints via plain HttpClient.
 * Demonstrates: token re-use across tests, JSON body construction,
 * status + body assertions on the same response.
 */
public class RestTicketTest {

    private static final String SHEET = "REST-Tickets";
    private final String apiBase = ConfigReader.get("api.base.url");
    private String employeeToken;

    @BeforeClass
    public void loginOnce() throws Exception {
        employeeToken = HttpHelper.login(apiBase,
                ConfigReader.get("employee.email"), ConfigReader.get("employee.password"));
    }

    @DataProvider(name = "ticketData")
    public Object[][] ticketData() {
        return ExcelReader.readSheet("testdata/ticket_data.xlsx", "TicketCases");
    }

    @Test(dataProvider = "ticketData",
          description = "POST /tickets — data-driven creation across categories")
    public void createTicketViaRest(Map<String, String> row) {
        String testId           = row.get("testId");
        String desc             = row.get("description");
        String expectedPriority = row.get("expectedPriority");

        Map<String, Object> body = new HashMap<>();
        body.put("category",      row.get("category"));
        body.put("title",         row.get("title"));
        body.put("description",   row.get("ticketDescription"));
        body.put("confidential",  Boolean.parseBoolean(row.get("confidential")));
        putIfPresent(body, "severity",    row.get("severity"));
        putIfPresent(body, "appVersion",  row.get("appVersion"));
        putIfPresent(body, "assetTag",    row.get("assetTag"));
        putIfPresent(body, "requestType", row.get("requestType"));
        
        TestResult tr = TestResult.of(testId, desc)
                .input("POST " + apiBase + "/tickets  category=" + row.get("category") +
                        ", title='" + row.get("title") + "'");

        try {
            HttpResponse<String> resp = HttpHelper.postJson(apiBase + "/tickets", body, employeeToken);
            JsonNode json = HttpHelper.parse(resp.body());

            String actualPriority = json.path("priority").asText();
            String status = json.path("status").asText();
            int code = resp.statusCode();

            boolean passed = code == 200
                    && status.equals("OPEN")
                    && actualPriority.equals(expectedPriority);

            tr.expected("HTTP 200, status=OPEN, priority=" + expectedPriority)
              .actual("HTTP " + code + ", status='" + status + "', priority='" + actualPriority + "'")
              .observed("id=" + json.path("id").asText() +
                        ", title='" + json.path("title").asText() + "'" +
                        ", category=" + json.path("category").asText() +
                        ", priority=" + actualPriority +
                        ", status=" + status)
              .pass(passed);
            ResultRecorder.record(SHEET, tr);
            Assert.assertTrue(passed,
                    testId + " — expected priority " + expectedPriority + ", got " + actualPriority);
        } catch (Throwable t) {
            tr.actual("Exception").error(t.getClass().getSimpleName() + ": " + t.getMessage()).errored();
            ResultRecorder.record(SHEET, tr);
            throw new RuntimeException(t);
        }
    }

    @Test(description = "GET /tickets returns the caller's tickets as a JSON array")
    public void listMyTickets() throws Exception {
        TestResult tr = TestResult.of("R_LIST", "GET /tickets returns array for current user");
        try {
            HttpResponse<String> resp = HttpHelper.get(apiBase + "/tickets", employeeToken);
            JsonNode arr = HttpHelper.parse(resp.body());
            boolean passed = resp.statusCode() == 200 && arr.isArray();
            tr.input("GET " + apiBase + "/tickets")
              .expected("HTTP 200, response is JSON array")
              .actual("HTTP " + resp.statusCode() + ", isArray=" + arr.isArray() + ", size=" + arr.size())
              .observed("First 3 IDs: " + firstFew(arr, 3))
              .pass(passed);
            ResultRecorder.record(SHEET, tr);
            Assert.assertTrue(passed);
        } catch (Throwable t) {
            tr.actual("Exception").error(t.getClass().getSimpleName() + ": " + t.getMessage()).errored();
            ResultRecorder.record(SHEET, tr);
            throw new RuntimeException(t);
        }
    }

    private static String firstFew(JsonNode arr, int n) {
        if (!arr.isArray() || arr.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < Math.min(n, arr.size()); i++) {
            if (i > 0) sb.append(", ");
            sb.append(arr.get(i).path("id").asText());
        }
        return sb.append(arr.size() > n ? ", ...]" : "]").toString();
    }

    private static void putIfPresent(Map<String, Object> map, String key, String value) {
        if (value != null && !value.isBlank()) {
            map.put(key, value);
        }
    }
}
