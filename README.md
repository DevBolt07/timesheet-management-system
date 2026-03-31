# Staff Timesheet Management System

An industry-oriented full-stack timesheet module for academic ERP workflows. The system is designed for college staff to log daily work entries, track activity history, and support an approval flow that can later be extended for HOD and admin operations.

## Overview
This project is structured as a decoupled frontend and backend application:

- `frontend`: Vue 3 application for dashboard, timesheet entry, and history screens
- `backend`: Grails 6 REST API for timesheet management, validation, and persistence

The current implementation already covers the core timesheet domain and CRUD flow. The next major milestone is replacing frontend mock state with live API integration and then extending the module into a full role-based workflow.

## Core Features
- Staff dashboard with quick metrics
- Timesheet entry form with client-side validation
- Timesheet history view
- Grails REST API for create, read, update, and delete operations
- MySQL persistence through GORM domain models
- Business validation for invalid time ranges and overlapping time entries
- Domain foundation for role-based workflow through `User`, `TaskType`, and `Timesheet`

## Tech Stack
- Frontend: Vue 3, Vite, Pinia, Vue Router, Vanilla CSS
- Backend: Grails 6, Groovy, Spring Boot REST profile
- Database: MySQL 8, GORM

## Project Structure
```text
timesheet/
|-- frontend/
|   |-- src/
|   |   |-- assets/
|   |   |-- components/
|   |   |-- layouts/
|   |   |-- router/
|   |   |-- stores/
|   |   `-- views/
|   |-- package.json
|   `-- vite.config.js
|-- backend/
|   |-- grails-app/
|   |   |-- conf/
|   |   |-- controllers/
|   |   |-- domain/
|   |   `-- services/
|   |-- src/
|   |-- build.gradle
|   `-- gradlew.bat
|-- .gitignore
`-- README.md
```

## Current Functional Scope
### Frontend
- Dashboard page
- Log Time page
- History page
- Pinia-based mock state for timesheet entries

### Backend
- `Timesheet` domain model
- `User` and `TaskType` supporting models
- REST endpoints for timesheet CRUD
- Service-layer validation for:
  - end time must be after start time
  - overlapping entries are rejected

## API Endpoints
| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/timesheets` | Fetch all timesheet entries |
| `GET` | `/api/timesheets/{id}` | Fetch a single timesheet entry |
| `POST` | `/api/timesheets` | Create a new timesheet entry |
| `PUT` | `/api/timesheets/{id}` | Update a pending timesheet entry |
| `DELETE` | `/api/timesheets/{id}` | Delete a pending timesheet entry |

## Local Setup
### Prerequisites
- Node.js 20+
- Java JDK 11 or 17
- MySQL 8+

### 1. Create the database
Open MySQL and create the project database:

```sql
CREATE DATABASE timesheet_db;
```

### 2. Configure backend database access
Update the MySQL connection values in:

`backend/grails-app/conf/application.yml`

Make sure the configured username, password, and database name match your local MySQL setup before starting the backend.

## Run the Project
Use two separate terminals: one for the backend and one for the frontend.

### Start the backend
1. Open a terminal in the project root
2. Move to the backend folder:

```bash
cd backend
```

3. Start the Grails application:

```bash
gradlew.bat bootRun
```

4. Wait until the application finishes booting
5. Confirm the API is available at:

```text
http://localhost:8080
```

Example API endpoint:

```text
http://localhost:8080/api/timesheets
```

### Start the frontend
1. Open a second terminal in the project root
2. Move to the frontend folder:

```bash
cd frontend
```

3. Install dependencies if this is your first run:

```bash
npm install
```

4. Start the Vite development server:

```bash
npm run dev
```

5. Open the frontend in the browser:

```text
http://localhost:5173
```

## Quick Start Commands
If you are already inside the project root, the fastest startup flow is:

### Terminal 1
```bash
cd backend
gradlew.bat bootRun
```

### Terminal 2
```bash
cd frontend
npm install
npm run dev
```

## Run Checklist
Before testing the project, make sure:

- MySQL is running
- `timesheet_db` exists
- backend credentials in `application.yml` are correct
- backend is running on `http://localhost:8080`
- frontend is running on `http://localhost:5173`

## Troubleshooting
- If the backend does not start, first verify Java and MySQL are installed and running correctly
- If database connection fails, recheck `backend/grails-app/conf/application.yml`
- If the frontend starts but shows no live backend data, that is currently expected because the frontend still uses mock Pinia state
- If `npm install` fails, confirm that Node.js 20+ is installed

## Current Status
- Frontend UI is implemented and navigable
- Frontend still uses mock Pinia state instead of live API calls
- Backend CRUD and domain validation are implemented
- Role-based authentication and approval workflow are not implemented yet

## Recommended Next Milestones
1. Replace mock Pinia data with live API integration
2. Add centralized API client and error handling in the frontend
3. Implement edit and delete actions in the history screen
4. Add authentication and role-based access for `STAFF` and `HOD`
5. Build HOD approval and rejection workflow
6. Add test coverage for service validation and API behavior

## Domain Direction
This module is intended to evolve into a broader ERP-style staff productivity and approval system for educational institutions. The current codebase should be treated as the foundation for:

- staff self-service logging
- department-level review and approval
- auditability of submitted work hours
- future reporting and anomaly detection

## Notes
- The root project contains `frontend` and `backend` folders. Older names such as `timesheet-app` and `timesheet-api` are not used in this repository.
- The backend currently creates fallback demo data for user and task type where required. This is temporary until authentication and seeded master data are added.

## License
This project is currently intended for academic and industry demonstration purposes.
