<script setup>
import { ref } from 'vue'
import { useTimesheetStore } from '@/stores/timesheetStore'
import TimesheetForm from '@/components/timesheet/TimesheetForm.vue'

// 1. Initialize store access
const store = useTimesheetStore()
const successMessage = ref('')

// 2. Event handler for the emitted data from the generic form component
const handleSaveEntry = (entryData) => {
  // Push to our local Pinia Mock DB
  store.addEntry(entryData)
  
  // UI Feedback
  successMessage.value = 'Timesheet logged successfully!'
  
  setTimeout(() => {
    successMessage.value = ''
  }, 3000)
}
</script>

<template>
  <div class="add-timesheet">
    <h1>Log Time</h1>
    <p class="subtitle">Enter your daily tasks and working hours below.</p>
    
    <div v-if="successMessage" class="success-banner">
      {{ successMessage }}
    </div>

    <!-- Pass no data, just listen for the yield -->
    <TimesheetForm @submit-entry="handleSaveEntry" />
  </div>
</template>

<style scoped>
.add-timesheet h1 {
  margin-bottom: 5px;
  color: var(--primary-color);
}

.subtitle {
  color: #666;
  margin-bottom: 30px;
}

.success-banner {
  background-color: #d4edda;
  color: #155724;
  padding: 12px 20px;
  border-radius: 4px;
  margin-bottom: 20px;
  max-width: 500px;
  border-left: 4px solid #28a745;
}
</style>
