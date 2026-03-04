# Excel Import Tool (Java)

A minimal Java tool to import Excel (`.xlsx` / `.xls`) user data.

## Expected columns

First sheet, first row is header. Required columns (English or Chinese):

- `name` or `姓名`
- `age` or `年龄`
- `email` or `邮箱`

## Build

```bash
mvn clean package
```

Output jar:

- `target/excel-import-tool-1.0.0-jar-with-dependencies.jar`

## Run

```bash
java -jar target/excel-import-tool-1.0.0-jar-with-dependencies.jar your-file.xlsx
```

## Validation rules

- `name` cannot be blank
- `age` must be integer in range 0-150
- `email` must contain `@`
