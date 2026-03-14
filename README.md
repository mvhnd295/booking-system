# Queue & Appointment Booking System

A Spring Boot application for managing queues and appointments across multiple branches. This system enables customers to book appointments, allows staff to manage appointments and queues, and provides administrators with comprehensive system configuration and monitoring capabilities.

## Features

- **User Management**: Multiple roles including Admin, Branch Manager, Staff, and Customer
- **Appointment Booking**: Customers can view available slots and book appointments
- **Branch Management**: Multiple branches with independent services and staff
- **Service Management**: Configure services offered at each branch
- **Slot Management**: Dynamic appointment time slots
- **Appointment Scheduling**: Staff can manage and reschedule appointments
- **File Attachments**: Support for ID images and document attachments
- **JWT Authentication**: Secure API endpoints with JSON Web Tokens
- **Audit Logging**: Track all system operations for compliance

## Prerequisites

- **Java**: JDK 17 or higher
- **Maven**: 3.9.0 or higher
- **PostgreSQL**: 12 or higher
- **Git**: For version control

## Setup Instructions

### 1. Clone the Repository

```bash
git clone <https://github.com/mvhnd295/booking-system.git>
cd queue-appointment-booking-system
```

### 2. Configure Environment Variables

Create a `.env` file in the project root directory with the following variables:

```bash
DB_USERNAME=postgres
DB_PASSWORD=your_secure_password
JWT_SECRET=your_very_long_and_secure_jwt_secret_key_minimum_32_characters
```

Or set them as system environment variables before running the application.

### 3. Create PostgreSQL Database

```bash
createdb flowcare
```

Or using psql:

```sql
CREATE DATABASE flowcare;
```

### 4. Build the Application

```bash
mvn clean install
```

### 5. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

Flyway will automatically run database migrations from `src/main/resources/db/migration/` on startup.

## Environment Variables

Configure these environment variables for your environment:

| Variable      | Description                                              | Example                                           | Required |
| ------------- | -------------------------------------------------------- | ------------------------------------------------- | -------- |
| `DB_USERNAME` | PostgreSQL database username                             | `postgres`                                        | Yes      |
| `DB_PASSWORD` | PostgreSQL database password                             | `SecurePassword123!`                              | Yes      |
| `JWT_SECRET`  | Secret key for JWT token signing (minimum 32 characters) | `your_very_long_secret_key_here_minimum_32_chars` | Yes      |

## Database Configuration

The application uses PostgreSQL as the primary database with Flyway for migrations.

**Current Configuration** (from `application.properties`):

- **URL**: `jdbc:postgresql://localhost:5432/flowcare`
- **Driver**: PostgreSQL JDBC Driver
- **Dialect**: PostgreSQL Hibernate Dialect
- **Migrations**: `classpath:db/migration`

### Database Schema

Available migrations:

- Users and roles (authentication & authorization)
- Branches and branch managers
- Staff and staff-service assignments
- Customers
- Service types
- Appointment slots
- Appointments
- Attachments
- Audit logs
- Application configuration

## Seeding Instructions

The system includes a sample dataset in `src/main/resources/seed/example.json` with:

### Sample Users

**Admin User**

- Username: `admin`
- Password: `Admin@123`
- Role: ADMIN

**Branch Managers**

- Username: `mgr_muscat` | Password: `Manager@123`
- Username: `mgr_suhar` | Password: `Manager@123`

**Staff Members**

- Username: `staff_muscat_1` | Password: `Staff@123`
- Username: `staff_muscat_2` | Password: `Staff@123`
- Username: `staff_suhar_1` | Password: `Staff@123`
- Username: `staff_suhar_2` | Password: `Staff@123`

**Customer Accounts**

- Username: `cust_ahmed` | Password: `Customer@123`
- Username: `cust_fatima` | Password: `Customer@123`

### To Seed the Database

While the current implementation uses Flyway for schema migrations, the `example.json` file provides sample data structure. You can:

1. **Option A**: Use the sample data as reference to manually insert test data via API endpoints
2. **Option B**: Create a data loader component to parse and insert the JSON data on application startup
3. **Option C**: Write SQL INSERT statements based on the JSON structure and execute them directly

## API Usage

The application provides RESTful API endpoints. Base URL: `http://localhost:8080`

### Authentication Endpoints

#### Register a New Customer

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "new_customer",
    "password": "SecurePass@123",
    "email": "customer@example.com",
    "fullName": "John Doe",
    "phone": "+96890000999"
  }'
