# Backend - Collegiate ERP Suite Timesheet API

This directory contains the Grails 6 REST API for the Timesheet Management module. It powers staff entry workflows, HOD approvals, admin oversight, task master management, and demo identity resolution.

## Backend Responsibilities
The backend currently handles:

- seeded demo users and task master data
- demo login endpoint for role-aware session context
- identity resolution for current user context
- role-based authorization for staff, HOD, and admin
- timesheet CRUD operations
- approval/rejection workflow
- reviewer remarks persistence
- task master management
- admin metrics, oversight, and user visibility

## Technology Stack
- Grails 6
- Groovy
- Spring Boot REST profile
- GORM
- MySQL 8

## Core Domain Models
- `User`: seeded staff, HOD, and admin accounts
- `TaskType`: task master categories used by timesheets
- `Timesheet`: transactional timesheet records with workflow status

## Workflow Rules Enforced
### Staff
- can create timesheets
- can view only their own logs
- can edit/delete only pending entries

### HOD
- can review pending timesheets
- can approve/reject with remarks
- can access department/global review-oriented logs as currently configured

### Admin
- can access admin-only metrics and oversight endpoints
- can inspect users and task master data
- can manage task categories safely

## API Areas
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

### Staff Directory Support
- endpoints used to support HOD/admin reporting and filtering where implemented

## Demo Seeded Accounts
These users are seeded through bootstrap logic and intended for development/demo use:

| Role | Username | Password |
|---|---|---|
| Staff | `dipak.pawar` | `staff123` |
| Staff | `kavita.deshmukh` | `staff123` |
| Staff | `rahul.verma` | `staff123` |
| HOD / Manager | `dr.sharma` | `manager123` |
| Admin | `system.admin` | `admin123` |

## Database Setup
### 1. Create MySQL database
```sql
CREATE DATABASE timesheet_db;
```

### 2. Configure datasource
Update:

`grails-app/conf/application.yml`

Make sure the following match your local environment:
- database host
- port
- username
- password
- database name

## Running the Backend
### Windows
```bash
cd backend
gradlew.bat bootRun
```

### macOS / Linux
```bash
cd backend
./gradlew bootRun
```

Backend default URL:

```text
http://localhost:8080
```

## Important Development Note
Bootstrap-based seed data runs during application startup. If you change:

- seeded users
- demo credentials
- task master defaults
- bootstrap logic

restart the backend so the changes are applied.

## Backend Validation Highlights
- required date/time/task/description checks
- future date prevention where applicable
- overlap validation for timesheet entries
- pending-only update/delete workflow
- pending-only review workflow
- task master integrity checks
- admin-only endpoint protection

## Current Backend Status
### Implemented
- seeded demo auth flow
- identity resolution service
- role-aware timesheet service/controller logic
- HOD review endpoint
- admin metrics and oversight support
- task master management

### Planned Next
- leave management domain and workflow
- better automated tests
- environment-based configuration hardening
- replacement of demo auth with real ERP auth integration

## Troubleshooting
- If login fails for seeded users, restart the backend so bootstrap user sync runs.
- If task categories or demo users appear outdated, verify bootstrap logic has run after changes.
- If the frontend cannot access the API, confirm the backend is running on port `8080`.
