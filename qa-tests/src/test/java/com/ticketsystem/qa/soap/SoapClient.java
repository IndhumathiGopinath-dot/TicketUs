package com.ticketsystem.qa.soap;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

public class SoapClient {

    private static final String NS  = "http://ticketsystem.com/soap/tickets";
    private static final String NS_ENV = "http://schemas.xmlsoap.org/soap/envelope/";
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)).build();

    public static Map<String, String> getTicketStats(String soapUrl, String categoryFilter) throws Exception {
        String envelope = buildEnvelope(categoryFilter);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(soapUrl))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "text/xml; charset=utf-8")
                .header("SOAPAction", "")
                .POST(HttpRequest.BodyPublishers.ofString(envelope))
                .build();

        HttpResponse<String> resp = CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new RuntimeException("SOAP call failed: HTTP " + resp.statusCode() + "\n" + resp.body());
        }
        return parseResponse(resp.body());
    }

    public static String fetchWsdl(String wsdlUrl) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(wsdlUrl))
                .timeout(Duration.ofSeconds(10))
                .GET().build();
        return CLIENT.send(req, HttpResponse.BodyHandlers.ofString()).body();
    }

    private static String buildEnvelope(String categoryFilter) {
        StringBuilder body = new StringBuilder()
                .append("<env:Envelope xmlns:env=\"").append(NS_ENV).append("\" ")
                .append("xmlns:tns=\"").append(NS).append("\">\n")
                .append("  <env:Body>\n")
                .append("    <tns:getTicketStatsRequest>\n");
        if (categoryFilter != null && !categoryFilter.isBlank()) {
            body.append("      <tns:category>").append(categoryFilter).append("</tns:category>\n");
        }
        body.append("    </tns:getTicketStatsRequest>\n")
            .append("  </env:Body>\n")
            .append("</env:Envelope>");
        return body.toString();
    }

    private static Map<String, String> parseResponse(String xml) throws Exception {
        // CRITICAL: parser must be namespace-aware to use getElementsByTagNameNS().
        // Without this, prefixed elements like <ns:totalTickets> aren't matched
        // by namespace lookups, even when xmlns is declared correctly.
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder b = factory.newDocumentBuilder();
        Document doc = b.parse(new InputSource(new StringReader(xml)));

        Map<String, String> out = new LinkedHashMap<>();
        for (String field : new String[]{"totalTickets", "openTickets", "resolvedTickets",
                                         "closedTickets", "categoryFilter", "generatedAt"}) {
            NodeList nodes = doc.getElementsByTagNameNS(NS, field);
            if (nodes.getLength() > 0) {
                out.put(field, nodes.item(0).getTextContent());
            }
        }
        return out;
    }
}