Based on the content of your documentation, here is the formatted Markdown version. You can copy this directly into a `README.md` file or a dedicated `API_DOCUMENTATION.md` file in your GitHub repository.

---

# API Documentation
## [cite_start]Zorvyn Assignment — Spring Boot REST API [cite: 2]

[cite_start]**Base URL:** `http://localhost:8080` [cite: 3]

### [cite_start]Role Legend [cite: 4]
| Role | Permissions |
| :--- | :--- |
| **ADMIN** | [cite_start]Full access — create, update, delete, restore, manage users [cite: 5] |
| **ANALYST** | [cite_start]Read + analytics — can query by exact date, view summaries [cite: 5] |
| **VIEWER** | [cite_start]Read-only — restricted to period-based filtering, no exact dates [cite: 5] |

---

## [cite_start]1. Auth Endpoints [cite: 6]
> [cite_start]ℹ️ These endpoints are public — no JWT token required. [cite: 7]

### [cite_start]Quick Reference [cite: 8]
| Method | Endpoint | Description | Auth |
| :--- | :--- | :--- | :--- |
| POST | `/auth/signup` | [cite_start]Register a new user account [cite: 9] | [cite_start]Public [cite: 10] |
| POST | `/auth/login` | [cite_start]Authenticate and receive a JWT token [cite: 9] | [cite_start]Public [cite: 24] |

#### **Register User**
[cite_start]`POST /auth/signup` [cite: 10]
* **Description:** Registers a new account. [cite_start]The role defaults to `VIEWER` if omitted. [cite: 11]
* [cite_start]**Role Options:** `ADMIN`, `ANALYST`, `VIEWER`. [cite: 19]
* **Request Body:**
    ```json
    {
      "name": "Priya Sharma",
      "email": "priya@example.com",
      "password": "secret123",
      "role": "VIEWER"
    }
    [cite_start]
http://googleusercontent.com/immersive_entry_chip/0
http://googleusercontent.com/immersive_entry_chip/1
http://googleusercontent.com/immersive_entry_chip/2
