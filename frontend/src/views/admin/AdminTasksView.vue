<script setup>
import { onMounted, ref } from 'vue'
import { apiClient } from '@/services/api'

const tasks = ref([])
const loading = ref(true)
const error = ref('')
const adding = ref(false)
const newTaskName = ref('')
const addError = ref('')
const deletingId = ref(null)

onMounted(async () => {
  await loadTasks()
})

const loadTasks = async () => {
  loading.value = true
  error.value = ''
  try {
    tasks.value = await apiClient.get('/api/admin/tasks')
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

const addTask = async () => {
  addError.value = ''
  const name = newTaskName.value.trim()
  if (!name) { addError.value = 'Task name cannot be empty.'; return }
  try {
    adding.value = true
    const created = await apiClient.post('/api/admin/tasks', { name })
    tasks.value.push(created)
    newTaskName.value = ''
  } catch (e) {
    addError.value = e.message
  } finally {
    adding.value = false
  }
}

const deleteTask = async (task) => {
  if (task.usageCount > 0) {
    alert(`Cannot delete "${task.name}" — it is referenced by ${task.usageCount} timesheet record(s). Deactivation is a planned future feature.`)
    return
  }
  if (!confirm(`Delete task category "${task.name}"?`)) return
  try {
    deletingId.value = task.id
    await apiClient.delete(`/api/admin/tasks/${task.id}`)
    tasks.value = tasks.value.filter(t => t.id !== task.id)
  } catch (e) {
    alert('Delete failed: ' + e.message)
  } finally {
    deletingId.value = null
  }
}
</script>

<template>
  <div class="admin-tasks">
    <div class="page-header">
      <h2>Task Master Control</h2>
      <p>Manage authoritative task categories used in staff timesheet submissions.</p>
    </div>

    <!-- Add Task Form -->
    <div class="add-task-panel">
      <h4>Add New Category</h4>
      <div class="add-form">
        <input
          type="text"
          v-model="newTaskName"
          placeholder="e.g. External Training"
          class="task-input"
          @keyup.enter="addTask"
        />
        <button class="blue-btn" @click="addTask" :disabled="adding">
          {{ adding ? 'Adding...' : '+ Add' }}
        </button>
      </div>
      <p v-if="addError" class="add-error">{{ addError }}</p>
    </div>

    <div v-if="loading" class="state-center"><div class="loader"></div></div>
    <div v-else-if="error" class="state-center error-text">{{ error }}</div>

    <div v-else class="table-wrap">
      <table class="erp-table">
        <thead>
          <tr>
            <th>#</th>
            <th>Category Name</th>
            <th>Usage Count</th>
            <th>Safety Status</th>
            <th class="center">Action</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(t, i) in tasks" :key="t.id">
            <td class="num-col">{{ i + 1 }}</td>
            <td class="name-col">{{ t.name }}</td>
            <td>
              <span :class="t.usageCount > 0 ? 'count-used' : 'count-zero'">
                {{ t.usageCount }} {{ t.usageCount === 1 ? 'entry' : 'entries' }}
              </span>
            </td>
            <td>
              <span v-if="t.usageCount > 0" class="badge-locked">In Use</span>
              <span v-else class="badge-safe">Safe to Delete</span>
            </td>
            <td class="center">
              <button
                class="del-btn"
                :disabled="deletingId === t.id"
                @click="deleteTask(t)"
                :title="t.usageCount > 0 ? 'Referenced by existing records — cannot delete' : 'Delete this category'"
              >
                {{ deletingId === t.id ? '...' : 'Delete' }}
              </button>
            </td>
          </tr>
          <tr v-if="tasks.length === 0">
            <td colspan="5" class="empty-row">No task categories defined.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="info-note">
      ℹ️ Task categories referenced by existing timesheet records are protected. Deactivation (soft-delete) support is planned for a future release.
    </div>
  </div>
</template>

<style scoped>
.admin-tasks { display: flex; flex-direction: column; gap: 20px; }
.page-header { padding: 14px 20px; background: var(--accent-warm); border-radius: var(--radius-lg) var(--radius-lg) 0 0; margin-bottom: 0; }
.page-header h2 { font-size: 1.0625rem; font-weight: 700; color: #fff; margin: 0 0 2px; letter-spacing: -0.01em; }
.page-header p  { font-size: 0.78rem; color: rgba(255,255,255,0.85); margin: 0; }

.add-task-panel { background: white; border: 1px solid #e2e8f0; border-radius: 8px; padding: 18px 20px; }
.add-task-panel h4 { font-size: 0.9rem; font-weight: 600; color: #1e293b; margin: 0 0 12px; }
.add-form { display: flex; gap: 10px; align-items: center; }
.task-input { flex: 1; padding: 8px 12px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 0.85rem; outline: none; }
.task-input:focus { border-color: #1d4ed8; }
.blue-btn { background: #1d4ed8; color: white; border: none; padding: 8px 16px; font-size: 0.85rem; font-weight: 600; border-radius: 6px; cursor: pointer; white-space: nowrap; }
.blue-btn:hover:not(:disabled) { background: #1e3a8a; }
.blue-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.add-error { color: #ef4444; font-size: 0.82rem; margin: 6px 0 0; }

.table-wrap { background: white; border: 1px solid #e2e8f0; border-radius: 8px; overflow: hidden; }
.erp-table { width: 100%; border-collapse: collapse; }
.erp-table th { background: #f1f5f9; padding: 11px 16px; font-size: 0.72rem; text-transform: uppercase; font-weight: 700; color: #64748b; letter-spacing: 0.05em; border-bottom: 2px solid #e2e8f0; }
.erp-table td { padding: 13px 16px; font-size: 0.85rem; color: #334155; border-bottom: 1px solid #f1f5f9; }
.num-col  { color: #94a3b8; font-size: 0.8rem; width: 40px; }
.name-col { font-weight: 600; color: #1e293b; }
.center   { text-align: center; }

.count-used { color: #92400e; font-weight: 600; font-size: 0.82rem; }
.count-zero { color: #94a3b8; font-size: 0.82rem; }

.badge-locked { background: #fef3c7; color: #b45309; padding: 3px 9px; border-radius: 12px; font-size: 0.72rem; font-weight: 700; }
.badge-safe   { background: #dcfce7; color: #166534; padding: 3px 9px; border-radius: 12px; font-size: 0.72rem; font-weight: 700; }

.del-btn { background: #fee2e2; color: #991b1b; border: 1px solid #fca5a5; font-size: 0.78rem; font-weight: 600; padding: 4px 10px; border-radius: 4px; cursor: pointer; transition: background 0.15s; }
.del-btn:hover:not(:disabled) { background: #fecaca; }
.del-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.empty-row { text-align: center; padding: 50px !important; color: #94a3b8; font-style: italic; }
.info-note { background: #f0f9ff; border: 1px solid #bae6fd; border-radius: 8px; padding: 12px 16px; font-size: 0.83rem; color: #0369a1; }

.state-center { display: flex; justify-content: center; padding: 60px; }
.error-text { color: #ef4444; }
.loader { border: 3px solid #f1f5f9; border-top-color: #3b82f6; border-radius: 50%; width: 28px; height: 28px; animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>
