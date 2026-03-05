package com.example.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelImporter {

    private final DataFormatter formatter = new DataFormatter();

    public List<UserRecord> importUsers(Path excelPath) throws IOException {
        try (InputStream inputStream = Files.newInputStream(excelPath);
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() < 1) {
                throw new IllegalArgumentException("Excel is empty.");
            }

            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            Map<String, Integer> headerMap = parseHeader(headerRow);

            int nameCol = requireColumn(headerMap, "name", "姓名");
            int ageCol = requireColumn(headerMap, "age", "年龄");
            int emailCol = requireColumn(headerMap, "email", "邮箱");

            List<UserRecord> result = new ArrayList<>();
            int firstDataRow = sheet.getFirstRowNum() + 1;
            int lastDataRow = sheet.getLastRowNum();

            for (int rowNum = firstDataRow; rowNum <= lastDataRow; rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null || isRowBlank(row)) {
                    continue;
                }

                String name = readCell(row.getCell(nameCol));
                String ageText = readCell(row.getCell(ageCol));
                String email = readCell(row.getCell(emailCol));

                if (name.isBlank()) {
                    throw new IllegalArgumentException("Row " + (rowNum + 1) + " name is blank.");
                }
                int age = parseAge(ageText, rowNum);
                if (!email.contains("@")) {
                    throw new IllegalArgumentException("Row " + (rowNum + 1) + " email format is invalid.");
                }

                result.add(new UserRecord(name, age, email));
            }
            return result;
        }
    }

    private Map<String, Integer> parseHeader(Row headerRow) {
        Map<String, Integer> headerMap = new HashMap<>();
        if (headerRow == null) {
            return headerMap;
        }
        for (Cell cell : headerRow) {
            String key = readCell(cell).trim().toLowerCase();
            if (!key.isEmpty()) {
                headerMap.put(key, cell.getColumnIndex());
            }
        }
        return headerMap;
    }

    private int requireColumn(Map<String, Integer> headerMap, String... aliases) {
        for (String alias : aliases) {
            Integer index = headerMap.get(alias.toLowerCase());
            if (index != null) {
                return index;
            }
        }
        throw new IllegalArgumentException("Missing required column: " + String.join("/", aliases));
    }

    private String readCell(Cell cell) {
        if (cell == null) {
            return "";
        }
        return formatter.formatCellValue(cell).trim();
    }

    private int parseAge(String ageText, int rowNum) {
        if (ageText.isBlank()) {
            throw new IllegalArgumentException("Row " + (rowNum + 1) + " age is blank.");
        }
        try {
            int age = Integer.parseInt(ageText);
            if (age < 0 || age > 150) {
                throw new IllegalArgumentException("Row " + (rowNum + 1) + " age out of range.");
            }
            return age;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Row " + (rowNum + 1) + " age is not a valid integer.");
        }
    }

    private boolean isRowBlank(Row row) {
        short first = row.getFirstCellNum();
        short last = row.getLastCellNum();
        if (first < 0 || last < 0) {
            return true;
        }
        for (int i = first; i < last; i++) {
            String value = readCell(row.getCell(i));
            if (!value.isBlank()) {
                return false;
            }
        }
        return true;
    }
}

