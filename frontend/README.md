# Frontend - Collegiate ERP Suite Timesheet Module

This directory contains the Vue 3 single-page application for the Timesheet Management module. It provides role-aware interfaces for staff, HOD/manager, and admin users inside the broader Collegiate ERP Suite.

## Frontend Responsibilities
The frontend currently handles:

- demo login flow for seeded accounts
- role-aware route access and navigation
- staff timesheet entry and history
- HOD review workflow for pending submissions
- admin dashboard and oversight screens
- task master interactions
- operational filters and summary views

## Technology Stack
- Vue 3
- Vite
- Pinia
- Vue Router
- Vanilla CSS

## Application Areas
### Staff
- Login using seeded staff credentials
- Add timesheet entries
- View personal logs
- Search and filter history
- Edit/delete only pending entries
- View approval status and reviewer remarks

### HOD / Manager
- Login using seeded manager credentials
- Review pending timesheets
- Approve/reject with remarks
- Filter department logs by staff, status, and date
- View staff-level summary information

### Admin
- Login using seeded admin credentials
- Access admin dashboard
- View global timesheet data
- Inspect user list and roles
- Manage task master records

## Frontend Structure
```text
frontend/
|-- src/
|   |-- assets/
|   |-- components/
|   |   `-- timesheet/
|   |-- layouts/
|   |-- router/
|   |-- services/
|   |-- stores/
|   `-- views/
|       |-- admin/
|       |-- AddTimesheetView.vue
|       |-- LandingView.vue
|       |-- ReviewTimesheetView.vue
|       `-- ViewTimesheetView.vue
|-- package.json
|-- vite.config.js
`-- README.md
```

## Key Frontend Files
- `src/views/LandingView.vue`: demo login page
- `src/layouts/MainLayout.vue`: shared ERP shell and sidebar
- `src/views/AddTimesheetView.vue`: staff timesheet entry screen
- `src/views/ViewTimesheetView.vue`: staff/HOD logs and filters
- `src/views/ReviewTimesheetView.vue`: HOD approval workspace
- `src/views/admin/`: admin dashboard and admin operations
- `src/stores/authStore.js`: auth/session context
- `src/stores/timesheetStore.js`: timesheet and task state management
- `src/services/api.js`: centralized API client

## Demo Login Credentials
Use the following seeded credentials after both frontend and backend are running:

| Role | Username | Password |
|---|---|---|
| Staff | `dipak.pawar` | `staff123` |
| Staff | `kavita.deshmukh` | `staff123` |
| Staff | `rahul.verma` | `staff123` |
| HOD / Manager | `dr.sharma` | `manager123` |
| Admin | `system.admin` | `admin123` |

## Running the Frontend
### 1. Open terminal in the frontend directory
```bash
cd frontend
```

### 2. Install dependencies
```bash
npm install
```

### 3. Start the development server
```bash
npm run dev
```

### 4. Open the app
```text
http://localhost:5173
```

## Backend Connection
The frontend expects the Grails backend to be running locally on:

```text
http://localhost:8080
```

If the backend port changes, update the API/proxy configuration accordingly in:

- `vite.config.js`
- any environment-based API configuration you introduce later

## Frontend Development Notes
- The frontend is currently wired to live backend APIs.
- Role navigation is based on resolved authenticated demo user context.
- Admin and HOD views are protected by route guards and backend authorization.
- UI patterns are intentionally kept institutional and ERP-oriented instead of flashy.

## Current Frontend Status
### Implemented
- demo login screen
- role-based routing
- staff timesheet form
- timesheet history filters
- HOD approval screen
- reviewer remarks visibility
- staff-wise HOD filters
- admin dashboard and admin screens

### Planned Next
- leave management module UI
- stronger analytics/reporting screens
- production-oriented environment configuration
- replacement of demo auth flow with real ERP auth integration

## Troubleshooting
- If login works but data looks empty, confirm the backend is running and seeded data loaded correctly.
- If the app cannot fetch data, verify the backend is available at `http://localhost:8080`.
- If role navigation looks wrong after switching users, log out and sign back in to refresh the auth context.
