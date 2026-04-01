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
    if (entry && entry.status === 'PENDING') {
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
       // Check if task exists in master list, otherwise it must have been a manual text
       if (taskTypesList.value.find(t => t.name === entry.task)) {
           form.taskSelect = entry.task
       } else {
           form.taskText = entry.task
       }
    } else {
       // Invalid edit request or locked status
       router.replace('/app/history')
    }
  }
})

// Time Helpers
const parse12Hour = (time24) => {
  if (!time24) return { h: '09', m: '00', a: 'AM' }
  let [h, m] = time24.split(':')
  let hrs = parseInt(h, 10)
  let a = hrs >= 12 ? 'PM' : 'AM'
  hrs = hrs % 12 || 12
  return { h: String(hrs).padStart(2, '0'), m: m, a: a }
}

// UI Form State
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

// Task Management Modal State
const showTaskModal = ref(false)
const newTaskName = ref('')
const isManagingTasks = ref(false)
const taskManageError = ref('')

const openManageTasks = () => {
    showTaskModal.value = true
    taskManageError.value = ''
    newTaskName.value = ''
}

const submitNewTask = async () => {
    if (!newTaskName.value.trim()) return
    taskManageError.value = ''
    try {
        isManagingTasks.value = true
        await store.addTaskType(newTaskName.value.trim())
        newTaskName.value = '' // clear on success
    } catch(e) {
        taskManageError.value = e.message || 'Failed to add task category. Name might already exist.'
    } finally {
        isManagingTasks.value = false
    }
}

const removeTask = async (id, name) => {
    if (!confirm(`Are you sure you want to remove the category '${name}'? Existing time records using this category will not be broken, it will simply be deactivated.`)) return
    try {
        isManagingTasks.value = true
        taskManageError.value = ''
        await store.deleteTaskType(id)
        
        // Safety wipe if user had it currently selected in the main form
        if (form.taskSelect === name) form.taskSelect = ''
        
    } catch (e) {
        taskManageError.value = e.message || 'Failed to safely remove category.'
    } finally {
        isManagingTasks.value = false
    }
}

// Watchers for Task Input mutual exclusivity
watch(() => form.taskText, (newVal) => {
  if (newVal) form.taskSelect = ''
})

watch(() => form.taskSelect, (newVal) => {
  if (newVal) form.taskText = ''
})

// Time Lists
const hours = Array.from({length: 12}, (_, i) => (i + 1).toString().padStart(2, '0'))
const ampm = ['AM', 'PM']

const formatTo24Hour = (h, m, a) => {
  let hrs = parseInt(h, 10)
  let mins = parseInt(m || 0, 10)
  if (mins < 0 || mins > 59) return 'INVALID' // Prevent formatting bad data
  
  if (a === 'PM' && hrs !== 12) hrs += 12
  if (a === 'AM' && hrs === 12) hrs = 0
  return `${hrs.toString().padStart(2, '0')}:${mins.toString().padStart(2, '0')}:00`
}

const cleanMinute = (field, event) => {
  let val = event.target.value.replace(/\D/g, ''); // strip non-numeric keystrokes
  if (val.length > 2) val = val.substring(0, 2);
  if (val !== '' && parseInt(val, 10) > 59) {
    val = '59'; // Hard physical clamp limit 59
  }
  form[field] = val;
  event.target.value = val; // Force DOM overlay refresh if decoupled
}

const padMinute = (field) => {
  if (form[field] === '') {
    form[field] = '00';
  } else {
    form[field] = String(form[field]).padStart(2, '0');
  }
}

const parseDateString = (dateStr) => {
  const parts = dateStr.split('-')
  return new Date(parts[0], parts[1] - 1, parts[2])
}

