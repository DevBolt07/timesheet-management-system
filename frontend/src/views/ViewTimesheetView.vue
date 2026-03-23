<script setup>
import { useTimesheetStore } from '@/stores/timesheetStore'
import TimesheetTable from '@/components/timesheet/TimesheetTable.vue'
import { storeToRefs } from 'pinia'

// 1. Initialize store access
const store = useTimesheetStore()

// 2. Extract reactive state to pass as Props.
// storeToRefs ensures we don't lose reactivity if we destructure the store.
const { entries } = storeToRefs(store)
</script>

<template>
  <div class="view-timesheet">
    <div class="header">
      <h1>Timesheet History</h1>
      <p class="subtitle">Review your logged and submitted hours.</p>
    </div>

    <!-- 3. Render 'dumb' Table component and pass state purely as v-bind data -->
    <TimesheetTable :data="entries" />
  </div>
</template>

<style scoped>
.view-timesheet h1 {
  margin-bottom: 5px;
  color: var(--primary-color);
}

.subtitle {
  color: #666;
  margin-bottom: 20px;
}

.header {
  margin-bottom: 30px;
}
</style>
