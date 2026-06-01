package com.ticketsystem.qa.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Generates the input .xlsx files at suite start. Files land at
 * target/test-classes/testdata/, on the runtime classpath.
 *
 * Each test class reads from its own sheet.
 */
public class TestDataGenerator {

    private static final String DIR = "target/test-classes/testdata";

    public static void generateAll() throws IOException {
        Path dir = Paths.get(DIR);
        Files.createDirectories(dir);
        loginData(dir.resolve("login_data.xlsx").toFile());
        ticketData(dir.resolve("ticket_data.xlsx").toFile());
        soapData(dir.resolve("soap_data.xlsx").toFile());
    }

    // 5 login scenarios — used by both REST and UI data-driven tests
    private static void loginData(File f) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet s = wb.createSheet("LoginCases");
            row(s, 0, "testId", "description", "email", "password", "expected");
            row(s, 1, "L01", "Valid IT admin",     "it.admin@company.com", "admin123",    "SUCCESS");
            row(s, 2, "L02", "Valid employee",     "john@company.com",     "password123", "SUCCESS");
            row(s, 3, "L03", "Wrong password",     "john@company.com",     "wrongpass",   "FAILURE");
            row(s, 4, "L04", "Unknown email",      "nobody@nowhere.com",   "anything",    "FAILURE");
            row(s, 5, "L05", "Empty credentials",  "",                     "",            "FAILURE");
            try (FileOutputStream o = new FileOutputStream(f)) { wb.write(o); }
        }
    }

    // 6 ticket creation scenarios across categories — REST + UI both use this
    private static void ticketData(File f) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet s = wb.createSheet("TicketCases");
            row(s, 0, "testId","description","category","title","ticketDescription",
                    "severity","appVersion","assetTag","requestType","confidential",
                    "expected","expectedPriority");
            row(s, 1, "T01","IT urgent via outage keyword","IT","Email server outage in production","Mail completely inaccessible","","","LAPTOP-001","","false","SUCCESS","URGENT");
            row(s, 2, "T02","IT low (password reset)","IT","password reset request","Forgot my password","","","","","false","SUCCESS","LOW");
            row(s, 3, "T03","Bug critical","BUG","Display glitch","Minor","CRITICAL","2.4.1","","","false","SUCCESS","URGENT");
            row(s, 4, "T04","Bug normal","BUG","Mobile layout broken","Form overflows","LOW","2.4.0","","","false","SUCCESS","LOW");
            row(s, 5, "T05","HR confidential payroll","HR","Payroll error","Wrong amount","","","","Payroll query","true","SUCCESS","URGENT");
            row(s, 6, "T06","HR normal leave request","HR","3 days off","Family event","","","","Leave request","false","SUCCESS","NORMAL");
            try (FileOutputStream o = new FileOutputStream(f)) { wb.write(o); }
        }
    }

    // 4 SOAP scenarios — different category filters
    private static void soapData(File f) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet s = wb.createSheet("SoapCases");
            row(s, 0, "testId", "description", "categoryFilter");
            row(s, 1, "SO01", "All categories (no filter)", "");
            row(s, 2, "SO02", "Filter by IT",               "IT");
            row(s, 3, "SO03", "Filter by BUG",              "BUG");
            row(s, 4, "SO04", "Filter by HR",               "HR");
            try (FileOutputStream o = new FileOutputStream(f)) { wb.write(o); }
        }
    }

    private static void row(Sheet s, int r, String... vals) {
        Row row = s.createRow(r);
        for (int i = 0; i < vals.length; i++) row.createCell(i).setCellValue(vals[i]);
    }
}
