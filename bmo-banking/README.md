# BMO Banking Microservices

A production-grade banking backend built with **Java 8**, **Spring Boot 2.7**, and **Spring Cloud**, modelled after real-world financial systems at BMO (Bank of Montreal).

---

## Architecture

```
Client (Web / Mobile / Postman)
              |
              ▼
        API Gateway  (:8080)   — JWT auth filter, route to services
              |
  ┌───────────┼───────────┐
  ▼           ▼           ▼
Account    Transaction  Payment
Service    Service      Service
(:8081)    (:8082)      (:8083)
  |           |           |
  └─────── Feign Clients ─┘
              |
        MySQL (RDS)    SQS Queue
                           |
                    Notification Service (:8084)
                           |
                     Email / SMS

────── INFRASTRUCTURE ──────
Eureka Server  (:8761)   — service registry
Config Server  (:8888)   — centralised config
Resilience4j             — circuit breaker
Jenkins                  — CI/CD pipeline
Docker Compose           — local dev stack
AWS EC2 / RDS / S3 / SQS / CloudWatch
```

---

## Microservices

| Service              | Port | Responsibility                              |
|----------------------|------|---------------------------------------------|
| eureka-server        | 8761 | Service discovery                           |
| config-server        | 8888 | Centralised configuration (native)          |
| api-gateway          | 8080 | JWT validation, routing                     |
| account-service      | 8081 | User registration/login, accounts, balances |
| transaction-service  | 8082 | Internal fund transfers                     |
| payment-service      | 8083 | UPI payments, bill payments                 |
| notification-service | 8084 | Email/SMS via SQS consumer                  |

---

## Tech Stack

- **Java 8** / **Spring Boot 2.7.14**
- **Spring Cloud 2021.0.8** — Eureka, Config, Gateway, OpenFeign
- **Resilience4j** — circuit breaker on inter-service calls
- **Spring Security + JWT (jjwt 0.9.1)**
- **Spring Data JPA + MySQL 8**
- **Lombok + ModelMapper**
- **AWS SDK v2** — S3 (statements), SQS (async notifications)
- **JUnit 5 + Mockito** — unit tests
- **Docker + Docker Compose**
- **Jenkins** — CI/CD pipeline

---

## Prerequisites

| Tool         | Version  |
|--------------|----------|
| Java         | 8        |
| Maven        | 3.6+     |
| MySQL        | 8.0      |
| Docker       | 20+      |
| Docker Compose | 1.29+  |

---

## Quick Start — Local (without Docker)

### 1. Start MySQL and create databases

```bash
mysql -u root -p < scripts/init-db.sql
```

### 2. Start services in order

```bash
# 1. Eureka
cd eureka-server && mvn spring-boot:run &

# 2. Config Server
cd config-server && mvn spring-boot:run &

# 3. API Gateway
cd api-gateway && mvn spring-boot:run &

# 4. Business Services (any order)
cd account-service     && mvn spring-boot:run &
cd transaction-service && mvn spring-boot:run &
cd payment-service     && mvn spring-boot:run &
cd notification-service && mvn spring-boot:run &
```

### 3. Verify

- Eureka dashboard → http://localhost:8761  (admin / admin123)
- API Gateway      → http://localhost:8080/actuator/health

---

## Quick Start — Docker Compose

```bash
# Build all services
mvn clean package -DskipTests

# Start everything
docker-compose up --build -d

# Tail logs
docker-compose logs -f
```

---

## API Usage

### Base URL
```
http://localhost:8080
```

### 1. Register a user
```http
POST /api/auth/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName":  "Doe",
  "email":     "john.doe@bmo.com",
  "password":  "password123",
  "phone":     "+1-416-555-0100"
}
```

### 2. Login → get JWT token
```http
POST /api/auth/login
Content-Type: application/json

{
  "email":    "john.doe@bmo.com",
  "password": "password123"
}
```
Response: `{ "data": { "token": "eyJ..." } }`

Use this token as `Authorization: Bearer <token>` on all subsequent requests.

### 3. Create account
```http
POST /api/accounts
Authorization: Bearer <token>

{
  "userId":         1,
  "accountType":    "CHEQUING",
  "currency":       "CAD",
  "initialDeposit": 5000.00
}
```

### 4. Check balance
```http
GET /api/accounts/{accountNumber}/balance
Authorization: Bearer <token>
```

### 5. Internal transfer
```http
POST /api/transactions/transfer
Authorization: Bearer <token>

{
  "fromAccountNumber": "BMO1234567890",
  "toAccountNumber":   "BMO0987654321",
  "amount":            250.00,
  "description":       "Rent payment"
}
```

### 6. UPI payment
```http
POST /api/payments/upi
Authorization: Bearer <token>

{
  "fromAccountNumber": "BMO1234567890",
  "upiId":             "jane.doe@td",
  "amount":            100.00,
  "description":       "Split dinner"
}
```

### 7. Bill payment
```http
POST /api/payments/bill
Authorization: Bearer <token>

{
  "fromAccountNumber": "BMO1234567890",
  "billerName":        "Rogers Telecom",
  "billNumber":        "INV-2023-10-001",
  "amount":            89.99,
  "description":       "Monthly phone bill"
}
```

Import **postman/BMO-Banking-API.postman_collection.json** into Postman for a ready-to-use collection with all endpoints.

---

## Running Tests

```bash
# All services
mvn test

# Single service
cd account-service && mvn test
```

---

## Project Structure

```
bmo-banking/
├── pom.xml                          ← parent POM
├── docker-compose.yml
├── Jenkinsfile
├── .gitignore
├── scripts/
│   └── init-db.sql
├── postman/
│   └── BMO-Banking-API.postman_collection.json
├── eureka-server/
├── config-server/
│   └── src/main/resources/configs/  ← per-service YML configs
├── api-gateway/
├── account-service/                 ← User, Account, Auth, JWT
├── transaction-service/             ← Internal transfers, Feign, CB
├── payment-service/                 ← UPI, Bill pay, SQS publish
└── notification-service/            ← SQS consumer, Email/SMS
```

---

## AWS Deployment Notes

1. Launch EC2 instances (t3.medium recommended per service)
2. Provision RDS MySQL 8.0 — update datasource URLs in config YML
3. Create SQS queue named `bmo-notification-queue`
4. Create S3 bucket `bmo-statements` for account statements
5. Attach IAM role to EC2 with SQS + S3 permissions
6. Configure Jenkins with EC2 SSH credentials and AWS account ID
7. Push to `main` branch → Jenkins pipeline auto-deploys

---

## Environment Variables (Production)

| Variable         | Description                  |
|------------------|------------------------------|
| `MAIL_USERNAME`  | SMTP sender email            |
| `MAIL_PASSWORD`  | SMTP password                |
| `AWS_REGION`     | AWS region (default us-east-1)|
| `EC2_HOST`       | Jenkins deploy target IP     |
| `AWS_ACCOUNT_ID` | For ECR registry URL         |

---

## License

Internal use — BMO Banking Project © 2023
