<script setup>
import { computed, onMounted, ref } from 'vue'
import { apiClient } from '@/services/api'

const entries = ref([])
const staffList = ref([])
const loading = ref(true)
const error = ref('')
const reopeningId = ref(null)

const searchTerm = ref('')
const statusFilter = ref('')
const staffFilter = ref('')
const dateFrom = ref('')
const dateTo = ref('')

const noteModal = ref({ open: false, title: '', text: '' })
const reopenModal = ref({ open: false, entry: null, reason: '', error: '' })

const loadAdminData = async () => {
  loading.value = true
  error.value = ''
  try {
    const [timesheets, staff] = await Promise.all([
      apiClient.get('/api/admin/timesheets'),
      apiClient.get('/api/users/staff')
    ])
    entries.value = timesheets
    staffList.value = staff
  } catch (e) {
    error.value = e.message || 'Unable to load admin timesheet oversight data.'
  } finally {
    loading.value = false
  }
}

onMounted(loadAdminData)

const clearFilters = () => {
  searchTerm.value = ''
  statusFilter.value = ''
  staffFilter.value = ''
  dateFrom.value = ''
  dateTo.value = ''
}

const filteredEntries = computed(() => entries.value.filter((entry) => {
  if (staffFilter.value && entry.user !== staffFilter.value) return false
  if (statusFilter.value && entry.status !== statusFilter.value) return false

  if (searchTerm.value) {
    const query = searchTerm.value.toLowerCase()
    const searchable = [entry.user, entry.task, entry.description, entry.reviewerRemarks, entry.adminOverrideReason]
      .filter(Boolean)
      .join(' ')
      .toLowerCase()
    if (!searchable.includes(query)) return false
  }

  if (dateFrom.value && new Date(entry.date) < new Date(dateFrom.value)) return false
  if (dateTo.value && new Date(entry.date) > new Date(dateTo.value)) return false
  return true
}))

const format12H = (time24) => {
  if (!time24) return ''
  const [hours, minutes] = time24.split(':')
  let parsedHours = parseInt(hours, 10)
  const suffix = parsedHours >= 12 ? 'PM' : 'AM'
  parsedHours = parsedHours % 12 || 12
  return `${parsedHours}:${minutes} ${suffix}`
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('en-US', { day: '2-digit', month: 'short', year: 'numeric' })
}

const statusClass = (status) => {
  if (status === 'APPROVED') return 'st-approved'
  if (status === 'REJECTED') return 'st-rejected'
  return 'st-pending'
}

const buildEntryNotes = (entry) => {
  const notes = []
  if (entry.reviewerRemarks) {
    notes.push(`Reviewer Note:\n${entry.reviewerRemarks}`)
  }
  if (entry.adminOverrideReason) {
    const meta = [
      entry.adminOverrideBy ? `Admin: ${entry.adminOverrideBy}` : null,
      entry.adminOverrideFromStatus ? `From Status: ${entry.adminOverrideFromStatus}` : null,
      entry.adminOverrideAt ? `Reopened At: ${entry.adminOverrideAt}` : null
    ].filter(Boolean).join('\n')
    notes.push(`Admin Reopen Note:\n${entry.adminOverrideReason}${meta ? `\n\n${meta}` : ''}`)
  }
  return notes.join('\n\n')
}

const openNotes = (entry) => {
  noteModal.value = {
    open: true,
    title: 'Workflow Notes',
    text: buildEntryNotes(entry)
  }
}

const closeNotes = () => {
  noteModal.value = { open: false, title: '', text: '' }
}

const canReopen = (entry) => entry.status === 'APPROVED' || entry.status === 'REJECTED'

const openReopenModal = (entry) => {
  reopenModal.value = { open: true, entry, reason: '', error: '' }
}

const closeReopenModal = () => {
  if (reopeningId.value) return
  reopenModal.value = { open: false, entry: null, reason: '', error: '' }
}

