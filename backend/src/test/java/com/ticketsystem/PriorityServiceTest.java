package com.ticketsystem;

import com.ticketsystem.model.enums.Category;
import com.ticketsystem.model.enums.Priority;
import com.ticketsystem.model.enums.Severity;
import com.ticketsystem.service.PriorityService;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class PriorityServiceTest {

    private PriorityService service;

    @BeforeClass
    public void setUp() {
        service = new PriorityService();
    }

    @Test
    public void testUrgentKeywordDetection() {
        Priority p = service.computePriority("Email server outage",
                "Production is down", Category.IT, null);
        Assert.assertEquals(p, Priority.URGENT);
    }

    @Test
    public void testHighKeywordDetection() {
        Priority p = service.computePriority("App throws error on save",
                "Failed to update record", Category.BUG, null);
        Assert.assertEquals(p, Priority.HIGH);
    }

    @Test
    public void testLowKeywordDetection() {
        Priority p = service.computePriority("Password reset",
                "Question about login", Category.IT, null);
        Assert.assertEquals(p, Priority.LOW);
    }

    @Test
public void testNormalDefault() {
    Priority p = service.computePriority("Office supplies",
            "Need new pens for my desk", Category.HR, null);
    Assert.assertEquals(p, Priority.NORMAL);
}

    @Test
    public void testBugSeverityCriticalOverridesKeywords() {
        Priority p = service.computePriority("Minor issue",
                "Just a question", Category.BUG, Severity.CRITICAL);
        Assert.assertEquals(p, Priority.URGENT);
    }

    @Test
    public void testEstimatedResolutionHours() {
        Assert.assertEquals(service.estimateResolutionHours(Priority.URGENT, Category.IT), Integer.valueOf(4));
        Assert.assertEquals(service.estimateResolutionHours(Priority.LOW, Category.HR), Integer.valueOf(72));
    }
}
