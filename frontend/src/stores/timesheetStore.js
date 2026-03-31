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

  const addTaskType = async (name) => {
    try {
      const resp = await apiClient.post('/api/taskTypes', { name })
      // Push new task or Reactivated task into local list
      if (!taskTypesList.value.find(t => t.id === resp.id)) {
          taskTypesList.value.push(resp)
      }
      return resp
    } catch (e) {
      throw e
    }
  }

  const deleteTaskType = async (id) => {
    try {
      await apiClient.delete(`/api/taskTypes/${id}`)
      taskTypesList.value = taskTypesList.value.filter(t => t.id !== id)
      return true
    } catch (e) {
      throw e
    }
  }

  const addEntry = async (entry) => {
    try {
      const newEntry = await apiClient.post('/api/timesheets', entry)
      entries.value.unshift(newEntry)
      return true
    } catch (e) {
      throw e
    }
  }

  const updateEntry = async (id, entry) => {
    try {
      const updatedEntry = await apiClient.put(`/api/timesheets/${id}`, entry)
      const index = entries.value.findIndex(e => e.id === id)
      if (index !== -1) {
        entries.value[index] = updatedEntry
      }
      return true
    } catch (e) {
      throw e
    }
  }

  const deleteEntry = async (id) => {
    try {
      isError.value = null
      await apiClient.delete(`/api/timesheets/${id}`)
      entries.value = entries.value.filter(e => e.id !== id)
      return true
    } catch (e) {
      isError.value = e.message || 'Failed to delete'
    }
  }

  return { 
    entries, 
    taskTypesList, 
    isLoading, 
    isError, 
    fetchEntries, 
    fetchTaskTypes,
    addTaskType,
    deleteTaskType,
    addEntry, 
    updateEntry, 
    deleteEntry 
  }
})
