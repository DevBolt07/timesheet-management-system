<script setup>
import { onMounted, ref, computed } from 'vue'
import { apiClient } from '@/services/api'

const entries = ref([])
const staffList = ref([])
const loading = ref(true)
const error = ref('')

const searchTerm = ref('')
const statusFilter = ref('')
const staffFilter = ref('')
const dateFrom = ref('')
const dateTo = ref('')

onMounted(async () => {
  try {
    const [ts, staff] = await Promise.all([
      apiClient.get('/api/admin/timesheets'),
      apiClient.get('/api/users/staff')
    ])
    entries.value = ts
    staffList.value = staff
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
})

const clearFilters = () => {
  searchTerm.value = ''
  statusFilter.value = ''
  staffFilter.value = ''
  dateFrom.value = ''
  dateTo.value = ''
}

const filtered = computed(() => entries.value.filter(e => {
  if (staffFilter.value && e.user !== staffFilter.value) return false
  if (statusFilter.value && e.status !== statusFilter.value) return false
  if (searchTerm.value) {
    const q = searchTerm.value.toLowerCase()
    if (!(e.task || '').toLowerCase().includes(q) && !(e.description || '').toLowerCase().includes(q) && !(e.user || '').toLowerCase().includes(q)) return false
  }
  if (dateFrom.value && new Date(e.date) < new Date(dateFrom.value)) return false
  if (dateTo.value && new Date(e.date) > new Date(dateTo.value)) return false
  return true
}))

const remarkModal = ref({ open: false, text: '' })
const openRemark = (text) => remarkModal.value = { open: true, text }
const closeRemark = () => remarkModal.value = { open: false, text: '' }

const format12H = (t) => {
  if (!t) return ''
  let [h, m] = t.split(':')
  let hrs = parseInt(h, 10)
  return `${hrs % 12 || 12}:${m} ${hrs >= 12 ? 'PM' : 'AM'}`
}
const formatDate = (s) => s ? new Date(s).toLocaleDateString('en-US', { day: '2-digit', month: 'short', year: 'numeric' }) : ''
const statusClass = (s) => s === 'APPROVED' ? 'st-approved' : s === 'REJECTED' ? 'st-rejected' : 'st-pending'
</script>

<template>
  <div class="admin-oversight">
    <div class="page-header">
      <h2>Timesheet Oversight</h2>
      <p>Full audit view across all staff entries. Read-only — workflow actions remain in the Manager Review module.</p>
    </div>

    <div class="filter-bar">
      <div class="search-box">
        <svg class="si" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
        <input type="text" v-model="searchTerm" placeholder="Search user, task, or description..." class="fi" />
      </div>
      <div class="fg">
        <label>Staff:</label>
        <select v-model="staffFilter" class="fs">
          <option value="">All Staff</option>
          <option v-for="s in staffList" :key="s.id" :value="s.username">{{ s.username }}</option>
        </select>
      </div>
      <div class="fg">
        <label>Status:</label>
        <select v-model="statusFilter" class="fs">
          <option value="">All</option>
          <option value="PENDING">Pending</option>
          <option value="APPROVED">Approved</option>
          <option value="REJECTED">Rejected</option>
        </select>
      </div>
      <div class="fg">
        <label>Date:</label>
        <input type="date" v-model="dateFrom" class="fis" />
        <span>–</span>
        <input type="date" v-model="dateTo" class="fis" />
      </div>
      <button @click="clearFilters" class="clear-btn">Clear</button>
    </div>

    <div class="record-count">Showing {{ filtered.length }} of {{ entries.length }} records</div>

    <div v-if="loading" class="state-center"><div class="loader"></div></div>
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
            <th>Remarks</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="e in filtered" :key="e.id">
            <td class="date-col">{{ formatDate(e.date) }}</td>
            <td class="user-col">{{ e.user }}</td>
            <td><span class="task-badge">{{ e.task || '—' }}</span></td>
            <td class="desc-col">{{ e.description }}</td>
            <td class="time-col">{{ format12H(e.startTime) }} – {{ format12H(e.endTime) }}</td>
            <td><span class="status-pill" :class="statusClass(e.status)">{{ e.status }}</span></td>
            <td>
              <button v-if="e.reviewerRemarks" class="remark-btn" @click="openRemark(e.reviewerRemarks)">💬 View</button>
              <span v-else class="empty-dash">—</span>
            </td>
          </tr>
          <tr v-if="filtered.length === 0">
            <td colspan="7" class="empty-row">No records match current filters.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="remarkModal.open" class="modal-overlay" @click.self="closeRemark">
      <div class="modal-box">
        <div class="modal-hdr"><h3>Reviewer Remark</h3><button @click="closeRemark">&times;</button></div>
        <div class="modal-body"><p>{{ remarkModal.text }}</p></div>
        <div class="modal-ftr"><button class="blue-btn" @click="closeRemark">Close</button></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-oversight { display: flex; flex-direction: column; gap: 16px; }
.page-header { padding: 14px 20px; background: var(--accent-warm); border-radius: var(--radius-lg) var(--radius-lg) 0 0; margin-bottom: 0; }
.page-header h2 { font-size: 1.0625rem; font-weight: 700; color: #fff; margin: 0 0 2px; letter-spacing: -0.01em; }
.page-header p  { font-size: 0.78rem; color: rgba(255,255,255,0.85); margin: 0; }

.filter-bar { display: flex; flex-wrap: wrap; align-items: center; gap: 14px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 0 0 var(--radius-lg) var(--radius-lg); padding: 14px 16px; border-top: none; }
.search-box { position: relative; flex: 1; min-width: 200px; }
.si { position: absolute; left: 10px; top: 50%; transform: translateY(-50%); width: 15px; height: 15px; color: #94a3b8; }
.fi { width: 100%; padding: 7px 10px 7px 32px; border: 1px solid #cbd5e1; border-radius: 4px; font-size: 0.85rem; box-sizing: border-box; outline: none; }
.fg { display: flex; align-items: center; gap: 7px; font-size: 0.85rem; color: #475569; font-weight: 500; }
.fs, .fis { padding: 6px 8px; border: 1px solid #cbd5e1; border-radius: 4px; font-size: 0.85rem; background: white; outline: none; }
.clear-btn { background: none; border: none; color: #64748b; font-size: 0.85rem; font-weight: 600; cursor: pointer; }
.clear-btn:hover { color: #1e293b; text-decoration: underline; }

.record-count { font-size: 0.8rem; color: #94a3b8; font-weight: 500; }

.table-wrap { overflow-x: auto; background: white; border: 1px solid #e2e8f0; border-radius: 8px; }
.erp-table { width: 100%; border-collapse: collapse; }
.erp-table th { background: #f1f5f9; padding: 11px 14px; font-size: 0.72rem; text-transform: uppercase; font-weight: 700; color: #64748b; letter-spacing: 0.05em; border-bottom: 2px solid #e2e8f0; }
.erp-table td { padding: 12px 14px; font-size: 0.84rem; color: #334155; border-bottom: 1px solid #f1f5f9; vertical-align: middle; }
.date-col { font-weight: 600; color: #0f172a; white-space: nowrap; }
.user-col { color: #1d4ed8; font-weight: 500; font-size: 0.8rem; }
.desc-col { max-width: 240px; color: #475569; }
.time-col { white-space: nowrap; font-size: 0.8rem; }

.task-badge { background: #f1f5f9; padding: 3px 7px; border-radius: 4px; font-size: 0.78rem; border: 1px solid #e2e8f0; font-weight: 500; color: #475569; }
.status-pill { display: inline-block; padding: 3px 9px; border-radius: 12px; font-size: 0.72rem; font-weight: 700; }
.st-pending  { background: #fef3c7; color: #b45309; }
.st-approved { background: #dcfce7; color: #166534; }
.st-rejected { background: #fee2e2; color: #991b1b; }
.remark-btn { background: #eff6ff; border: 1px solid #bfdbfe; color: #1d4ed8; font-size: 0.78rem; font-weight: 600; padding: 3px 8px; border-radius: 4px; cursor: pointer; }
.remark-btn:hover { background: #dbeafe; }
.empty-dash { color: #cbd5e1; }
.empty-row { text-align: center; padding: 50px !important; color: #94a3b8; font-style: italic; }

.state-center { display: flex; justify-content: center; padding: 60px; }
.error-text { color: #ef4444; font-size: 0.9rem; }
.loader { border: 3px solid #f1f5f9; border-top-color: #3b82f6; border-radius: 50%; width: 28px; height: 28px; animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.modal-overlay { position: fixed; inset: 0; background: rgba(15,23,42,0.4); display: flex; align-items: center; justify-content: center; z-index: 999; }
.modal-box { background: white; border-radius: 10px; width: 450px; max-width: 90vw; box-shadow: 0 10px 40px rgba(0,0,0,0.15); }
.modal-hdr { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; border-bottom: 1px solid #e2e8f0; }
.modal-hdr h3 { font-size: 1rem; font-weight: 600; margin: 0; }
.modal-hdr button { background: none; border: none; font-size: 1.4rem; color: #94a3b8; cursor: pointer; padding: 0; }
.modal-body { padding: 20px; font-size: 0.9rem; color: #334155; line-height: 1.6; }
.modal-ftr { padding: 14px 20px; border-top: 1px solid #e2e8f0; display: flex; justify-content: flex-end; }
.blue-btn { background: #1d4ed8; color: white; border: none; padding: 8px 16px; font-size: 0.85rem; font-weight: 600; border-radius: 4px; cursor: pointer; }
</style>
