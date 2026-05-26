package com.ticketsystem;

import com.ticketsystem.dto.TicketDtos;
import com.ticketsystem.model.Ticket;
import com.ticketsystem.model.User;
import com.ticketsystem.model.enums.Category;
import com.ticketsystem.model.enums.Priority;
import com.ticketsystem.model.enums.Role;
import com.ticketsystem.model.enums.Status;
import com.ticketsystem.repository.UserRepository;
import com.ticketsystem.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

@SpringBootTest
public class TicketServiceTest extends AbstractTestNGSpringContextTests {

    @Autowired private TicketService ticketService;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private User employee;
    private User admin;

    @BeforeClass
    public void setup() {
        long ts = System.currentTimeMillis();
        admin = userRepository.save(User.builder()
                .name("Admin IT")
                .email("admin_it_" + ts + "@test.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .department("IT")
                .build());
        employee = userRepository.save(User.builder()
                .name("Emp Test")
                .email("emp_" + ts + "@test.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.EMPLOYEE)
                .department("Engineering")
                .build());
    }

    @Test
    public void testCreateTicketAutoPriorityAndRouting() {
        TicketDtos.CreateTicketRequest req = TicketDtos.CreateTicketRequest.builder()
                .title("Email server outage in production")
                .description("Cannot access mail")
                .category(Category.IT)
                .build();
        Ticket t = ticketService.createTicket(req, employee);
        Assert.assertNotNull(t.getId());
        Assert.assertEquals(t.getPriority(), Priority.URGENT);
        Assert.assertEquals(t.getStatus(), Status.OPEN);
        Assert.assertNotNull(t.getAssignedTo());
        Assert.assertEquals(t.getAssignedTo().getDepartment(), "IT");
    }

    @Test
    public void testUpdateStatusByAdmin() {
        TicketDtos.CreateTicketRequest req = TicketDtos.CreateTicketRequest.builder()
                .title("Password reset")
                .description("Need access")
                .category(Category.IT)
                .build();
        Ticket t = ticketService.createTicket(req, employee);
        Ticket updated = ticketService.updateStatus(t.getId(), Status.IN_PROGRESS, "Working on it", admin);
        Assert.assertEquals(updated.getStatus(), Status.IN_PROGRESS);

        Ticket resolved = ticketService.updateStatus(t.getId(), Status.RESOLVED, "Done", admin);
        Assert.assertEquals(resolved.getStatus(), Status.RESOLVED);
        Assert.assertNotNull(resolved.getResolvedAt());
    }

    @Test
    public void testRateTicket() {
        TicketDtos.CreateTicketRequest req = TicketDtos.CreateTicketRequest.builder()
                .title("Something to rate")
                .description("desc")
                .category(Category.IT)
                .build();
        Ticket t = ticketService.createTicket(req, employee);
        ticketService.updateStatus(t.getId(), Status.RESOLVED, "ok", admin);
        Ticket rated = ticketService.rateTicket(t.getId(), 1, employee);
        Assert.assertEquals(rated.getSatisfactionRating(), Integer.valueOf(1));
    }

    @Test
    public void testSimilarTickets() {
        TicketDtos.CreateTicketRequest req = TicketDtos.CreateTicketRequest.builder()
                .title("Printer offline issue")
                .description("printer not responding")
                .category(Category.IT)
                .build();
        ticketService.createTicket(req, employee);
        List<Ticket> similar = ticketService.findSimilarTickets("printer", Category.IT);
        Assert.assertFalse(similar.isEmpty());
    }

    @Test
    public void testGetTicketsForEmployee() {
        long before = ticketService.getTicketsForUser(employee).size();
        ticketService.createTicket(TicketDtos.CreateTicketRequest.builder()
                .title("Listing test")
                .description("Test desc")
                .category(Category.HR)
                .build(), employee);
        long after = ticketService.getTicketsForUser(employee).size();
        Assert.assertEquals(after, before + 1);
    }
}
