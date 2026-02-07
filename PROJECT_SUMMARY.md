# PROJECT SUMMARY — Loan Origination Credit API

**Build Date:** 2026-03-20
**Status:** COMPLETE
**Version:** 1.0.0-SNAPSHOT

## Overview

A loan origination system with automated credit scoring, application processing, and comprehensive audit logging for financial compliance. Built with Spring Boot 3.2.5 and Java 21.

## Tech Stack

| Component | Technology | Version |
|-----------|------------|---------|
| Language | Java | 21 |
| Framework | Spring Boot | 3.2.5 |
| Build Tool | Maven | 3.9.x |
| Database (Dev) | H2 | In-memory |
| Database (Prod) | PostgreSQL | 15 |
| ORM | Spring Data JPA | 3.2.x |
| Security | Spring Security + JWT | - |
| Migrations | Flyway | 9.22.x |
| Container | Docker | - |

## Features Implemented

### 1. Applicant Management
- CRUD operations for loan applicants
- Validation with Jakarta Bean Validation
- Audit trail for all changes

### 2. Loan Application Management
- Submit loan applications with requested amount and term
- Status workflow: DRAFT → SUBMITTED → UNDER_REVIEW → DECIDED → DISBURSED
- Manual override capability with audit trail

### 3. Credit Scoring Engine
- FICO-style scoring (300-850 range)
- Weighted factors: income (30%), employment (20%), credit history (25%), debt (25%)
- Risk level classification: LOW, MEDIUM, HIGH

### 4. Decision Engine
- Auto-approve: score >= 700
- Manual review: score 600-699
- Auto-decline: score < 600
- Manual override with reason tracking

### 5. Audit Logging
- Comprehensive logging of all state changes
- Queryable by entity type, entity ID, action, actor, date range
- Correlation ID tracking for request tracing

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | /api/v1/applicants | Create applicant |
| GET | /api/v1/applicants/{id} | Get applicant |
| GET | /api/v1/applicants | List applicants (paginated) |
| PUT | /api/v1/applicants/{id} | Update applicant |
| DELETE | /api/v1/applicants/{id} | Delete applicant |
| POST | /api/v1/applications | Create loan application |
| GET | /api/v1/applications/{id} | Get application |
| GET | /api/v1/applications | List applications (paginated) |
| POST | /api/v1/applications/{id}/submit | Submit application |
| POST | /api/v1/applications/{id}/assess | Assess and decide |
| POST | /api/v1/applications/{id}/override | Override decision |
| GET | /api/v1/audit-logs | Query audit logs |
| GET | /actuator/health | Health check |

## Test Results

```
Tests run: 21, Failures: 0, Errors: 0, Skipped: 0
```

- ApplicantIntegrationTest: 5 tests
- LoanApplicationIntegrationTest: 6 tests
- AuditLogIntegrationTest: 2 tests
- CreditScoringServiceTest: 7 tests
- ScaffoldTest: 1 test

## How to Run

### Development (H2)
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Docker
```bash
docker-compose up -d
```

### Test
```bash
mvn test
```

## Security Features

- CORS configuration with explicit allowed origins
- Security headers (X-Frame-Options, X-Content-Type-Options, HSTS)
- Request logging with correlation ID
- Actuator health details require authorization

## Known Issues

None identified.

## File Structure

```
loan-origination-credit-api/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── .env.example
├── src/main/java/com/openclaw/loanorigination/
│   ├── Application.java
│   ├── config/
│   ├── controller/
│   ├── dto/
│   ├── entity/
│   ├── exception/
│   ├── repository/
│   ├── security/
│   └── service/
├── src/main/resources/
│   ├── application.properties
│   ├── application-dev.yml
│   └── application-prod.yml
└── src/test/java/com/openclaw/loanorigination/
```
