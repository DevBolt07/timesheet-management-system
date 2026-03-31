# Timesheet Management System - Frontend SPA

This is the Vue 3 frontend for the College ERP Timesheet module. It uses modern tooling to provide a robust, responsive, institutional-grade single-page application.

## Tech Stack
- **Framework:** Vue 3 (Composition API)
- **Tooling:** Vite
- **State Management:** Pinia
- **Routing:** Vue Router
- **Styling:** Vanilla CSS (Enterprise ERP standard theme)

## Setup & Running the Application

### 1. Install Dependencies
Open a terminal in the `frontend` directory and run:
```bash
npm install
```

### 2. Configure Backend Proxy
The frontend proxies API calls strictly through Vite. If your backend is running on a port other than `8080`, ensure you update `vite.config.js`:
```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

### 3. Start Development Server
```bash
npm run dev
```
The application will be accessible at: `http://localhost:5173`

---

## Application Structure & Navigation

The interface was redesigned to match legacy-but-clean college ERP patterns:

### Views
- **`LandingView.vue` (`/`)**: A clean Role Selection page (Staff, Manager, Admin). Non-staff roles are currently visually present but disabled for future expansion.
- **`MainLayout.vue`**: The main ERP shell once a user logs in. Features a fixed top institutional banner, breadcrumbs, and a side menu.
- **`ViewTimesheetView.vue` (`/app/history`)**: Master Data view displaying logged timesheets in a classic HTML table with a functional layout (search, status filters, delete actions).
- **`AddTimesheetView.vue` (`/app/log-time`)**: Specialized entry form replicating the "Add Timesheet" UI of a traditional ERP portal.

### State Management (`stores/timesheetStore.js`)
All API calls and system states are maintained globally via Pinia.
- **`entries`**: Array containing the active user's timesheet logs.
- **`taskTypes`**: Array of administrative tasks dynamically fetched from the Grails `/api/taskTypes` endpoint.
- **`fetchEntries()`**: Synchronizes local state with backend database.
- **`addEntry(payload)`**: POSTs new valid records to the API.
- **`deleteEntry(id)`**: Deletes records providing they are strictly in the `PENDING` state.

## Styling Guidelines (`assets/main.css`)
- **Do not use tailwind or complex CSS frameworks.**
- The project runs on standard CSS variables targeting an "Institutional Appearance".
- **Primary Color:** `#2563eb` (Professional Blue)
- **Backgrounds:** `#f1f5f9` for generic spaces, `#ffffff` for standard `.erp-card` containers.
- Form components must rely on traditional standard HTML input designs with clean borders and explicit label tracking, eschewing generic floating or placeholder-only designs.

## Next Development Steps
- Bind actual user authentication limits (JWT storage).
- Complete the "Edit" entry workflow by passing parameters into the `TimesheetForm`.
- Build the "Manager Dashboard" view for bulk approving `PENDING` statuses.
