# Employee Leave and Approval Management System

A Spring Boot REST API for managing employee leave requests with role-based access control.

## Tech Stack
- Java 21
- Spring Boot
- Spring Security + JWT
- Spring Data JPA
- MySQL (Docker)
- Swagger UI

## Roles
- **Admin** — manages users, views all leave requests
- **Manager** — approves/rejects leave requests of subordinates
- **Employee** — submits and views own leave requests

## API Endpoints

### Authentication
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/auth/login` | Public | Login and get JWT token |
| POST | `/api/users` | Admin only | Register new user |

### Leave Management
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/leaves` | Authenticated | Submit leave request |
| GET | `/api/leaves` | Authenticated | View leaves (role based) |
| PATCH | `/api/leaves/{id}` | Manager/Admin | Approve or reject leave |

## Filtering
```
GET /api/leaves?status=PENDING
GET /api/leaves?startDate=2026-03-01
GET /api/leaves?endDate=2026-04-01
GET /api/leaves?search=medical
GET /api/leaves?employeeId=2
GET /api/leaves?managerId=1
GET /api/leaves?createdAt=2026-02-23T00:00:00
```

## Business Rules
- Leave start date cannot be in the past
- End date cannot be before start date
- Overlapping leave requests are blocked
- Approved leave cannot be modified
- Rejected leave cannot be approved
- Only valid status transitions allowed: PENDING → APPROVED or REJECTED

## Running the Application

### Prerequisites
- Java 21
- Docker

### Start MySQL with Docker
```bash
docker run --name mysql-leave -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=leavedb -p 3306:3306 -d mysql:8
```

### Configure `application.properties`
```properties
spring.datasource.url=jdbc:mysql://localhost:3307/leavemanagement
spring.datasource.username=root
spring.datasource.password=secret
spring.jpa.hibernate.ddl-auto=validate
```

### Run the app
```bash
./mvnw spring-boot:run
```

### Run tests
```bash
./mvnw test
```

## API Documentation
Swagger UI available at:
```
http://localhost:8080/swagger-ui/index.html
```

## Security
- JWT tokens required in `Authorization: Bearer <token>` header for all protected endpoints
- Tokens contain user ID and role information
- Only admins can register new users