package com.example.excel.server;

import com.example.excel.UserRecord;
import com.example.excel.service.UserDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelServerApp {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final UserDataService SERVICE = new UserDataService();

    public static void main(String[] args) throws IOException {
        int port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/health", exchange -> {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendJson(exchange, 405, Map.of("success", false, "message", "Method Not Allowed"));
                return;
            }
            sendJson(exchange, 200, Map.of("success", true, "message", "ok"));
        });

        server.createContext("/import", exchange -> {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendJson(exchange, 405, Map.of("success", false, "message", "Method Not Allowed"));
                return;
            }
            try {
                Map<String, String> query = parseQuery(exchange.getRequestURI());
                String path = query.get("path");
                if (path == null || path.isBlank()) {
                    sendJson(exchange, 400, Map.of("success", false, "message", "Missing query param: path"));
                    return;
                }

                int count = SERVICE.importFrom(Path.of(path));
                sendJson(exchange, 200, Map.of("success", true, "message", "Import success", "count", count));
            } catch (Exception e) {
                sendJson(exchange, 400, Map.of("success", false, "message", e.getMessage()));
            }
        });

        server.createContext("/users", exchange -> {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendJson(exchange, 405, Map.of("success", false, "message", "Method Not Allowed"));
                return;
            }
            try {
                Map<String, String> query = parseQuery(exchange.getRequestURI());
                String keyword = query.getOrDefault("query", "");
                String field = query.getOrDefault("field", "all");

                List<UserRecord> result = SERVICE.query(keyword, field);
                sendJson(exchange, 200, Map.of("success", true, "count", result.size(), "data", result));
            } catch (Exception e) {
                sendJson(exchange, 400, Map.of("success", false, "message", e.getMessage()));
            }
        });

        server.createContext("/export", exchange -> {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendJson(exchange, 405, Map.of("success", false, "message", "Method Not Allowed"));
                return;
            }
            try {
                Map<String, String> query = parseQuery(exchange.getRequestURI());
                String path = query.get("path");
                if (path == null || path.isBlank()) {
                    sendJson(exchange, 400, Map.of("success", false, "message", "Missing query param: path"));
                    return;
                }

                int count = SERVICE.exportTo(Path.of(path));
                sendJson(exchange, 200, Map.of("success", true, "message", "Export success", "count", count));
            } catch (Exception e) {
                sendJson(exchange, 400, Map.of("success", false, "message", e.getMessage()));
            }
        });

        server.start();
        System.out.println("Server started on http://localhost:" + port);
        System.out.println("APIs: GET /health, POST /import?path=..., GET /users?query=...&field=all|name|email, POST /export?path=...");
    }

    private static Map<String, String> parseQuery(URI uri) {
        Map<String, String> result = new HashMap<>();
        String rawQuery = uri.getRawQuery();
        if (rawQuery == null || rawQuery.isBlank()) {
            return result;
        }

        for (String pair : rawQuery.split("&")) {
            if (pair.isBlank()) {
                continue;
            }
            String[] parts = pair.split("=", 2);
            String key = decode(parts[0]);
            String value = parts.length > 1 ? decode(parts[1]) : "";
            result.put(key, value);
        }
        return result;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static void sendJson(HttpExchange exchange, int statusCode, Object body) throws IOException {
        byte[] bytes = MAPPER.writeValueAsBytes(body);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }
}
