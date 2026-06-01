# 🎫 Enterprise Ticket Management System

A full-stack ticket management application with Angular (frontend), Spring Boot (backend), MySQL (database), and TestNG (testing).

## Features

### Ticket Submission
- Simple form with category selection (IT, Bug, HR) so tickets are routed correctly
- Attachment support (upload screenshots, logs, docs)
- Auto-fill suggestions based on common issues per category
- "Similar tickets" prompt before submission to reduce duplicates

### Ticket Tracking & Transparency
- Personal employee dashboard with status filters (Open / In Progress / Resolved)
- In-app notifications on status changes
- Estimated resolution time per ticket based on category & priority
- Timeline view showing every step on the ticket

### Prioritization & Routing
- Auto-priority tagging based on keywords (`outage` → URGENT, `password reset` → LOW)
- Department-based auto-routing (IT, Bug, HR → appropriate admin)
- Escalation rules: tickets unresolved beyond 24h are flagged automatically (runs every 15 min)

### HR-Specific
- Confidential flag — visible only to creator, assigned agent, and HR admins
- Predefined HR request types (Leave, Payroll, Policy, etc.)

### Bug-Specific
- Environment fields (OS, browser, app version) auto-detected
- Severity self-assignment (Low / High / Critical)
- Link related bug tickets

### IT-Specific
- Asset tag field
- Knowledge base panel surfacing relevant FAQs while composing

### Admin & Analytics
- Reporting dashboard: tickets by category, status, agent workload, avg. resolution time
- Satisfaction ratings (👍 / 👎) shown after resolution
- User management (delete users, view all)

### Auth
- Two user types: **Employee** and **Admin** — both sign up and log in
- JWT-based stateless authentication
- Admins can manage everything; employees only see their own tickets (and non-confidential ones)

---

## Tech Stack

| Layer    | Technology                                    |
|----------|-----------------------------------------------|
| Frontend | Angular 17, TypeScript, RxJS                  |
| Backend  | Spring Boot 3.2, Java 17, Spring Security, JWT|
| Database | MySQL 8                                       |
| Testing  | TestNG                                        |

---

## Project Structure

```
ticket-system/
├── backend/                Spring Boot application
│   ├── pom.xml
│   ├── src/main/java/com/ticketsystem/
│   │   ├── TicketSystemApplication.java
│   │   ├── config/         JWT, security, CORS, data seeder
│   │   ├── controller/     REST endpoints
│   │   ├── dto/            Request/response objects
│   │   ├── exception/      Global handler
│   │   ├── model/          JPA entities + enums
│   │   ├── repository/     Spring Data JPA repositories
│   │   └── service/        Business logic
│   ├── src/main/resources/application.properties
│   └── src/test/           TestNG tests
├── frontend/               Angular application
│   ├── package.json
│   ├── angular.json
│   ├── src/app/
│   │   ├── components/     Each feature has its own component
│   │   ├── services/       Auth, ticket, admin, notification, knowledge
│   │   ├── guards/         Route guards (auth, admin)
│   │   ├── interceptors/   JWT auth interceptor
│   │   ├── models/         TypeScript types
│   │   ├── app.module.ts
│   │   └── app.routes.ts
│   └── src/environments/
└── database/
    └── schema.sql          Reference MySQL schema
```

---

## Setup

### 1. Prerequisites

| Tool           | Version    |
|----------------|------------|
| Java           | 17 or 21   |
| Maven          | 3.6+       |
| Node.js        | 18+        |
| Angular CLI    | 17 (`npm i -g @angular/cli`) |
| MySQL          | 8.0+       |

### 2. Database

Make sure MySQL is running on `localhost:3306`. The app will create the `ticket_system` database automatically on first startup (`createDatabaseIfNotExist=true` in JDBC URL).

If you want to set it up manually:

```bash
mysql -u root -p < database/schema.sql
```

Default JDBC credentials (edit `backend/src/main/resources/application.properties` if yours differ):
- URL: `jdbc:mysql://localhost:3306/ticket_system`
- Username: `root`
- Password: `root`

### 3. Run the Backend

```bash
cd backend
mvn spring-boot:run
```

The API will be on `http://localhost:8080/api`.

On first boot, the seeder creates these demo users:

| Email                    | Password    | Role     | Department |
|--------------------------|-------------|----------|------------|
| it.admin@company.com     | admin123    | ADMIN    | IT         |
| hr.admin@company.com     | admin123    | ADMIN    | HR         |
| john@company.com         | password123 | EMPLOYEE | Engineering|

