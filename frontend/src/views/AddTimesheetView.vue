<script setup>
import { reactive, ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTimesheetStore } from '@/stores/timesheetStore'
import { storeToRefs } from 'pinia'

const store = useTimesheetStore()
const { taskTypesList, entries } = storeToRefs(store)
const route = useRoute()
const router = useRouter()

const isEditMode = ref(false)
const editingId = ref(null)

onMounted(async () => {
  if (taskTypesList.value.length === 0) {
    await store.fetchTaskTypes()
  }
  
  if (entries.value.length === 0) {
    await store.fetchEntries()
  }

  // Handle Edit Population
  if (route.query.editId) {
    isEditMode.value = true
    editingId.value = parseInt(route.query.editId, 10)
    
    const entry = entries.value.find(e => e.id === editingId.value)
    if (entry && (entry.status === 'PENDING' || entry.status === 'REJECTED')) {
       form.date = entry.date
       
       const startParsed = parse12Hour(entry.startTime)
       form.startH = startParsed.h
       form.startM = startParsed.m
       form.startA = startParsed.a
       
       const endParsed = parse12Hour(entry.endTime)
       form.endH = endParsed.h
       form.endM = endParsed.m
       form.endA = endParsed.a
       
       form.description = entry.description
       if (taskTypesList.value.find(t => t.name === entry.task)) {
           form.taskSelect = entry.task
       } else {
           form.taskText = entry.task
       }
    } else {
       router.replace('/app/history')
    }
  }
})

const parse12Hour = (time24) => {
  if (!time24) return { h: '09', m: '00', a: 'AM' }
  let [h, m] = time24.split(':')
  let hrs = parseInt(h, 10)
  let a = hrs >= 12 ? 'PM' : 'AM'
  hrs = hrs % 12 || 12
  return { h: String(hrs).padStart(2, '0'), m: m, a: a }
}

const form = reactive({
  date: new Date().toISOString().split('T')[0],
  startH: '09',
  startM: '00',
  startA: 'AM',
  endH: '05',
  endM: '00',
  endA: 'PM',
  taskText: '',
  taskSelect: '',
  description: '',
  isLastTask: false
})

const isSaving = ref(false)
const hasSubmitted = ref(false)
const globalErrorMsg = ref('')
const successMsg = ref('')

watch(() => form.taskText, (newVal) => {
  if (newVal) form.taskSelect = ''
})

watch(() => form.taskSelect, (newVal) => {
  if (newVal) form.taskText = ''
})

const hours = Array.from({length: 12}, (_, i) => (i + 1).toString().padStart(2, '0'))
const minutes = Array.from({length: 60}, (_, i) => i.toString().padStart(2, '0'))
const ampm = ['AM', 'PM']

const formatTo24Hour = (h, m, a) => {
  let hrs = parseInt(h, 10)
  let mins = parseInt(m || 0, 10)
  if (isNaN(hrs) || isNaN(mins) || hrs < 1 || hrs > 12 || mins < 0 || mins > 59) return 'INVALID'
  if (a === 'PM' && hrs !== 12) hrs += 12
  if (a === 'AM' && hrs === 12) hrs = 0
  return `${hrs.toString().padStart(2, '0')}:${mins.toString().padStart(2, '0')}:00`
}

const parseDateString = (dateStr) => {
  const parts = dateStr.split('-')
  return new Date(parts[0], parts[1] - 1, parts[2])
}

const computedDuration = computed(() => {
  const start = formatTo24Hour(form.startH, form.startM, form.startA)
  const end = formatTo24Hour(form.endH, form.endM, form.endA)
  if (start === 'INVALID' || end === 'INVALID') return null
  if (start >= end) return null

  const startParts = start.split(':')
  const endParts = end.split(':')
  const startMins = parseInt(startParts[0]) * 60 + parseInt(startParts[1])
  const endMins = parseInt(endParts[0]) * 60 + parseInt(endParts[1])
  
  const diffMins = endMins - startMins
  const hrs = Math.floor(diffMins / 60)
  const mins = diffMins % 60
  
  if (hrs === 0) return `${mins} minutes`
  if (mins === 0) return hrs === 1 ? '1 hour' : `${hrs} hours`
  return `${hrs} ${hrs === 1 ? 'hour' : 'hours'} ${mins} minutes`
})

