<script setup>
import { reactive, ref, computed } from 'vue'

const emit = defineEmits(['submit-entry'])

const props = defineProps({
  taskTypes: {
    type: Array,
    required: true,
    default: () => []
  },
  initialData: {
    type: Object,
    default: null
  }
})

const form = reactive({
  date: props.initialData?.date || new Date().toISOString().split('T')[0],
  startTime: props.initialData?.startTime || '',
  endTime: props.initialData?.endTime || '',
  task: props.initialData?.task || '',
  description: props.initialData?.description || ''
})

const errorMsg = ref('')

const isFormValid = computed(() => {
  return form.date && form.startTime && form.endTime && form.task && form.description
})

const submitForm = () => {
  errorMsg.value = ''
  if (!isFormValid.value) {
    errorMsg.value = 'All parameters must be completed to file an official timesheet.'
    return
  }
  if (form.endTime <= form.startTime) {
    errorMsg.value = 'Cronological failure: End time must fall after start time.'
    return
  }
  emit('submit-entry', { ...form })
  
  form.startTime = ''
  form.endTime = ''
  form.task = ''
  form.description = ''
}
</script>

<template>
  <div class="erp-form-wrapper">
    <div class="form-header">
      <h3>Timesheet Details</h3>
      <p>Log your daily activities accurately for payroll processing and managerial approval.</p>
    </div>

    <form @submit.prevent="submitForm" class="erp-form-body">
      
      <div class="form-row">
        <div class="form-group half">
          <label class="required">Date of Execution</label>
          <input type="date" v-model="form.date" required class="form-control" />
        </div>
        <div class="form-group half">
          <label class="required">Task Classification</label>
          <select v-model="form.task" required class="form-control">
            <option disabled value="">Select institutional task type...</option>
            <option v-for="task in taskTypes" :key="task" :value="task">{{ task }}</option>
          </select>
        </div>
      </div>

      <div class="form-row time-boundaries">
        <div class="form-group half">
          <label class="required">Start Time</label>
          <div class="time-input-wrap">
            <span class="icon">🕗</span>
            <input type="time" v-model="form.startTime" required class="form-control pl-icon" />
          </div>
        </div>
        <div class="form-group half">
          <label class="required">End Time</label>
          <div class="time-input-wrap">
            <span class="icon">🕖</span>
            <input type="time" v-model="form.endTime" required class="form-control pl-icon" />
          </div>
        </div>
      </div>

      <div class="form-group wide">
        <label class="required">Execution Output / Description</label>
        <textarea v-model="form.description" rows="4" required class="form-control" placeholder="Provide a granular description of outputs produced..."></textarea>
      </div>

      <!-- Action Footer -->
      <div class="form-footer">
        <div class="footer-error" v-if="errorMsg">
           <span class="icon">⚠</span> {{ errorMsg }}
        </div>
        <div class="footer-actions">
          <button type="button" class="btn-ghost">Cancel</button>
          <button type="submit" class="btn-primary" :disabled="!isFormValid">
            Submit Timesheet Record
          </button>
        </div>
      </div>
    </form>
  </div>
</template>

<style scoped>
.erp-form-wrapper {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.02);
  max-width: 800px;
  margin: 0;
  overflow: hidden;
}

.form-header {
  padding: 24px 32px;
  border-bottom: 1px solid var(--border-subtle);
  background: #f9fafb;
}

.form-header h3 {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-main);
}

.form-header p {
  color: var(--text-muted);
  font-size: 0.875rem;
  margin-top: 4px;
}

.erp-form-body {
  padding: 32px;
}

.form-row {
  display: flex;
  gap: 32px;
  margin-bottom: 24px;
}

.half {
  flex: 1;
}

.form-group {
  display: flex;
  flex-direction: column;
}

label {
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--text-main);
  margin-bottom: 8px;
}

label.required::after {
  content: '*';
  color: #dc2626;
  margin-left: 4px;
}

.form-control {
  padding: 10px 14px;
  border: 1px solid var(--border-subtle);
  border-radius: 6px;
  font-size: 0.875rem;
  background-color: var(--bg-surface);
  color: var(--text-main);
  transition: all 0.2s;
}

.form-control:focus {
  outline: none;
  border-color: var(--primary-blue);
  box-shadow: 0 0 0 3px rgba(0, 102, 255, 0.1);
}

/* Time inputs with icon prepends */
.time-input-wrap {
  position: relative;
  display: flex;
  align-items: center;
}
.time-input-wrap .icon {
  position: absolute;
  left: 14px;
  font-size: 1.1rem;
  pointer-events: none;
}
.pl-icon {
  padding-left: 40px;
  width: 100%;
}

textarea.form-control {
  resize: vertical;
}

.form-footer {
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px solid var(--border-subtle);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-error {
  color: #dc2626;
  font-size: 0.875rem;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
}

.footer-actions {
  display: flex;
  gap: 16px;
  margin-left: auto;
}

.btn-ghost {
  padding: 10px 20px;
  color: var(--text-muted);
  font-weight: 500;
  border-radius: 6px;
  border: 1px solid transparent;
}
.btn-ghost:hover {
  background-color: #f3f4f6;
  color: var(--text-main);
}

.btn-primary {
  padding: 10px 24px;
  font-size: 0.875rem;
}
.btn-primary:disabled {
  background-color: #9ca3af;
  cursor: not-allowed;
}
</style>