It also seeds 5 knowledge base articles.

### 4. Run the Frontend

```bash
cd frontend
npm install
npm start
```

App opens at `http://localhost:4200`.

### 5. Run TestNG Tests

```bash
cd backend
mvn test
```

Tests use an in-memory H2 database (no MySQL needed for testing).

---

## API Overview

All endpoints prefixed with `/api`. Protected endpoints require `Authorization: Bearer <JWT>`.

### Auth (public)
- `POST /auth/signup` — `{ name, email, password, role, department }`
- `POST /auth/login` — `{ email, password }` → `{ token, userId, name, role, ... }`

### Tickets (authenticated)
- `POST /tickets` — create
- `GET /tickets` — list (employees see their own; admins see all)
- `GET /tickets/assigned` — agent's assigned tickets
- `GET /tickets/{id}` — view (enforces confidential rules)
- `GET /tickets/{id}/timeline` — full history
- `GET /tickets/similar?title=...&category=IT` — duplicate suggestions
- `PUT /tickets/{id}/status` — update status (agent/admin only)
- `PUT /tickets/{id}/rate` — satisfaction rating (`rating`: 1 or -1)
- `POST /tickets/{id}/attachments` — upload file
- `GET /tickets/{id}/attachments` — list
- `GET /tickets/attachments/download/{name}` — download

### Admin (admin only)
- `GET /admin/analytics` — dashboard stats
- `GET /admin/users` — all users
- `GET /admin/agents` — admins available for assignment
- `PUT /admin/tickets/{id}/assign` — reassign
- `DELETE /admin/users/{id}` — remove a user

### Knowledge Base
- `GET /knowledge` — all articles
- `GET /knowledge/suggest?query=&category=` — smart search
- `POST /knowledge` (admin) — create
- `DELETE /knowledge/{id}` (admin) — delete

### Notifications
- `GET /notifications` — list
- `GET /notifications/unread` — unread only
- `PUT /notifications/{id}/read` — mark one read
- `PUT /notifications/read-all` — mark all

### Users
- `GET /users/me` — current user
- `GET /users/common-issues` — for autofill suggestions

---

## How Key Features Work

**Auto-priority** — `PriorityService` scans the title + description for keyword patterns. Bug tickets with explicit severity override keyword detection.

**Routing** — `RoutingService` matches the category to a department (IT/BUG → IT, HR → HR) and picks the admin in that department with the fewest open tickets.

**Escalation** — `EscalationService` runs every 15 minutes (`@Scheduled`) and flags tickets unresolved past 24 hours, notifying both the agent and creator.

**Similar tickets** — As the user types the title, the frontend debounces (400ms) and calls `/tickets/similar`, showing matches in the sidebar. Before final submit, if matches exist, the user sees a "Did you mean one of these?" confirmation step.

**Confidential tickets** — Only the creator, the assigned agent, or an HR admin can view confidential tickets. Enforced in `TicketService.getTicket()`.

**Notifications** — Backend writes `Notification` rows on status changes, assignments, and escalations. Frontend polls every 30 seconds.

---

## Customization

- **Change JWT secret**: edit `app.jwt.secret` in `application.properties` (must be base64-encoded, at least 256 bits)
- **Change DB credentials**: `spring.datasource.username` / `password`
- **Change priority keywords**: edit `PriorityService.URGENT_KEYWORDS` etc.
- **Change escalation window**: edit the `LocalDateTime.now().minusHours(24)` in `EscalationService`
- **Change API base URL**: edit `frontend/src/environments/environment.ts`

---

## Troubleshooting

**"Access denied for user 'root'@'localhost'"** — Update `spring.datasource.password` to match your local MySQL password.

**CORS errors** — Ensure backend is on port 8080 and frontend on 4200. CORS is configured globally in `SecurityConfig`.

**Tokens expiring** — JWT lifetime is 24h. Adjust `app.jwt.expiration` (milliseconds).

**Port already in use** — Change `server.port` in `application.properties` and the `apiUrl` in `environment.ts` accordingly.

---
Email                      Password         Role                  Department
it.admin@company.com       admin123         ADMIN                     IT               
hr.admin@company.com       admin123         ADMIN                     HR 
john@company.com           password123      EMPLOYEE                  Engineering
## License

MIT