const validationState = computed(() => {
  const errors = {}
  
  if (!form.date) {
    errors.date = 'Date is required.'
  } else {
    const selectedDate = parseDateString(form.date)
    selectedDate.setHours(0,0,0,0)
    const today = new Date()
    today.setHours(0,0,0,0)
    if (selectedDate > today) {
      errors.date = 'Future dates are not permitted.'
    }
  }

  const startTime24 = formatTo24Hour(form.startH, form.startM, form.startA)
  const endTime24 = formatTo24Hour(form.endH, form.endM, form.endA)
  
  if (startTime24 === 'INVALID' || endTime24 === 'INVALID') {
    errors.time = 'Critical time format violation. Ensure minutes are strictly between 00 and 59.'
  } else if (startTime24 === endTime24) {
    errors.time = 'Start and End times cannot be exactly the same.'
  } else if (startTime24 > endTime24) {
    errors.time = 'End time must be cleanly after the Start time.'
  }

  const finalTask = form.taskText.trim() || form.taskSelect
  if (!finalTask) {
    errors.task = 'Provide a manual task or select from the master list.'
  }

  if (!form.description.trim()) {
    errors.description = 'A detailed description is required.'
  } else if (form.description.trim().length <= 5) {
    errors.description = 'Description is too short.'
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors,
    payload: {
      date: form.date,
      startTime: startTime24,
      endTime: endTime24,
      task: finalTask,
      description: form.description
    }
  }
})

const submitForm = async () => {
  hasSubmitted.value = true 
  globalErrorMsg.value = ''
  successMsg.value = ''

  const state = validationState.value
  if (!state.isValid) {
    globalErrorMsg.value = 'Validation failed. Please correct the highlighted fields.'
    return
  }
  
  try {
    isSaving.value = true
     if (isEditMode.value) {
        await store.updateEntry(editingId.value, state.payload)
        successMsg.value = 'Timesheet entry updated and resubmitted for review.'
        setTimeout(() => router.push('/app/history'), 800)
     } else {
       await store.addEntry(state.payload)
       successMsg.value = 'Timesheet entry successfully saved.'
       form.taskText = ''
       form.taskSelect = ''
       form.description = ''
       form.isLastTask = false
       hasSubmitted.value = false 
    }
  } catch (e) {
    globalErrorMsg.value = e.message || 'System error: Failed to save record.'
  } finally {
    isSaving.value = false
  }
}
</script>

