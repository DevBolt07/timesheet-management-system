# ⏱️ Staff Timesheet Management System

## 📖 Overview
An industry-grade, full-stack Timesheet Management System designed for academic and enterprise demonstration. This platform enables Staff to seamlessly log their daily working hours, categorize tasks, and submit their timesheets for Head of Department (HOD) review. Built with a focus on clean architecture, strict data validation, and rapid responsive UI design.

## ✨ Features
- **Timesheet Logging**: Intuitive interface for staff to log daily tasks with precision timing.
- **History Tracking**: Comprehensive, searchable history of all submitted and pending timesheets.
- **Strict Validations**: Backend architectural constraints prevent time overlapping and reverse chronological time entries.
- **Role-Based Architecture**: (Planned) Strict separation of concerns between `STAFF`, `HOD`, and `ADMIN`.
- **Responsive Dashboard**: Real-time aggregation of logged hours and tasks.

## 🛠️ Tech Stack
- **Frontend**: Vue 3 (Composition API), Vite, Pinia, Vue Router, Vanilla CSS
- **Backend**: Grails 6, Groovy, Spring Boot (REST API Profile)
- **Database**: MySQL 8, GORM (Grails Object Relational Mapping)

## 🏗️ Architecture
The system follows a strict decoupled Client-Server architecture:
1. **Client Layer (Vue 3)**: Handles client-side validations, state management (Pinia), and dynamic routing.
2. **API Layer (Grails 6)**: Exposes stateless RESTful JSON endpoints. Uses Services for heavy business logic (e.g., overlap detection).
3. **Data Layer (MySQL)**: Normalized relational database managed entirely via GORM domain modeling.

## 📂 Project Structure
```text
timesheet/
├── timesheet-app/          # Frontend Vue 3 application
│   ├── src/components/     # Reusable UI widgets
│   ├── src/stores/         # Pinia state management
│   └── src/views/          # Page-level smart components
└── timesheet-api/          # Backend Grails 6 application
    ├── grails-app/controllers/ # REST API endpoints
    ├── grails-app/domain/      # GORM database schema
    └── grails-app/services/    # Business logic & validations
```

## 🚀 Setup Instructions

### Prerequisites
- Node.js (v20+)
- Java JDK 11 or 17
- MySQL Server 8+

### 1. Database Setup
Ensure MySQL is running and create the database:
```sql
CREATE DATABASE timesheet_db;
```

### 2. Backend Setup (Grails 6 API)
```bash
cd timesheet-api
# The application.yml is already configured for MySQL connections
# Run the Grails development server (Defaults to http://localhost:8080)
./gradlew bootRun
```

### 3. Frontend Setup (Vue 3 UI)
```bash
cd timesheet-app
# Install dependencies
npm install
# Start the Vite development server (Defaults to http://localhost:5173)
npm run dev
```

## 🔌 API Endpoints (Timesheet CRUD)
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/timesheets` | Create a new timesheet entry |
| `GET` | `/api/timesheets` | Retrieve all timesheet entries |
| `GET` | `/api/timesheets/{id}` | Retrieve a specific entry |
| `PUT` | `/api/timesheets/{id}` | Update an entry (Only if status is `PENDING`) |
| `DELETE` | `/api/timesheets/{id}` | Delete an entry (Only if status is `PENDING`) |

## 📊 Current Status
- **Phase 1 (Complete)**: Frontend UI polished, mock Pinia state functioning, responsive CSS complete.
- **Phase 2 (Complete)**: Grails REST API scaffolded, MySQL configured, Domain Models built, and CRUD Controllers/Services active.
- **Phase 3 (Next)**: Wiring Axios in the Vue frontend to replace Pinia mock data with live Grails API calls.

## 🔮 Future Improvements
- **JWT Authentication**: Secure the API and replace mock active users with real tokenized sessions.
- **HOD Approval Workflow**: Enable the HOD dashboard to batch approve/reject staff submissions.
- **🔥 Anomaly Detection**: Implement a smart algorithm to flag unusual logging patterns (e.g., excessive overtime, impossible task overlaps).

## 📸 Screenshots
*(Add screenshots of Dashboard, Log Time form, and History table here)*

---
*Developed for industry-oriented academic demonstration.*
