package com.ticketsystem.qa.soap;

import com.ticketsystem.qa.utils.ConfigReader;
import com.ticketsystem.qa.utils.ExcelReader;
import com.ticketsystem.qa.utils.ResultRecorder;
import com.ticketsystem.qa.utils.TestResult;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * Hits the SOAP endpoint at /api/ws and parses the response.
 *
 * The backend exposes a getTicketStats operation that returns aggregate
 * counts. Each row in soap_data.xlsx hits it with a different category
 * filter and verifies the contract (numeric fields present, filter echoed).
 */
public class SoapTicketStatsTest {

    private static final String SHEET = "SOAP-Stats";
    private final String soapUrl = ConfigReader.get("soap.endpoint.url");
    private final String wsdlUrl = ConfigReader.get("soap.wsdl.url");

    @DataProvider(name = "soapData")
    public Object[][] soapData() {
        return ExcelReader.readSheet("testdata/soap_data.xlsx", "SoapCases");
    }

    @Test(description = "SOAP-00 — WSDL is reachable and contains the operation")
    public void wsdlAvailable() {
        TestResult tr = TestResult.of("SO00", "WSDL exposes getTicketStats operation");
        try {
            String wsdl = SoapClient.fetchWsdl(wsdlUrl);
            boolean hasOp = wsdl.contains("getTicketStatsRequest")
                         && wsdl.contains("getTicketStatsResponse");
            tr.input("GET " + wsdlUrl)
              .expected("WSDL contains getTicketStatsRequest + getTicketStatsResponse elements")
              .actual("WSDL length=" + wsdl.length() + " bytes, operation present=" + hasOp)
              .observed(snippet(wsdl, 700))
              .pass(hasOp);
            ResultRecorder.record(SHEET, tr);
            Assert.assertTrue(hasOp, "WSDL should declare getTicketStats operation");
        } catch (Throwable t) {
            tr.actual("Exception").error(t.getClass().getSimpleName() + ": " + t.getMessage()).errored();
            ResultRecorder.record(SHEET, tr);
            throw new RuntimeException(t);
        }
    }

    @Test(dataProvider = "soapData",
          description = "SOAP getTicketStats — data-driven by category filter")
    public void soapStatsByCategory(Map<String, String> row) {
        String testId = row.get("testId");
        String desc   = row.get("description");
        String filter = row.get("categoryFilter");

        TestResult tr = TestResult.of(testId, desc)
                .input("SOAP getTicketStats" + (filter.isEmpty() ? " (no filter)" : " category=" + filter));

        try {
            Map<String, String> resp = SoapClient.getTicketStats(soapUrl, filter);

            long total    = Long.parseLong(resp.getOrDefault("totalTickets", "-1"));
            long open     = Long.parseLong(resp.getOrDefault("openTickets", "-1"));
            long resolved = Long.parseLong(resp.getOrDefault("resolvedTickets", "-1"));
            long closed   = Long.parseLong(resp.getOrDefault("closedTickets", "-1"));
            String echo   = resp.getOrDefault("categoryFilter", "");
            String when   = resp.getOrDefault("generatedAt", "");

            boolean countsLookSane = total >= 0 && open >= 0 && resolved >= 0 && closed >= 0
                    && (open + resolved + closed) <= total; // (in-progress + new statuses may live in total)
            boolean filterEcho = filter.isEmpty() ? echo.isEmpty() : echo.equalsIgnoreCase(filter);
            boolean hasTimestamp = !when.isBlank();

            boolean passed = countsLookSane && filterEcho && hasTimestamp;

            tr.expected("Counts ≥ 0, sum of statuses ≤ total" +
                        (filter.isEmpty() ? ", no categoryFilter in response" :
                                            ", categoryFilter='" + filter + "'") +
                        ", generatedAt present")
              .actual("total=" + total + " open=" + open + " resolved=" + resolved + " closed=" + closed +
                      ", categoryFilter='" + echo + "', generatedAt='" + when + "'")
              .observed(resp.toString())
              .pass(passed);

            ResultRecorder.record(SHEET, tr);
            Assert.assertTrue(passed, testId + " — SOAP response checks failed");
        } catch (Throwable t) {
            tr.actual("Exception").error(t.getClass().getSimpleName() + ": " + t.getMessage()).errored();
            ResultRecorder.record(SHEET, tr);
            throw new RuntimeException(t);
        }
    }

    private static String snippet(String s, int n) {
        if (s == null) return "";
        return s.length() > n ? s.substring(0, n) + "..." : s;
    }
}
