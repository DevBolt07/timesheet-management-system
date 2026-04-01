<script setup>
import { onMounted, ref } from 'vue'
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
    <div class="page-header">
      <h2>System Dashboard</h2>
      <p>High-level governance overview of the ERP Timesheet module.</p>
    </div>

    <div v-if="loading" class="state-center"><div class="loader"></div></div>
    <div v-else-if="error" class="state-center error-text">{{ error }}</div>

    <template v-else-if="metrics">
      <!-- Timesheet Stats -->
      <div class="section-label">Timesheet Records</div>
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

      <!-- User Stats -->
      <div class="section-label" style="margin-top: 28px;">User Accounts</div>
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

      <!-- System Status Banner -->
      <div class="system-status-banner">
        <div class="status-dot active"></div>
        <span>All core ERP modules operational &mdash; Timesheet, Review Workflow, Identity Layer</span>
      </div>
    </template>
  </div>
</template>

<style scoped>
.admin-dashboard { display: flex; flex-direction: column; gap: 0; }

.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 1.25rem; font-weight: 700; color: #1e293b; margin: 0 0 4px; }
.page-header p  { font-size: 0.875rem; color: #64748b; margin: 0; }

.section-label {
  font-size: 0.7rem;
  text-transform: uppercase;
  font-weight: 700;
  letter-spacing: 0.08em;
  color: #94a3b8;
  margin-bottom: 12px;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.metric-card {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 20px 24px;
  border-left: 4px solid transparent;
}
.metric-value { font-size: 2rem; font-weight: 700; color: #1e293b; line-height: 1; margin-bottom: 6px; }
.metric-name  { font-size: 0.8rem; color: #64748b; font-weight: 500; }

.total       { border-left-color: #3b82f6; }
.pending     { border-left-color: #f59e0b; }
.approved    { border-left-color: #10b981; }
.rejected    { border-left-color: #ef4444; }
.user-staff  { border-left-color: #6366f1; }
.user-hod    { border-left-color: #8b5cf6; }
.user-admin  { border-left-color: #0ea5e9; }
.task-card   { border-left-color: #64748b; }

.system-status-banner {
  margin-top: 28px;
  display: flex;
  align-items: center;
  gap: 10px;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 8px;
  padding: 12px 18px;
  font-size: 0.85rem;
  color: #166534;
}
.status-dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  background: #22c55e;
  flex-shrink: 0;
}

.state-center { display: flex; justify-content: center; padding: 60px; }
.loader {
  border: 3px solid #f1f5f9;
  border-top-color: #3b82f6;
  border-radius: 50%;
  width: 28px; height: 28px;
  animation: spin 1s linear infinite;
}
.error-text { color: #ef4444; font-size: 0.9rem; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>