const submitReopen = async () => {
  const entry = reopenModal.value.entry
  const reason = reopenModal.value.reason.trim()

  if (!entry) return
  if (!reason) {
    reopenModal.value.error = 'Admin override reason is required.'
    return
  }

  reopeningId.value = entry.id
  reopenModal.value.error = ''

  try {
    const updated = await apiClient.put(`/api/admin/timesheets/${entry.id}/reopen`, { reason })
    const index = entries.value.findIndex((item) => item.id === entry.id)
    if (index !== -1) {
      entries.value[index] = updated
    }
    reopenModal.value = { open: false, entry: null, reason: '', error: '' }
  } catch (e) {
    reopenModal.value.error = e.message || 'Failed to reopen the timesheet.'
  } finally {
    reopeningId.value = null
  }
}
</script>

<template>
  <div class="admin-oversight">
    <div class="page-header">
      <h2>Timesheet Oversight</h2>
      <p>
        Review all staff logs across the institution. Admin can reopen approved or rejected records for controlled correction,
        but cannot silently edit or delete operational history.
      </p>
    </div>

    <div class="filter-bar">
      <div class="search-box">
        <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8" />
          <line x1="21" y1="21" x2="16.65" y2="16.65" />
        </svg>
        <input v-model="searchTerm" type="text" class="filter-input" placeholder="Search staff, task, description, or notes..." />
      </div>

      <div class="filter-group">
        <label>Staff:</label>
        <select v-model="staffFilter" class="filter-select">
          <option value="">All Staff</option>
          <option v-for="staff in staffList" :key="staff.id" :value="staff.username">{{ staff.username }}</option>
        </select>
      </div>

      <div class="filter-group">
        <label>Status:</label>
        <select v-model="statusFilter" class="filter-select">
          <option value="">All</option>
          <option value="PENDING">Pending</option>
          <option value="APPROVED">Approved</option>
          <option value="REJECTED">Rejected</option>
        </select>
      </div>

      <div class="filter-group">
        <label>Date:</label>
        <input v-model="dateFrom" type="date" class="filter-input-small" />
        <span>to</span>
        <input v-model="dateTo" type="date" class="filter-input-small" />
      </div>

      <button class="clear-btn" @click="clearFilters">Clear</button>
    </div>

    <div class="record-count">Showing {{ filteredEntries.length }} of {{ entries.length }} records</div>

    <div v-if="loading" class="state-center">
      <div class="loader"></div>
    </div>
    <div v-else-if="error" class="state-center error-text">{{ error }}</div>
    <div v-else class="table-wrap">
      <table class="erp-table">
        <thead>
          <tr>
            <th>Date</th>
            <th>Staff</th>
            <th>Task</th>
            <th>Description</th>
            <th>Hours</th>
            <th>Status</th>
            <th>Notes</th>
            <th class="center-col">Admin Action</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="entry in filteredEntries" :key="entry.id">
            <td class="date-col">{{ formatDate(entry.date) }}</td>
            <td class="user-col">{{ entry.user }}</td>
            <td><span class="task-badge">{{ entry.task || '—' }}</span></td>
            <td class="desc-col">{{ entry.description }}</td>
            <td class="time-col">{{ format12H(entry.startTime) }} — {{ format12H(entry.endTime) }}</td>
            <td><span class="status-pill" :class="statusClass(entry.status)">{{ entry.status }}</span></td>
            <td>
              <button
                v-if="entry.reviewerRemarks || entry.adminOverrideReason"
                class="note-btn"
                @click="openNotes(entry)"
              >
                View Notes
              </button>
              <span v-else class="empty-dash">—</span>
            </td>
            <td class="center-col">
              <button
                v-if="canReopen(entry)"
                class="reopen-btn"
                :disabled="reopeningId === entry.id"
                @click="openReopenModal(entry)"
              >
                Reopen
              </button>
              <span v-else class="empty-dash">—</span>
            </td>
          </tr>
          <tr v-if="filteredEntries.length === 0">
            <td colspan="8" class="empty-row">No records match current filters.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="noteModal.open" class="modal-overlay" @click.self="closeNotes">
      <div class="modal-box">
        <div class="modal-header">
          <h3>{{ noteModal.title }}</h3>
          <button class="modal-close" @click="closeNotes">&times;</button>
        </div>
        <div class="modal-body">
          <pre class="note-text">{{ noteModal.text }}</pre>
        </div>
        <div class="modal-footer">
          <button class="blue-btn" @click="closeNotes">Close</button>
        </div>
      </div>
    </div>

    <div v-if="reopenModal.open" class="modal-overlay" @click.self="closeReopenModal">
      <div class="modal-box">
        <div class="modal-header">
          <h3>Reopen Timesheet for Correction</h3>
          <button class="modal-close" @click="closeReopenModal">&times;</button>
        </div>
        <div class="modal-body">
          <p class="modal-copy">
            This will move the record back to <strong>PENDING</strong> so the staff member can correct it and send it through review again.
          </p>
          <div class="entry-meta" v-if="reopenModal.entry">
            <span>{{ reopenModal.entry.user }}</span>
            <span>•</span>
            <span>{{ reopenModal.entry.task }}</span>
            <span>•</span>
            <span>{{ reopenModal.entry.status }}</span>
          </div>
          <label class="reason-label" for="admin-reopen-reason">Admin Reason</label>
          <textarea
            id="admin-reopen-reason"
            v-model="reopenModal.reason"
            class="reason-input"
            rows="4"
            placeholder="Explain why the record is being reopened and what correction is required..."
            :disabled="!!reopeningId"
          ></textarea>
          <p v-if="reopenModal.error" class="modal-error">{{ reopenModal.error }}</p>
        </div>
        <div class="modal-footer">
          <button class="secondary-btn" @click="closeReopenModal" :disabled="!!reopeningId">Cancel</button>
          <button class="blue-btn" @click="submitReopen" :disabled="!!reopeningId">
            <span v-if="reopeningId">Reopening...</span>
            <span v-else>Confirm Reopen</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-oversight { display: flex; flex-direction: column; gap: 16px; }

