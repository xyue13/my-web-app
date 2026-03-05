# Excel Import Tool (Java)

支持两种模式：

- CLI（命令行导入/导出/查询）
- C/S（Server + Client 操作界面）

## Excel 列要求

第一张 Sheet，第一行为表头，必须包含：

- `name` 或 `姓名`
- `age` 或 `年龄`
- `email` 或 `邮箱`

## 构建

```bash
mvn clean package
```

产物：

- `target/excel-import-tool-1.0.0-jar-with-dependencies.jar`

## C/S 架构运行方式（推荐）

1) 启动服务端（默认 8080）

```bash
java -cp target/excel-import-tool-1.0.0-jar-with-dependencies.jar com.example.excel.server.ExcelServerApp
```

指定端口：

```bash
java -cp target/excel-import-tool-1.0.0-jar-with-dependencies.jar com.example.excel.server.ExcelServerApp 9090
```

2) 启动客户端 GUI

```bash
java -cp target/excel-import-tool-1.0.0-jar-with-dependencies.jar com.example.excel.client.ExcelClientApp
```

客户端功能：

- 输入 Excel 路径后点击“导入”
- 按关键词 + 字段（all/name/email）点击“查询”
- 输入导出路径点击“导出”

## Server API

- `GET /health`
- `POST /import?path=绝对路径`
- `GET /users?query=关键词&field=all|name|email`
- `POST /export?path=绝对路径`

## CLI 运行方式

```bash
java -jar target/excel-import-tool-1.0.0-jar-with-dependencies.jar input.xlsx
java -jar target/excel-import-tool-1.0.0-jar-with-dependencies.jar input.xlsx output.xlsx
java -jar target/excel-import-tool-1.0.0-jar-with-dependencies.jar input.xlsx --query alice
java -jar target/excel-import-tool-1.0.0-jar-with-dependencies.jar input.xlsx --query example.com --field email
```

## 校验规则

- `name` 不能为空
- `age` 必须是 `0-150` 的整数
- `email` 必须包含 `@`
