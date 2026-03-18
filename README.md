## Project Overview
<img width="1724" height="894" alt="image" src="https://github.com/user-attachments/assets/da444752-5d47-4c1f-a0e9-58bb009eae6b" />
The Leave Management System is a high-performance, event-driven backend service designed to automate and streamline employee leave requests, approvals, and leave balance management.

## Core Features

### Role-Based Access Control (RBAC)

The system enforces strict access control with distinct workflows for:

- **Employees**
  - Submit leave requests
  - View leave balances and history

- **Managers**
  - Approve or reject employee leave requests
  - View pending approvals

- **System Admins**
  - Configure leave policies
  - Import/export leave data
  - Manage system audit logs

---

### Advanced Leave Validation

The leave engine performs automatic validation and calculation before persisting a request:

- Validates leave date ranges and overlapping requests
- Enforces advance notice rules per leave type
- Limits maximum leave units per request
- Dynamically calculates **working days only**
- Automatically excludes:
  - Weekends
  - Public holidays

---

### Hierarchical Holiday Resolution

Public holidays are resolved through a **multi-layer lookup hierarchy**:

1. **Redis Cache**
   - Sub-millisecond lookup for previously cached holidays.

2. **Database Cache (MySQL)**
   - Stores previously resolved holiday data.

3. **External Holiday API**
   - Used only if cache misses occur.

When holidays are fetched from the external API:

- Data is written back to the database using a **REQUIRES_NEW transaction**
- This ensures the main leave request transaction remains isolated from API failures.

---

### Event-Driven Background Processing

The system decouples background tasks from core business logic using asynchronous event processing.

Background operations include:

- Email notifications
- Bulk data imports
- Bulk export report generation

Execution flow:

1. Main transaction commits successfully.
2. `afterCommit()` triggers an event.
3. `@Async` worker processes the task.

This guarantees **fast API responses** while ensuring reliable execution of non-critical processes.

---

### Resilient Task Scheduling

The system includes a robust scheduling mechanism to handle operational tasks.

Key capabilities:

- **Exponential Backoff Retries**
  - Automatically retries failed email notifications.

- **Daily Scheduled Jobs**
  - Sends reminders for pending manager approvals.

- **Fault-tolerant processing**
  - Ensures tasks are retried without blocking the main system.

---

### Strict Data Auditability

Every system action is permanently recorded to maintain traceability and compliance.

Tracked events include:

- Leave request creation
- Approval and rejection decisions
- Leave cancellations
- Administrative actions

Audit tables:

- `leave_status_history`
- `audit_log`


## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.3 |
| Security | Spring Security + JWT |
| Database | MySQL 8 (Docker) |
| Cache | Redis (Docker) |
| ORM | Spring Data JPA / Hibernate 7 |
| Email | Spring Mail (Gmail SMTP) |
| API Docs | SpringDoc OpenAPI (Swagger) |
| Build | Maven |
| Containerisation | Docker + Docker Compose |

---

---

## Features

---

## Getting Started

### Prerequisites

- Java 21
- Maven
- Docker + Docker Compose

### 1. Clone the repository

```bash
git clone https://github.com/Enigma1303/LeaveManagementSystem.git
cd leave-management
```

### 2. Create `.env` file

```env
DB_URL=jdbc:mysql://mysql:3306/leavemanagement
DB_USERNAME=root
DB_PASSWORD=yourpassword
MYSQL_ROOT_PASSWORD=yourpassword
MYSQL_DATABASE=leavemanagement
JWT_SECRET=your-256-bit-secret-key
JWT_EXPIRATION=86400000
MAIL_USERNAME=your@gmail.com
MAIL_PASSWORD=your-app-password
REDIS_HOST=redis
REDIS_PORT=6379
```

> For Gmail, generate an App Password at [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)

### 3. Start MySQL and Redis containers

```bash
docker-compose up -d mysql redis
```

### 4. Run the application

```bash
./mvnw spring-boot:run
```

### 5. Access Swagger UI

```
http://localhost:8080/docs
```

### Running everything via Docker Compose

```bash
docker-compose up -d
```

> MySQL runs on host port `3307`, Redis on `6380`, app on `8080`.

---

## Environment Variables

| Variable | Description | Example |
|---|---|---|
| `DB_URL` | JDBC URL for MySQL | `jdbc:mysql://mysql:3306/leavemanagement` |
| `DB_USERNAME` | MySQL username | `root` |
| `DB_PASSWORD` | MySQL password | `secret` |
| `JWT_SECRET` | HS256 signing key (min 32 chars) | `my-very-long-secret-key-here` |
| `JWT_EXPIRATION` | Token expiry in ms | `86400000` (24h) |
| `MAIL_USERNAME` | Gmail address | `you@gmail.com` |
| `MAIL_PASSWORD` | Gmail App Password | `abcd efgh ijkl mnop` |
| `REDIS_HOST` | Redis hostname | `redis` (Docker) or `localhost` |
| `REDIS_PORT` | Redis port | `6379` |

---

## API Reference

### Auth
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/auth/login` | Public | Returns JWT token |

### Users
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/users` | Admin | Create employee/manager |
| GET | `/api/users` | Admin | List all users |
| GET | `/api/users/{id}` | Admin | Get user by id |

### Leave Types
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/leave-types` | Admin | Create leave type |
| GET | `/api/leave-types` | Admin | List all leave types |
| GET | `/api/leave-types/active` | All | List active leave types |
| GET | `/api/leave-types/{id}` | Admin | Get leave type |
| PUT | `/api/leave-types/{id}` | Admin | Update leave type |
| DELETE | `/api/leave-types/{id}` | Admin | Deactivate leave type |

### Leaves
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/v2/leaves` | Employee | Submit leave request |
| GET | `/api/v2/leaves` | All | Get leaves (role-scoped + filtered) |
| POST | `/api/v2/leaves/{id}/approve` | Manager/Admin | Approve leave |
| POST | `/api/v2/leaves/{id}/reject` | Manager/Admin | Reject leave |
| POST | `/api/v2/leaves/{id}/cancel` | Employee | Cancel own leave |
| GET | `/api/v2/leave-requests/export` | All | Export leaves as CSV |

### Leave Balances
| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/v2/leave-balances` | Employee/Manager | View own/team balances |
| GET | `/api/v2/leave-balances/{employeeId}` | Admin/Manager | View specific employee balance |
| POST | `/api/v2/leave-balances/import` | Admin | Bulk import via CSV |
| GET | `/api/v2/leave-balances/import/{jobId}` | Admin | Poll import job status |
| GET | `/api/v2/leave-balances/export` | Admin | Export balances as CSV |

### Holiday Cache
| Method | Endpoint | Access | Description |
|---|---|---|---|
| DELETE | `/api/v2/holidays/cache` | Admin | Invalidate holiday cache |

### Notifications
| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/v2/notifications/failed` | Admin | List failed/exhausted notifications |
| POST | `/api/v2/notifications/{id}/retry` | Admin | Retrigger notification manually |
### Metrics
| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/v2/metrics` | Admin | Business metrics by time period |

### Health
| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/actuator/health` | Public | Health status (DB, mail, Redis, disk) |

---

