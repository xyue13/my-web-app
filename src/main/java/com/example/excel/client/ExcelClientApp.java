package com.example.excel.client;

import com.example.excel.UserRecord;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ExcelClientApp extends JFrame {

    private final JTextField serverUrlField = new JTextField("http://localhost:8080", 22);
    private final JTextField importPathField = new JTextField(28);
    private final JTextField queryField = new JTextField(16);
    private final JComboBox<String> fieldBox = new JComboBox<>(new String[]{"all", "name", "email"});
    private final JTextField exportPathField = new JTextField(28);
    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"name", "age", "email"}, 0);
    private final JLabel statusLabel = new JLabel("Ready");

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ExcelClientApp app = new ExcelClientApp();
            app.setVisible(true);
        });
    }

    public ExcelClientApp() {
        setTitle("Excel Import Tool - Client");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        add(buildTopPanel(), BorderLayout.NORTH);
        add(new JScrollPane(new JTable(tableModel)), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        JPanel serverPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        serverPanel.add(new JLabel("Server:"));
        serverPanel.add(serverUrlField);
        JButton refreshButton = new JButton("刷新");
        refreshButton.addActionListener(e -> loadUsers());
        serverPanel.add(refreshButton);

        JPanel importPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        importPanel.add(new JLabel("Excel路径:"));
        importPanel.add(importPathField);
        JButton importButton = new JButton("导入");
        importButton.addActionListener(e -> importUsers());
        importPanel.add(importButton);

        JPanel queryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        queryPanel.add(new JLabel("关键词:"));
        queryPanel.add(queryField);
        queryPanel.add(new JLabel("字段:"));
        queryPanel.add(fieldBox);
        JButton queryButton = new JButton("查询");
        queryButton.addActionListener(e -> loadUsers());
        queryPanel.add(queryButton);

        JPanel rows = new JPanel(new BorderLayout());
        rows.add(serverPanel, BorderLayout.NORTH);
        rows.add(importPanel, BorderLayout.CENTER);
        rows.add(queryPanel, BorderLayout.SOUTH);

        panel.add(rows, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        exportPanel.add(new JLabel("导出路径:"));
        exportPanel.add(exportPathField);
        JButton exportButton = new JButton("导出");
        exportButton.addActionListener(e -> exportUsers());
        exportPanel.add(exportButton);

        panel.add(exportPanel, BorderLayout.NORTH);
        panel.add(statusLabel, BorderLayout.SOUTH);
        return panel;
    }

    private void importUsers() {
        String path = importPathField.getText().trim();
        if (path.isEmpty()) {
            showError("请输入导入文件路径");
            return;
        }
        runInBackground("正在导入...", () -> {
            String url = serverUrl() + "/import?path=" + encode(path);
            String body = send("POST", url);
            JsonNode root = objectMapper.readTree(body);
            ensureSuccess(root);
            return "导入完成，数量: " + root.path("count").asInt();
        }, msg -> {
            statusLabel.setText(msg);
            loadUsers();
        });
    }

    private void loadUsers() {
        String keyword = queryField.getText().trim();
        String field = String.valueOf(fieldBox.getSelectedItem());

        runInBackground("正在查询...", () -> {
            String url = serverUrl() + "/users?query=" + encode(keyword) + "&field=" + encode(field);
            String body = send("GET", url);
            JsonNode root = objectMapper.readTree(body);
            ensureSuccess(root);

            List<UserRecord> users = new ArrayList<>();
            for (JsonNode item : root.path("data")) {
                users.add(new UserRecord(
                        item.path("name").asText(""),
                        item.path("age").asInt(0),
                        item.path("email").asText("")
                ));
            }
            return users;
        }, users -> {
            tableModel.setRowCount(0);
            for (UserRecord user : users) {
                tableModel.addRow(new Object[]{user.name(), user.age(), user.email()});
            }
            statusLabel.setText("查询完成，数量: " + users.size());
        });
    }

    private void exportUsers() {
        String path = exportPathField.getText().trim();
        if (path.isEmpty()) {
            showError("请输入导出文件路径");
            return;
        }
        runInBackground("正在导出...", () -> {
            String url = serverUrl() + "/export?path=" + encode(path);
            String body = send("POST", url);
            JsonNode root = objectMapper.readTree(body);
            ensureSuccess(root);
            return "导出完成，数量: " + root.path("count").asInt();
        }, msg -> statusLabel.setText(msg));
    }

    private String send(String method, String url) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url));
        if ("POST".equals(method)) {
            builder.POST(HttpRequest.BodyPublishers.noBody());
        } else {
            builder.GET();
        }
        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("HTTP " + response.statusCode() + ": " + response.body());
        }
        return response.body();
    }

    private void ensureSuccess(JsonNode root) {
        if (!root.path("success").asBoolean(false)) {
            throw new IllegalStateException(root.path("message").asText("Unknown error"));
        }
    }

    private String serverUrl() {
        return serverUrlField.getText().trim().replaceAll("/+$", "");
    }

    private String encode(String raw) {
        return URLEncoder.encode(raw, StandardCharsets.UTF_8);
    }

    private <T> void runInBackground(String workingText, WorkerSupplier<T> action, WorkerConsumer<T> onSuccess) {
        statusLabel.setText(workingText);
        SwingWorker<T, Void> worker = new SwingWorker<>() {
            @Override
            protected T doInBackground() throws Exception {
                return action.get();
            }

            @Override
            protected void done() {
                try {
                    T result = get();
                    onSuccess.accept(result);
                } catch (Exception e) {
                    String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    showError(message == null ? "请求失败" : message);
                    statusLabel.setText("失败");
                }
            }
        };
        worker.execute();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "错误", JOptionPane.ERROR_MESSAGE);
    }

    @FunctionalInterface
    private interface WorkerSupplier<T> {
        T get() throws Exception;
    }

    @FunctionalInterface
    private interface WorkerConsumer<T> {
        void accept(T value);
    }
}
