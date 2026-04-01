<script setup>
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { apiClient } from '@/services/api'

const metrics = ref(null)
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    metrics.value = await apiClient.get('/api/admin/metrics')
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="admin-dashboard">

    <div v-if="loading" class="state-center"><div class="loader"></div></div>
    <div v-else-if="error" class="state-center error-text">{{ error }}</div>

    <template v-else-if="metrics">

      <!-- === Timesheet Records Section === -->
      <div class="dash-section">
        <div class="section-header">
          <span class="section-label">Timesheet Records</span>
          <RouterLink to="/app/admin/oversight" class="section-link">View All &rarr;</RouterLink>
        </div>
        <div class="metrics-grid">
          <div class="metric-card total">
            <div class="metric-value">{{ metrics.timesheets.total }}</div>
            <div class="metric-name">Total Entries</div>
          </div>
          <div class="metric-card pending">
            <div class="metric-value">{{ metrics.timesheets.pending }}</div>
            <div class="metric-name">Pending Review</div>
          </div>
          <div class="metric-card approved">
            <div class="metric-value">{{ metrics.timesheets.approved }}</div>
            <div class="metric-name">Approved</div>
          </div>
          <div class="metric-card rejected">
            <div class="metric-value">{{ metrics.timesheets.rejected }}</div>
            <div class="metric-name">Rejected</div>
          </div>
        </div>
      </div>

      <!-- === User & System Section === -->
      <div class="dash-section">
        <div class="section-header">
          <span class="section-label">User Accounts &amp; System</span>
          <RouterLink to="/app/admin/users" class="section-link">Manage Users &rarr;</RouterLink>
        </div>
        <div class="metrics-grid">
          <div class="metric-card user-staff">
            <div class="metric-value">{{ metrics.users.staff }}</div>
            <div class="metric-name">Staff Members</div>
          </div>
          <div class="metric-card user-hod">
            <div class="metric-value">{{ metrics.users.hod }}</div>
            <div class="metric-name">HOD / Managers</div>
          </div>
          <div class="metric-card user-admin">
            <div class="metric-value">{{ metrics.users.admin }}</div>
            <div class="metric-name">Administrators</div>
          </div>
          <div class="metric-card task-card">
            <div class="metric-value">{{ metrics.tasks.total }}</div>
            <div class="metric-name">Task Categories</div>
          </div>
        </div>
      </div>

      <!-- === Quick Access Row === -->
      <div class="dash-section">
        <div class="section-header">
          <span class="section-label">Quick Access</span>
        </div>
        <div class="quick-nav-grid">
          <RouterLink to="/app/admin/oversight" class="quick-nav-card">
            <div class="qn-icon">📋</div>
            <div><div class="qn-title">Timesheet Oversight</div><div class="qn-sub">Full audit across all staff</div></div>
          </RouterLink>
          <RouterLink to="/app/admin/users" class="quick-nav-card">
            <div class="qn-icon">👥</div>
            <div><div class="qn-title">User Directory</div><div class="qn-sub">Manage system accounts</div></div>
          </RouterLink>
          <RouterLink to="/app/admin/tasks" class="quick-nav-card">
            <div class="qn-icon">🗂️</div>
            <div><div class="qn-title">Task Master</div><div class="qn-sub">Control task categories</div></div>
          </RouterLink>
        </div>
      </div>

      <!-- === System Status === -->
      <div class="system-status-banner">
        <div class="status-dot"></div>
        <span>All core ERP modules operational &mdash; Timesheet, Review Workflow, Identity Layer</span>
      </div>

    </template>
  </div>
</template>

<style scoped>
.admin-dashboard {
  display: flex;
  flex-direction: column;
  gap: 0;
}

/* Sections */
.dash-section {
  background: var(--surface-color);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  overflow: hidden;
  margin-bottom: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  border-bottom: 1px solid var(--border-light);
  background-color: #fafbfc;
}

.section-label {
  font-size: 0.65rem;
  text-transform: uppercase;
  font-weight: 700;
  letter-spacing: 0.07em;
  color: var(--text-muted);
}

.section-link {
  font-size: 0.78rem;
  font-weight: 600;
  color: var(--primary-color);
  text-decoration: none;
  transition: color 0.15s;
}
.section-link:hover { color: var(--primary-hover); }

/* Metric Grid */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 0;
}

.metric-card {
  padding: 20px 24px;
  border-right: 1px solid var(--border-light);
  border-left: 4px solid transparent;
  transition: background 0.15s;
}
.metric-card:last-child { border-right: none; }
.metric-card:hover { background: #f8fafc; }

.metric-value {
  font-size: 1.875rem;
  font-weight: 700;
  color: var(--text-main);
  line-height: 1;
  margin-bottom: 6px;
  font-variant-numeric: tabular-nums;
}
.metric-name {
  font-size: 0.78rem;
  color: var(--text-muted);
  font-weight: 500;
}

.total      { border-left-color: #3b82f6; }
.pending    { border-left-color: #f59e0b; }
.approved   { border-left-color: #10b981; }
.rejected   { border-left-color: #ef4444; }
.user-staff { border-left-color: #6366f1; }
.user-hod   { border-left-color: #8b5cf6; }
.user-admin { border-left-color: #0ea5e9; }
.task-card  { border-left-color: #64748b; }

/* Quick Nav */
.quick-nav-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0;
}

.quick-nav-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 24px;
  border-right: 1px solid var(--border-light);
  text-decoration: none;
  transition: background 0.15s;
  color: var(--text-main);
}
.quick-nav-card:last-child { border-right: none; }
.quick-nav-card:hover { background: #f0f7ff; }

.qn-icon { font-size: 1.5rem; flex-shrink: 0; }
.qn-title { font-size: 0.875rem; font-weight: 600; color: var(--text-main); margin-bottom: 2px; }
.qn-sub { font-size: 0.75rem; color: var(--text-muted); }

/* Status Banner */
.system-status-banner {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: var(--radius-lg);
  padding: 12px 18px;
  font-size: 0.82rem;
  color: #166534;
  margin-bottom: 0;
}
.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #22c55e;
  flex-shrink: 0;
  animation: pulse 2s ease-in-out infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.state-center { display: flex; justify-content: center; padding: 80px; }
.loader {
  border: 3px solid var(--border-light);
  border-top-color: var(--primary-color);
  border-radius: 50%;
  width: 28px;
  height: 28px;
  animation: spin 1s linear infinite;
}
.error-text { color: #ef4444; font-size: 0.9rem; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>
