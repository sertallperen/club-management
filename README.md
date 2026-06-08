# Football Club Management API

A complete, production-ready REST API backend for managing a football club — built with Spring Boot 3, Spring Security (JWT), and Spring Data JPA.

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3.5 |
| ORM | Spring Data JPA / Hibernate |
| Security | Spring Security + JWT (JJWT 0.12.6) |
| Database | H2 (dev) / PostgreSQL (prod) |
| Documentation | OpenAPI 3 / Swagger UI |
| Build Tool | Maven |

---

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+

### 1. Run the backend
```bash
mvn spring-boot:run
```
The API starts on **http://localhost:8080**

### 2. Open the portal (optional frontend)
Open `portal.html` in your browser — no build step, no dependencies.

### 3. Explore the API
| URL | Description |
|---|---|
| **http://localhost:8080/swagger-ui.html** | Interactive API docs (try all endpoints) |
| http://localhost:8080/h2-console | H2 database browser (dev only) |

> **Swagger tip:** Click **Authorize** → enter `Bearer <token>` (get token from `POST /api/auth/login` first)

### H2 Console credentials
```
JDBC URL : jdbc:h2:mem:clubdb
Username : sa
Password : (leave empty)
```

### PostgreSQL (optional)
Uncomment the PostgreSQL lines in `application.properties` and comment out the H2 section.

---

## Sample Credentials (auto-seeded on startup)

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `terim.fatih` | `terim123` | TECHNICAL_DIRECTOR (Fatih Terim) |
| `ercan.abdullah` | `assist123` | ASSISTANT_COACH |
| `muslera.fernando` | `player123` | PLAYER |
| `icardi.mauro` | `player123` | PLAYER |
| `torreira.lucas` | `player123` | PLAYER |
| `ziyech.hakim` | `player123` | PLAYER |
| `akturkoglu.kerem` | `player123` | PLAYER |

> All player accounts follow the format `lastname.firstname@gsapp.com`

---

## API Endpoints Overview

### Authentication (public)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login → JWT token |

### Dashboard (all roles)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/dashboard` | Personalized daily view |

### Players
| Method | Endpoint | Roles |
|---|---|---|
| GET | `/api/players` | All (supports ?position=&nationality=&name=&page=&size=) |
| GET | `/api/players/{id}` | All |
| POST | `/api/players` | ADMIN |
| PUT | `/api/players/{id}` | ADMIN, TECHNICAL_DIRECTOR |
| PATCH | `/api/players/{id}/condition` | PLAYER (self) |
| DELETE | `/api/players/{id}` | ADMIN |
| GET | `/api/players/export/csv` | ADMIN, TECHNICAL_DIRECTOR |

### Matches
| Method | Endpoint | Roles |
|---|---|---|
| GET | `/api/matches` | All (supports ?status=&from=&to=&opponent=) |
| GET | `/api/matches/upcoming` | All |
| GET | `/api/matches/{id}` | All |
| POST | `/api/matches` | ADMIN, TECHNICAL_DIRECTOR |
| PUT | `/api/matches/{id}` | ADMIN, TECHNICAL_DIRECTOR |
| DELETE | `/api/matches/{id}` | ADMIN |
| GET | `/api/matches/export/csv` | ADMIN, TECHNICAL_DIRECTOR |

### Training Sessions
| Method | Endpoint | Roles |
|---|---|---|
| GET | `/api/training-sessions` | All |
| GET | `/api/training-sessions/today` | All |
| GET | `/api/training-sessions/week` | All |
| POST | `/api/training-sessions` | ADMIN, TD, ASSISTANT |
| PUT | `/api/training-sessions/{id}` | ADMIN, TD, ASSISTANT |
| DELETE | `/api/training-sessions/{id}` | ADMIN, TD |

