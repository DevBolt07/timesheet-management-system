<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const selectRole = (role) => {
  if (role === 'staff') {
    router.push('/app')
  }
}
</script>

<template>
  <div class="landing-page">
    <header class="landing-header">
      <div class="brand">
        <span class="logo-mark"></span>
        <span class="logo-text">Collegiate ERP Suite</span>
      </div>
      <div class="header-actions">
        <span class="version-text">v2.4.0</span>
      </div>
    </header>

    <main class="page-body">
      <div class="intro-section">
        <h1>Welcome to the Timesheet Module</h1>
        <p>Please select your operational role to access your workspace. This ensures you see the appropriate tools and workflows for your responsibilities.</p>
      </div>

      <div class="role-grid">
        <!-- Staff Card -->
        <button class="role-card active-card" @click="selectRole('staff')">
          <div class="card-icon staff-icon">
            <svg viewBox="0 0 24 24" fill="none" class="icon-svg"><path d="M16 7C16 9.20914 14.2091 11 12 11C9.79086 11 8 9.20914 8 7C8 4.79086 9.79086 3 12 3C14.2091 3 16 4.79086 16 7Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M12 14C8.13401 14 5 17.134 5 21H19C19 17.134 15.866 14 12 14Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
          </div>
          <div class="card-content">
            <h3>Staff Member</h3>
            <p>Log your daily working hours, submit timesheets, and review your historical records.</p>
          </div>
          <div class="card-footer">
            <span class="action-text">Enter Portal &rarr;</span>
          </div>
        </button>

        <!-- Manager Card (Disabled) -->
        <button class="role-card disabled-card" disabled>
          <div class="card-icon manager-icon">
            <svg viewBox="0 0 24 24" fill="none" class="icon-svg"><path d="M17 21V19C17 16.7909 15.2091 15 13 15H5C2.79086 15 1 16.7909 1 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M9 11C11.2091 11 13 9.20914 13 7C13 4.79086 11.2091 3 9 3C6.79086 3 5 4.79086 5 7C5 9.20914 6.79086 11 9 11Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M23 21V19C22.9993 18.1137 22.7044 17.2528 22.1614 16.5523C21.6184 15.8519 20.8581 15.3516 20 15.13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M16 3.13C16.8604 3.35031 17.623 3.85071 18.1676 4.55232C18.7122 5.25392 19.0078 6.11683 19.0078 7.005C19.0078 7.89318 18.7122 8.75608 18.1676 9.45769C17.623 10.1593 16.8604 10.6597 16 10.88" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
          </div>
          <div class="card-content">
            <h3>Manager / HOD <span class="badge">Soon</span></h3>
            <p>Review submitted timesheets, manage approvals, and track department productivity.</p>
          </div>
          <div class="card-footer">
            <span class="action-text">Currently Unavailable</span>
          </div>
        </button>

        <!-- Admin Card (Disabled) -->
        <button class="role-card disabled-card" disabled>
          <div class="card-icon admin-icon">
             <svg viewBox="0 0 24 24" fill="none" class="icon-svg"><path d="M12 15C13.6569 15 15 13.6569 15 12C15 10.3431 13.6569 9 12 9C10.3431 9 9 10.3431 9 12C9 13.6569 10.3431 15 12 15Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M19.4 15V9H18.2C17.9 8.1 17.4 7.2 16.7 6.5L17.5 5.6L13.3 1.4L12.4 2.2C11.5 1.9 10.6 1.9 9.8 2.2L8.9 1.4L4.7 5.6L5.5 6.5C4.8 7.2 4.3 8.1 4 9H2.8V15H4C4.3 15.9 4.8 16.8 5.5 17.5L4.7 18.4L8.9 22.6L9.8 21.8C10.6 22.1 11.5 22.1 12.4 21.8L13.3 22.6L17.5 18.4L16.7 17.5C17.4 16.8 17.9 15.9 18.2 15H19.4Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
          </div>
          <div class="card-content">
            <h3>Administrator <span class="badge">Soon</span></h3>
            <p>Configure system workflows, manage task master data, and configure organizational rules.</p>
          </div>
          <div class="card-footer">
            <span class="action-text">Currently Unavailable</span>
          </div>
        </button>

      </div>
    </main>
  </div>
</template>

<style scoped>
.landing-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.landing-header {
  background-color: var(--surface-color);
  border-bottom: 1px solid var(--border-light);
  padding: 16px 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-mark {
  width: 24px;
  height: 24px;
  background-color: var(--primary-color);
  border-radius: 4px;
}

.logo-text {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-main);
  letter-spacing: -0.01em;
}

.version-text {
  font-size: 0.8125rem;
  color: var(--text-muted);
  font-weight: 500;
}

.page-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.intro-section {
  text-align: center;
  max-width: 600px;
  margin-bottom: 56px;
}

.intro-section h1 {
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-main);
  margin-bottom: 16px;
  letter-spacing: -0.02em;
}

.intro-section p {
  font-size: 1rem;
  color: var(--text-muted);
  line-height: 1.6;
}

.role-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
  width: 100%;
  max-width: 1000px;
}

.role-card {
  background-color: var(--surface-color);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 32px 24px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  text-align: left;
  transition: all 0.2s ease;
  box-shadow: var(--shadow-sm);
  position: relative;
  overflow: hidden;
}

.active-card {
  cursor: pointer;
}

.active-card:hover {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.disabled-card {
  opacity: 0.5;
  background-color: #f8fafc;
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
}

.icon-svg {
  width: 24px;
  height: 24px;
  color: var(--primary-color);
}

.staff-icon { background-color: #eff6ff; }
.manager-icon { background-color: #f1f5f9; }
.admin-icon { background-color: #f1f5f9; }

.disabled-card .icon-svg { color: var(--text-muted); }

.card-content {
  flex: 1;
}

.card-content h3 {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-main);
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.badge {
  font-size: 0.6875rem;
  padding: 2px 6px;
  background-color: #e2e8f0;
  color: #475569;
  border-radius: 4px;
  font-weight: 500;
  text-transform: uppercase;
}

.card-content p {
  font-size: 0.875rem;
  color: var(--text-muted);
  line-height: 1.5;
}

.card-footer {
  margin-top: 24px;
  width: 100%;
}

.action-text {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--primary-color);
}

.active-card:hover .action-text {
  text-decoration: underline;
}

.disabled-card .action-text {
  color: var(--text-muted);
}
</style>
