# FlowCare — Queue & Appointment Booking System Rihal

A Spring Boot REST API for managing queues and appointments across FlowCare branches in Oman.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 4.x, Java 17 |
| Database | PostgreSQL 16 |
| Migrations | Flyway |
| Auth | Spring Security + JWT (jjwt) |
| Scheduler | Quartz |
| Docs | Springdoc OpenAPI (Swagger UI) |
| Build | Maven |
| Container | Docker + Docker Compose |

---

## Running Locally

### Prerequisites
- Java 17+
- Maven 3.9+
- PostgreSQL 16 running locally, or use Docker Compose

### Option A — Run with Docker Compose (recommended)

1. Clone the repository
2. Copy the environment file and fill in values:
   ```bash
   cp .env.example .env
   ```
3. Start everything:
   ```bash
   docker compose up --build
   ```
4. The API will be available at `http://localhost:8080`
5. Swagger UI: `http://localhost:8080/swagger-ui.html`

### Option B — Run locally with IntelliJ / Maven

1. Create a PostgreSQL database named `flowcare`
2. Set environment variables in your run configuration:
   ```
   DB_USERNAME=your_db_user
   DB_PASSWORD=your_db_password
   JWT_SECRET=your_base64_256bit_secret
   ```
3. Run:
   ```bash
   mvn spring-boot:run
   ```

### Generating a JWT Secret
```bash
openssl rand -base64 32
```

---

## API Documentation

Swagger UI is available at:
```
http://localhost:8080/swagger-ui.html
```

Raw OpenAPI spec:
```
http://localhost:8080/v3/api-docs
```

### Authentication
1. Call `POST /api/auth/login` with your credentials
2. Copy the `token` from the response
3. In Swagger UI, click **Authorize** and enter: `Bearer <token>`

---

## Default Seed Credentials

| Role | Username | Password |
|---|---|---|
| Admin | admin | Admin@123 |
| Manager (Muscat) | mgr_muscat | Manager@123 |
| Manager (Suhar) | mgr_suhar | Manager@123 |
| Staff | staff_muscat_1 | Staff@123 |
| Customer | cust_ahmed | Customer@123 |

---

## Environment Variables

| Variable | Description | Required |
|---|---|---|
| `DB_USERNAME` | PostgreSQL username | ✅ |
| `DB_PASSWORD` | PostgreSQL password | ✅ |
| `JWT_SECRET` | Base64-encoded 256-bit secret | ✅ |
| `APP_BASE_URL` | Public base URL of the app | Optional (default: `http://localhost:8080`) |

---

## Key Endpoints

### Public
| Method | Path | Description |
|---|---|---|
| GET | `/api/public/branches` | List all branches |
| GET | `/api/public/branches/{id}/services` | List services by branch |
| GET | `/api/public/branches/{id}/services/{id}/slots` | Available slots (supports `?date=`) |

### Auth
| Method | Path | Description |
|---|---|---|
| POST | `/api/auth/register` | Register customer (multipart with ID image) |
| POST | `/api/auth/login` | Login — returns JWT |

### Customer
| Method | Path | Description |
|---|---|---|
| POST | `/api/customer/appointments` | Book appointment (multipart, optional attachment) |
| GET | `/api/customer/appointments` | My appointments |
| PATCH | `/api/customer/appointments/{id}/cancel` | Cancel |
| PATCH | `/api/customer/appointments/{id}/reschedule` | Reschedule |
| GET | `/api/customer/appointments/{id}/queue-position` | Real-time queue position |

### Admin Config
| Method | Path | Description |
|---|---|---|
| PUT | `/api/config/retention-days` | Set soft-delete retention period |
| PUT | `/api/config/max-bookings-per-day` | Set daily booking limit |
| PUT | `/api/config/max-reschedules-per-day` | Set daily reschedule limit |
| POST | `/api/config/cleanup` | Trigger hard-delete cleanup |

---

## Soft Delete & Cleanup

Slots are soft-deleted (marked `deleted = true`) when removed via the API. A Quartz job runs daily at **2:00 AM (Asia/Muscat)** to hard-delete slots whose `deletedAt` is older than the configured retention period (default: 30 days).

The cleanup can also be triggered manually via `POST /api/config/cleanup`.

---

## Deploying to Railway

1. Push your code to GitHub
2. Create a new Railway project and connect your repo
3. Add a PostgreSQL plugin
4. Set environment variables in Railway dashboard:
    - `DB_USERNAME`, `DB_PASSWORD` — from Railway PostgreSQL plugin
    - `JWT_SECRET` — generate with `openssl rand -base64 32`
    - `SPRING_DATASOURCE_URL` — set to Railway's PostgreSQL URL
    - `APP_BASE_URL` — set to your Railway app URL
5. Deploy — Railway auto-detects the `Dockerfile`

---

## Project Structure

```
src/
├── main/
│   ├── java/com/rihal/queue_appointment_booking_system/
│   │   ├── config/          # Spring + Quartz + OpenAPI config
│   │   ├── controller/      # REST controllers
│   │   ├── domain/
│   │   │   ├── entity/      # JPA entities
│   │   │   └── enums/       # Enums
│   │   ├── dto/
│   │   │   ├── request/     # Request DTOs
│   │   │   └── response/    # Response DTOs
│   │   ├── repository/      # Spring Data repositories
│   │   ├── scheduler/       # Quartz jobs
│   │   ├── security/        # JWT filter + service
│   │   ├── service/         # Business logic
│   │   ├── specification/   # JPA Specifications for search
│   │   ├── storage/         # File storage service
│   │   └── util/            # Shared utilities
│   └── resources/
│       ├── db/migration/    # Flyway SQL migrations (V1–V17)
│       └── seed/            # example.json seed data
```