<script setup>
import { RouterLink, RouterView, useRoute } from 'vue-router'

import { useAuthStore } from '@/stores/authStore'
import { storeToRefs } from 'pinia'

const route = useRoute()
const authStore = useAuthStore()
const { currentUser, isManager, isStaff } = storeToRefs(authStore)

const breadcrumbMap = {
  '/app/history': 'Timesheet Logs',
  '/app/log-time': 'Log New Timesheet',
  '/app/review': 'Review Pending Approvals'
}

const getBreadcrumb = () => {
  return breadcrumbMap[route.path] || 'Workspace'
}
</script>

<template>
  <div class="erp-layout">
    <!-- Top System Header -->
    <header class="sys-header">
      <div class="brand">
        <div class="logo-box"></div>
        <span class="brand-text">Collegiate ERP Suite</span>
      </div>
      <div class="sys-profile">
        <div class="profile-details">
          <span class="user-name">{{ currentUser?.name || 'Unrecognized Staff' }}</span>
          <span class="user-role">{{ currentUser?.department || 'Unassigned Dept' }}</span>
          <button class="logout-link" @click="authStore.logout()">Exit Demo Session</button>
        </div>
        <div class="profile-avatar">{{ currentUser?.name ? currentUser.name.charAt(0) : 'U' }}</div>
      </div>
    </header>

    <!-- Context Strip (Breadcrumbs) -->
    <div class="context-strip">
      <nav class="breadcrumb">
        <RouterLink to="/" class="crumb-link">Home</RouterLink>
        <span class="crumb-separator">/</span>
        <span class="crumb-current">{{ getBreadcrumb() }}</span>
      </nav>
      <div class="emp-id">Employee ID: 10791</div>
    </div>

    <!-- Main Workspace -->
    <div class="workspace-area">
      <aside class="sidebar-nav">
        <template v-if="isStaff">
          <p class="nav-heading">Timesheet Operations</p>
          <nav class="nav-menu">
            <RouterLink to="/app/history" class="nav-item" active-class="nav-active">
              <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg>
              Master Logs
            </RouterLink>
            <RouterLink to="/app/log-time" class="nav-item" active-class="nav-active">
              <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/></svg>
              Add Timesheet
            </RouterLink>
          </nav>
        </template>
        <template v-if="isManager">
          <p class="nav-heading">Manager Operations</p>
          <nav class="nav-menu">
            <RouterLink to="/app/review" class="nav-item" active-class="nav-active">
              <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>
              Review Approvals
            </RouterLink>
            <RouterLink to="/app/history" class="nav-item" active-class="nav-active">
              <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg>
              Department Logs
            </RouterLink>
          </nav>
        </template>
      </aside>

      <!-- Content Region -->
      <main class="content-region">
        <div class="content-container">
          <RouterView />
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
.erp-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

/* System Header */
.sys-header {
  height: 60px;
  background-color: var(--surface-color);
  border-bottom: 1px solid var(--border-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 32px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-box {
  width: 24px;
  height: 24px;
  background-color: var(--primary-color);
  border-radius: 4px;
}

.brand-text {
  font-size: 1rem;
  font-weight: 600;
  letter-spacing: -0.01em;
  color: var(--text-main);
}

.sys-profile {
  display: flex;
  align-items: center;
  gap: 16px;
}

.profile-details {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.user-name {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-main);
}

.user-role {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.logout-link {
  font-size: 0.70rem;
  color: var(--primary-color);
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
  margin-top: 2px;
  font-weight: 500;
}
.logout-link:hover {
  text-decoration: underline;
}

.profile-avatar {
  width: 36px;
  height: 36px;
  background-color: #e2e8f0;
  color: var(--text-main);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.8125rem;
}

/* Context Strip */
.context-strip {
  background-color: #f8fafc;
  border-bottom: 1px solid var(--border-color);
  padding: 12px 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.8125rem;
}

.crumb-link {
  color: var(--text-muted);
}

.crumb-link:hover {
  color: var(--primary-hover);
}

.crumb-separator {
  color: #94a3b8;
}

.crumb-current {
  font-weight: 500;
  color: var(--primary-color);
}

.emp-id {
  font-size: 0.8125rem;
  font-weight: 500;
  color: var(--text-muted);
}

/* Workspace Area */
.workspace-area {
  display: flex;
  flex: 1;
}

/* Sidebar */
.sidebar-nav {
  width: var(--sidebar-w);
  background-color: var(--surface-color);
  border-right: 1px solid var(--border-light);
  padding: 24px 0;
}

.nav-heading {
  font-size: 0.6875rem;
  text-transform: uppercase;
  font-weight: 600;
  color: var(--text-muted);
  letter-spacing: 0.05em;
  padding: 0 24px;
  margin-bottom: 12px;
}

.nav-menu {
  display: flex;
  flex-direction: column;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 10px 24px;
  color: var(--text-main);
  font-size: 0.875rem;
  font-weight: 500;
  border-left: 3px solid transparent;
  transition: all 0.2s;
}

.nav-item:hover {
  background-color: var(--bg-color);
}

.nav-active {
  background-color: #eff6ff;
  border-left-color: var(--primary-color);
  color: var(--primary-color);
  font-weight: 600;
}

.nav-icon {
  width: 18px;
  height: 18px;
  margin-right: 12px;
  opacity: 0.7;
}

.nav-active .nav-icon {
  opacity: 1;
}

/* Content Area */
.content-region {
  flex: 1;
  padding: 32px;
  overflow-y: auto;
}

.content-container {
  max-width: 1000px;
  margin: 0 auto;
}
</style>