<template>
  <div class="erp-add-view">

    <!-- Panel Header — warm accent strip -->
    <div class="panel-header">
      <div class="panel-header-left">
        <h2 class="panel-title">{{ isEditMode ? 'Modify Timesheet Entry' : 'Add Timesheet Entry' }}</h2>
        <p class="panel-note">Complete all required fields. Select <strong>Yes</strong> for "Last task of the day" to mark daily completion.</p>
      </div>
      <div v-if="isEditMode" class="edit-mode-badge">Edit Mode</div>
    </div>

    <!-- Global State Banner -->
    <div v-if="globalErrorMsg" class="form-feedback error">
      <span class="fb-icon">⚠</span>
      <div>
        <strong>Submission Failed:</strong>
        <p>{{ globalErrorMsg }}</p>
      </div>
    </div>
    <div v-if="successMsg" class="form-feedback success">
      <span class="fb-icon">✓</span>
      <div>
        <strong>Success:</strong>
        <p>{{ successMsg }}</p>
      </div>
    </div>

    <form @submit.prevent="submitForm" class="erp-form" novalidate>

      <!-- Section: Date & Time -->
      <div class="form-section">
        <div class="form-section-header-wrap">
          <div class="form-section-label">Date &amp; Time</div>
          <div v-if="computedDuration" class="duration-badge">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="clock-icon"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>
            {{ computedDuration }}
          </div>
        </div>
        <div class="form-row">
          <div class="input-block date-block" :class="{ 'has-field-error': hasSubmitted && validationState.errors.date }">
            <label>Date <span class="req">*</span></label>
            <div class="input-wrapper">
              <input type="date" v-model="form.date" class="classic-input clean-date" :disabled="isSaving" />
            </div>
            <span class="field-error" v-if="hasSubmitted && validationState.errors.date">{{ validationState.errors.date }}</span>
          </div>

          <div class="input-block time-block" :class="{ 'has-field-error': hasSubmitted && validationState.errors.time }">
            <label>Start Time <span class="req">*</span></label>
            <div class="time-entry-group">
              <div class="time-select-wrap">
                <span class="time-hint">HH</span>
                <select v-model="form.startH" class="time-select" :disabled="isSaving">
                  <option v-for="h in hours" :key="'start-hour-' + h" :value="h">{{ h }}</option>
                </select>
              </div>
              <span class="time-divider">:</span>
              <div class="time-select-wrap">
                <span class="time-hint">MM</span>
                <select v-model="form.startM" class="time-select" :disabled="isSaving">
                  <option v-for="m in minutes" :key="'start-minute-' + m" :value="m">{{ m }}</option>
                </select>
              </div>
              <div class="time-ampm-wrap">
                <select v-model="form.startA" class="time-ampm" :disabled="isSaving">
                  <option v-for="a in ampm" :key="'sa'+a" :value="a">{{ a }}</option>
                </select>
              </div>
            </div>
          </div>

          <div class="input-block time-block" :class="{ 'has-field-error': hasSubmitted && validationState.errors.time }">
            <label>End Time <span class="req">*</span></label>
            <div class="time-entry-group">
              <div class="time-select-wrap">
                <span class="time-hint">HH</span>
                <select v-model="form.endH" class="time-select" :disabled="isSaving">
                  <option v-for="h in hours" :key="'end-hour-' + h" :value="h">{{ h }}</option>
                </select>
              </div>
              <span class="time-divider">:</span>
              <div class="time-select-wrap">
                <span class="time-hint">MM</span>
                <select v-model="form.endM" class="time-select" :disabled="isSaving">
                  <option v-for="m in minutes" :key="'end-minute-' + m" :value="m">{{ m }}</option>
                </select>
              </div>
              <div class="time-ampm-wrap">
                <select v-model="form.endA" class="time-ampm" :disabled="isSaving">
                  <option v-for="a in ampm" :key="'ea'+a" :value="a">{{ a }}</option>
                </select>
              </div>
            </div>
          </div>

          <div class="input-block checkbox-block">
            <label class="checkbox-label" :class="{ 'disabled-text': isSaving }">
              <input type="checkbox" v-model="form.isLastTask" :disabled="isSaving" class="erp-checkbox" />
              Last task of the day?
            </label>
          </div>
        </div>
        <div class="row-error" v-if="hasSubmitted && validationState.errors.time">{{ validationState.errors.time }}</div>
      </div>

      <!-- Section: Task Category -->
      <div class="form-section">
        <div class="form-section-label">Task Category</div>
        <div class="form-row align-center task-row">
          <div class="input-block flex-1" :class="{ 'has-field-error': hasSubmitted && validationState.errors.task }">
            <label>Custom Entry</label>
            <div class="input-wrapper">
              <input type="text" v-model="form.taskText" placeholder="Type a custom task name..." class="classic-input" :disabled="isSaving" />
            </div>
          </div>

          <div class="or-separator">OR</div>

          <div class="input-block flex-1" :class="{ 'has-field-error': hasSubmitted && validationState.errors.task }">
            <label>Select from Master List</label>
            <div class="select-with-btn">
              <div class="input-wrapper select-wrapper flex-full">
                <select v-model="form.taskSelect" class="classic-input" :disabled="isSaving">
                  <option value="">— Choose Assigned Module —</option>
                  <option v-for="task in taskTypesList" :key="task.id" :value="task.name">{{ task.name }}</option>
                </select>
              </div>
            </div>
            <span class="field-hint">Master task categories are maintained by Admin. Use Custom Entry only for approved one-off work not yet present in the list.</span>
          </div>
        </div>
        <div class="row-error" v-if="hasSubmitted && validationState.errors.task">{{ validationState.errors.task }}</div>
      </div>

      <!-- Section: Description -->
      <div class="form-section">
        <div class="form-section-label">Deliverable Description</div>
        <div class="form-row">
          <div class="input-block flex-full" :class="{ 'has-field-error': hasSubmitted && validationState.errors.description }">
            <label>Description <span class="req">*</span></label>
            <div class="input-wrapper">
              <textarea v-model="form.description" rows="4" placeholder="Describe the work accomplished during this session..." class="classic-input" :disabled="isSaving"></textarea>
            </div>
            <span class="field-error" v-if="hasSubmitted && validationState.errors.description">{{ validationState.errors.description }}</span>
          </div>
        </div>
      </div>

      <!-- Panel Footer / Submit -->
      <div class="form-panel-footer">
        <span class="footer-policy">Timesheet Integrity Monitored &bull; Institutional Policy 4.2</span>
        <div class="form-actions">
          <button v-if="isEditMode" type="button" @click="router.push('/app/history')" class="btn-cancel" :disabled="isSaving">Cancel</button>
          <button type="submit" class="btn-primary-action" :disabled="isSaving">
            <span v-if="isSaving" class="btn-flex"><span class="loader"></span> Saving...</span>
            <span v-else>{{ isEditMode ? 'Update Entry' : 'Save Entry' }}</span>
          </button>
        </div>
      </div>

    </form>
  </div>
