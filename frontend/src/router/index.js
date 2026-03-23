import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from '../views/DashboardView.vue'
import AddTimesheetView from '../views/AddTimesheetView.vue'
import ViewTimesheetView from '../views/ViewTimesheetView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'dashboard',
      component: DashboardView,
    },
    {
      path: '/log-time',
      name: 'log-time',
      component: AddTimesheetView,
    },
    {
      path: '/history',
      name: 'history',
      component: ViewTimesheetView,
    }
  ],
})

export default router
