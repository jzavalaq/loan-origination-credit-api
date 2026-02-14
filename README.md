# Loan Origination Credit API

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue?logo=docker)](Dockerfile)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue?logo=postgresql)](https://www.postgresql.org/)

A production-ready loan origination system with automated FICO-style credit scoring, application processing, and comprehensive audit logging for fintech applications.

## Features

- **Credit Scoring**: FICO-style scoring engine (300-850 range)
- **Application Workflow**: Status tracking (PENDING → APPROVED/REJECTED)
- **Risk Assessment**: Automated decision making based on credit factors
- **Audit Logging**: Immutable audit trail for compliance
- **Document Management**: Support for income/identity verification

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        Client Applications                                   │
│                        (Web Browser / Mobile)                                │
└─────────────────────────────────────────────────────────────────────────────┘
                                   │
                                   ▼
                              HTTPS
                                   │
┌─────────────────────────────────────────────────────────────────────────────┐
│                      Loan Origination Service                                │
│                           (Spring Boot:8080)                                 │
│  ┌────────────────┐  ┌────────────────┐  ┌────────────────┐  ┌────────────┐ │
│  │   Applicants   │  │  Applications  │  │ Credit Assess  │  │   Audits   │ │
│  └────────────────┘  └────────────────┘  └────────────────┘  └────────────┘ │
│  ┌─────────────────────────────────────────────────────────────────────────┐│
│  │                    Database (PostgreSQL/H2)                              ││
│  └─────────────────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────────────────┘
```

## Tech Stack

| Component | Technology | Version |
|-----------|------------|---------|
| Language | Java | 21 |
| Framework | Spring Boot | 3.2.5 |
| Build Tool | Maven | 3.9.x |
| Database (Dev) | H2 | In-memory |
| Database (Prod) | PostgreSQL | 15 |
| ORM | Spring Data JPA | 3.2.x |
| Security | Spring Security | 6.x |
| Migrations | Flyway | 9.22.x |

## Quick Start

```bash
# Clone and run
git clone https://github.com/jzavalaq/loan-origination-credit-api.git
cd loan-origination-credit-api

# Development mode (H2)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Docker Compose (PostgreSQL)
docker-compose up -d

# API: http://localhost:8080
# Swagger: http://localhost:8080/swagger-ui.html
```

## API Examples

### Authentication

```bash
# Register user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "loan_officer",
    "email": "officer@bank.com",
    "password": "Secure123!",
    "role": "LOAN_OFFICER"
  }'

# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"loan_officer","password":"Secure123!"}'
# Returns: {"token": "eyJhbG...", "type": "Bearer"}
```

### Applicant Management

```bash
TOKEN="your-jwt-token"

# Create applicant
curl -X POST http://localhost:8080/api/v1/applicants \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@email.com",
    "phone": "+1234567890",
    "ssn": "123-45-6789",
    "dateOfBirth": "1985-06-15",
    "annualIncome": 75000.00,
    "employmentStatus": "EMPLOYED",
    "employer": "Tech Corp",
    "yearsAtEmployer": 5
  }'

# Get applicant
curl -X GET http://localhost:8080/api/v1/applicants/1 \
  -H "Authorization: Bearer $TOKEN"
```

### Loan Application

```bash
# Submit loan application
curl -X POST http://localhost:8080/api/v1/applications \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "applicantId": 1,
    "loanType": "PERSONAL",
    "requestedAmount": 25000.00,
    "requestedTermMonths": 36,
    "purpose": "Home improvement"
  }'

# Get application status
curl -X GET http://localhost:8080/api/v1/applications/1 \
  -H "Authorization: Bearer $TOKEN"

# Get all applications (with filters)
curl -X GET "http://localhost:8080/api/v1/applications?status=PENDING&page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"
```

### Credit Assessment

```bash
# Trigger credit assessment
curl -X POST http://localhost:8080/api/v1/applications/1/assess \
  -H "Authorization: Bearer $TOKEN"

# Get credit assessment result
curl -X GET http://localhost:8080/api/v1/applications/1/assessment \
  -H "Authorization: Bearer $TOKEN"
# Response example:
# {
#   "creditScore": 720,
#   "riskLevel": "LOW",
#   "decision": "APPROVED",
#   "approvedAmount": 25000.00,
#   "approvedRate": 6.5,
#   "factors": [
#     {"factor": "PAYMENT_HISTORY", "score": 95, "weight": 0.35},
#     {"factor": "CREDIT_UTILIZATION", "score": 85, "weight": 0.30},
#     {"factor": "CREDIT_HISTORY_LENGTH", "score": 75, "weight": 0.15}
#   ]
# }
```

### Application Decision

```bash
# Approve application
curl -X POST http://localhost:8080/api/v1/applications/1/approve \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "approvedAmount": 25000.00,
    "approvedRate": 6.5,
    "approvedTermMonths": 36,
    "notes": "Strong credit history, stable income"
  }'

# Reject application
curl -X POST http://localhost:8080/api/v1/applications/1/reject \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "Insufficient credit history",
    "notes": "Recommend reapply after 6 months"
  }'
```

### Audit Trail

```bash
# Get audit logs for application
curl -X GET http://localhost:8080/api/v1/applications/1/audit \
  -H "Authorization: Bearer $TOKEN"
```

## Credit Scoring Factors

| Factor | Weight | Description |
|--------|--------|-------------|
| Payment History | 35% | On-time payments, delinquencies |
| Credit Utilization | 30% | Credit used vs. available |
| Credit History | 15% | Length of credit accounts |
| Credit Mix | 10% | Types of credit accounts |
| New Credit | 10% | Recent credit inquiries |

## Environment Variables

| Variable | Description |
|----------|-------------|
| `DB_URL` | PostgreSQL connection URL |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET` | JWT signing key (256+ bits) |

## License

MIT License - see [LICENSE](LICENSE)
