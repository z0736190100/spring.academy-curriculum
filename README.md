# Cardcashian

A small Spring Boot REST API for managing "Cash Cards". The project demonstrates:
- Spring Boot 3 (Java 17)
- Spring Web (REST)
- Spring Security (HTTP Basic, role-based access)
- Spring Data JDBC with an in-memory H2 database
- Spring Cloud **services discovery practices** via a Eureka client
- uses a **TDD with JUnit 5** approach to develop the app
- Paging, sorting, and typical CRUD endpoints

## Requirements
- Java 17+
- Gradle (wrapper included)

## Getting started
1. Build the project:
   - Linux/macOS: `./gradlew clean build`
   - Windows: `gradlew.bat clean build`
2. Run the app:
   - Linux/macOS: `./gradlew bootRun`
   - Windows: `gradlew.bat bootRun`
3. The service starts on http://localhost:8081

## Authentication
HTTP Basic is enabled and required for all `/cashcards/**` endpoints. Predefined in-memory users:
- Username: `sarah1`  Password: `abc123`  Roles: `ROLE_CARD-OWNER`
- Username: `kumar2`  Password: `xyz789`  Roles: `ROLE_CARD-OWNER`
- Username: `hank-owns-no-cards`  Password: `qrs456`  Roles: `ROLE_NON-OWNER`

Only users with role `CARD-OWNER` are authorized to access `/cashcards/**`.
Additionally, a user can only access resources they own (enforced at repository/controller level).

## Data storage
- Uses Spring Data JDBC with H2 in-memory DB.
- Schema is initialized from `src/main/resources/schema.sql`.
- Integration tests load sample data from `src/test/resources/data.sql`.

## API

*see `/openapi/openapi.yaml` for full API spec*

## Configuration
- Server port: `8081` (see `src/main/resources/application.yaml`)
- Eureka client disabled by default in this project setup

## Running tests
```
./gradlew test
```
Tests include:
- JSON serialization/deserialization tests for CashCard
- Integration tests for all endpoints including auth, paging, sorting, create, update, delete

