import { createRouter, createWebHistory } from 'vue-router'
import LandingView from '@/views/LandingView.vue'
import MainLayout from '@/layouts/MainLayout.vue'
import AddTimesheetView from '@/views/AddTimesheetView.vue'
import ViewTimesheetView from '@/views/ViewTimesheetView.vue'
import { useAuthStore, ROLE_HOME_ROUTES } from '@/stores/authStore'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'landing',
      component: LandingView,
    },
    {
      path: '/app',
      component: MainLayout,
      meta: { requiresAuth: true },
      // Dynamic redirect: resolves the correct home screen based on the
      // authenticated user's role. Falls back to /app/history for safety.
      redirect: () => {
        const auth = useAuthStore()
        return ROLE_HOME_ROUTES[auth.currentUser?.role] ?? '/app/history'
      },
      children: [
        // Staff routes
        { path: 'log-time',  name: 'log-time', component: AddTimesheetView },
        { path: 'history',   name: 'history',  component: ViewTimesheetView },
        { path: 'review',    name: 'review',   component: () => import('@/views/ReviewTimesheetView.vue'), meta: { requiresHodOrAdmin: true } },

        // Admin routes
        { path: 'admin',          name: 'admin-dashboard', component: () => import('@/views/admin/AdminDashboardView.vue'), meta: { requiresAdmin: true } },
        { path: 'admin/oversight',name: 'admin-oversight', component: () => import('@/views/admin/AdminTimesheetView.vue'),meta: { requiresAdmin: true } },
        { path: 'admin/users',    name: 'admin-users',     component: () => import('@/views/admin/AdminUsersView.vue'),    meta: { requiresAdmin: true } },
        { path: 'admin/tasks',    name: 'admin-tasks',     component: () => import('@/views/admin/AdminTasksView.vue'),    meta: { requiresAdmin: true } },
      ]
    }
  ],
})

// ── Route Guard ─────────────────────────────────────────────────────────────
// Centralized navigation guard enforcing authentication and role-based access.
router.beforeEach((to, from, next) => {
  const auth = useAuthStore()
  const role = auth.currentUser?.role

  // 1. Any route inside /app requires a logged-in user.
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return next('/')
  }

  // 2. Admin-only routes — redirect unauthorized users to their own home.
  if (to.meta.requiresAdmin && role !== 'ADMIN') {
    return next(auth.homeRoute)
  }

  // 3. HOD / Admin-only routes (review workflow) — block plain staff.
  if (to.meta.requiresHodOrAdmin && role !== 'HOD' && role !== 'ADMIN') {
    return next(auth.homeRoute)
  }

  next()
})

export default router
