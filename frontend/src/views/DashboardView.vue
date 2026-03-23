<script setup>
import { computed } from 'vue'
import { useTimesheetStore } from '@/stores/timesheetStore'

const store = useTimesheetStore()

// Create dynamic summary widgets directly from Pinia State
const totalEntries = computed(() => store.entries.length)

const today = new Date().toISOString().split('T')[0]
const entriesToday = computed(() => {
  return store.entries.filter(entry => entry.date === today).length
})
</script>

<template>
  <div class="dashboard">
    <h1>Dashboard</h1>
    <p class="subtitle">Welcome to the Staff Timesheet System.</p>

    <div class="metrics-grid">
      <!-- Metric 1: Global Count -->
      <div class="metric-card">
        <h3>Total Logged Tasks</h3>
        <div class="number">{{ totalEntries }}</div>
        <p class="metric-label">All time</p>
      </div>

      <!-- Metric 2: Today's Tasks -->
      <div class="metric-card">
        <h3>Tasks Today</h3>
        <div class="number">{{ entriesToday }}</div>
        <p class="metric-label">{{ today }}</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard h1 { margin-bottom: 5px; color: var(--primary-color); }
.subtitle { color: #666; margin-bottom: 30px; }

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.metric-card {
  background: white;
  padding: 32px 24px;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.05);
  border-left: 5px solid var(--accent-color);
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.metric-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.08);
}

.metric-card h3 {
  font-size: 1.1rem;
  color: #64748b;
  margin-bottom: 15px;
  font-weight: 600;
}

.number {
  font-size: 3.5rem;
  font-weight: 800;
  color: var(--primary-color);
  margin-bottom: 8px;
  line-height: 1;
}

.metric-label {
  font-size: 0.9rem;
  color: #94a3b8;
}
</style>
