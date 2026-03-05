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
- End date cannot be before start date
- Overlapping leave requests are blocked
- Only valid status transitions allowed: PENDING → APPROVED or REJECTED
- Once approved or rejected, leave status cannot be modified
- Managers can only update leave requests of their direct subordinates

## Ordering
- All leave responses are ordered by `createdAt DESC` by default
- Status history within each leave is ordered by `createdAt DESC`

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

## Recent Changes
- Removed direct repository access from controller layer, all data resolution moved to service layer
- Added centralized `getUser(String email)` helper in service to eliminate repeated repository calls
- Replaced scattered status transition checks with a transition map enforcing valid state changes only
- Added default `ORDER BY createdAt DESC` ordering to all repository queries
- Fixed merged leave list ordering for manager role after combining own and subordinates' leaves
- Added status history sorting by `createdAt DESC` within each leave response
- Implemented `createdAt` filter which was previously accepted but silently ignored
- Added SLF4J logging across all service classes, controllers, and GlobalExceptionHandler
- Removed past date restriction on leave submission to support emergency and retroactive requests
- Updated service interface to accept `String email` instead of `Users` entity