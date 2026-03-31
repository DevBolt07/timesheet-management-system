# Timesheet Management System - Backend API

This is the Grails 6 REST API profile backend for the College ERP Timesheet module.

## Tech Stack
- **Framework:** Grails 6.1.0 (REST API Profile)
- **Language:** Groovy / Java
- **Database:** MySQL 8+
- **ORM:** GORM (Grails Object Relational Mapping)

## Setup & Running the Application

### 1. Database Configuration
Ensure MySQL is running on your local machine on port `3306`. Create the database:
```sql
CREATE DATABASE timesheet_db;
```
*(Update `grails-app/conf/application.yml` with your local MySQL username and password if it differs from the defaults).*

### 2. Starting the Backend Server
Open a terminal in the `backend` directory and run the Gradle boot wrapper:
```bash
# On Windows
.\gradlew.bat bootRun

# On Mac/Linux
./gradlew bootRun
```
The server will start at: `http://localhost:8080`

---

## Implemented API Endpoints

The system relies on a set of RESTful APIs to connect with the Vue.js frontend. All responses are returned in `application/json`.

### 1. Task Types API
Used to dynamically fetch available administrative and academic task types for the frontend form dropdowns.

- **URL:** `/api/taskTypes`
- **Method:** `GET`
- **Response:**
  ```json
  [
    {"id": 1, "name": "Academic Planning"},
    {"id": 2, "name": "Student Mentoring"}
  ]
  ```

### 2. Timesheet API (CRUD)

#### 2.1 Get All Timesheet Logs
Fetches all timesheet records for the current user.

- **URL:** `/api/timesheets`
- **Method:** `GET`
- **Response:**
  ```json
  [
    {
      "id": 1,
      "date": "2026-03-30",
      "startTime": "09:00:00",
      "endTime": "11:30:00",
      "task": "Academic Planning",
      "description": "Prepared syllabus for next semester.",
      "status": "PENDING"
    }
  ]
  ```

#### 2.2 Create a New Timesheet
Logs a new timesheet entry. Validations prevent future-dated entries and overlap.

- **URL:** `/api/timesheets`
- **Method:** `POST`
- **Payload:**
  ```json
  {
      "date": "2026-03-30",
      "startTime": "09:00:00",
      "endTime": "11:30:00",
      "task": "Academic Planning",
      "description": "Prepared syllabus for next semester."
  }
  ```

#### 2.3 Update an Existing Timesheet
Updates a specific timesheet entry provided the status is still `PENDING`.

- **URL:** `/api/timesheets/{id}`
- **Method:** `PUT`
- **Payload:** *(Same as POST)*

#### 2.4 Delete a Timesheet
Removes a specific timesheet entry provided the status is still `PENDING`.

- **URL:** `/api/timesheets/{id}`
- **Method:** `DELETE`
- **Response:** `204 No Content` on success.

---

## Domain Models
The backend logic is driven by the following GORM domain classes located in `grails-app/domain/timesheet/api/`:
- **`User`**: Base user entity to store staff/manager credentials.
- **`TaskType`**: Master data table supplying tasks to dropdowns.
- **`Timesheet`**: The primary transaction table storing staff logged hours, heavily validated via domain constraints.

## Future Developments
- Implementing Spock unit tests for `TimesheetService`.
- Integrating Spring Security REST for JWT-based auth and assigning users strictly via token.
- Building the `/api/manager/timesheets` endpoints for HOD bulk approvals.
