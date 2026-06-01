package com.ticketsystem.soap;

import com.ticketsystem.model.enums.Category;
import com.ticketsystem.model.enums.Status;
import com.ticketsystem.repository.TicketRepository;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Single SOAP operation: {@code getTicketStats}.
 *
 * Returns aggregate ticket counts (total/open/resolved/closed), optionally
 * filtered by category. Read-only, no auth — kept simple as a learning artifact.
 *
 * Hand-built DOM handler instead of JAXB-generated classes, so the project
 * doesn't need an XSD-to-Java code generation step.
 */
@Endpoint
public class TicketStatsEndpoint {

    private static final String NAMESPACE = "http://ticketsystem.com/soap/tickets";
    private final TicketRepository ticketRepository;

    public TicketStatsEndpoint(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "getTicketStatsRequest")
    @ResponsePayload
    public Element handle(@RequestPayload Element request) throws Exception {
        String filter = extractCategory(request);
        Category cat = parseCategoryOrNull(filter);

        long total = (cat == null)
                ? ticketRepository.count()
                : ticketRepository.findByCategory(cat).size();
        long open     = countByStatusAndCategory(Status.OPEN,     cat);
        long resolved = countByStatusAndCategory(Status.RESOLVED, cat);
        long closed   = countByStatusAndCategory(Status.CLOSED,   cat);

        return buildResponse(total, open, resolved, closed, filter);
    }

    private long countByStatusAndCategory(Status s, Category cat) {
        if (cat == null) return ticketRepository.countByStatus(s);
        return ticketRepository.findByStatus(s).stream()
                .filter(t -> t.getCategory() == cat)
                .count();
    }

    private static String extractCategory(Element req) {
        NodeList nodes = req.getElementsByTagNameNS(NAMESPACE, "category");
        if (nodes.getLength() == 0) return null;
        String text = nodes.item(0).getTextContent();
        return (text == null || text.isBlank()) ? null : text.trim();
    }

    private static Category parseCategoryOrNull(String s) {
        if (s == null) return null;
        try { return Category.valueOf(s.toUpperCase()); }
        catch (IllegalArgumentException e) { return null; }
    }

    private static Element buildResponse(long total, long open, long resolved,
                                          long closed, String filter) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
        Element resp = doc.createElementNS(NAMESPACE, "ns:getTicketStatsResponse");
        doc.appendChild(resp);

        appendText(doc, resp, "totalTickets",    String.valueOf(total));
        appendText(doc, resp, "openTickets",     String.valueOf(open));
        appendText(doc, resp, "resolvedTickets", String.valueOf(resolved));
        appendText(doc, resp, "closedTickets",   String.valueOf(closed));
        if (filter != null) appendText(doc, resp, "categoryFilter", filter);
        appendText(doc, resp, "generatedAt",
                OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        return resp;
    }

    private static void appendText(Document doc, Element parent, String name, String value) {
        Element el = doc.createElementNS(NAMESPACE, "ns:" + name);
        el.setTextContent(value);
        parent.appendChild(el);
    }
}
