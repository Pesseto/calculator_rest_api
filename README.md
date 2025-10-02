# Calculator REST API

Distributed calculator in a **two-module** architecture:
- **REST module** (HTTP client): exposes endpoints, generates a **Request-Id** and sends requests through Kafka (request–reply).
- **Calculator module** (worker): consumes requests from Kafka, performs the math using `BigDecimal`, and replies.

Includes **MDC**, **structured logging** (Logback), and **persistent logs on the host**.

---

## Features

- Math operations: sum, subtraction, multiplication, division  
- Arbitrary precision: `BigDecimal` (division scale 10, `HALF_UP`)  
- Kafka request–reply between modules  
- Docker & Docker Compose ready  
- Unit tests for endpoints and calculator logic  
- SLF4J/Logback with MDC (`requestId`) and file appenders

---

## Tech

- Java 21
- Spring Boot 3.5.6
- Spring for Apache Kafka (request–reply)
- Maven (multi-module)
- Docker & Docker Compose v2
- JUnit 5
- SLF4J/Logback

---

## How to Run

### Prerequisites
- Docker Engine and **docker compose v2** (`docker compose version`)
- Java 21 + Maven

### 1) Build jars
```bash
mvn clean package
```

### 2) Up with Docker Compose
```bash
docker compose up --build -d
```
This will start:
- `kafka` (KRaft, with healthcheck)
- `calculator` (worker)
- `client` (REST API at `http://localhost:8080`)

> Logs are persisted on the host:  
> • `./rest/logs/*.log`  
> • `./calculator/logs/*.log`

### 3) Try it in a new terminal
```bash
curl -i -w "\n" 'http://localhost:8080/sum?a=2.5&b=3.7'
curl -i -w "\n" 'http://localhost:8080/sub?a=5&b=3'
curl -i -w "\n" 'http://localhost:8080/mul?a=4&b=5'
curl -i -w "\n" 'http://localhost:8080/div?a=1&b=3'
```

Example success response:
```http
HTTP/1.1 200 OK
Request-Id: 7f2b5e1a-3d1f-4d7c-9b55-1a2b3c4d5e6f
Content-Type: application/json

{"result": 6.2}
```

Division by zero:
```http
HTTP/1.1 500 Internal Server Error
Request-Id: 0f0a9c8b-...
Content-Type: application/json

{"error": "division by zero attempted"}
```

---

## API Endpoints

Base URL: `http://localhost:8080`

| Method | Path                     | Params | Description                 |
|------:|---------------------------|--------|-----------------------------|
| GET   | `/sum`                    | a, b   | Addition                    |
| GET   | `/sub` `/subtraction`     | a, b   | Subtraction                 |
| GET   | `/mul` `/multiplication`  | a, b   | Multiplication              |
| GET   | `/div` `/division`        | a, b   | Division (scale 10, HALF_UP)|

---

## Tests

```bash
# all tests
mvn test

# only REST module
mvn -pl rest test

# only calculator module
mvn -pl calculator test
```

---

## Logs

- Logback is configured in both modules (`logback-spring.xml`) with MDC (`requestId`)
  and rolling file appenders.
- With Docker Compose, logs persist on the host:
  - `./rest/logs/*.log`
  - `./calculator/logs/*.log`

---

## Shut down & cleanup

```bash
docker compose down -v
```

---