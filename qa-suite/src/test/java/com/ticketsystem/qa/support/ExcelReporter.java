package com.ticketsystem.qa.support;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes a timestamped Excel report alongside the ExtentReports HTML.
 * One row per test method, PASS rows tinted green, FAIL rows tinted red.
 */
public final class ExcelReporter {

    public record Row(String testId, String testName, String category,
                      String groups, String status, long durationMs, String message) {}

    private static final List<Row> ROWS = new ArrayList<>();
    private static String filePath;

    private ExcelReporter() {}

    public static synchronized void record(Row row) {
        ROWS.add(row);
    }

    public static synchronized void flush() {
        if (ROWS.isEmpty()) return;

        String timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File dir = new File("target/test-results");
        if (!dir.exists()) dir.mkdirs();
        filePath = dir.getAbsolutePath() + "/QASuite_Results_" + timestamp + ".xlsx";

        try (Workbook wb = new XSSFWorkbook();
             FileOutputStream out = new FileOutputStream(filePath)) {

            Sheet sheet = wb.createSheet("Results");

            CellStyle header = wb.createCellStyle();
            Font hf = wb.createFont(); hf.setBold(true); hf.setColor(IndexedColors.WHITE.getIndex());
            header.setFont(hf);
            header.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            header.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle pass = wb.createCellStyle();
            pass.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            pass.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle fail = wb.createCellStyle();
            fail.setFillForegroundColor(IndexedColors.ROSE.getIndex());
            fail.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            String[] headers = {"Test ID","Test Name","Category","Groups","Status","Duration (ms)","Message"};
            Row0(sheet, 0, headers, header);
            for (int i = 0; i < ROWS.size(); i++) {
                Row r = ROWS.get(i);
                CellStyle s = "PASS".equals(r.status()) ? pass : fail;
                Row0(sheet, i + 1, new String[]{
                    r.testId(), r.testName(), r.category(), r.groups(),
                    r.status(), String.valueOf(r.durationMs()), r.message()
                }, s);
            }
            for (int c = 0; c < headers.length; c++) sheet.autoSizeColumn(c);
            wb.write(out);
            System.out.println("==> Excel results: " + filePath);
        } catch (Exception e) {
            System.err.println("Excel report failed: " + e.getMessage());
        }
    }

    private static void Row0(Sheet sheet, int rowIdx, String[] vals, CellStyle style) {
        org.apache.poi.ss.usermodel.Row r = sheet.createRow(rowIdx);
        for (int c = 0; c < vals.length; c++) {
            Cell cell = r.createCell(c);
            cell.setCellValue(vals[c] == null ? "" : vals[c]);
            cell.setCellStyle(style);
        }
    }

    public static String filePath() { return filePath; }
}
