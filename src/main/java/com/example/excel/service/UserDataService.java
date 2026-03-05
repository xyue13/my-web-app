package com.example.excel.service;

import com.example.excel.ExcelExporter;
import com.example.excel.ExcelImporter;
import com.example.excel.UserRecord;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserDataService {

    private final ExcelImporter importer = new ExcelImporter();
    private final ExcelExporter exporter = new ExcelExporter();
    private final List<UserRecord> users = new ArrayList<>();

    public synchronized int importFrom(Path inputPath) throws IOException {
        List<UserRecord> imported = importer.importUsers(inputPath);
        users.clear();
        users.addAll(imported);
        return users.size();
    }

    public synchronized int exportTo(Path outputPath) throws IOException {
        exporter.exportUsers(outputPath, users);
        return users.size();
    }

    public synchronized List<UserRecord> query(String keyword, String field) {
        String normalizedField = field == null ? "all" : field.toLowerCase(Locale.ROOT);
        String normalizedKeyword = keyword == null ? "" : keyword.toLowerCase(Locale.ROOT).trim();

        if (normalizedKeyword.isEmpty()) {
            return new ArrayList<>(users);
        }

        List<UserRecord> result = new ArrayList<>();
        for (UserRecord user : users) {
            String name = user.name().toLowerCase(Locale.ROOT);
            String email = user.email().toLowerCase(Locale.ROOT);

            boolean matched;
            if ("name".equals(normalizedField)) {
                matched = name.contains(normalizedKeyword);
            } else if ("email".equals(normalizedField)) {
                matched = email.contains(normalizedKeyword);
            } else {
                matched = name.contains(normalizedKeyword) || email.contains(normalizedKeyword);
            }

            if (matched) {
                result.add(user);
            }
        }
        return result;
    }
}