### Player Tasks
| Method | Endpoint | Roles |
|---|---|---|
| GET | `/api/tasks` | All (PLAYER sees own only) |
| POST | `/api/tasks` | ADMIN, TD, ASSISTANT |
| PATCH | `/api/tasks/{id}/complete` | PLAYER |
| PATCH | `/api/tasks/{id}/status` | All |
| DELETE | `/api/tasks/{id}` | ADMIN |

### Daily Meals
| Method | Endpoint | Roles |
|---|---|---|
| GET | `/api/meals` | All |
| GET | `/api/meals/today` | All |
| GET | `/api/meals/date/{date}` | All |
| POST | `/api/meals` | ADMIN, TD, ASSISTANT |
| PUT | `/api/meals/{id}` | ADMIN, TD, ASSISTANT |
| DELETE | `/api/meals/{id}` | ADMIN |

### Announcements
| Method | Endpoint | Roles |
|---|---|---|
| GET | `/api/announcements` | All (filtered by role) |
| GET | `/api/announcements/all` | ADMIN |
| POST | `/api/announcements` | ADMIN, TD |
| PUT | `/api/announcements/{id}` | ADMIN, TD |
| DELETE | `/api/announcements/{id}` | ADMIN |

### Injury Reports
| Method | Endpoint | Roles |
|---|---|---|
| GET | `/api/injuries` | All (PLAYER sees own) |
| POST | `/api/injuries` | All |
| PATCH | `/api/injuries/{id}/status` | ADMIN, TD, ASSISTANT |
| DELETE | `/api/injuries/{id}` | ADMIN |

### Leave Requests
| Method | Endpoint | Roles |
|---|---|---|
| GET | `/api/leave-requests` | All (PLAYER sees own) |
| POST | `/api/leave-requests` | All |
| PUT | `/api/leave-requests/{id}/review` | ADMIN, TD |
| DELETE | `/api/leave-requests/{id}` | ADMIN |

### User Management (ADMIN only)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/admin/users` | List all users |
| POST | `/api/admin/users` | Create user |
| PUT | `/api/admin/users/{id}` | Update user |
| DELETE | `/api/admin/users/{id}` | Soft-delete user |
| PUT | `/api/admin/users/{id}/restore` | Restore deleted user |

---

## Running Tests

```bash
mvn test
```

### Test Coverage
- `AuthServiceTest` — unit tests for registration/login logic
- `PlayerServiceTest` — unit tests for CRUD and business rules
- `AuthControllerIntegrationTest` — integration tests against live H2 database

---

## PDF Requirement Checklist

| Requirement | Status |
|---|---|
| Java 17+ | ✅ Java 17 |
| Spring Boot 3.x | ✅ 3.3.5 |
| Spring Data JPA | ✅ |
| Spring Security + JWT | ✅ BCrypt + JJWT |
| Role-Based Access (2+ roles) | ✅ 4 roles: ADMIN, TECHNICAL_DIRECTOR, ASSISTANT_COACH, PLAYER |
| 3+ main entities with CRUD | ✅ 8 entities |
| DTOs (no entity exposure) | ✅ Request + Response DTOs for all |
| Bean Validation | ✅ @NotNull, @NotBlank, @Email, @Size, @Min/@Max |
| Global Exception Handling | ✅ @ControllerAdvice |
| OpenAPI/Swagger | ✅ /swagger-ui.html |
| Unit Tests (service layer) | ✅ PlayerServiceTest, AuthServiceTest |
| Integration Tests | ✅ AuthControllerIntegrationTest |
| H2 / PostgreSQL | ✅ H2 default, PostgreSQL config available |
| **Additional: Pagination & Sorting** | ✅ All list endpoints |
| **Additional: Advanced Search & Filtering** | ✅ Players, Matches |
| **Additional: Soft Delete** | ✅ All entities |
| **Additional: Audit Trail** | ✅ BaseAuditEntity (createdAt, updatedAt) |
| **Additional: Export (CSV)** | ✅ Players and Matches |
| **Additional: Advanced Security** | ✅ Account lockout after 5 failed attempts |
