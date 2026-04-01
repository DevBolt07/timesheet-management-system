# Collegiate ERP Suite - Timesheet Management Module

An industry-oriented ERP module for college staff productivity tracking, managerial approvals, and administrative oversight. This project is being built as a reusable sub-system that can later integrate into a larger institutional ERP or college website platform.

## Project Overview
This repository currently contains a fully working Timesheet Management module with role-aware workflows for:

- `STAFF` users who log and manage their own timesheet entries
- `HOD` users who review, approve, and reject staff submissions
- `ADMIN` users who oversee timesheet data, users, and task master records

The project is structured as a decoupled full-stack application:

- `frontend`: Vue 3 + Vite single-page application
- `backend`: Grails 6 REST API
- `database`: MySQL 8

For implementation-specific details, refer to:

- `frontend/README.md`
- `backend/README.md`

## Current Functional Scope
### 1. Staff Workflow
- Secure demo login using seeded user accounts
- Add new timesheet entries
- 12-hour time entry with AM/PM support
- Real-time and backend validation for date, time, task, and description
- Overlap protection against conflicting time entries
- View personal timesheet history
- Search and filter personal logs
- Edit or delete only `PENDING` entries
- View final status (`PENDING`, `APPROVED`, `REJECTED`)
- View reviewer remarks on reviewed entries

### 2. HOD / Manager Workflow
- Secure demo login using seeded HOD account
- Dedicated review workspace for pending approvals
- Approve or reject timesheet entries
- Add reviewer remarks during approval workflow
- Department-style logs view
- Staff-wise filtering support
- Staff-level summary visibility for reporting and review

### 3. Admin Workflow
- Secure demo login using seeded admin account
- Admin dashboard with system-level metrics
- Full timesheet oversight across users
- User directory / role visibility
- Task master management
- Task usage visibility and protected delete behavior

## Key Features Implemented
- Role-aware seeded demo login
- Backend identity resolution abstraction
- Staff / HOD / Admin navigation separation
- Task master seeding and controlled task creation
- Safe task delete / deactivate behavior
- Review workflow with remarks
- Consistent JSON API response handling
- AM/PM-friendly display in logs and review flows
- Search, status, date, and staff filters in operational views
- Admin governance layer for system oversight

## Technology Stack
- Frontend: Vue 3, Vite, Pinia, Vue Router, Vanilla CSS
- Backend: Grails 6, Groovy, Spring Boot REST profile
- Database: MySQL 8, GORM

## Repository Structure
```text
timesheet/
|-- frontend/
|   |-- src/
|   |   |-- assets/
|   |   |-- components/
|   |   |-- layouts/
|   |   |-- router/
|   |   |-- services/
|   |   |-- stores/
|   |   `-- views/
|   |       `-- admin/
|   |-- package.json
|   `-- vite.config.js
|-- backend/
|   |-- grails-app/
|   |   |-- conf/
|   |   |-- controllers/
|   |   |-- domain/
|   |   |-- init/
|   |   `-- services/
|   |-- src/
|   |-- build.gradle
|   `-- gradlew.bat
|-- .gitignore
`-- README.md
```

## Demo Login Credentials
The current build uses seeded demo accounts for realistic role-based testing.

| Role | Username | Password |
|---|---|---|
| Staff | `dipak.pawar` | `staff123` |
| Staff | `kavita.deshmukh` | `staff123` |
| Staff | `rahul.verma` | `staff123` |
| HOD / Manager | `dr.sharma` | `manager123` |
| Admin | `system.admin` | `admin123` |

## Local Setup
### Prerequisites
- Node.js 20+
- Java JDK 11 or 17
- MySQL 8+

### 1. Create the database
```sql
CREATE DATABASE timesheet_db;
```

### 2. Configure backend database access
Update the MySQL connection details in:

`backend/grails-app/conf/application.yml`

Make sure username, password, host, and database name match your local MySQL setup.

## Run the Project
Use two terminals.

### Backend
```bash
cd backend
gradlew.bat bootRun
```

Backend default URL:

```text
http://localhost:8080
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

Frontend default URL:

```text
http://localhost:5173
```

## Core API Areas
### Authentication
- `POST /api/auth/demo-login`

### Timesheets
- `GET /api/timesheets`
- `GET /api/timesheets/{id}`
- `POST /api/timesheets`
- `PUT /api/timesheets/{id}`
- `DELETE /api/timesheets/{id}`
- `PUT /api/timesheets/{id}/review`

### Task Master
- `GET /api/taskTypes`
- `POST /api/taskTypes`
- `DELETE /api/taskTypes/{id}`

### Admin
- `GET /api/admin/metrics`
- `GET /api/admin/timesheets`
- `GET /api/admin/users`
- `GET /api/admin/tasks`
- `POST /api/admin/tasks`
- `DELETE /api/admin/tasks/{id}`

## Current Implementation Status
### Completed
- Staff timesheet entry workflow
- Staff history and filtering
- Pending-only edit/delete rules
- HOD approval/rejection workflow
- Reviewer remarks flow
- Seeded demo login with multiple accounts
- Admin dashboard and oversight foundation
- Task master control
- Role-aware navigation and route protection

### In Progress / Planned Next
- Leave Management module
- Broader admin configuration modules
- Better reporting and analytics
- Integration-ready auth replacement for real ERP SSO/JWT
- Deployment/staging configuration hardening
- Automated service/controller test coverage

## Business Workflow Summary
1. Staff logs a timesheet entry
2. Entry is stored in `PENDING` status
3. Staff can edit/delete only while it remains pending
4. HOD reviews pending entries
5. HOD approves or rejects with optional remarks
6. Final status is reflected in staff history
7. Admin can oversee the full system state

## Notes for Review
- This is a role-aware ERP module prototype intended for institutional integration, not a standalone public consumer app.
- Demo authentication is intentionally seeded and controlled for evaluation and workflow testing.
- The architecture is being shaped so the current identity layer can later be replaced with the company's real authentication system.

## Recommended Next Phase
The next major module planned after stabilizing the current Timesheet system is:

- `Leave Management`

That module will reuse the same staff / HOD / admin workflow foundation established here.

## License / Usage
This repository is currently intended for academic, demo, and industry-review purposes.
