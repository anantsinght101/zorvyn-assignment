# 💰 Zorvyn Finance: Data Processing & RBAC Backend

A robust Spring Boot backend system for managing financial records with integrated **JWT Authentication**, **Role-Based Access Control (RBAC)**, and a dashboard-ready **Analytics API**.

---

## 🚀 Tech Stack

| Layer | Technology |
| :--- | :--- |
| **Language** | Java 17 |
| **Framework** | Spring Boot 4.0.5 |
| **Security** | Spring Security + JWT (jjwt 0.11.5) |
| **Database** | MySQL 8.0 |
| **Persistence** | Spring Data JPA / Hibernate |
| **Validation** | Spring Boot Validation (Jakarta) |
| **Build Tool** | Maven |

---

## 📂 Project Structure

```text
src/main/java/com/zorvyn/assignment/
├── config/             # SecurityConfig & StartupInitializer (Data Seeding)
├── controller/         # REST Endpoints (Auth, Transactions, Admin, Analytics)
├── dto/                # Request/Response Data Transfer Objects
├── entity/             # JPA Entities (User, Role, TransactionRecord)
├── exception/          # GlobalExceptionHandler (@RestControllerAdvice)
├── repository/         # Database access with custom JPQL & Pagination
├── security/           # JwtUtil & JwtFilter implementation
└── service/            # Business logic (UserService, TransactionService)


🔐 Roles & Access Control
Permissions are strictly enforced via @PreAuthorize based on the following roles:
Role
Description
ADMIN
Full CRUD on records, user management, and advanced analytics.
ANALYST
Read records + summary analytics. Cannot create or delete.
VIEWER
Read-only access to records. Restricted from aggregated analytics.

Note: Viewers cannot query by exact startDate/endDate. They must use the period parameter (WEEKLY, MONTHLY, etc.) to view data.
🛠️ Setup and Installation
1. Prerequisites
Java 17 or higher
Maven 3.8+
MySQL 8.0 running locally
2. Database Setup
Create the schema in your MySQL instance:

SQL


CREATE DATABASE zorvyn_finance;


3. Configuration
Update src/main/resources/application.properties with your local credentials:

Properties


spring.datasource.url=jdbc:mysql://localhost:3306/zorvyn_finance
spring.datasource.username=your_username
spring.datasource.password=your_password

# JWT Configuration
jwt.secret=mysecretkeymysecretkeymysecretkeymysecretkey (Min 32 chars)


4. Run the Application

Bash


mvn spring-boot:run


The server starts on http://localhost:8080.
Automatic Seeding: On the first startup, StartupInitializer will automatically create the required Roles and a set of sample Transaction records.
📡 API Overview
Authentication Flow
Signup: POST /auth/signup (Assigns Name, Email, Password, Role).
Login: POST /auth/login (Returns JWT).
Authorize: Include the token in the header: Authorization: Bearer <token>.
Key Endpoints
Transactions: GET /api/transactions (Supports pagination/sorting).
Analytics: GET /api/transactions/summary (Calculates totals and net spend).
Admin: GET /users (Manage user accounts and active/inactive status).
💡 Implementation Highlights
Soft Deletes: Records are flagged as deleted=true to maintain audit trails.
Global Error Handling: Consistent JSON error responses for 400, 401, 403, and 404 errors.
Pagination: Implemented using Spring Data Pageable for high-performance data retrieval.
Stateless: Security is entirely token-based; no server-side session storage is used.
🧪 Testing
Run the test suite using Maven:

Bash


mvn test


Includes Unit tests for Services and Integration tests for REST Controllers.