```

**Response** (201 Created):

```json
{
  "success": true,
  "message": "Registration successful",
  "data": {
    "userId": "usr_cust_new",
    "username": "new_customer",
    "email": "customer@example.com",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

#### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "cust_ahmed",
    "password": "Customer@123"
  }'
```

**Response** (200 OK):

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "userId": "usr_cust_001",
    "username": "cust_ahmed",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "role": "CUSTOMER"
  }
}
```

### Public API Endpoints (No Authentication Required)

#### Get All Branches

```bash
curl -X GET http://localhost:8080/api/public/branches \
  -H "Content-Type: application/json"
```

**Response** (200 OK):

```json
{
  "success": true,
  "message": "Branches retrieved",
  "data": [
    {
      "id": "br_muscat_001",
      "name": "Muscat Branch",
      "location": "Muscat, Oman",
      "phone": "+96824123456",
      "email": "muscat@flowcare.local"
    },
    {
      "id": "br_suhar_001",
      "name": "Suhar Branch",
      "location": "Suhar, Oman",
      "phone": "+96826234567",
      "email": "suhar@flowcare.local"
    }
  ]
}
```

#### Get Specific Branch

```bash
curl -X GET http://localhost:8080/api/public/branches/br_muscat_001 \
  -H "Content-Type: application/json"
```

#### Get Services by Branch

```bash
curl -X GET http://localhost:8080/api/public/branches/br_muscat_001/services \
  -H "Content-Type: application/json"
```

**Response** (200 OK):

```json
{
  "success": true,
  "message": "Services retrieved",
  "data": [
    {
      "id": "svc_001",
      "name": "General Consultation",
      "description": "General medical consultation",
      "durationMinutes": 30
    },
    {
      "id": "svc_002",
      "name": "Specialist Consultation",
      "description": "Specialist medical consultation",
      "durationMinutes": 45
    }
  ]
}
```

#### Get Available Slots

```bash
curl -X GET "http://localhost:8080/api/public/branches/br_muscat_001/services/svc_001/slots" \
  -H "Content-Type: application/json"
```

### Customer Appointment Endpoints (Authentication Required)

**Note**: Include the JWT token from login in the Authorization header:

```bash
-H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

#### Book Appointment

```bash
curl -X POST http://localhost:8080/api/customer/appointments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "branchId": "br_muscat_001",
    "serviceId": "svc_001",
    "slotId": "slot_001",
    "notes": "I have a fever and cough"
  }'
```

**Response** (201 Created):

```json
{
  "success": true,
  "message": "Appointment Booked successfully.",
  "data": {
    "appointmentId": "apt_001",
    "customerId": "usr_cust_001",
    "branchId": "br_muscat_001",
    "serviceId": "svc_001",
    "slotId": "slot_001",
    "appointmentDate": "2026-03-15",
    "appointmentTime": "10:00",
    "status": "BOOKED",
    "notes": "I have a fever and cough"
  }
}
```

#### Get My Appointments

```bash
curl -X GET http://localhost:8080/api/customer/appointments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response** (200 OK):

```json
{
  "success": true,
  "message": "Appointments retrieved.",
  "data": [
    {
      "appointmentId": "apt_001",
      "customerId": "usr_cust_001",
      "branchId": "br_muscat_001",
      "status": "BOOKED",
      "appointmentDate": "2026-03-15",
      "appointmentTime": "10:00"
    }
  ]
}
```

#### Get Appointment Details

```bash
curl -X GET http://localhost:8080/api/customer/appointments/apt_001 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### Reschedule Appointment

```bash
curl -X PUT http://localhost:8080/api/customer/appointments/apt_001/reschedule \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "newSlotId": "slot_002"
  }'
```

#### Cancel Appointment

```bash
curl -X DELETE http://localhost:8080/api/customer/appointments/apt_001 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Upload Attachments

```bash
curl -X POST http://localhost:8080/api/customer/appointments/apt_001/attachments \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -F "file=@/path/to/document.pdf"
```

**File Limits**:

- ID Images: 5 MB max
- Attachments: 10 MB max
- Total Request: 15 MB max

## Project Structure

```
src/
├── main/
│   ├── java/com/rihal/queue_appointment_booking_system/
│   │   ├── controller/          # REST API endpoints
│   │   ├── service/             # Business logic
│   │   ├── domain/              # JPA entities
│   │   ├── dto/                 # Data transfer objects
│   │   ├── repository/          # Data access layer
│   │   ├── security/            # JWT and security config
│   │   ├── audit/               # Audit logging
│   │   └── Application.java     # Main entry point
│   └── resources/
│       ├── application.properties
│       ├── db/migration/        # Flyway migrations
│       └── seed/example.json    # Sample data
└── test/                        # Unit and integration tests
```

## Building and Running Tests

```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report

# Build without running tests
mvn clean install -DskipTests
```

## Technology Stack

- **Framework**: Spring Boot 4.0.3
- **Language**: Java 17
- **Database**: PostgreSQL
- **ORM**: Hibernate/JPA
- **Authentication**: JWT (JSON Web Tokens)
- **Build Tool**: Maven
- **Migrations**: Flyway
- **Scheduling**: Quartz
- **Validation**: Jakarta Validation
- **Logging**: SLF4J

## Development

### IDE Setup

**IntelliJ IDEA / Eclipse / VS Code**:

1. Import as Maven project
2. Configure JDK 17
3. Run `mvn install` to download dependencies
4. Configure PostgreSQL connection in IDE

### Lombok Setup

This project uses Lombok for reducing boilerplate code. Make sure to:

- Install Lombok plugin in your IDE
- Enable annotation processing in compiler settings

### Logging

- Default log level: DEBUG for Spring and application code
- Log output: Console (configurable in `application.properties`)

## Troubleshooting

### Database Connection Issues

```
Error: Connection to localhost:5432 refused
```

**Solution**: Ensure PostgreSQL is running and accessible:

```bash
# Check PostgreSQL status
psql -U postgres -d flowcare
```

### JWT Token Errors

```
Error: Invalid JWT token
```

**Solution**: Ensure `JWT_SECRET` is set and matches between encoding/decoding. The secret must be at least 32 characters long.

### Flyway Migration Errors

```
Error: Current version of schema (X) is newer than the latest migration
```

**Solution**: Clean the `schema_version` table if migrations are in conflict:

```sql
DELETE FROM flyway_schema_history WHERE version > X;
```

## License

This project is part of the RIHAL organization.

## Support

For issues, questions, or contributions, please reach out to the development team.
