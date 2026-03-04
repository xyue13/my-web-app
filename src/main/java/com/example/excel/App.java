package com.example.excel;

import java.nio.file.Path;
import java.util.List;

public class App {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage:");
            System.out.println("  java -jar excel-import-tool-1.0.0-jar-with-dependencies.jar <input-excel-path>");
            System.out.println("  java -jar excel-import-tool-1.0.0-jar-with-dependencies.jar <input-excel-path> <output-excel-path>");
            return;
        }

        Path inputPath = Path.of(args[0]);
        ExcelImporter importer = new ExcelImporter();
        ExcelExporter exporter = new ExcelExporter();

        try {
            List<UserRecord> users = importer.importUsers(inputPath);
            System.out.println("Import success. Total rows: " + users.size());
            for (UserRecord user : users) {
                System.out.printf("name=%s, age=%d, email=%s%n", user.name(), user.age(), user.email());
            }

            if (args.length >= 2) {
                Path outputPath = Path.of(args[1]);
                exporter.exportUsers(outputPath, users);
                System.out.println("Export success. Output file: " + outputPath.toAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Process failed: " + e.getMessage());
            System.exit(1);
        }
    }
}
