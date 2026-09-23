# Weather Metrics Service

## Prerequisites

- Java 25
- Maven
- Docker

## Run Locally

Start PostgreSQL:
```bash
docker compose up -d postgres
```

Start the application with the codegen profile to generate jOOQ sources from the database schema:
```bash
mvn spring-boot:run -Pcodegen
```
The application will be available at:

```bash
http://localhost:8080
```

## Docker Compose

Start PostgreSQL:
```bash
docker compose up -d postgres
```
Build the application:
```bash
mvn clean package -Pcodegen
```
Then start the application with Docker Compose:

```bash
docker compose up --build
```
## Flyway

Database schema migrations are managed by Flyway and applied automatically when the application starts.

Migration files are located in:
```bash
src/main/resources/db/migration
```

## Swagger

Swagger UI:
```bash
http://localhost:8080/swagger-ui.html
```
OpenAPI JSON:
```bash
http://localhost:8080/v3/api-docs
```
## Tests

Run the test suite with:
```bash
mvn test
```
Integration tests use Testcontainers with PostgreSQL. Docker must be running when executing the integration tests.
