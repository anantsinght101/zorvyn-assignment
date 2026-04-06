# Zorvyn Assignment — API Documentation

**Base URL:** `http://localhost:8080`

---

## 🔐 Role Legend

- **ADMIN** → Full access (CRUD + restore + user management)
- **ANALYST** → Read + analytics (can query exact dates)
- **VIEWER** → Read-only (only period-based filtering)

---

## 1. Auth Endpoints

📌 Public — No JWT required

### Endpoints

| Method | Endpoint       | Description                     |
|--------|--------------|---------------------------------|
| POST   | /auth/signup | Register new user               |
| POST   | /auth/login  | Login & get JWT token           |

---

### ➤ Signup

```json
POST /auth/signup

Request

{
  "name": "Priya Sharma",
  "email": "priya@example.com",
  "password": "secret123",
  "role": "VIEWER"
}

Response

{
  "name": "Priya Sharma",
  "email": "priya@example.com",
  "role": "VIEWER"
}

⚠️ Role defaults to VIEWER

➤ Login
POST /auth/login

Request

{
  "email": "priya@example.com",
  "password": "secret123"
}

Response

{
  "message": "Login successful",
  "token": "JWT_TOKEN"
}
2. Transaction APIs

📌 Requires JWT → Authorization: Bearer <token>

Endpoints Overview
Method	Endpoint	Role Access
GET	/api/transactions	All
GET	/api/transactions/{id}	All
GET	/api/transactions/count	All
GET	/api/transactions/recent	All
POST	/api/transactions	ADMIN
PUT	/api/transactions/{id}	ADMIN
DELETE	/api/transactions/{id}	ADMIN
PUT	/api/transactions/{id}/restore	ADMIN
GET	/api/transactions/deleted	ADMIN
POST	/api/transactions/summary	ADMIN, ANALYST
➤ Get Transactions
GET /api/transactions

Query Params

Param	Description
type	INCOME / EXPENSE
category	FOOD / TRANSPORT / UTILITIES
startDate	(Admin/Analyst only)
endDate	(Admin/Analyst only)
period	WEEKLY / MONTHLY / YEARLY
page	default 0
size	default 20

📌 Viewer cannot use startDate / endDate

➤ Create Transaction
POST /api/transactions
{
  "amount": 28000,
  "date": "2025-04-01",
  "type": "EXPENSE",
  "category": "UTILITIES",
  "description": "Electricity bill"
}
➤ Analytics Endpoints
Endpoint	Description
/count	Total records
/recordCount	Records in range
/totalIncome	Total income
/totalExpense	Total expense
/netspend	Income - Expense
➤ Summary API
POST /api/transactions/summary

Request

{
  "period": "MONTHLY"
}

Response

{
  "totalIncome": 290000,
  "totalExpense": 88200,
  "netBalance": 201800
}
3. User APIs

📌 ADMIN only

Endpoints
Method	Endpoint	Description
GET	/users	All users
GET	/users/{id}	Get user
POST	/users	Create user
PUT	/users/{id}	Update user
DELETE	/users/{id}	Delete user
PUT	/users/{id}/status	Activate/Deactivate
➤ Create User
POST /users
{
  "name": "Rohan Mehta",
  "email": "rohan@example.com",
  "role": "ANALYST",
  "password": ""
}

📌 Default password → defaultPassword1

➤ Update Status
PUT /users/{id}/status?active=false
4. Error Format
{
  "status": 404,
  "message": "Record not found",
  "timestamp": "2025-04-06T14:32:11"
}
🔴 Common Errors
Code	Meaning
400	Bad request
401	Invalid credentials
403	Access denied
404	Resource not found
409	Duplicate resource
500	Server error
🔐 Authentication

All protected endpoints require:

Authorization: Bearer <jwt_token>
