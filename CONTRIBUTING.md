# Contributing to Atomic Payments

Thank you for your interest in contributing to this project!  
Contributions of all kinds are welcome — bug fixes, improvements, tests, and documentation.

This document describes the development workflow and quality standards used in this repository.

## 🚀 Getting Started

1. Fork the repository
2. Clone your fork:
   ```bash
   git clone https://github.com/<your-username>/atomic-payments.git
   cd atomic-payments
   ```
3. Build the project
    ```bash 
    mvn clean verify
    ```

## 🎨 Code Formatting

This project enforces code formatting using Spotless. Commits that do not comply with formatting rules will fail the build.

Before committing any changes, run:
```
mvn spotless:apply
```

## 📏 Code Quality

The project enforces additional quality checks:

- Checkstyle for basic style and complexity rules
- JaCoCo for test coverage

**Coverage requirements:** Minimum overall line coverage: 70%
