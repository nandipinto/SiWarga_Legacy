# Siwarga — Neighborhood Dues Management System

A REST API for managing monthly residential dues for a neighborhood cluster. Handles resident data, payment processing, automated debt/fine calculation, and reporting.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 1.9 |
| Framework | Spring Boot 3.3.x |
| Build Tool | Maven |
| Database | PostgreSQL 16 |
| Authentication | JWT (JJWT 0.12.x) |
| CSV Parsing | OpenCSV 5.9 |

---

## Domain Hierarchy

```
RW (Neighborhood Association)
└── RT (Neighborhood Unit)
    └── House (Block + House Number)
        └── Resident (Head of Household)
```

---

## Business Rules

### Monthly Dues
- **Base fee:** Rp 120,000 per house per month
- **System start date:** January 1, 2024 (baseline for all calculations)

### Payment Allocation
When a payment is received, it is applied in this priority order:
1. Oldest arrears (unpaid dues) and associated fines first
2. Once all debt is cleared, excess is applied to future months

### Advance Payment Discounts
Discounts apply **only if the house has zero arrears**:

| Block | Discount |
|---|---|
| 6-month upfront (Rp 720,000+) | Rp 60,000 per 6-month block |
| 12-month upfront (Rp 1,440,000+) | Rp 120,000 per 12-month block |

### Delinquency Fines
Fines are triggered by consecutive unpaid months (counter resets on any payment):

| Threshold | Fine |
|---|---|
| Every 6 consecutive unpaid months | +Rp 60,000 |
| Every 12 consecutive unpaid months | +Rp 120,000 (in addition to the 6-month fine) |

---

## User Roles

| Role | Access |
|---|---|
| **Administrator** | Full access to all modules |
| **Supervisor** | Dues management only (payments, reports, arrears) |

---

## Getting Started

### Prerequisites
- Docker and Docker Compose
- Java 21+
- Maven 3.8+

### 1. Start the Database

```bash
docker-compose up -d
```

This starts a PostgreSQL 16 instance at `localhost:5432` with database `siwarga`.

### 2. Run the Application

```bash
mvn spring-boot:run
```

The API starts at `http://localhost:8080`.

On first boot, a default admin account is created:
- **Username:** `admin`
- **Password:** `admin123`

Override via environment variables: `ADMIN_USERNAME`, `ADMIN_PASSWORD`.

### 3. Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username": "admin", "password": "admin123"}'
```

Response:
```json
{
  "token": "<jwt-token>",
  "username": "admin",
  "role": "ROLE_ADMIN"
}
```

Use the token as `Authorization: Bearer <token>` on all subsequent requests.

---

## API Reference

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/auth/login` | Login and receive a JWT token |

### RW Management (Admin only)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/rw` | List all RW |
| GET | `/rw/{id}` | Get RW by ID |
| POST | `/rw` | Create RW |
| PUT | `/rw/{id}` | Update RW |
| DELETE | `/rw/{id}` | Delete RW |

### RT Management (Admin only)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/rt` | List all RT |
| GET | `/rt/{id}` | Get RT by ID |
| POST | `/rt` | Create RT |
| PUT | `/rt/{id}` | Update RT |
| DELETE | `/rt/{id}` | Delete RT |

### House & Resident Management (Admin only)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/houses` | List all houses |
| GET | `/houses/{id}` | Get house by ID |
| POST | `/houses` | Create house |
| PUT | `/houses/{id}` | Update house |
| DELETE | `/houses/{id}` | Delete house |
| GET | `/residents` | List all residents |
| GET | `/residents/{id}` | Get resident by ID |
| POST | `/residents` | Create resident |
| PUT | `/residents/{id}` | Update resident |
| DELETE | `/residents/{id}` | Delete resident |

### Payments (Admin + Supervisor)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/payments` | List all payments |
| GET | `/payments/{id}` | Get payment by ID |
| GET | `/payments/house/{houseId}` | List payments for a house |
| POST | `/payments` | Record a payment (triggers allocation) |
| DELETE | `/payments/{id}` | Delete a payment (reverses allocation) |

### Reports & Status (Admin + Supervisor)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/reports/monthly?year=&month=` | Monthly dues report (global or filtered by `rtId`) |
| GET | `/status/arrears/{houseId}` | Detailed arrears and fine breakdown for a house |

### Bulk Import (CSV)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/upload/residents` | Admin | Bulk import residents from CSV |
| POST | `/upload/payments` | Admin + Supervisor | Bulk import payment history from CSV |

---

## CSV Import Formats

### Residents (`/upload/residents`)

```csv
rw_name,rt_name,block,house_number,resident_name,email,phone
RW 01,RT 01,E,20,Budi Santoso,budi@email.com,081234567890
RW 01,RT 01,E,21,Siti Rahayu,,082345678901
```

- `email` and `phone` are optional
- RW and RT are created automatically if they don't exist
- Rows where the house already has a resident are skipped

### Payments (`/upload/payments`)

```csv
rw_name,rt_name,block,house_number,amount,payment_date,receipt_number,notes
RW 01,RT 01,E,20,360000,2024-06-15,RCP-2024-001,3 bulan
RW 01,RT 01,E,21,720000,2024-06-20,,
```

- `payment_date` format: `yyyy-MM-dd`
- `receipt_number` and `notes` are optional
- Each row triggers the full payment allocation algorithm
- Row failures do not roll back previous rows; returns per-row status

---

## Configuration

Key settings in `application.yml` (all overridable via environment variables):

| Environment Variable | Default | Description |
|---|---|---|
| `DB_USER` | `siwarga` | Database username |
| `DB_PASS` | `siwarga` | Database password |
| `JWT_SECRET` | *(built-in)* | JWT signing secret (min 32 chars) |
| `ADMIN_USERNAME` | `admin` | Default admin username |
| `ADMIN_PASSWORD` | `admin123` | Default admin password |

---

## Running Tests

```bash
mvn test
```

Unit tests cover the core business logic in `FineService`:
- No fines when all months are paid
- 6-month fine fires at 6 consecutive unpaid months
- Both 6-month and 12-month fines fire at 12 consecutive unpaid
- Fine counter resets after a payment
- No duplicate fines are created

---

## Database Schema

```
rw              → RW (Neighborhood Association)
rt              → RT, belongs to rw
house           → belongs to rt, unique (rt + block + house_number)
resident        → one per house (Head of Household)
app_user        → system users (ADMIN / SUPERVISOR)

dues_ledger     → one row per house per month from 2024-01
fine            → triggered fine events per house
payment         → one record per payment received
payment_allocation → maps each payment to specific ledger rows or fines
```
