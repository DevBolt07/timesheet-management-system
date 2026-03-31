<script setup>
import { useTimesheetStore } from '@/stores/timesheetStore'
import { storeToRefs } from 'pinia'
import { onMounted } from 'vue'

const store = useTimesheetStore()
const { entries, isLoading, isError } = storeToRefs(store)

onMounted(() => {
  store.fetchEntries()
})

const formatTime = (timeString) => {
  if (!timeString) return '';
  const [hours, minutes] = timeString.split(':');
  let h = parseInt(hours, 10);
  const ampm = h >= 12 ? 'PM' : 'AM';
  h = h % 12 || 12;
  const paddedH = h.toString().padStart(2, '0');
  return `${paddedH}:${minutes} ${ampm}`;
}

const statusClass = (status) => {
  if (status === 'APPROVED') return 'status-badge approved';
  if (status === 'REJECTED') return 'status-badge rejected';
  return 'status-badge pending';
}
</script>

<template>
  <div class="erp-view fade-in">
    <div class="view-header">
      <h1 class="page-title">Timesheet Logs</h1>
      <p class="page-subtitle">Review, edit, or track the approval status of your submitted hours.</p>
    </div>

    <!-- Alert / Banner Region -->
    <div v-if="isLoading" class="erp-alert info">
      <span class="spinner">↻</span> Loading records...
    </div>
    <div v-if="isError" class="erp-alert error">
      <span class="icon">⚠</span> {{ isError }}
    </div>

    <div class="erp-card list-card" v-if="!isLoading && !isError">
      <!-- Filter Bar / Toolbar -->
      <div class="card-toolbar">
        <div class="search-wrap">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          <input type="text" class="search-input" placeholder="Search logs..." />
        </div>
        
        <div class="toolbar-actions">
          <select class="filter-select">
            <option>All Statuses</option>
            <option>Pending</option>
            <option>Approved</option>
          </select>
          <button class="btn-outline">
            <svg class="filter-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/></svg>
            Filter
          </button>
        </div>
      </div>

      <!-- Data Table -->
      <div class="table-responsive">
        <table class="modern-table">
          <thead>
            <tr>
              <th width="15%">Date</th>
              <th width="20%">Duration</th>
              <th width="20%">Task Category</th>
              <th width="25%">Description</th>
              <th width="10%">Status</th>
              <th width="10%" class="text-right">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="entries.length === 0">
              <td colspan="6" class="empty-state">No historical records found for your account.</td>
            </tr>
            <tr v-for="entry in entries" :key="entry.id">
              <td class="semi-bold">{{ entry.date }}</td>
              <td>
                <div class="time-block">
                  <span class="time-text">{{ formatTime(entry.startTime) }}</span> &rarr;
                  <span class="time-text">{{ formatTime(entry.endTime) }}</span>
                </div>
              </td>
              <td class="text-primary">{{ entry.task }}</td>
              <td class="text-muted ellipsis-text" :title="entry.description">{{ entry.description }}</td>
              <td>
                 <span :class="statusClass(entry.status)">{{ entry.status || 'PENDING' }}</span>
              </td>
              <td class="action-cell">
                 <button 
                  class="btn-text btn-delete" 
                  @click="store.deleteEntry(entry.id)"
                  :disabled="entry.status !== 'PENDING'"
                  title="Delete Pending Log"
                 >
                   Delete
                 </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.fade-in {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(4px); }
  to { opacity: 1; transform: translateY(0); }
}

.view-header {
  margin-bottom: 24px;
}

.erp-alert {
  padding: 14px 20px;
  border-radius: var(--radius-md);
  margin-bottom: 24px;
  font-size: 0.875rem;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 12px;
}

.erp-alert.info {
  background-color: #eff6ff;
  color: #1e40af;
  border: 1px solid #bfdbfe;
}

.erp-alert.error {
  background-color: #fef2f2;
  color: #991b1b;
  border: 1px solid #fecaca;
}

.spinner {
  display: inline-block;
  animation: spin 1s linear infinite;
}

@keyframes spin { 100% { transform: rotate(360deg); } }

.list-card {
  width: 100%;
}

.card-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid var(--border-light);
  background-color: var(--surface-color);
}

.search-wrap {
  position: relative;
  width: 300px;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 12px;
  width: 16px;
  height: 16px;
  color: var(--text-muted);
}

.search-input {
  width: 100%;
  padding: 8px 12px 8px 36px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  font-size: 0.875rem;
  color: var(--text-main);
  transition: all 0.2s;
  background-color: var(--surface-color);
}

.search-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.toolbar-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.filter-select {
  padding: 8px 32px 8px 12px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  font-size: 0.875rem;
  background-color: var(--surface-color);
  color: var(--text-main);
  outline: none;
}

.btn-outline {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border: 1px solid var(--border-color);
  background-color: var(--surface-color);
  border-radius: var(--radius-md);
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--text-main);
  transition: all 0.2s;
}

.btn-outline:hover {
  background-color: #f1f5f9;
}

.filter-icon {
  width: 16px;
  height: 16px;
}

.table-responsive {
  width: 100%;
  overflow-x: auto;
}

.modern-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
}

.modern-table th {
  background-color: #f8fafc;
  padding: 14px 24px;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border-bottom: 2px solid var(--border-light);
}

.modern-table td {
  padding: 16px 24px;
  border-bottom: 1px solid var(--border-light);
  font-size: 0.875rem;
  vertical-align: middle;
}

.modern-table tr:hover {
  background-color: #f8fafc;
}

.modern-table tr:last-child td {
  border-bottom: none;
}

.semi-bold {
  font-weight: 500;
  color: var(--text-main);
}

.time-block {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-muted);
}

.time-text {
  font-weight: 500;
  color: var(--text-main);
}

.text-primary {
  color: var(--primary-color);
  font-weight: 500;
}

.text-muted {
  color: var(--text-muted);
}

.ellipsis-text {
  max-width: 250px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.status-badge {
  font-size: 0.75rem;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 12px;
  letter-spacing: 0.02em;
}

.status-badge.pending { color: #d97706; background-color: #fef3c7; }
.status-badge.approved { color: #166534; background-color: #dcfce7; }
.status-badge.rejected { color: #991b1b; background-color: #fee2e2; }

.text-right {
  text-align: right;
}

.action-cell {
  text-align: right;
}

.btn-text {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--text-muted);
  padding: 6px 12px;
  border-radius: 4px;
  transition: all 0.2s;
}

.btn-delete:hover:not(:disabled) {
  background-color: #fee2e2;
  color: #dc2626;
}

.btn-delete:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.empty-state {
  text-align: center;
  padding: 48px 24px;
  color: var(--text-muted);
  font-style: italic;
}
</style>