</template>

<style scoped>
/* ── Panel Shell ─────────────────────────────────────────────────────────── */
.erp-add-view {
  background-color: var(--surface-color);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* ── Panel Header — warm amber accent strip (from screenshot reference) ───── */
.panel-header {
  padding: 16px 28px;
  background: var(--accent-warm);
  border-bottom: none;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-header-left {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.panel-title {
  font-size: 1.0625rem;
  font-weight: 700;
  color: var(--accent-warm-text);
  margin: 0;
  letter-spacing: -0.01em;
}

.panel-note {
  font-size: 0.775rem;
  color: rgba(255, 255, 255, 0.88);
  margin: 0;
  max-width: 620px;
  line-height: 1.5;
}

.edit-mode-badge {
  background-color: rgba(255, 255, 255, 0.22);
  color: var(--accent-warm-text);
  font-size: 0.7rem;
  font-weight: 700;
  padding: 4px 12px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  white-space: nowrap;
  letter-spacing: 0.03em;
}

/* ── Form Feedback Banners ───────────────────────────────────────────────── */
.form-feedback {
  margin: 16px 28px 0;
  padding: 12px 16px;
  border-radius: var(--radius-md);
  font-size: 0.875rem;
  display: flex;
  align-items: flex-start;
  gap: 12px;
}
.fb-icon { font-size: 1.1rem; font-weight: bold; margin-top: -1px; }
.form-feedback p { margin: 3px 0 0; font-weight: normal; }
.form-feedback.error { color: #991b1b; border: 1px solid #fecaca; background-color: #fef2f2; }
.form-feedback.success { color: #166534; border: 1px solid #bbf7d0; background-color: #f0fdf4; }

/* ── Form Body ───────────────────────────────────────────────────────────── */
.erp-form {
  padding: 0;
  flex: 1;
}

.form-section {
  padding: 20px 28px;
  border-bottom: 1px solid var(--border-light);
}

.form-section-header-wrap {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.form-section-label {
  font-size: 0.65rem;
  text-transform: uppercase;
  font-weight: 700;
  letter-spacing: 0.07em;
  color: var(--text-muted);
  margin-bottom: 14px;
}

.form-section-header-wrap .form-section-label {
  margin-bottom: 0;
}

.duration-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  background-color: #f1f5f9;
  color: #475569;
  font-size: 0.75rem;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 12px;
  border: 1px solid #cbd5e1;
}

.clock-icon {
  width: 13px;
  height: 13px;
  color: #64748b;
}

.form-row {
  display: flex;
  gap: 20px;
  align-items: stretch;
}

.form-row.task-row { margin-bottom: 4px; }
.form-row.align-center { align-items: center; }

/* ── Input Blocks ────────────────────────────────────────────────────────── */
.input-block {
  display: flex;
  flex-direction: column;
}

.flex-1 { flex: 1; }
.flex-full { width: 100%; }
.date-block { width: 200px; flex-shrink: 0; }
.time-block { min-width: 180px; flex-shrink: 0; }
.checkbox-block {
  flex: 1;
  display: flex;
  align-items: flex-end;
  padding-bottom: 2px;
}

label {
  font-size: 0.78rem;
  font-weight: 600;
  color: #475569;
  margin-bottom: 6px;
  display: inline-block;
}

.req { color: #dc2626; margin-left: 2px; }

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  font-size: 0.82rem;
  color: #475569;
  cursor: pointer;
}

.erp-checkbox {
  width: 15px;
  height: 15px;
  cursor: pointer;
  flex-shrink: 0;
}

.disabled-text { color: var(--text-muted); cursor: not-allowed; }

.or-separator {
  font-weight: 700;
  font-size: 0.75rem;
  color: var(--text-muted);
  padding-top: 20px;
  flex-shrink: 0;
}

/* ── Inputs ──────────────────────────────────────────────────────────────── */
.input-wrapper {
  position: relative;
  display: flex;
  background-color: #fff;
}

.select-wrapper::after {
  content: '▼';
  font-size: 0.55rem;
  color: #888;
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  pointer-events: none;
}

.classic-input {
  box-sizing: border-box;
  width: 100%;
  padding: 8px 10px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  font-size: 0.84rem;
  font-family: inherit;
  color: var(--text-main);
  background-color: transparent;
  appearance: none;
  -webkit-appearance: none;
}

.classic-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.08);
}

textarea.classic-input {
  resize: vertical;
  min-height: 90px;
  line-height: 1.5;
}

.classic-input:disabled {
  background-color: #f8fafc;
  color: var(--text-muted);
  cursor: not-allowed;
  border-color: var(--border-light);
}

.clean-date::-webkit-calendar-picker-indicator {
  cursor: pointer;
  opacity: 0.5;
}
.clean-date::-webkit-calendar-picker-indicator:hover { opacity: 0.9; }

/* 12-HR Time Selectors */
.time-entry-group {
  display: flex;
  align-items: flex-start;
  gap: 6px;
}
.time-select-wrap,
.time-input-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background-color: #fff;
  padding: 5px 6px 4px;
  width: 44px;
  box-sizing: border-box;
  transition: all 0.15s;
}
.time-select-wrap {
  width: 52px;
}
.time-input-wrap:focus-within,
.time-select-wrap:focus-within {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.08);
}
.time-hint {
  font-size: 0.55rem;
  font-weight: 700;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 3px;
}
.time-select {
  width: 100%;
  border: none;
  background: transparent;
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--text-main);
  text-align: center;
  text-align-last: center;
  font-family: inherit;
  padding: 0;
  cursor: pointer;
  appearance: none;
  -webkit-appearance: none;
  line-height: 1.2;
}
.time-select:focus {
  outline: none;
}
.time-select:disabled {
  cursor: not-allowed;
  color: var(--text-muted);
}

