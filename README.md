# Atomic Payments

Atomic Payments is a reactive backend service that models **account wallets, payments, and double-entry ledger transactions** with strong consistency guarantees.

The project is designed as a **fintech-style payment engine**, focusing on:
- Atomic transfers
- Idempotent payment processing
- Concurrency safety
- Auditability

---

## Tech Stack

- Java 21
- Spring Boot 3 (WebFlux)
- Project Reactor
- Spring Data R2DBC
- H2 / PostgreSQL
- Flyway
- Maven

---

## Architecture

The application follows a **layered, domain-centric architecture**:


The **domain layer is framework-agnostic** and contains the core business rules.

---

## Core Concepts

- **Accounts (Wallets)**  
  Hold balances and transaction history.

- **Payments**  
  Atomic transfers between accounts with idempotency guarantees.

- **Ledger**  
  Double-entry bookkeeping ensuring financial correctness.

---

## Features (Planned)

- Create and query accounts
- Perform atomic payments
- Double-entry ledger entries
- Idempotent payment API
- Reactive persistence with R2DBC
- Database migrations with Flyway
- Domain events and audit logging

---

## Running the Application

```
docker compose up -d
./mvnw spring-boot:run
```