// Reactive Validation
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
       successMsg.value = 'Timesheet entry successfully updated.'
       setTimeout(() => router.push('/app/history'), 800)
    } else {
       await store.addEntry(state.payload)
       successMsg.value = 'Timesheet entry successfully saved.'
       
       // Reset Form purely
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

    <!-- Panel Header -->
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
        <div class="form-section-label">Date &amp; Time</div>
        <div class="form-row">
          <!-- Date Block -->
          <div class="input-block date-block" :class="{ 'has-field-error': hasSubmitted && validationState.errors.date }">
            <label>Date <span class="req">*</span></label>
            <div class="input-wrapper">
              <input type="date" v-model="form.date" class="classic-input clean-date" :disabled="isSaving" />
            </div>
            <span class="field-error" v-if="hasSubmitted && validationState.errors.date">{{ validationState.errors.date }}</span>
          </div>

          <!-- Start Time -->
          <div class="input-block time-block" :class="{ 'has-field-error': hasSubmitted && validationState.errors.time }">
            <label>Start Time <span class="req">*</span></label>
            <div class="time-selectors">
              <select v-model="form.startH" class="time-part" :disabled="isSaving">
                <option v-for="h in hours" :key="'sh'+h" :value="h">{{ h }}</option>
              </select>
              <span class="colon">:</span>
              <input type="text" inputmode="numeric" maxlength="2" placeholder="00" :value="form.startM" @input="cleanMinute('startM', $event)" class="time-part min-input" :disabled="isSaving" @blur="padMinute('startM')" autocomplete="off" />
              <select v-model="form.startA" class="time-ampm" :disabled="isSaving">
                <option v-for="a in ampm" :key="'sa'+a" :value="a">{{ a }}</option>
              </select>
            </div>
          </div>

          <!-- End Time -->
          <div class="input-block time-block" :class="{ 'has-field-error': hasSubmitted && validationState.errors.time }">
            <label>End Time <span class="req">*</span></label>
            <div class="time-selectors">
              <select v-model="form.endH" class="time-part" :disabled="isSaving">
                <option v-for="h in hours" :key="'eh'+h" :value="h">{{ h }}</option>
              </select>
              <span class="colon">:</span>
              <input type="text" inputmode="numeric" maxlength="2" placeholder="00" :value="form.endM" @input="cleanMinute('endM', $event)" class="time-part min-input" :disabled="isSaving" @blur="padMinute('endM')" autocomplete="off" />
              <select v-model="form.endA" class="time-ampm" :disabled="isSaving">
                <option v-for="a in ampm" :key="'ea'+a" :value="a">{{ a }}</option>
              </select>
            </div>
          </div>

          <!-- Last Task Checkbox -->
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
              <button type="button" @click="openManageTasks" class="btn-manage-circle" title="Manage task categories" :disabled="isSaving">
                <svg viewBox="0 0 24 24" fill="none" class="add-icon"><path d="M12 5V19M5 12H19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
              </button>
            </div>
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

    <!-- TASK MANAGEMENT MODAL -->
    <div v-if="showTaskModal" class="modal-overlay" @mousedown.self="showTaskModal = false">
      <div class="modal-card">
        <div class="modal-header">
          <h3>Manage Task Categories</h3>
          <button @click="showTaskModal = false" class="btn-close-modal">✕</button>
        </div>
        <div v-if="taskManageError" class="modal-error">{{ taskManageError }}</div>
        <div class="add-task-form">
          <input type="text" v-model="newTaskName" placeholder="New category name..." @keyup.enter="submitNewTask" :disabled="isManagingTasks" class="classic-input" />
          <button @click="submitNewTask" class="blue-btn small-btn" :disabled="isManagingTasks || !newTaskName.trim()">
            <span v-if="isManagingTasks" class="loader small-loader"></span>
            <span v-else>ADD</span>
          </button>
        </div>
        <div class="task-list">
          <div v-for="task in taskTypesList" :key="task.id" class="task-item">
            <span class="task-name">{{ task.name }}</span>
            <button @click="removeTask(task.id, task.name)" class="btn-delete-task" title="Remove Category" :disabled="isManagingTasks">Delete</button>
          </div>
          <div v-if="taskTypesList.length === 0" class="empty-state">No master tasks defined.</div>
        </div>
        <div class="modal-actions-footer">
          <button @click="showTaskModal = false" class="cancel-btn">Close</button>
        </div>
      </div>
    </div>

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

/* ── Panel Header ─────────────────────────────────────────────────────────── */
.panel-header {
  padding: 18px 28px;
  border-bottom: 1px solid var(--border-light);
  background-color: #fafbfc;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.panel-title {
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-main);
  margin: 0 0 4px;
}

