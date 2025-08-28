# Cardcashian

A small Spring Boot REST API for managing "Cash Cards". The project demonstrates:
- Spring Boot 3 (Java 17)
- Spring Web (REST)
- Spring Security (HTTP Basic, role-based access)
- Spring Data JDBC with an in-memory H2 database
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
Base path: `/cashcards`

Entity: CashCard
- id: Long
- amount: Double
- owner: String (derived from the authenticated principal for create/update; access restricted to the owner)

### Get by id
GET `/cashcards/{id}`
- 200 OK if the card exists and is owned by the caller
- 404 Not Found otherwise

Example:
```
curl -i -u sarah1:abc123 http://localhost:8081/cashcards/99
```

### List (paged and sortable)
GET `/cashcards?page={page}&size={size}&sort={field},{dir}`
- Returns a JSON array of CashCards owned by the caller
- Defaults for page/size depend on Spring Data Pageable
- `sort` supports fields like `amount` with `asc` or `desc`

Examples:
```
# First page with 1 item
curl -i -u sarah1:abc123 "http://localhost:8081/cashcards?page=0&size=1"

# First page with 1 item sorted by amount ascending
curl -i -u sarah1:abc123 "http://localhost:8081/cashcards?page=0&size=1&sort=amount,asc"
```

### Create
POST `/cashcards`
Request body (owner is set from the authenticated user; id is generated):
```
{
  "amount": 250.00
}
```
Responses:
- 201 Created with `Location` header pointing to the new resource

Example:
```
curl -i -u sarah1:abc123 \
  -H "Content-Type: application/json" \
  -d '{"amount":250.00}' \
  http://localhost:8081/cashcards
```

### Update
PUT `/cashcards/{id}`
Request body (only `amount` is used; owner is kept as the authenticated user):
```
{
  "amount": 19.99
}
```
Responses:
- 204 No Content if updated and owned by caller
- 404 Not Found if not found or not owned by caller

Example:
```
curl -i -u sarah1:abc123 \
  -X PUT \
  -H "Content-Type: application/json" \
  -d '{"amount":19.99}' \
  http://localhost:8081/cashcards/99
```

### Delete
DELETE `/cashcards/{id}`
Responses:
- 204 No Content if deleted and owned by caller
- 404 Not Found if not found or not owned by caller

Example:
```
curl -i -u sarah1:abc123 -X DELETE http://localhost:8081/cashcards/99
```

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

## Project structure (key files)
- `build.gradle` — Gradle build configuration (Java 17, Spring Boot 3.3)
- `src/main/java/com/z0736190100/cardcashian/CardcashianApplication.java` — Spring Boot main class
- `src/main/java/com/z0736190100/cardcashian/controller/CashCardController.java` — REST endpoints
- `src/main/java/com/z0736190100/cardcashian/repo/CashCardRepository.java` — Spring Data JDBC repository
- `src/main/java/com/z0736190100/cardcashian/config/SecurityConfig.java` — Basic auth and role restrictions
- `src/main/resources/application.yaml` — App configuration (port, Eureka client)
- `src/main/resources/schema.sql` — H2 DB schema
- `src/test/resources/data.sql` — Test data
- `src/test/java/.../CardcashianApplicationTests.java` — Integration tests

## Notes
- This app is intended for learning/testing. Credentials are for demo only.
- Owner is implicitly handled using the authenticated principal; do not set it in requests.
