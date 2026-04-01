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
 *
 * Role → Home Route Map (single source of truth):
 *   ADMIN → /app/admin
 *   HOD   → /app/review
 *   STAFF → /app/history
 */

/** Centralized role-to-default-route mapping. */
export const ROLE_HOME_ROUTES = {
  ADMIN: '/app/admin',
  HOD:   '/app/review',
  STAFF: '/app/history',
}

export const useAuthStore = defineStore('auth', () => {
  const storedValue = localStorage.getItem('erp_auth_context')
  const currentUser = ref(storedValue ? JSON.parse(storedValue) : null)

  const router = useRouter()

  // ── Computed role flags ─────────────────────────────────────────────────
  const isAuthenticated = computed(() => !!currentUser.value)
  const isAdmin   = computed(() => currentUser.value?.role === 'ADMIN')
  const isManager = computed(() => currentUser.value?.role === 'HOD')
  const isStaff   = computed(() => currentUser.value?.role === 'STAFF')

  /**
   * homeRoute — resolves the correct landing path for the current user's role.
   * Falls back to /app/history for unknown / missing roles.
   */
  const homeRoute = computed(() =>
    ROLE_HOME_ROUTES[currentUser.value?.role] ?? '/app/history'
  )

  // ── TEMPORARY DEMO AUTH FLIGHT ──────────────────────────────────────────
  const loginAsDemo = async (username, password) => {
    try {
      const payload = await apiClient.post('/api/auth/demo-login', { username, password })
      currentUser.value = payload
      localStorage.setItem('erp_auth_context', JSON.stringify(payload))
    } catch (e) {
      throw new Error(e.message || 'Failed to seamlessly map Identity Context')
    }
  }

  const logout = () => {
    currentUser.value = null
    localStorage.removeItem('erp_auth_context')
    router.push('/')
  }

  return { currentUser, isAuthenticated, isAdmin, isManager, isStaff, homeRoute, loginAsDemo, logout }
})
