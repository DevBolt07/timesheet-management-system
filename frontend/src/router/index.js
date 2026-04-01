import { createRouter, createWebHistory } from 'vue-router'
import LandingView from '@/views/LandingView.vue'
import MainLayout from '@/layouts/MainLayout.vue'
import AddTimesheetView from '@/views/AddTimesheetView.vue'
import ViewTimesheetView from '@/views/ViewTimesheetView.vue'

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
      redirect: '/app/history',
      children: [
        {
          path: 'log-time',
          name: 'log-time',
          component: AddTimesheetView,
        },
        {
          path: 'history',
          name: 'history',
          component: ViewTimesheetView,
        },
        {
          path: 'review',
          name: 'review',
          component: () => import('@/views/ReviewTimesheetView.vue')
        }
      ]
    }
  ],
})

export default router
