package com.ticketsystem.e2e.support;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Collects step-level results across all journeys and writes a single
 * timestamped Excel report at the end of the suite. Each row records:
 *   Journey ID • Step • Status • Duration (ms) • Notes
 *
 * Green PASS rows / red FAIL rows make pass/fail obvious at a glance.
 */
public final class JourneyReporter {

    private static final List<Row> ROWS = new ArrayList<>();

    private JourneyReporter() {}

    public static void record(String journeyId, String step, String status,
                              long durationMs, String notes) {
        ROWS.add(new Row(journeyId, step, status, durationMs, notes));
    }

    public static String flush() {
        String dir = ConfigReader.get("report.directory");
        new File(dir).mkdirs();
        String stamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filePath = dir + "/E2E_Results_" + stamp + ".xlsx";

        try (Workbook wb = new XSSFWorkbook();
             FileOutputStream out = new FileOutputStream(filePath)) {

            Sheet sheet = wb.createSheet("E2E Results");
            CellStyle header = headerStyle(wb);
            CellStyle pass = colourRow(wb, IndexedColors.LIGHT_GREEN);
            CellStyle fail = colourRow(wb, IndexedColors.ROSE);
            CellStyle plain = wb.createCellStyle();

            String[] columns = {"Journey", "Step", "Status", "Duration (ms)", "Notes"};
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(columns[i]);
                c.setCellStyle(header);
            }

            for (int i = 0; i < ROWS.size(); i++) {
                Row r = ROWS.get(i);
                org.apache.poi.ss.usermodel.Row excelRow = sheet.createRow(i + 1);
                CellStyle style = "PASS".equals(r.status) ? pass
                                : "FAIL".equals(r.status) ? fail
                                : plain;
                Cell c0 = excelRow.createCell(0); c0.setCellValue(r.journey);  c0.setCellStyle(style);
                Cell c1 = excelRow.createCell(1); c1.setCellValue(r.step);     c1.setCellStyle(style);
                Cell c2 = excelRow.createCell(2); c2.setCellValue(r.status);   c2.setCellStyle(style);
                Cell c3 = excelRow.createCell(3); c3.setCellValue(r.duration); c3.setCellStyle(style);
                Cell c4 = excelRow.createCell(4); c4.setCellValue(r.notes);    c4.setCellStyle(style);
            }

            for (int i = 0; i < columns.length; i++) sheet.autoSizeColumn(i);

            wb.write(out);
            return filePath;
        } catch (IOException e) {
            throw new RuntimeException("Failed to write E2E report", e);
        }
    }

    private static CellStyle headerStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont(); f.setBold(true); f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return s;
    }

    private static CellStyle colourRow(Workbook wb, IndexedColors c) {
        CellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(c.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return s;
    }

    private record Row(String journey, String step, String status,
                       long duration, String notes) {}
}
