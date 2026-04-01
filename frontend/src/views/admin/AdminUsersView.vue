<script setup>
import { onMounted, ref } from 'vue'
import { apiClient } from '@/services/api'

const users = ref([])
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    users.value = await apiClient.get('/api/admin/users')
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
})

const roleClass = (role) => {
  if (role === 'ADMIN') return 'role-admin'
  if (role === 'HOD') return 'role-hod'
  return 'role-staff'
}
</script>

<template>
  <div class="admin-users">
    <div class="page-header">
      <h2>User Directory</h2>
      <p>All provisioned ERP accounts with their assigned roles. Account management is centrally controlled.</p>
    </div>

    <div v-if="loading" class="state-center"><div class="loader"></div></div>
    <div v-else-if="error" class="state-center error-text">{{ error }}</div>

    <div v-else class="table-wrap">
      <table class="erp-table">
        <thead>
          <tr>
            <th>#</th>
            <th>Username</th>
            <th>Role</th>
            <th>Department</th>
            <th>Account Status</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(u, i) in users" :key="u.id">
            <td class="num-col">{{ i + 1 }}</td>
            <td class="user-col">{{ u.username }}</td>
            <td><span class="role-badge" :class="roleClass(u.role)">{{ u.role }}</span></td>
            <td class="dept-col">{{ u.department }}</td>
            <td><span class="active-pill">Active</span></td>
          </tr>
          <tr v-if="users.length === 0">
            <td colspan="5" class="empty-row">No users found.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="info-note">
      ℹ️ User provisioning, role reassignment, and account deactivation are planned future admin capabilities.
    </div>
  </div>
</template>

<style scoped>
.admin-users { display: flex; flex-direction: column; gap: 20px; }
.page-header h2 { font-size: 1.25rem; font-weight: 700; color: #1e293b; margin: 0 0 4px; }
.page-header p  { font-size: 0.875rem; color: #64748b; margin: 0; }

.table-wrap { background: white; border: 1px solid #e2e8f0; border-radius: 8px; overflow: hidden; }
.erp-table { width: 100%; border-collapse: collapse; }
.erp-table th { background: #f1f5f9; padding: 11px 16px; font-size: 0.72rem; text-transform: uppercase; font-weight: 700; color: #64748b; letter-spacing: 0.05em; border-bottom: 2px solid #e2e8f0; }
.erp-table td { padding: 13px 16px; font-size: 0.85rem; color: #334155; border-bottom: 1px solid #f1f5f9; }
.num-col  { color: #94a3b8; font-size: 0.8rem; width: 40px; }
.user-col { font-weight: 600; color: #1e293b; }
.dept-col { color: #64748b; }

.role-badge { display: inline-block; padding: 3px 10px; border-radius: 12px; font-size: 0.72rem; font-weight: 700; letter-spacing: 0.04em; }
.role-staff { background: #ede9fe; color: #4c1d95; }
.role-hod   { background: #fce7f3; color: #9d174d; }
.role-admin { background: #e0f2fe; color: #0c4a6e; }

.active-pill { display: inline-block; background: #dcfce7; color: #166534; font-size: 0.72rem; font-weight: 700; padding: 3px 10px; border-radius: 12px; }

.empty-row { text-align: center; padding: 50px !important; color: #94a3b8; font-style: italic; }

.info-note { background: #f0f9ff; border: 1px solid #bae6fd; border-radius: 8px; padding: 12px 16px; font-size: 0.83rem; color: #0369a1; }

.state-center { display: flex; justify-content: center; padding: 60px; }
.error-text { color: #ef4444; font-size: 0.9rem; }
.loader { border: 3px solid #f1f5f9; border-top-color: #3b82f6; border-radius: 50%; width: 28px; height: 28px; animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>