.time-divider {
  font-weight: 700;
  color: #475569;
  font-size: 1rem;
  margin-top: 19px;
}

.time-ampm-wrap {
  margin-left: 2px;
}
.time-ampm {
  border: 1px solid var(--border-color);
  background: #f1f5f9;
  border-radius: var(--radius-md);
  height: 48px;
  font-weight: 700;
  font-size: 0.78rem;
  color: #334155;
  width: 48px;
  text-align: center;
  text-align-last: center;
  appearance: none;
  -webkit-appearance: none;
  cursor: pointer;
  padding: 0;
  margin-top: 0;
}
.time-ampm:focus { outline: none; border-color: var(--primary-color); }
.time-ampm:disabled { cursor: not-allowed; color: var(--text-muted); opacity: 0.8; }

/* Error States */
.has-field-error .classic-input,
.has-field-error .time-select-wrap { border-color: #dc2626; box-shadow: 0 0 0 1px #fee2e2; }
.has-field-error .time-ampm { border-color: #fca5a5; background-color: #fee2e2; }

.field-error {
  color: #dc2626;
  font-size: 0.72rem;
  margin-top: 4px;
  font-weight: 500;
  display: block;
}
.field-hint {
  color: var(--text-muted);
  display: block;
  font-size: 0.72rem;
  line-height: 1.45;
  margin-top: 6px;
}
.row-error {
  color: #dc2626;
  font-size: 0.78rem;
  margin-top: 6px;
  font-weight: 600;
}

/* Task Manage Button */
.select-with-btn { display: flex; gap: 10px; align-items: center; width: 100%; }
.select-wrapper { flex: 1; }

.btn-manage-circle {
  width: 34px;
  height: 34px;
  border-radius: var(--radius-md);
  background-color: var(--accent-warm);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
  transition: background-color 0.15s;
  color: white;
}
.btn-manage-circle:hover { background-color: var(--accent-warm-dark); }
.btn-manage-circle:disabled { background-color: #cbd5e1; cursor: not-allowed; }
.add-icon { width: 16px; height: 16px; color: white; }

/* ── Panel Footer ────────────────────────────────────────────────────────── */
.form-panel-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 28px;
  background-color: #fafbfc;
  border-top: 1px solid var(--border-light);
}

.footer-policy {
  font-size: 0.72rem;
  color: var(--text-muted);
}

.form-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.btn-cancel {
  background: transparent;
  border: 1px solid var(--border-color);
  font-size: 0.84rem;
  font-weight: 600;
  color: var(--text-muted);
  cursor: pointer;
  padding: 8px 18px;
  border-radius: var(--radius-md);
  transition: all 0.15s;
}
.btn-cancel:hover { border-color: #94a3b8; color: var(--text-main); }

.btn-primary-action {
  background-color: var(--primary-color);
  color: #ffffff;
  border: none;
  padding: 9px 28px;
  font-size: 0.875rem;
  font-weight: 600;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background-color 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.btn-primary-action:hover:not(:disabled) { background-color: var(--primary-hover); }
.btn-primary-action:disabled { background-color: #94a3b8; cursor: not-allowed; }

/* Loader */
.btn-flex { display: flex; align-items: center; gap: 8px; }
.loader {
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  width: 14px; height: 14px;
  animation: spin 0.8s linear infinite;
  display: inline-block;
}
.small-loader {
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  width: 12px; height: 12px;
  animation: spin 0.8s linear infinite;
  display: inline-block;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ── Task Modal ──────────────────────────────────────────────────────────── */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}
.modal-card {
  background: white;
  border-radius: 10px;
  width: 480px;
  max-width: 90vw;
  box-shadow: 0 20px 60px rgba(0,0,0,0.18);
  overflow: hidden;
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  border-bottom: none;
  background: var(--accent-warm);
}
.modal-header h3 { font-size: 0.9375rem; font-weight: 700; margin: 0; color: #fff; }
.btn-close-modal {
  background: rgba(255,255,255,0.2);
  border: 1px solid rgba(255,255,255,0.35);
  border-radius: 4px;
  font-size: 0.9rem;
  color: #fff;
  cursor: pointer;
  padding: 2px 7px;
  transition: background 0.15s;
}
.btn-close-modal:hover { background: rgba(255,255,255,0.35); }

.modal-error {
  margin: 12px 20px 0;
  padding: 10px 14px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: var(--radius-md);
  color: #991b1b;
  font-size: 0.82rem;
}

.add-task-form {
  display: flex;
  gap: 10px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-light);
}
.add-task-form .classic-input { flex: 1; }

.blue-btn {
  background-color: var(--primary-color);
  color: #fff;
  border: none;
  padding: 8px 18px;
  font-size: 0.84rem;
  font-weight: 600;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background-color 0.15s;
}
.blue-btn:hover:not(:disabled) { background-color: var(--primary-hover); }
.blue-btn:disabled { background-color: #94a3b8; cursor: not-allowed; }
.small-btn { padding: 7px 14px; white-space: nowrap; }

.task-list {
  max-height: 220px;
  overflow-y: auto;
  padding: 8px 0;
}
.task-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 9px 20px;
  border-bottom: 1px solid var(--border-light);
}
.task-item:last-child { border-bottom: none; }
.task-name { font-size: 0.84rem; color: var(--text-main); font-weight: 500; }
.btn-delete-task {
  background: none;
  border: 1px solid #fca5a5;
  color: #dc2626;
  font-size: 0.75rem;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.15s;
}
.btn-delete-task:hover:not(:disabled) { background: #fee2e2; }
.btn-delete-task:disabled { opacity: 0.5; cursor: not-allowed; }

.empty-state { padding: 24px 20px; text-align: center; color: var(--text-muted); font-size: 0.84rem; }

.modal-actions-footer {
  padding: 12px 20px;
  border-top: 1px solid var(--border-light);
  display: flex;
  justify-content: flex-end;
}
.cancel-btn {
  background: none;
  border: 1px solid var(--border-color);
  color: var(--text-muted);
  font-size: 0.84rem;
  font-weight: 600;
  padding: 7px 18px;
  border-radius: var(--radius-md);
  cursor: pointer;
}
.cancel-btn:hover { border-color: #94a3b8; color: var(--text-main); }
</style>
