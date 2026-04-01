<script setup>
import { onMounted, computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { useTimesheetStore } from '@/stores/timesheetStore'
import { storeToRefs } from 'pinia'
import { apiClient } from '@/services/api'

const store = useTimesheetStore()
const authStore = useAuthStore()
const { entries, isLoading, isError } = storeToRefs(store)
const { isStaff, isManager, currentUser } = storeToRefs(authStore)
const router = useRouter()

// HOD-only: staff user list for filtering
const staffList = ref([])
const selectedStaffUser = ref('')

onMounted(async () => {
  store.fetchEntries()
  if (isManager.value) {
    try {
      staffList.value = await apiClient.get('/api/users/staff')
    } catch(e) {
      console.error('Could not load staff list', e)
    }
  }
})

// Filters
const searchTerm = ref('')
const statusFilter = ref('')
const dateFrom = ref('')
const dateTo = ref('')

const clearFilters = () => {
  searchTerm.value = ''
  statusFilter.value = ''
  dateFrom.value = ''
  dateTo.value = ''
  selectedStaffUser.value = ''
}

const filteredEntries = computed(() => {
  return entries.value.filter(entry => {
    // HOD staff filter
    if (selectedStaffUser.value && entry.user !== selectedStaffUser.value) return false

    // Search
    if (searchTerm.value) {
      const q = searchTerm.value.toLowerCase()
      const matchesTask = (entry.task || '').toLowerCase().includes(q)
      const matchesDesc = (entry.description || '').toLowerCase().includes(q)
      if (!matchesTask && !matchesDesc) return false
    }
    
    // Status Filter
    if (statusFilter.value && entry.status !== statusFilter.value) return false

    // Date Range
    if (dateFrom.value && new Date(entry.date) < new Date(dateFrom.value)) return false
    if (dateTo.value && new Date(entry.date) > new Date(dateTo.value)) return false

    return true
  })
})

// Per-staff summary for HOD when a staff member is selected
const staffSummary = computed(() => {
  if (!selectedStaffUser.value) return null
  const subset = filteredEntries.value

  const calcHours = (entry) => {
    try {
      const [sh, sm] = entry.startTime.split(':').map(Number)
      const [eh, em] = entry.endTime.split(':').map(Number)
      return Math.max(0, (eh + em / 60) - (sh + sm / 60))
    } catch { return 0 }
  }

  const totalHours = subset.reduce((sum, e) => sum + calcHours(e), 0)

  return {
    total: subset.length,
    pending: subset.filter(e => e.status === 'PENDING').length,
    approved: subset.filter(e => e.status === 'APPROVED').length,
    rejected: subset.filter(e => e.status === 'REJECTED').length,
    hours: totalHours.toFixed(1)
  }
})

// Remark modal
const remarkModal = ref({ open: false, text: '' })
const openRemark = (text) => {
  remarkModal.value = { open: true, text }
}
const closeRemark = () => {
  remarkModal.value = { open: false, text: '' }
}

const format12H = (time24) => {
  if (!time24) return ''
  let [h, m] = time24.split(':')
  let hrs = parseInt(h, 10)
  let a = hrs >= 12 ? 'PM' : 'AM'
  hrs = hrs % 12 || 12
  return `${hrs}:${m} ${a}`
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.toLocaleDateString('en-US', { day: '2-digit', month: 'short', year: 'numeric' })
}

const getStatusClass = (status) => {
  if (status === 'APPROVED') return 'status-approved'
  if (status === 'REJECTED') return 'status-rejected'
  return 'status-pending'
}

const isDeleting = ref(null)

const removeEntry = async (id) => {
  if (!confirm('Are you sure you want to permanently delete this pending timesheet entry?')) return
  try {
    isDeleting.value = id
    await store.deleteEntry(id)
  } catch(e) {
    alert("Delete failed: " + e.message)
  } finally {
    isDeleting.value = null
  }
}

const editEntry = (id) => {
  router.push({ path: '/app/log-time', query: { editId: id } })
}

const canEditEntry = (entry) => entry.status === 'PENDING' || entry.status === 'REJECTED'
const isLockedEntry = (entry) => entry.status === 'APPROVED'
</script>

<template>
  <div class="view-timesheets">
    <div class="toolbar">
      <h2>{{ isStaff ? 'My Timesheet Logs' : 'Department Logs Viewer' }}</h2>
      <div v-if="isStaff" class="actions">
        <button class="blue-btn outline" @click="router.push('/app/log-time')">+ New Entry</button>
      </div>
    </div>

    <!-- HOD Staff Summary Card -->
    <div v-if="isManager && selectedStaffUser && staffSummary" class="staff-summary-bar">
      <div class="summary-header">
        <span class="summary-label">Staff Report:</span>
        <strong>{{ selectedStaffUser }}</strong>
      </div>
      <div class="summary-stats">
        <div class="stat-item">
          <span class="stat-num">{{ staffSummary.total }}</span>
          <span class="stat-label">Total Logs</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item pending-stat">
          <span class="stat-num">{{ staffSummary.pending }}</span>
          <span class="stat-label">Pending</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item approved-stat">
          <span class="stat-num">{{ staffSummary.approved }}</span>
          <span class="stat-label">Approved</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item rejected-stat">
          <span class="stat-num">{{ staffSummary.rejected }}</span>
          <span class="stat-label">Rejected</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-num">{{ staffSummary.hours }}h</span>
          <span class="stat-label">Logged Hours</span>
        </div>
      </div>
    </div>

    <div class="filter-bar">
      <div class="search-box">
        <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
        <input type="text" v-model="searchTerm" placeholder="Search task or description..." class="filter-input" />
      </div>

      <!-- HOD-only: Staff filter -->
      <div v-if="isManager" class="filter-group">
        <label>Staff Member:</label>
        <select v-model="selectedStaffUser" class="filter-select">
          <option value="">All Staff</option>
          <option v-for="s in staffList" :key="s.id" :value="s.username">{{ s.username }}</option>
        </select>
      </div>

      <div class="filter-group">
        <label>Date Range:</label>
        <input type="date" v-model="dateFrom" class="filter-input-small" />
        <span>to</span>
        <input type="date" v-model="dateTo" class="filter-input-small" />
      </div>

      <div class="filter-group">
        <label>Status:</label>
        <select v-model="statusFilter" class="filter-select">
          <option value="">All States</option>
          <option value="PENDING">Pending</option>
          <option value="APPROVED">Approved</option>
          <option value="REJECTED">Rejected</option>
        </select>
      </div>
      
      <button @click="clearFilters" class="clear-btn" title="Reset Filters">Clear</button>
    </div>

    <!-- State Holders -->
    <div v-if="isLoading" class="state-container">
      <div class="loader-lg"></div>
      <p>Synchronizing operational data...</p>
    </div>

    <div v-else-if="isError" class="state-container error">
      <div class="err-icon">⚠</div>
      <p>Internal System Error: {{ isError }}</p>
      <button @click="store.fetchEntries" class="blue-btn">Retry Connection</button>
    </div>

    <!-- Data Table -->
    <div v-else class="master-table-container">
      <table class="erp-table">
        <thead>
          <tr>
            <th width="10%">Log Date</th>
            <th v-if="isManager" width="12%">Staff Member</th>
            <th width="14%">Task Category</th>
            <th width="28%">Deliverable Description</th>
            <th width="14%">Working Hours</th>
            <th width="8%">Status</th>
            <th width="10%">Remarks</th>
            <th width="10%" class="center-col">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="entry in filteredEntries" :key="entry.id" class="table-row">
            <td class="bold-date">{{ formatDate(entry.date) }}</td>
            <td v-if="isManager" class="user-cell">{{ entry.user }}</td>
            <td><span class="task-badge">{{ entry.task || 'Unclassified' }}</span></td>
            <td class="desc-cell">{{ entry.description }}</td>
            <td class="time-cell">
              {{ format12H(entry.startTime) }} &mdash; {{ format12H(entry.endTime) }}
            </td>
            <td>
              <span class="status-pill" :class="getStatusClass(entry.status)">{{ entry.status }}</span>
            </td>
            <td class="remarks-cell">
              <button
                v-if="entry.reviewerRemarks"
                class="remark-btn"
                :title="entry.reviewerRemarks"
                @click="openRemark(entry.reviewerRemarks)"
              >
                💬 View Note
              </button>
              <span v-else class="empty-dash">—</span>
            </td>
            <td class="center-col actions-col">
              <!-- Staff edit/delete actions -->
              <div v-if="isStaff" class="action-btn-group">
                <button 
                  class="act-btn edit-btn" 
                  :disabled="!canEditEntry(entry)" 
                  @click="editEntry(entry.id)"
                  :title="entry.status === 'REJECTED' ? 'Revise rejected entry and resubmit it for review' : 'Modify pending entry'">Edit</button>
                <div class="act-divider"></div>
                <button 
                  class="act-btn delete-btn" 
                  :disabled="entry.status !== 'PENDING' || isDeleting === entry.id" 
                  @click="removeEntry(entry.id)"
                  title="Remove pending entry"><span v-if="isDeleting===entry.id">...</span><span v-else>Del</span></button>
              </div>
              <!-- Lock indicator for staff on non-pending -->
              <span v-if="entry.status !== 'PENDING' && isStaff" class="locked-icon" title="Entry is locked by Management">🔒</span>
              <!-- Manager view: no actions needed here (review is on separate screen) -->
              <span v-if="isManager" class="empty-dash">—</span>
            </td>
          </tr>
          
          <tr v-if="filteredEntries.length === 0">
            <td :colspan="isManager ? 8 : 7" class="empty-row">No timesheet records match current operational filters.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Remark Full-Text Modal -->
    <div v-if="remarkModal.open" class="remark-overlay" @click.self="closeRemark">
      <div class="remark-modal">
        <div class="remark-modal-header">
          <h3>Manager Remark</h3>
          <button class="modal-close-btn" @click="closeRemark">&times;</button>
        </div>
        <div class="remark-modal-body">
          <p>{{ remarkModal.text }}</p>
        </div>
        <div class="remark-modal-footer">
          <button class="blue-btn" @click="closeRemark">Close</button>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
.view-timesheets {
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  border: 1px solid #e2e8f0;
  min-height: calc(100vh - 130px);
  display: flex;
  flex-direction: column;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: var(--accent-warm);
  border-bottom: none;
}

.toolbar h2 {
  font-size: 1.0625rem;
  font-weight: 700;
  color: var(--accent-warm-text);
  margin: 0;
  letter-spacing: -0.01em;
}

.blue-btn {
  background-color: var(--primary-color);
  color: #ffffff;
  border: none;
  padding: 8px 16px;
  font-size: 0.84rem;
  font-weight: 600;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.2s;
}
.blue-btn:hover { background-color: var(--primary-hover); }
.blue-btn.outline {
  background-color: rgba(255,255,255,0.18);
  color: #fff;
  border: 1px solid rgba(255,255,255,0.45);
}
.blue-btn.outline:hover { background-color: rgba(255,255,255,0.28); }

/* HOD Staff Summary */
.staff-summary-bar {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 14px 24px;
  background: #f0f7ff;
  border-bottom: 1px solid #bfdbfe;
  flex-wrap: wrap;
}
.summary-header {
  font-size: 0.85rem;
  color: #1e40af;
  display: flex;
  gap: 6px;
  align-items: center;
  white-space: nowrap;
}
.summary-label { font-weight: 500; color: #3b82f6; }
.summary-stats {
  display: flex;
  align-items: center;
  gap: 0;
  flex-wrap: wrap;
}
.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 20px;
}
.stat-num {
  font-size: 1.2rem;
  font-weight: 700;
  color: #1e293b;
  line-height: 1;
}
.stat-label {
  font-size: 0.7rem;
  color: #64748b;
  margin-top: 2px;
  text-transform: uppercase;
  font-weight: 500;
  letter-spacing: 0.03em;
}
.pending-stat .stat-num { color: #b45309; }
.approved-stat .stat-num { color: #166534; }
.rejected-stat .stat-num { color: #991b1b; }
.stat-divider {
  width: 1px;
  height: 30px;
  background: #bfdbfe;
}

/* Filter Bar */
.filter-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 16px;
  padding: 16px 24px;
  background-color: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.search-box {
  position: relative;
  flex: 1;
  min-width: 200px;
}
.search-icon {
  position: absolute;
  left: 10px; top: 50%;
  transform: translateY(-50%);
  width: 16px; height: 16px;
  color: #94a3b8;
}
.filter-input {
  width: 100%;
  padding: 8px 12px 8px 34px;
  border: 1px solid #cbd5e1;
  border-radius: 4px;
  font-size: 0.85rem;
  outline: none;
  box-sizing: border-box;
}
.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.85rem;
  color: #475569;
  font-weight: 500;
}
.filter-input-small, .filter-select {
  padding: 6px 8px;
  border: 1px solid #cbd5e1;
  border-radius: 4px;
  font-size: 0.85rem;
  outline: none;
  background-color: white;
}
.clear-btn {
  background: none;
  border: none;
  color: #64748b;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
}
.clear-btn:hover { color: #0f172a; text-decoration: underline; }

/* Table */
.master-table-container {
  overflow-x: auto;
  flex: 1;
}
.erp-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
}
.erp-table th {
  background-color: #f1f5f9;
  padding: 12px 16px;
  font-size: 0.75rem;
  text-transform: uppercase;
  font-weight: 600;
  color: #64748b;
  letter-spacing: 0.05em;
  border-bottom: 2px solid #e2e8f0;
}
.erp-table td {
  padding: 14px 16px;
  font-size: 0.85rem;
  color: #334155;
  border-bottom: 1px solid #e2e8f0;
  vertical-align: middle;
}
.table-row:hover { background-color: #f8fafc; }
.bold-date { font-weight: 600; color: #0f172a; white-space: nowrap; }
.user-cell { font-weight: 500; color: #1d4ed8; font-size: 0.8rem; }
.task-badge {
  background-color: #f1f5f9;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 500;
  border: 1px solid #e2e8f0;
  color: #475569;
}
.desc-cell { line-height: 1.4; color: #475569; }
.time-cell { font-variant-numeric: tabular-nums; white-space: nowrap; }

/* Remarks */
.remarks-cell { font-size: 0.8rem; color: #64748b; }
.remark-btn {
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  color: #1d4ed8;
  font-size: 0.78rem;
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.15s;
  white-space: nowrap;
}
.remark-btn:hover { background: #dbeafe; }
.empty-dash { color: #cbd5e1; }

/* Status Pills */
.status-pill {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: capitalize;
  letter-spacing: 0.02em;
}
.status-pending { background-color: #fef3c7; color: #b45309; }
.status-approved { background-color: #dcfce7; color: #166534; }
.status-rejected { background-color: #fee2e2; color: #991b1b; }

.center-col { text-align: center; }
.actions-col { position: relative; }
.action-btn-group {
  display: inline-flex;
  align-items: center;
  background: #f1f5f9;
  border-radius: 4px;
  padding: 2px;
  border: 1px solid #e2e8f0;
}
.act-btn {
  background: transparent;
  border: none;
  font-size: 0.75rem;
  font-weight: 600;
  padding: 4px 8px;
  cursor: pointer;
  transition: all 0.1s;
  color: #475569;
}
.act-btn:hover:not(:disabled) {
  background: #fff;
  border-radius: 2px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}
.act-btn:disabled { color: #cbd5e1; cursor: not-allowed; }
.act-divider { width: 1px; height: 12px; background-color: #cbd5e1; }
.edit-btn:hover:not(:disabled) { color: #0284c7; }
.delete-btn:hover:not(:disabled) { color: #dc2626; }
.locked-icon { font-size: 0.85rem; opacity: 0.5; margin-left: 8px; vertical-align: middle; }

.empty-row {
  text-align: center;
  padding: 60px !important;
  color: #64748b !important;
  font-style: italic;
  background-color: #f8fafc;
}

.state-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 80px 20px;
  color: #64748b;
  flex: 1;
}
.loader-lg {
  border: 3px solid #f1f5f9;
  border-top-color: #3b82f6;
  border-radius: 50%;
  width: 32px; height: 32px;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}
.err-icon { font-size: 2rem; margin-bottom: 12px; color: #dc2626; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

/* Remark Modal */
.remark-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}
.remark-modal {
  background: white;
  border-radius: 10px;
  width: 460px;
  max-width: 90vw;
  box-shadow: 0 10px 40px rgba(0,0,0,0.15);
  display: flex;
  flex-direction: column;
}
.remark-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 20px 14px;
  border-bottom: 1px solid #e2e8f0;
}
.remark-modal-header h3 {
  font-size: 1rem;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}
.modal-close-btn {
  background: none;
  border: none;
  font-size: 1.4rem;
  line-height: 1;
  color: #94a3b8;
  cursor: pointer;
  padding: 0 4px;
}
.modal-close-btn:hover { color: #1e293b; }
.remark-modal-body {
  padding: 20px;
  font-size: 0.9rem;
  color: #334155;
  line-height: 1.6;
  min-height: 60px;
}
.remark-modal-footer {
  padding: 14px 20px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-end;
}
</style>
