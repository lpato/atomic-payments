# Atomic Payments

**Atomic Payments** is a reactive, transactionally safe payment processing service designed to demonstrate **real-world fintech backend concerns**: atomicity, idempotency, concurrency control, and event-driven architecture.

The project is intentionally small but **architecturally complete**, focusing on correctness over feature breadth.

![CI](https://github.com/lpato/atomic-payments/actions/workflows/ci.yml/badge.svg)
![Coverage](https://img.shields.io/badge/coverage-%3E=70%25-brightgreen)

## 📚 Table of Contents

- [Key Features](#-key-features)
- [Architecture Overview](#-architecture-overview)
- [Payment Flow (Happy Path)](#-payment-flow-happy-path)
- [Idempotency](#-idempotency)
- [Concurrency & Consistency](#-concurrency--consistency)
- [Ledger Model](#-ledger-model)
- [Events & Messaging](#-events--messaging)
- [API Example](#-api-example)
- [Testing Strategy](#-testing-strategy)
- [Tech Stack](#-tech-stack)
- [Scope & Intent](#-scope--intent)
- [Key Takeaways](#-key-takeaways)
- [Getting Started](#-getting-started)



## ✨ Key Features

- **Atomic payment execution**
  - Account balance updates, payment creation, and ledger entries are committed as a single unit
- **Double-entry ledger**
  - Every payment produces one debit and one credit entry
- **Optimistic locking & concurrency safety**
  - Prevents lost updates when concurrent payments hit the same account
- **Idempotent API**
  - Safe retries using an idempotency key (industry-standard behavior)
- **Reactive & non-blocking**
  - Built with Spring WebFlux and R2DBC
- **Post-commit event publishing**
  - Emits payment events only after a successful transaction commit


## 🧱 Architecture Overview

The project follows a **clean, layered architecture**:

```
api
 └─ HTTP controllers, DTOs, exception mapping

application
 └─ Use-case orchestration (idempotency, transactions, events)

domain
 └─ Core business model (Account, Payment, Ledger)
 └─ Business rules & invariants

infra
 ├─ persistence (R2DBC repositories, Flyway)
 └─ messaging (event listeners / publishers)
```

### Design principles

- Domain logic is **pure and immutable**
- Infrastructure concerns are **isolated**
- Transactions are handled **explicitly**
- Side effects (events) occur **after commit**
- APIs expose **business semantics, not internals**


## 💸 Payment Flow (Happy Path)

1. Client sends `POST /payments`
2. API layer maps request → `PaymentCommand`
3. **IdempotentPaymentService**
   - Replays response if idempotency key already exists
4. **PaymentService**
   - Loads accounts
   - Validates business rules
   - Creates payment
   - Updates balances
   - Writes ledger entries
5. Transaction commits atomically
6. `PaymentInitiatedEvent` is published
7. API responds `201 Created`


## 🔐 Idempotency

The API supports safe retries using an **Idempotency-Key** header.

### Behavior

| Scenario | Result |
|-------|-------|
| Same key + same request | Replayed response |
| Same key + different request | `409 Conflict` |
| No key | Normal execution |

### Storage

```sql
idempotency_records
- idempotency_key (PK)
- request_hash
- response_payload
- created_at
```
## ⚖️ Concurrency & Consistency

- Account updates use optimistic locking

- Concurrent payments against the same account:

- One succeeds

- One fails with a domain-level ConcurrentAccountUpdateException

- No partial writes are possible

This behavior is explicitly tested.

## 📒 Ledger Model

Atomic Payments uses a double-entry ledger:

- Every payment produces:

  - One DEBIT entry (source account)
  - One CREDIT entry (destination account)

- Ledger entries are immutable and auditable

- Account balances are derived state, ledger is the source of truth

## 📡 Events & Messaging

After a successful transaction commit, the system publishes:
```
PaymentInitiatedEvent
```

Characteristics:

- Published only after commit
- Never published on rollback
- Decoupled from transaction logic
- Ready to be extended to Kafka / SNS / etc.

Current implementation uses Spring application events as a lightweight messaging mechanism.

## 🌐 API Example
### Create Payment
```
POST /payments
Idempotency-Key: abc-123
Content-Type: application/json

{
  "fromAccountId": "uuid",
  "toAccountId": "uuid",
  "amount": 20,
  "currency": "EUR",
  "reference": "invoice-42"
}
```

### Successful Response
```
{
  "paymentId": "uuid",
  "status": "PENDING",
  "amount": 20,
  "currency": "EUR",
  "createdAt": "2025-01-01T10:00:00Z"
}
```

## 🧪 Testing Strategy

The project includes:

- Domain & repository integration tests
- Transactional service tests
- Concurrency tests
- Idempotency tests
- WebFlux API tests

All critical invariants (atomicity, rollback, idempotency, concurrency) are explicitly tested.

## 🚀 Tech Stack

- Java 17
- Spring Boot (WebFlux)
- Spring Data R2DBC
- PostgreSQL
- Flyway
- Reactor
- JUnit 5

## 🎯 Scope & Intent

This project is intentionally not a full payment platform. It is designed to demonstrate:

- Correct transactional boundaries
- Real-world payment semantics
- Production-grade backend design decisions

## 🧠 Key Takeaways

- Transactions create facts — events announce them

- Idempotency is a protocol concern, not a domain concern

- Reactive systems require explicit transaction & error handling

- Correctness beats complexity

## 🚀 Getting Started

Prerequisites

- Java 17+
- Docker & Docker Compose
- Maven

```bash
# The project uses PostgreSQL. A Docker Compose file is provided.

docker compose up -d

```
- This will start a PostgreSQL instance and expose it on localhost:5432.
- Flyway migrations run automatically on application startup.
- No manual steps required.

```bash
./mvnw spring-boot:run
```
The application starts on http://localhost:8080. Test with curl:

```bash
curl -X POST http://localhost:8080/payments \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: demo-123" \
  -d '{
        "fromAccountId": "UUID",
        "toAccountId": "UUID",
        "amount": 20,
        "currency": "EUR",
        "reference": "demo-payment"
      }'
```
**Note:** Accounts must exist before creating a payment.
They can be inserted directly into the database for testing purposes.

```sql
INSERT INTO accounts (
    id,
    owner,
    balance_amount,
    balance_currency,
    status,
    version,
    created_at
) VALUES
(
    '11111111-1111-1111-1111-111111111111',
    'from',
    1000.00,
    'EUR',
    'ACTIVE',
    0,
    now()
),
(
    '22222222-2222-2222-2222-222222222222',
    'to',
    500.00,
    'EUR',
    'ACTIVE',
    0,
    now()
);
```
- The service is intentionally minimal and does not expose full account CRUD APIs

- Messaging is implemented using Spring application events and can be extended to Kafka or similar systems

- The focus of the project is correctness and transactional safety rather than feature completeness






---
