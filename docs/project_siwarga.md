---
name: Siwarga Project Overview
description: Neighborhood dues management system - Kotlin/Spring Boot REST API
type: project
---

Greenfield Kotlin + Spring Boot REST API for managing monthly residential dues for a neighborhood cluster (RT system).

**Why:** Managing monthly dues (Rp 120,000/house), arrears tracking, fines for delinquency, and advance payment discounts for a residential complex.

**How to apply:** Use this context when making changes to understand business rules and existing structure.

## Stack
- Kotlin 1.9 + Maven + Spring Boot 3.3.x
- PostgreSQL via Docker Compose (`docker-compose.yml` exists in project root)
- JWT authentication (JJWT 0.12.x)
- OpenCSV for bulk import

## DB
Uses PostgreSQL (not H2) — docker-compose.yml already present. Connect: `localhost:5432/siwarga`, user/pass: `siwarga/siwarga`.

## Key business logic files
- `src/main/kotlin/com/siwarga/service/FineService.kt` — computes consecutive-unpaid fines
- `src/main/kotlin/com/siwarga/service/PaymentService.kt` — allocates payments (arrears-first, discounts, future months)
- `src/main/kotlin/com/siwarga/service/LedgerService.kt` — ensures dues_ledger rows exist from 2024-01

## Fine rules
- SIX_MONTH: Rp 60,000 every 6th consecutive unpaid month
- TWELVE_MONTH: Rp 120,000 every 12th (both fire at 12, 24, 36...)

## Roles
- ADMIN: full access
- SUPERVISOR: payments, reports, arrears only

## Default admin
Username/password set via `ADMIN_USERNAME`/`ADMIN_PASSWORD` env vars (defaults: admin/admin123)