.page-header {
  padding: 14px 20px;
  background: var(--accent-warm);
  border-radius: var(--radius-lg) var(--radius-lg) 0 0;
  margin-bottom: 0;
}
.page-header h2 {
  font-size: 1.0625rem;
  font-weight: 700;
  color: #fff;
  margin: 0 0 2px;
  letter-spacing: -0.01em;
}
.page-header p {
  font-size: 0.78rem;
  color: rgba(255, 255, 255, 0.88);
  margin: 0;
  line-height: 1.5;
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 14px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 0 0 var(--radius-lg) var(--radius-lg);
  padding: 14px 16px;
  border-top: none;
}
.search-box { position: relative; flex: 1; min-width: 220px; }
.search-icon {
  position: absolute;
  left: 10px;
  top: 50%;
  transform: translateY(-50%);
  width: 15px;
  height: 15px;
  color: #94a3b8;
}
.filter-input {
  width: 100%;
  padding: 7px 10px 7px 32px;
  border: 1px solid #cbd5e1;
  border-radius: 4px;
  font-size: 0.85rem;
  box-sizing: border-box;
  outline: none;
}
.filter-group {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 0.85rem;
  color: #475569;
  font-weight: 500;
}
.filter-select, .filter-input-small {
  padding: 6px 8px;
  border: 1px solid #cbd5e1;
  border-radius: 4px;
  font-size: 0.85rem;
  background: white;
  outline: none;
}
.clear-btn {
  background: none;
  border: none;
  color: #64748b;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
}
.clear-btn:hover { color: #1e293b; text-decoration: underline; }

.record-count { font-size: 0.8rem; color: #94a3b8; font-weight: 500; }

.table-wrap {
  overflow-x: auto;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}
.erp-table { width: 100%; border-collapse: collapse; }
.erp-table th {
  background: #f1f5f9;
  padding: 11px 14px;
  font-size: 0.72rem;
  text-transform: uppercase;
  font-weight: 700;
  color: #64748b;
  letter-spacing: 0.05em;
  border-bottom: 2px solid #e2e8f0;
}
.erp-table td {
  padding: 12px 14px;
  font-size: 0.84rem;
  color: #334155;
  border-bottom: 1px solid #f1f5f9;
  vertical-align: middle;
}
.date-col { font-weight: 600; color: #0f172a; white-space: nowrap; }
.user-col { color: #1d4ed8; font-weight: 500; font-size: 0.8rem; }
.desc-col { max-width: 260px; color: #475569; }
.time-col { white-space: nowrap; font-size: 0.8rem; }
.center-col { text-align: center; }

.task-badge {
  background: #f1f5f9;
  padding: 3px 7px;
  border-radius: 4px;
  font-size: 0.78rem;
  border: 1px solid #e2e8f0;
  font-weight: 500;
  color: #475569;
}
.status-pill {
  display: inline-block;
  padding: 3px 9px;
  border-radius: 12px;
  font-size: 0.72rem;
  font-weight: 700;
}
.st-pending { background: #fef3c7; color: #b45309; }
.st-approved { background: #dcfce7; color: #166534; }
.st-rejected { background: #fee2e2; color: #991b1b; }

.note-btn,
.reopen-btn {
  border: 1px solid #cbd5e1;
  border-radius: 4px;
  padding: 4px 9px;
  font-size: 0.78rem;
  font-weight: 600;
  cursor: pointer;
  background: #fff;
}
.note-btn {
  color: #1d4ed8;
  background: #eff6ff;
  border-color: #bfdbfe;
}
.note-btn:hover { background: #dbeafe; }
.reopen-btn {
  color: #92400e;
  background: #fff7ed;
  border-color: #fdba74;
}
.reopen-btn:hover:not(:disabled) { background: #ffedd5; }
.reopen-btn:disabled { opacity: 0.65; cursor: not-allowed; }
.empty-dash { color: #cbd5e1; }
.empty-row {
  text-align: center;
  padding: 50px !important;
  color: #94a3b8;
  font-style: italic;
}

.state-center { display: flex; justify-content: center; padding: 60px; }
.error-text { color: #ef4444; font-size: 0.9rem; }
.loader {
  border: 3px solid #f1f5f9;
  border-top-color: #3b82f6;
  border-radius: 50%;
  width: 28px;
  height: 28px;
  animation: spin 1s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}
.modal-box {
  background: white;
  border-radius: 10px;
  width: 520px;
  max-width: 92vw;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e2e8f0;
}
.modal-header h3 {
  font-size: 1rem;
  font-weight: 600;
  margin: 0;
}
.modal-close {
  background: none;
  border: none;
  font-size: 1.4rem;
  color: #94a3b8;
  cursor: pointer;
  padding: 0;
}
.modal-body {
  padding: 20px;
  font-size: 0.9rem;
  color: #334155;
  line-height: 1.6;
}
.modal-footer {
  padding: 14px 20px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
.modal-copy { margin: 0 0 12px; color: #475569; }
.entry-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-bottom: 14px;
  font-size: 0.78rem;
  color: #64748b;
}
.reason-label {
  display: block;
  font-size: 0.8rem;
  font-weight: 600;
  color: #475569;
  margin-bottom: 6px;
}
.reason-input {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  padding: 10px 12px;
  font: inherit;
  resize: vertical;
  min-height: 110px;
  outline: none;
}
.reason-input:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.08);
}
.modal-error {
  margin-top: 10px;
  color: #991b1b;
  font-size: 0.8rem;
  font-weight: 600;
}
.note-text {
  margin: 0;
  white-space: pre-wrap;
  font: inherit;
}
.blue-btn,
.secondary-btn {
  border: none;
  padding: 8px 16px;
  font-size: 0.85rem;
  font-weight: 600;
  border-radius: 4px;
  cursor: pointer;
}
.blue-btn {
  background: #1d4ed8;
  color: white;
}
.secondary-btn {
  background: #f8fafc;
  color: #475569;
  border: 1px solid #cbd5e1;
}
</style>
