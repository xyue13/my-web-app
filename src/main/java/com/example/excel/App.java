package com.example.excel;

import java.nio.file.Path;
import java.util.List;

public class App {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java -jar excel-import-tool-1.0.0-jar-with-dependencies.jar <excel-file-path>");
            return;
        }

        Path filePath = Path.of(args[0]);
        ExcelImporter importer = new ExcelImporter();

        try {
            List<UserRecord> users = importer.importUsers(filePath);
            System.out.println("Import success. Total rows: " + users.size());
            for (UserRecord user : users) {
                System.out.printf("name=%s, age=%d, email=%s%n", user.name(), user.age(), user.email());
            }
        } catch (Exception e) {
            System.err.println("Import failed: " + e.getMessage());
            System.exit(1);
        }
    }
}
