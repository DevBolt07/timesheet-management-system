import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useTimesheetStore = defineStore('timesheet', () => {
  const entries = ref([
    {
      id: 1,
      date: new Date().toISOString().split('T')[0],
      startTime: '09:00',
      endTime: '11:00',
      task: 'Teaching',
      description: 'Dummy data: Morning Lecture'
    }
  ])

  const addEntry = (entry) => {
    entries.value.push({
      ...entry,
      id: Date.now() // Mock database auto-increment ID
    })
  }

  return { entries, addEntry }
})
