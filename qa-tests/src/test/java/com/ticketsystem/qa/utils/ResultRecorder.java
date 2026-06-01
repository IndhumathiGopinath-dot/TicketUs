package com.ticketsystem.qa.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Collects TestResult rows across all test categories and writes a single
 * styled Excel workbook at suite end. One sheet per category, plus a
 * Summary sheet up front.
 *
 *   target/test-results/Test_Results_&lt;yyyyMMdd_HHmmss&gt;.xlsx
 */
public class ResultRecorder {

    private static final Map<String, List<TestResult>> RESULTS = new ConcurrentHashMap<>();
    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static void record(String sheet, TestResult r) {
        RESULTS.computeIfAbsent(sheet, k -> Collections.synchronizedList(new ArrayList<>())).add(r);
        System.out.printf("  [%-5s] %-18s %-8s %s%n",
                r.getStatus(), sheet, r.getTestId(), r.getDescription());
    }

    public static void clear() { RESULTS.clear(); }

    public static String flushToExcel() {
        String filename = "Test_Results_" + LocalDateTime.now().format(FILE_TS) + ".xlsx";
        String dir = "target/test-results";
        try { Files.createDirectories(Paths.get(dir)); }
        catch (IOException e) { throw new RuntimeException(e); }
        String path = dir + "/" + filename;
        flushToExcel(path);
        return path;
    }

    public static void flushToExcel(String fullPath) {
        try (Workbook wb = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(fullPath)) {
            Styles s = buildStyles(wb);
            writeSummary(wb, s);
            RESULTS.keySet().stream().sorted().forEach(sheet ->
                    writeSheet(wb, sheet, RESULTS.get(sheet), s));
            wb.write(out);
            System.out.println();
            System.out.println("=== Excel result report: " + fullPath + " ===");
            printConsoleSummary();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write result Excel", e);
        }
    }

    // ---- writing ----

    private static final String[] HEADERS = {
            "Test ID", "Description", "Input", "Expected", "Actual",
            "Status", "Observed Output", "Error", "Timestamp"
    };

    private static void writeSheet(Workbook wb, String sheetName, List<TestResult> rows, Styles s) {
        Sheet sheet = wb.createSheet(safe(sheetName));
        sheet.createFreezePane(0, 1);

        Row header = sheet.createRow(0);
        for (int i = 0; i < HEADERS.length; i++) {
            Cell c = header.createCell(i);
            c.setCellValue(HEADERS[i]);
            c.setCellStyle(s.header);
        }

        for (int i = 0; i < rows.size(); i++) {
            TestResult r = rows.get(i);
            Row row = sheet.createRow(i + 1);
            put(row, 0, r.getTestId(),      s.cell);
            put(row, 1, r.getDescription(), s.cell);
            put(row, 2, r.getInput(),       s.cellWrap);
            put(row, 3, r.getExpected(),    s.cellWrap);
            put(row, 4, r.getActual(),      s.cellWrap);
            put(row, 5, r.getStatus(),      statusStyle(r.getStatus(), s));
            put(row, 6, r.getObserved(),    s.cellWrap);
            put(row, 7, r.getError(),       s.cellWrap);
            put(row, 8, r.getTimestamp(),   s.cell);
        }

        int[] widths = {3000, 8000, 7000, 8000, 8000, 2500, 14000, 8000, 4500};
        for (int i = 0; i < widths.length; i++) sheet.setColumnWidth(i, widths[i]);
    }