.panel-note {
  font-size: 0.78rem;
  color: var(--text-muted);
  margin: 0;
  max-width: 600px;
  line-height: 1.5;
}

.edit-mode-badge {
  background-color: #fef3c7;
  color: #92400e;
  font-size: 0.7rem;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 12px;
  border: 1px solid #fde68a;
  white-space: nowrap;
  margin-top: 2px;
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

.form-section-label {
  font-size: 0.65rem;
  text-transform: uppercase;
  font-weight: 700;
  letter-spacing: 0.07em;
  color: var(--text-muted);
  margin-bottom: 14px;
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
.time-block { width: 160px; flex-shrink: 0; }
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
.time-selectors {
  display: flex;
  align-items: center;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background-color: #fff;
  height: 36px;
  overflow: hidden;
}
.time-selectors:focus-within {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.08);
}
.time-part {
  border: none;
  background: transparent;
  flex: 1;
  text-align: center;
  font-size: 0.84rem;
  font-family: inherit;
  color: var(--text-main);
  appearance: none;
  -webkit-appearance: none;
  padding: 0 3px;
  cursor: pointer;
  text-align-last: center;
}
.min-input::-webkit-outer-spin-button,
.min-input::-webkit-inner-spin-button { -webkit-appearance: none; margin: 0; }
.min-input { -moz-appearance: textfield; }
.time-part:focus { outline: none; }
.time-ampm {
  border: none;
  background: #f1f5f9;
  border-left: 1px solid var(--border-color);
  height: 100%;
  font-weight: 700;
  font-size: 0.78rem;
  color: #334155;
  width: 46px;
  text-align: center;
  text-align-last: center;
  appearance: none;
  -webkit-appearance: none;
  cursor: pointer;
}
.time-ampm:focus { outline: none; }
.colon { font-weight: 700; color: #94a3b8; font-size: 0.9rem; }

/* Error States */
.has-field-error .classic-input,
.has-field-error .time-selectors { border-color: #dc2626; }
.has-field-error .time-ampm { background-color: #fee2e2; border-left-color: #fca5a5; }

.field-error {
  color: #dc2626;
  font-size: 0.72rem;
  margin-top: 4px;
  font-weight: 500;
  display: block;
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
  background-color: #475569;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
  transition: background-color 0.15s;
  color: white;
}
.btn-manage-circle:hover { background-color: #334155; }
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
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-light);
  background: #fafbfc;
}
.modal-header h3 { font-size: 0.9375rem; font-weight: 700; margin: 0; color: var(--text-main); }
.btn-close-modal {
  background: none;
  border: none;
  font-size: 1rem;
  color: var(--text-muted);
  cursor: pointer;
  padding: 2px 6px;
}
.btn-close-modal:hover { color: var(--text-main); }

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


.header-strip {
  background-color: #ffb833;
  color: #fff;
  padding: 14px 24px;
  font-size: 1.1rem;
  font-weight: bold;
}

.note-text-container {
  display: flex;
  justify-content: center;
  padding: 24px 20px 16px 20px;
}

.note-text {
  color: #d11a2a;
  text-align: center;
  font-size: 0.85rem;
  font-weight: bold;
  line-height: 1.6;
}

.req {
  color: #d11a2a;
  margin-left: 2px;
}

/* Base Global Feedback */
.form-feedback {
  margin: 0 40px 24px 40px;
  padding: 14px 20px;
  border-radius: 4px;
  font-size: 0.875rem;
  display: flex;
  align-items: flex-start;
  gap: 12px;
}
.fb-icon { font-size: 1.25rem; font-weight: bold; margin-top: -2px; }
.form-feedback p { margin: 4px 0 0 0; font-weight: normal; }
.form-feedback.error { color: #991b1b; border: 1px solid #fecaca; background-color: #fef2f2; }
.form-feedback.success { color: #166534; border: 1px solid #bbf7d0; background-color: #f0fdf4; }

.erp-form {
  padding: 0 40px;
}

.form-row {
  display: flex;
  gap: 24px;
  margin-bottom: 24px;
  align-items: stretch;
}

.form-row.task-row {
  margin-bottom: 8px;
}

.form-row.align-center {
  align-items: center;
}

.mt-12 {
  margin-top: 12px;
}

/* Logic Layouts */
.input-block {
  display: flex;
  flex-direction: column;
}

.flex-1 { flex: 1; }
.flex-full { width: 100%; }
.date-block { width: 220px; }
.time-block { width: 180px; }
.checkbox-block {
  flex: 1;
  display: flex;
  align-items: flex-start;
  justify-content: flex-end; 
  padding-top: 28px;
}

label {
  font-size: 0.85rem;
  font-weight: bold;
  color: #475569;
  margin-bottom: 6px;
  display: inline-block;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: normal;
  color: #444;
  cursor: pointer;
}

.erp-checkbox {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.disabled-text {
  color: #94a3b8;
  cursor: not-allowed;
}

.or-separator {
  font-weight: bold;
  font-size: 0.85rem;
  color: #555;
  padding-top: 16px;
}

/* Wrapper to fix native input visual overlaps */
.input-wrapper {
  position: relative;
  display: flex;
  background-color: #fff;
}

.select-wrapper::after {
  content: '▼';
  font-size: 0.6rem;
  color: #666;
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  pointer-events: none;
}

/* Standardizing Input boxes safely */
.classic-input {
  box-sizing: border-box;
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #cbd5e1;
  border-radius: 4px;
  font-size: 0.85rem;
  font-family: inherit;
  color: #1e293b;
  box-shadow: none;
  background-color: transparent; 
  appearance: none; 
  -webkit-appearance: none;
}

.classic-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: inset 0 0 0 1px #3b82f6;
}

textarea.classic-input {
  resize: vertical;
  min-height: 80px;
}

.classic-input:disabled {
  background-color: #f8fafc;
  color: #94a3b8;
  cursor: not-allowed;
  border-color: #e2e8f0;
}

.clean-date::-webkit-calendar-picker-indicator {
  cursor: pointer;
  opacity: 0.6;
}
.clean-date::-webkit-calendar-picker-indicator:hover {
  opacity: 1;
}

/* 12-HR Time Selectors Logic */
.time-selectors {
  display: flex;
  align-items: center;
  border: 1px solid #cbd5e1;
  border-radius: 4px;
  background-color: #fff;
  height: 38px;
  overflow: hidden;
}

.time-selectors:focus-within {
  border-color: #3b82f6;
  box-shadow: inset 0 0 0 1px #3b82f6;
}

.time-part {
  border: none;
  background: transparent;
  flex: 1;
  text-align: center;
  font-size: 0.85rem;
  font-family: inherit;
  color: #1e293b;
  appearance: none;
  -webkit-appearance: none;
  padding: 0 4px;
  cursor: pointer;
  text-align-last: center; 
}

/* Hide spin arrows in min input */
.min-input::-webkit-outer-spin-button,
.min-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}
.min-input {
  -moz-appearance: textfield;
}

.time-part:focus { outline: none; }

.time-ampm {
  border: none;
  background: #f1f5f9;
  border-left: 1px solid #cbd5e1;
  height: 100%;
  font-weight: bold;
  color: #333;
  width: 50px;
  text-align: center;
  text-align-last: center;
  appearance: none;
  -webkit-appearance: none;
  cursor: pointer;
}
.time-ampm:focus { outline: none; }

.colon {
  font-weight: bold;
  color: #94a3b8;
}

/* Error UI Feedback */
.has-field-error .classic-input, 
.has-field-error .time-selectors {
  border-color: #dc2626;
  background-color: #fef2f2;
}
.has-field-error .time-ampm {
  background-color: #fee2e2;
  border-left-color: #fca5a5;
}

.field-error {
  color: #dc2626;
  font-size: 0.75rem;
  margin-top: 4px;
  font-weight: 500;
  display: block;
}

.row-error {
  color: #dc2626;
  font-size: 0.8rem;
  margin-top: -8px;
  margin-bottom: 24px;
  font-weight: 600;
}

/* Task Selector Manage Integration */
.select-with-btn {
  display: flex;
  gap: 12px;
  align-items: center;
  width: 100%;
}

.select-wrapper {
  flex: 1; 
}

.btn-manage-circle {
  width: 36px;
  height: 36px;
  border-radius: 4px;
  background-color: #475569;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
  transition: background-color 0.2s;
  color: white;
}

.btn-manage-circle:hover { background-color: #334155; }
.btn-manage-circle:disabled { background-color: #cbd5e1; cursor: not-allowed; }

.add-icon {
  width: 18px;
  height: 18px;
  color: white;
}

/* Buttons */
.form-actions {
  text-align: center;
  margin-top: 32px;
  padding-top: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
}

.cancel-link {
  background: transparent;
  border: none;
  font-size: 0.9rem;
  font-weight: 600;
  color: #64748b;
  cursor: pointer;
  text-decoration: underline;
}
.cancel-link:hover { color: #0f172a; }

.blue-btn {
  background-color: #1d4ed8;
  color: #ffffff;
  border: none;
  padding: 12px 48px;
  font-size: 0.9rem;
  font-weight: bold;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.blue-btn.small-btn {
  padding: 8px 24px;
  font-size: 0.85rem;
}

.blue-btn:hover:not(:disabled) {
  background-color: #1e3a8a;
}

.blue-btn:disabled {
  background-color: #94a3b8;
  cursor: not-allowed;
}

.btn-flex {
  display: flex;
  align-items: center;
  gap: 8px;
}

.loader {
  border: 2px solid #ffffff;
  border-top-color: transparent;
  border-radius: 50%;
  width: 14px;
  height: 14px;
  animation: spin 1s linear infinite;
  display: inline-block;
}
.small-loader {
  width: 10px; height: 10px;
}

@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

.footer-msg {
  text-align: left;
  font-size: 0.85rem;
  color: #64748b;
  margin-top: 60px;
  padding: 24px 40px;
  border-top: 1px solid #e2e8f0;
}

/* -----------------------------
   Manage Tasks Modal UX
------------------------------*/
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: rgba(15, 23, 42, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-card {
  background-color: #ffffff;
  border-radius: 6px;
  width: 90%;
  max-width: 480px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
}

.modal-header {
  padding: 16px 24px;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.1rem;
  color: #1e293b;
}

.btn-close-modal {
  background: transparent;
  border: none;
  font-size: 1.25rem;
  color: #64748b;
  cursor: pointer;
}
.btn-close-modal:hover { color: #0f172a; }

.modal-error {
  padding: 12px 24px;
  background-color: #fef2f2;
  color: #991b1b;
  font-size: 0.85rem;
  border-bottom: 1px solid #fee2e2;
}

.add-task-form {
  padding: 20px 24px;
  display: flex;
  gap: 12px;
  background-color: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.add-task-form .classic-input {
  /* Inherit layout clean */
  background-color: #fff;
}

.task-list {
  padding: 8px 0;
  max-height: 280px;
  overflow-y: auto;
}

.task-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 24px;
  border-bottom: 1px solid #f1f5f9;
}
.task-item:last-child { border-bottom: none; }

.task-item:hover {
  background-color: #f8fafc;
}

.task-name {
  font-size: 0.9rem;
  color: #334155;
  font-weight: 500;
}

.btn-delete-task {
  background: transparent;
  border: 1px solid #ef4444;
  color: #ef4444;
  font-size: 0.75rem;
  padding: 4px 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-delete-task:hover:not(:disabled) {
  background-color: #ef4444;
  color: white;
}
.btn-delete-task:disabled {
  border-color: #cbd5e1;
  color: #cbd5e1;
  cursor: not-allowed;
}

.empty-state {
  padding: 24px;
  text-align: center;
  color: #64748b;
  font-size: 0.9rem;
  font-style: italic;
}

.modal-actions-footer {
  padding: 16px 24px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-end;
}

.cancel-btn {
  background-color: #f1f5f9;
  border: 1px solid #cbd5e1;
  padding: 8px 24px;
  border-radius: 4px;
  color: #475569;
  font-weight: bold;
  cursor: pointer;
  font-size: 0.85rem;
}

.cancel-btn:hover {
  background-color: #e2e8f0;
}
</style>
