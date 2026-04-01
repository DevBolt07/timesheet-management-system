<script setup>
import { onMounted, computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useTimesheetStore } from '@/stores/timesheetStore'
import { storeToRefs } from 'pinia'

const store = useTimesheetStore()
const { entries, isLoading, isError } = storeToRefs(store)

onMounted(() => {
  store.fetchEntries()
})

// Focus purely on Reviewable Queue
const pendingEntries = computed(() => {
  return entries.value.filter(e => e.status === 'PENDING')
})

const processingId = ref(null)
const remarksInput = ref({})

const submitReview = async (id, decision) => {
    try {
        processingId.value = id
        const payload = {
            status: decision,
            remarks: remarksInput.value[id] || ''
        }
        await store.reviewEntry(id, payload)
        // Cleanup remarks map softly
        remarksInput.value[id] = ''
    } catch (e) {
        alert("Workflow action failed: " + e.message)
    } finally {
        processingId.value = null
    }
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
</script>

<template>
  <div class="review-timesheets">
    <div class="toolbar">
      <h2>Pending Management Review Workspace</h2>
      <div class="stat-badge">
        <span class="stat-num">{{ pendingEntries.length }}</span> Require Action
      </div>
    </div>

    <!-- State Holders -->
    <div v-if="isLoading" class="state-container">
      <div class="loader-lg"></div>
      <p>Synchronizing queue...</p>
    </div>

    <div v-else-if="isError" class="state-container error">
      <div class="err-icon">⚠</div>
      <p>Internal System Error: {{ isError }}</p>
      <button @click="store.fetchEntries" class="blue-btn">Retry Connection</button>
    </div>

    <!-- Queue Engine -->
    <div v-else class="queue-container">
       <div v-for="entry in pendingEntries" :key="entry.id" class="review-card">
          
          <div class="rc-header">
             <div class="staff-info">
                 <div class="avatar">U</div>
                 <div>
                    <h4>{{ entry.user || 'Unknown Staff' }}</h4>
                    <span>{{ formatDate(entry.date) }}</span>
                 </div>
             </div>
             <div class="time-metrics">
                 <span class="time-block">{{ format12H(entry.startTime) }} - {{ format12H(entry.endTime) }}</span>
             </div>
          </div>

          <div class="rc-body">
              <div class="task-cat">{{ entry.task || 'Unclassified' }}</div>
              <p class="task-desc">"{{ entry.description }}"</p>
          </div>

          <div class="rc-footer">
              <div class="remark-form">
                 <input type="text" v-model="remarksInput[entry.id]" placeholder="Add optional reviewer remarks..." class="remark-input" :disabled="processingId === entry.id" />
              </div>
              <div class="action-buttons">
                  <button 
                     class="btn review-reject" 
                     @click="submitReview(entry.id, 'REJECTED')"
                     :disabled="processingId === entry.id"
                  >
                     {{ processingId === entry.id ? '...' : 'Reject' }}
                  </button>
                  <button 
                     class="btn review-approve" 
                     @click="submitReview(entry.id, 'APPROVED')"
                     :disabled="processingId === entry.id"
                  >
                     {{ processingId === entry.id ? 'Saving...' : 'Approve' }}
                  </button>
              </div>
          </div>

       </div>

       <div v-if="pendingEntries.length === 0" class="empty-queue">
           <svg viewBox="0 0 24 24" fill="none" class="empty-icon"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M22 4L12 14.01L9 11.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
           <h3>Inbox Zero.</h3>
           <p>All timesheets have been processed.</p>
       </div>
    </div>
  </div>
</template>

<style scoped>
.review-timesheets {
  background-color: transparent;
  min-height: calc(100vh - 130px);
  display: flex;
  flex-direction: column;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  margin-bottom: 24px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.02);
}

.toolbar h2 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.stat-badge {
  background-color: #fef3c7;
  color: #92400e;
  padding: 6px 16px;
  border-radius: 20px;
  font-weight: 600;
  font-size: 0.85rem;
  display: flex;
  align-items: center;
  gap: 8px;
}
.stat-num {
  background-color: #d97706;
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
}

.state-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 80px 20px;
  color: #64748b;
  flex: 1;
  background-color: #ffffff;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.loader-lg {
  border: 3px solid #f1f5f9;
  border-top-color: #3b82f6;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

.err-icon {
  font-size: 2rem;
  margin-bottom: 12px;
  color: #dc2626;
}

@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

/* Queue Board */
.queue-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-bottom: 32px;
}

.review-card {
  background-color: #ffffff;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.04);
  transition: all 0.2s;
}

.review-card:hover {
  border-color: #94a3b8;
  box-shadow: 0 4px 6px rgba(0,0,0,0.04);
}

.rc-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px 24px;
  border-bottom: 1px solid #f1f5f9;
}

.staff-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 40px; height: 40px;
  border-radius: 50%;
  background-color: #e0e7ff;
  color: #4f46e5;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
}

.staff-info h4 { margin: 0; font-size: 0.95rem; color: #1e293b; }
.staff-info span { font-size: 0.8rem; color: #64748b; }

.time-metrics {
  text-align: right;
}
.time-block {
  background-color: #f1f5f9;
  padding: 4px 10px;
  border-radius: 4px;
  font-weight: 600;
  font-size: 0.85rem;
  color: #334155;
  border: 1px solid #e2e8f0;
}

.rc-body {
  padding: 16px 24px;
}

.task-cat {
  font-size: 0.75rem;
  text-transform: uppercase;
  font-weight: 700;
  color: #0ea5e9;
  letter-spacing: 0.05em;
  margin-bottom: 8px;
}

.task-desc {
  margin: 0;
  font-size: 0.95rem;
  color: #334155;
  line-height: 1.5;
  font-style: italic;
}

.rc-footer {
  padding: 12px 24px;
  background-color: #f8fafc;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom-left-radius: 8px;
  border-bottom-right-radius: 8px;
  gap: 16px;
}

.remark-form {
  flex: 1;
}

.remark-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #cbd5e1;
  border-radius: 4px;
  font-size: 0.85rem;
  outline: none;
  background-color: #fff;
}
.remark-input:focus {
  border-color: #3b82f6;
  box-shadow: inset 0 0 0 1px #3b82f6;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.btn {
  padding: 8px 20px;
  font-size: 0.85rem;
  font-weight: 600;
  border-radius: 4px;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.review-reject {
  background-color: transparent;
  border: 1px solid #ef4444;
  color: #ef4444;
}
.review-reject:hover:not(:disabled) {
  background-color: #fee2e2;
}

.review-approve {
  background-color: #22c55e;
  color: white;
  box-shadow: 0 1px 2px rgba(34, 197, 94, 0.3);
}
.review-approve:hover:not(:disabled) {
  background-color: #16a34a;
}

.empty-queue {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  background-color: #ffffff;
  border-radius: 8px;
  border: 1px dashed #cbd5e1;
  color: #64748b;
}

.empty-icon {
  width: 48px; height: 48px;
  color: #22c55e;
  margin-bottom: 16px;
}

.empty-queue h3 { margin: 0 0 8px 0; font-size: 1.25rem; color: #1e293b; }
.empty-queue p { margin: 0; font-size: 0.9rem; }
</style>