    private static void writeSummary(Workbook wb, Styles s) {
        Sheet sheet = wb.createSheet("Summary");

        Row title = sheet.createRow(0);
        Cell tc = title.createCell(0);
        tc.setCellValue("Ticket System QA Suite — Run Summary");
        tc.setCellStyle(s.title);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

        Row meta = sheet.createRow(1);
        meta.createCell(0).setCellValue("Generated:");
        meta.createCell(1).setCellValue(LocalDateTime.now().toString());

        Row header = sheet.createRow(3);
        String[] cols = {"Category", "Total", "Passed", "Failed", "Errored"};
        for (int i = 0; i < cols.length; i++) {
            Cell c = header.createCell(i);
            c.setCellValue(cols[i]);
            c.setCellStyle(s.header);
        }

        int row = 4, gT = 0, gP = 0, gF = 0, gE = 0;
        for (String key : RESULTS.keySet().stream().sorted().toList()) {
            List<TestResult> rs = RESULTS.get(key);
            int total = rs.size();
            int pass = (int) rs.stream().filter(r -> "PASS".equals(r.getStatus())).count();
            int fail = (int) rs.stream().filter(r -> "FAIL".equals(r.getStatus())).count();
            int err  = (int) rs.stream().filter(r -> "ERROR".equals(r.getStatus())).count();
            gT += total; gP += pass; gF += fail; gE += err;

            Row r = sheet.createRow(row++);
            put(r, 0, key,                  s.cell);
            put(r, 1, String.valueOf(total), s.cell);
            put(r, 2, String.valueOf(pass),  s.pass);
            put(r, 3, String.valueOf(fail),  fail > 0 ? s.fail : s.cell);
            put(r, 4, String.valueOf(err),   err  > 0 ? s.fail : s.cell);
        }
        Row totalRow = sheet.createRow(row);
        put(totalRow, 0, "TOTAL",            s.header);
        put(totalRow, 1, String.valueOf(gT), s.header);
        put(totalRow, 2, String.valueOf(gP), s.header);
        put(totalRow, 3, String.valueOf(gF), s.header);
        put(totalRow, 4, String.valueOf(gE), s.header);

        int[] widths = {7000, 3000, 3000, 3000, 3000};
        for (int i = 0; i < widths.length; i++) sheet.setColumnWidth(i, widths[i]);
    }

    private static void printConsoleSummary() {
        int t = 0, p = 0, f = 0, e = 0;
        for (List<TestResult> rs : RESULTS.values()) {
            t += rs.size();
            p += (int) rs.stream().filter(r -> "PASS".equals(r.getStatus())).count();
            f += (int) rs.stream().filter(r -> "FAIL".equals(r.getStatus())).count();
            e += (int) rs.stream().filter(r -> "ERROR".equals(r.getStatus())).count();
        }
        System.out.printf("Total: %d   PASS: %d   FAIL: %d   ERROR: %d%n", t, p, f, e);
    }

    // ---- styles ----

    private static class Styles {
        CellStyle header, title, cell, cellWrap, pass, fail;
    }

    private static Styles buildStyles(Workbook wb) {
        Styles st = new Styles();

        Font bold = wb.createFont(); bold.setBold(true); bold.setColor(IndexedColors.WHITE.getIndex());
        st.header = wb.createCellStyle();
        st.header.setFont(bold);
        st.header.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        st.header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        st.header.setAlignment(HorizontalAlignment.CENTER);
        st.header.setVerticalAlignment(VerticalAlignment.CENTER);
        borders(st.header);

        Font tf = wb.createFont(); tf.setBold(true); tf.setFontHeightInPoints((short)14);
        st.title = wb.createCellStyle(); st.title.setFont(tf);
        st.title.setAlignment(HorizontalAlignment.CENTER);

        st.cell = wb.createCellStyle(); st.cell.setVerticalAlignment(VerticalAlignment.TOP); borders(st.cell);

        st.cellWrap = wb.createCellStyle();
        st.cellWrap.setWrapText(true); st.cellWrap.setVerticalAlignment(VerticalAlignment.TOP);
        borders(st.cellWrap);

        Font wf = wb.createFont(); wf.setBold(true); wf.setColor(IndexedColors.WHITE.getIndex());

        st.pass = wb.createCellStyle(); st.pass.setFont(wf);
        st.pass.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
        st.pass.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        st.pass.setAlignment(HorizontalAlignment.CENTER);
        borders(st.pass);

        st.fail = wb.createCellStyle(); st.fail.setFont(wf);
        st.fail.setFillForegroundColor(IndexedColors.RED.getIndex());
        st.fail.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        st.fail.setAlignment(HorizontalAlignment.CENTER);
        borders(st.fail);

        return st;
    }

    private static void borders(CellStyle cs) {
        cs.setBorderTop(BorderStyle.THIN); cs.setBorderBottom(BorderStyle.THIN);
        cs.setBorderLeft(BorderStyle.THIN); cs.setBorderRight(BorderStyle.THIN);
    }

    private static CellStyle statusStyle(String s, Styles st) {
        if ("PASS".equals(s)) return st.pass;
        if ("FAIL".equals(s) || "ERROR".equals(s)) return st.fail;
        return st.cell;
    }

    private static void put(Row row, int col, String value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value == null ? "" : value);
        if (style != null) c.setCellStyle(style);
    }

    private static String safe(String s) {
        String cleaned = s == null ? "Sheet" : s.replaceAll("[\\\\/?*\\[\\]:]", "_");
        return cleaned.length() > 31 ? cleaned.substring(0, 31) : cleaned;
    }
}
