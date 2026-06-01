package com.ticketsystem.qa.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Reads .xlsx test data. Row 0 is the header; subsequent non-empty rows
 * become Map&lt;String,String&gt; keyed by column header. Returned as the
 * shape TestNG's @DataProvider expects: Object[][].
 */
public class ExcelReader {

    public static Object[][] readSheet(String resourcePath, String sheetName) {
        try (InputStream is = ExcelReader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) throw new RuntimeException("Excel not found on classpath: " + resourcePath);
            try (Workbook wb = new XSSFWorkbook(is)) {
                Sheet sheet = wb.getSheet(sheetName);
                if (sheet == null) throw new RuntimeException("Sheet not found: " + sheetName);

                Row headerRow = sheet.getRow(0);
                if (headerRow == null) return new Object[0][0];
                List<String> headers = new ArrayList<>();
                for (Cell c : headerRow) headers.add(cellString(c));

                List<Object[]> rows = new ArrayList<>();
                for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null || isEmpty(row)) continue;
                    Map<String, String> m = new HashMap<>();
                    for (int c = 0; c < headers.size(); c++) {
                        Cell cell = row.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                        m.put(headers.get(c), cellString(cell));
                    }
                    rows.add(new Object[]{m});
                }
                return rows.toArray(new Object[0][0]);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read " + resourcePath, e);
        }
    }

    private static boolean isEmpty(Row r) {
        for (int c = 0; c < r.getLastCellNum(); c++) {
            Cell cell = r.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null && !cellString(cell).isEmpty()) return false;
        }
        return true;
    }

    private static String cellString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? cell.getDateCellValue().toString()
                    : trimNumeric(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            case BLANK, _NONE, ERROR -> "";
        };
    }

    private static String trimNumeric(double d) {
        if (d == Math.floor(d) && !Double.isInfinite(d)) return String.valueOf((long) d);
        return String.valueOf(d);
    }
}
