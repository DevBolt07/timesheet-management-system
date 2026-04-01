import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { apiClient } from '@/services/api'

/**
 * Foundation Layer: Client Authorization Context
 * 
 * This isolates all identity expectations. In an ERP integration phase:
 * 1. This file will connect to the company SSO / JWT payload verifier.
 * 2. `loginAsDemo` gets removed/replaced by legitimate OAuth sweeps.
 * 3. `currentUser` state acts identically providing standardized objects to the Views.
 */
export const useAuthStore = defineStore('auth', () => {
  const storedValue = localStorage.getItem('erp_auth_context')
  const currentUser = ref(storedValue ? JSON.parse(storedValue) : null)
  
  const router = useRouter()

  const isAuthenticated = computed(() => !!currentUser.value)
  const isManager = computed(() => currentUser.value?.role === 'HOD')
  const isStaff = computed(() => currentUser.value?.role === 'STAFF')

  // TEMPORARY DEMO AUTH FLIGHT
  const loginAsDemo = async (username, password) => {
    try {
        const payload = await apiClient.post('/api/auth/demo-login', { username, password })
        currentUser.value = payload
        localStorage.setItem('erp_auth_context', JSON.stringify(payload))
    } catch (e) {
        throw new Error(e.message || "Failed to seamlessly map Identity Context")
    }
  }

  const logout = () => {
    currentUser.value = null
    localStorage.removeItem('erp_auth_context')
    router.push('/')
  }

  return { currentUser, isAuthenticated, isManager, isStaff, loginAsDemo, logout }
})
