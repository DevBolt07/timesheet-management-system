<script setup>
import { computed, onMounted } from 'vue'
import { useTimesheetStore } from '@/stores/timesheetStore'

const store = useTimesheetStore()

onMounted(() => {
  store.fetchEntries()
})

const totalEntries = computed(() => store.entries.length)

const getTodayLocal = () => {
  const tzOffset = (new Date()).getTimezoneOffset() * 60000;
  return new Date(Date.now() - tzOffset).toISOString().split('T')[0];
}

const today = getTodayLocal()
const entriesToday = computed(() => {
  return store.entries.filter(entry => entry.date === today).length
})

const pendingEntries = computed(() => {
  return store.entries.filter(entry => entry.status === 'PENDING').length
})
</script>

<template>
  <div class="dashboard">
    <div class="page-header">
      <h1 class="page-title">Timesheet Overview</h1>
      <p class="page-subtitle">High-level activity metrics and pending approvals.</p>
    </div>

    <div class="metrics-grid">
      <div class="metric-card">
        <div class="metric-icon">📊</div>
        <div class="metric-content">
          <p class="metric-label">Total Logged Tasks</p>
          <div class="number">{{ totalEntries }}</div>
        </div>
      </div>

      <div class="metric-card">
        <div class="metric-icon">⚡</div>
        <div class="metric-content">
          <p class="metric-label">Tasks Today</p>
          <div class="number">{{ entriesToday }}</div>
        </div>
      </div>
      
      <div class="metric-card pending-card">
        <div class="metric-icon warning">⏳</div>
        <div class="metric-content">
          <p class="metric-label">Pending Approval</p>
          <div class="number warning-text">{{ pendingEntries }}</div>
        </div>
      </div>
    </div>
    
    <div class="dashboard-widgets">
      <!-- Future widget placeholders for ERP extension -->
      <div class="widget-box">
        <div class="widget-header">
          <h3>Recent Activity</h3>
          <button class="btn-text">View All</button>
        </div>
        <div class="widget-body empty-state">
          <p>No recent activity flags.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-header {
  margin-bottom: 32px;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.metric-card {
  background: var(--bg-surface);
  padding: 24px;
  border-radius: 12px;
  border: 1px solid var(--border-subtle);
  display: flex;
  align-items: center;
  gap: 20px;
  transition: box-shadow 0.2s, transform 0.2s;
}

.metric-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  transform: translateY(-2px);
}

.metric-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background-color: #eff6ff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
}

.metric-icon.warning {
  background-color: #fffbeb;
}

.metric-label {
  font-size: 0.8125rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-muted);
  margin-bottom: 8px;
}

.number {
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-main);
  line-height: 1;
}

.warning-text {
  color: #d97706;
}

/* Dashboard Widgets Frame */
.dashboard-widgets {
  display: grid;
  grid-template-columns: 1fr;
  gap: 24px;
}

.widget-box {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: 12px;
  overflow: hidden;
}

.widget-header {
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-subtle);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.widget-header h3 {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-main);
}

.btn-text {
  color: var(--primary-blue);
  font-size: 0.875rem;
  font-weight: 500;
}

.btn-text:hover {
  text-decoration: underline;
}

.widget-body {
  padding: 24px;
}

.empty-state {
  text-align: center;
  color: var(--text-muted);
  padding: 40px 20px;
  font-size: 0.875rem;
}
</style>
