# Excel Import Tool (Java)

A minimal Java tool to import Excel (`.xlsx` / `.xls`) user data.

## Expected columns

First sheet, first row is header. Required columns (English or Chinese):

- `name` or `–’√˚`
- `age` or `ƒÍ¡‰`
- `email` or `” œ‰`

## Build

```bash
mvn clean package
```

Output jar:

- `target/excel-import-tool-1.0.0-jar-with-dependencies.jar`

## Run

```bash
java -jar target/excel-import-tool-1.0.0-jar-with-dependencies.jar input.xlsx
```

Import and export in one command:

```bash
java -jar target/excel-import-tool-1.0.0-jar-with-dependencies.jar input.xlsx output.xlsx
```

Query users (default field is `all`, supports `name/email/all`):

```bash
java -jar target/excel-import-tool-1.0.0-jar-with-dependencies.jar input.xlsx --query alice
java -jar target/excel-import-tool-1.0.0-jar-with-dependencies.jar input.xlsx --query example.com --field email
java -jar target/excel-import-tool-1.0.0-jar-with-dependencies.jar input.xlsx output.xlsx --query alice --field name
```

## Validation rules

- `name` cannot be blank
- `age` must be integer in range 0-150
- `email` must contain `@`
