package com.example.excel;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ExcelExporter {

    public void exportUsers(Path excelPath, List<UserRecord> users) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("users");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("name");
            header.createCell(1).setCellValue("age");
            header.createCell(2).setCellValue("email");

            for (int i = 0; i < users.size(); i++) {
                UserRecord user = users.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(user.name());
                row.createCell(1).setCellValue(user.age());
                row.createCell(2).setCellValue(user.email());
            }

            for (int i = 0; i < 3; i++) {
                sheet.autoSizeColumn(i);
            }

            try (OutputStream outputStream = Files.newOutputStream(excelPath)) {
                workbook.write(outputStream);
            }
        }
    }
}
