package com.example.excel;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class App {

    public static void main(String[] args) {
        if (args.length < 1) {
            printUsage();
            return;
        }

        Path inputPath = Path.of(args[0]);
        Path outputPath = null;
        String query = null;
        String queryField = "all";

        int index = 1;
        if (index < args.length && !args[index].startsWith("--")) {
            outputPath = Path.of(args[index]);
            index++;
        }

        while (index < args.length) {
            String arg = args[index];
            if ("--query".equals(arg)) {
                if (index + 1 >= args.length) {
                    System.err.println("Missing value for --query");
                    printUsage();
                    System.exit(1);
                }
                query = args[index + 1].trim();
                if (query.isEmpty()) {
                    System.err.println("--query value cannot be blank");
                    System.exit(1);
                }
                index += 2;
            } else if ("--field".equals(arg)) {
                if (index + 1 >= args.length) {
                    System.err.println("Missing value for --field");
                    printUsage();
                    System.exit(1);
                }
                queryField = args[index + 1].trim().toLowerCase(Locale.ROOT);
                if (!"all".equals(queryField) && !"name".equals(queryField) && !"email".equals(queryField)) {
                    System.err.println("--field only supports: all/name/email");
                    System.exit(1);
                }
                index += 2;
            } else {
                System.err.println("Unknown argument: " + arg);
                printUsage();
                System.exit(1);
            }
        }

        ExcelImporter importer = new ExcelImporter();
        ExcelExporter exporter = new ExcelExporter();

        try {
            List<UserRecord> users = importer.importUsers(inputPath);
            System.out.println("Import success. Total rows: " + users.size());

            List<UserRecord> displayUsers = users;
            if (query != null) {
                displayUsers = filterUsers(users, query, queryField);
                System.out.println("Query success. Matched rows: " + displayUsers.size());
            }

            for (UserRecord user : displayUsers) {
                System.out.printf("name=%s, age=%d, email=%s%n", user.name(), user.age(), user.email());
            }

            if (outputPath != null) {
                exporter.exportUsers(outputPath, users);
                System.out.println("Export success. Output file: " + outputPath.toAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Process failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private static List<UserRecord> filterUsers(List<UserRecord> users, String query, String field) {
        String keyword = query.toLowerCase(Locale.ROOT);
        List<UserRecord> result = new ArrayList<>();

        for (UserRecord user : users) {
            String name = user.name().toLowerCase(Locale.ROOT);
            String email = user.email().toLowerCase(Locale.ROOT);

            boolean matched;
            if ("name".equals(field)) {
                matched = name.contains(keyword);
            } else if ("email".equals(field)) {
                matched = email.contains(keyword);
            } else {
                matched = name.contains(keyword) || email.contains(keyword);
            }

            if (matched) {
                result.add(user);
            }
        }

        return result;
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  java -jar excel-import-tool-1.0.0-jar-with-dependencies.jar <input-excel-path>");
        System.out.println("  java -jar excel-import-tool-1.0.0-jar-with-dependencies.jar <input-excel-path> <output-excel-path>");
        System.out.println("  java -jar excel-import-tool-1.0.0-jar-with-dependencies.jar <input-excel-path> [output-excel-path] --query <keyword> [--field all|name|email>");
    }
}

