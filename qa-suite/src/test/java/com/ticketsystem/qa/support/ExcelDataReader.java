package com.ticketsystem.qa.support;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads .xlsx test-data files using Apache POI. Returns each row as a
 * Map<columnHeader, cellValue> so tests can refer to fields by their
 * spreadsheet column name rather than by column index.
 *
 * Companion to ExcelReporter — this class READS test data, ExcelReporter
 * WRITES test results.
 */
public final class ExcelDataReader {

    private ExcelDataReader() {}

    /**
     * Reads the named sheet from a .xlsx resource on the classpath.
     * Row 0 is treated as the header row.
     *
     * @param resourceName e.g. "login-data.xlsx"
     * @param sheetName    name of the sheet inside the workbook
     * @return list of rows, each row is column-name → cell-value
     */
    public static List<Map<String, String>> readSheet(String resourceName, String sheetName) {
        List<Map<String, String>> result = new ArrayList<>();

        try (InputStream in = ExcelDataReader.class.getClassLoader()
                .getResourceAsStream(resourceName);
             Workbook wb = new XSSFWorkbook(in)) {

            if (in == null) {
                throw new RuntimeException("Excel resource not found on classpath: " + resourceName);
            }

            Sheet sheet = wb.getSheet(sheetName);
            if (sheet == null) {
                throw new RuntimeException("Sheet '" + sheetName + "' not found in " + resourceName);
            }

            // Header row — column names
            Row header = sheet.getRow(0);
            List<String> headers = new ArrayList<>();
            for (Cell c : header) headers.add(cellAsString(c));

            // Data rows
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                Map<String, String> rec = new LinkedHashMap<>();
                for (int c = 0; c < headers.size(); c++) {
                    Cell cell = row.getCell(c);
                    rec.put(headers.get(c), cellAsString(cell));
                }
                // Skip fully-blank rows
                if (rec.values().stream().anyMatch(v -> !v.isBlank())) {
                    result.add(rec);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read Excel: " + e.getMessage(), e);
        }

        return result;
    }

    /** Convert a POI Cell to a String, regardless of underlying type. */
    private static String cellAsString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double d = cell.getNumericCellValue();
                // Render integers without decimal point
                yield (d == Math.floor(d)) ? String.valueOf((long) d) : String.valueOf(d);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }
}
