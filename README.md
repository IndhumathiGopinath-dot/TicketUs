# 🎫 Enterprise Ticket Management System

A full-stack ticket management application with Angular (frontend), Spring Boot (backend), MySQL (database), and TestNG (testing).

---

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

| Layer    | Technology                                     |
|----------|------------------------------------------------|
| Frontend | Angular 17, TypeScript, RxJS                   |
| Backend  | Spring Boot 3.2, Java 17, Spring Security, JWT |
| Database | MySQL 8                                        |
| Testing  | TestNG, Selenium, Rest Assured, Cucumber       |

---
## Quick Start

### Prerequisites

- Java 21+
- Node 18+ and Angular CLI
- MySQL 8 running on `localhost:3306`
- Maven 3.9+

### Backend

```bash
cd backend
# set env vars
export DB_PASSWORD=your_mysql_password
export JWT_SECRET=any-32-char-or-longer-secret-string

mvn spring-boot:run
```

Runs on `http://localhost:8082/api`

### Frontend

```bash
cd frontend
npm install
ng serve
```

Runs on `http://localhost:4200`

### QA Suite

```bash
cd qa-tests
mvn clean test
```

See `qa-tests/README.md` for full details.

---

## Default Users (seeded on first run)

| Email                  | Password    | Role     |
|------------------------|-------------|----------|
| it.admin@company.com   | admin123    | IT Admin |
| hr.admin@company.com   | admin123    | HR Admin |
| john@company.com       | password123 | Employee |

---

## Architecture

Three-tier:

Angular SPA (4200)  →  Spring Boot REST API (8082)  →  MySQL (3306)

SOAP endpoint also exposed at `/api/ws` for aggregate queries.

---

## License

MIT
