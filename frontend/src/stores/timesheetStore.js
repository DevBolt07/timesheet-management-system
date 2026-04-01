import { defineStore } from 'pinia'
import { ref } from 'vue'
import { apiClient } from '@/services/api'

export const useTimesheetStore = defineStore('timesheet', () => {
  const entries = ref([])
  const taskTypesList = ref([]) // Holds {id, name} objects for management
  const isLoading = ref(false)
  const isError = ref(null)

  const fetchEntries = async () => {
    isLoading.value = true
    isError.value = null
    try {
      entries.value = await apiClient.get('/api/timesheets')
    } catch (e) {
      isError.value = e.message || 'Failed to fetch entries'
      console.error(e)
    } finally {
      isLoading.value = false
    }
  }

  const fetchTaskTypes = async () => {
    try {
      const resp = await apiClient.get('/api/taskTypes')
      taskTypesList.value = resp
    } catch (e) {
      console.error("Failed to fetch task types", e)
      taskTypesList.value = []
    }
  }

  const addEntry = async (entry) => {
    try {
      await apiClient.post('/api/timesheets', entry)
      await fetchEntries() // Re-sync to enforce backend sequence map
      return true
    } catch (e) {
      throw e
    }
  }

  const updateEntry = async (id, entry) => {
    try {
      await apiClient.put(`/api/timesheets/${id}`, entry)
      await fetchEntries() // Reliable sync resolving any secondary status side-effects
      return true
    } catch (e) {
      throw e
    }
  }

  const deleteEntry = async (id) => {
    try {
      isError.value = null
      await apiClient.delete(`/api/timesheets/${id}`)
      await fetchEntries()
      return true
    } catch (e) {
      isError.value = e.message || 'Failed to delete'
      throw e
    }
  }

  const reviewEntry = async (id, statusPayload) => {
    try {
      const resp = await apiClient.put(`/api/timesheets/${id}/review`, statusPayload)
      // Opt to map exact DTO payload dynamically substituting old reference safely
      const index = entries.value.findIndex(e => e.id === id)
      if (index !== -1) {
         entries.value[index] = resp
      }
      return resp
    } catch (e) {
      throw e
    }
  }

  return { 
    entries, 
    taskTypesList, 
    isLoading, 
    isError, 
    fetchEntries, 
    fetchTaskTypes,
    addEntry, 
    updateEntry, 
    deleteEntry,
    reviewEntry 
  }
})
