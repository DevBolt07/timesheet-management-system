<script setup>
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { storeToRefs } from 'pinia'

const route = useRoute()
const authStore = useAuthStore()
const { currentUser, isManager, isStaff } = storeToRefs(authStore)
const isAdmin = computed(() => currentUser.value?.role === 'ADMIN')

const pageMap = {
  '/app/history':          { title: 'Timesheet Logs',        sub: 'View and manage your submitted time records' },
  '/app/log-time':         { title: 'Log Timesheet',         sub: 'Submit a new timesheet entry for review' },
  '/app/review':           { title: 'Pending Approvals',     sub: 'Review and action staff-submitted timesheets' },
  '/app/admin':            { title: 'Admin Dashboard',       sub: 'System-wide governance overview' },
  '/app/admin/oversight':  { title: 'Timesheet Oversight',   sub: 'Full audit view across all staff entries' },
  '/app/admin/users':      { title: 'User Directory',        sub: 'Manage system user accounts' },
  '/app/admin/tasks':      { title: 'Task Master Control',   sub: 'Manage assignable task categories' },
}

const pageInfo = computed(() => pageMap[route.path] || { title: 'Workspace', sub: '' })
</script>

<template>
  <div class="erp-layout">
    <!-- Top System Header -->
    <header class="sys-header">
      <div class="brand">
        <div class="logo-box">
          <svg viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg" width="14" height="14">
            <rect x="2" y="2" width="7" height="7" rx="1" fill="white" fill-opacity="0.9"/>
            <rect x="11" y="2" width="7" height="7" rx="1" fill="white" fill-opacity="0.6"/>
            <rect x="2" y="11" width="7" height="7" rx="1" fill="white" fill-opacity="0.6"/>
            <rect x="11" y="11" width="7" height="7" rx="1" fill="white" fill-opacity="0.9"/>
          </svg>
        </div>
        <span class="brand-text">Collegiate ERP</span>
        <span class="brand-module">Timesheet Module</span>
      </div>
      <div class="sys-profile">
        <div class="profile-details">
          <span class="user-name">{{ currentUser?.name || 'Unrecognized Staff' }}</span>
          <span class="user-meta">{{ currentUser?.department || 'Unassigned' }} &bull; {{ currentUser?.role }}</span>
        </div>
        <div class="profile-avatar">{{ currentUser?.name ? currentUser.name.charAt(0).toUpperCase() : 'U' }}</div>
        <button class="logout-btn" @click="authStore.logout()">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="16" height="16"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
        </button>
      </div>
    </header>

    <!-- Main Workspace -->
    <div class="workspace-area">
      <!-- Sidebar Navigation -->
      <aside class="sidebar-nav">
        <template v-if="isStaff">
          <p class="nav-section-label">Timesheet</p>
          <nav class="nav-menu">
            <RouterLink to="/app/history" class="nav-item" active-class="nav-active">
              <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>
              My Logs
            </RouterLink>
            <RouterLink to="/app/log-time" class="nav-item" active-class="nav-active">
              <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/></svg>
              Add Entry
            </RouterLink>
          </nav>
        </template>

        <template v-if="isManager">
          <p class="nav-section-label">Manager</p>
          <nav class="nav-menu">
            <RouterLink to="/app/review" class="nav-item" active-class="nav-active">
              <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>
              Review Queue
            </RouterLink>
            <RouterLink to="/app/history" class="nav-item" active-class="nav-active">
              <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/></svg>
              Dept. Logs
            </RouterLink>
          </nav>
        </template>

        <template v-if="isAdmin">
          <p class="nav-section-label">Administration</p>
          <nav class="nav-menu">
            <RouterLink to="/app/admin" class="nav-item" active-class="nav-active" exact>
              <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>
              Dashboard
            </RouterLink>
            <RouterLink to="/app/admin/oversight" class="nav-item" active-class="nav-active">
              <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>
              Oversight
            </RouterLink>
            <RouterLink to="/app/admin/users" class="nav-item" active-class="nav-active">
              <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
              Users
            </RouterLink>
            <RouterLink to="/app/admin/tasks" class="nav-item" active-class="nav-active">
              <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg>
              Task Master
            </RouterLink>
          </nav>
        </template>

        <!-- Sidebar Footer -->
        <div class="sidebar-footer">
          <span class="sidebar-emp-id">ID: {{ currentUser?.id || '—' }}</span>
        </div>
      </aside>

      <!-- Content Region -->
      <main class="content-region">
        <!-- Page Header Strip -->
        <div class="page-header-strip">
          <div class="page-title-block">
            <h1 class="page-title">{{ pageInfo.title }}</h1>
            <span class="page-sub" v-if="pageInfo.sub">{{ pageInfo.sub }}</span>
          </div>
          <div class="page-header-right">
            <span class="header-breadcrumb">
              <RouterLink to="/" class="crumb-link">Home</RouterLink>
              <span class="crumb-sep">/</span>
              <span class="crumb-current">{{ pageInfo.title }}</span>
            </span>
          </div>
        </div>

        <!-- View Content -->
        <div class="content-body">
          <RouterView />
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
/* ── Shell ─────────────────────────────────────────────────────────────────── */
.erp-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: var(--bg-color);
}

/* ── Top Header ────────────────────────────────────────────────────────────── */
.sys-header {
  height: var(--header-h);
  background-color: var(--surface-color);
  border-bottom: 1px solid var(--border-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  flex-shrink: 0;
  z-index: 100;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-box {
  width: 28px;
  height: 28px;
  background-color: var(--primary-color);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.brand-text {
  font-size: 0.9375rem;
  font-weight: 700;
  color: var(--text-main);
  letter-spacing: -0.01em;
}

.brand-module {
  font-size: 0.75rem;
  color: var(--text-muted);
  background: var(--bg-color);
  padding: 2px 8px;
  border-radius: 4px;
  border: 1px solid var(--border-light);
  font-weight: 500;
}

.sys-profile {
  display: flex;
  align-items: center;
  gap: 12px;
}

.profile-details {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.user-name {
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--text-main);
  line-height: 1.3;
}

.user-meta {
  font-size: 0.7rem;
  color: var(--text-muted);
  line-height: 1.3;
}

.profile-avatar {
  width: 32px;
  height: 32px;
  background-color: #dbeafe;
  color: var(--primary-color);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.8rem;
  border: 1px solid #bfdbfe;
  flex-shrink: 0;
}

.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: 1px solid var(--border-light);
  border-radius: 6px;
  background: none;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.15s;
}
.logout-btn:hover {
  background: #fee2e2;
  border-color: #fca5a5;
  color: #dc2626;
}

/* ── Workspace (Sidebar + Content) ─────────────────────────────────────────── */
.workspace-area {
  display: flex;
  flex: 1;
  overflow: hidden;
  min-height: 0;
}

/* ── Sidebar ────────────────────────────────────────────────────────────────── */
.sidebar-nav {
  width: var(--sidebar-w);
  min-width: var(--sidebar-w);
  background-color: var(--surface-color);
  border-right: 1px solid var(--border-light);
  display: flex;
  flex-direction: column;
  padding-top: 20px;
  overflow-y: auto;
  flex-shrink: 0;
}

.nav-section-label {
  font-size: 0.6375rem;
  text-transform: uppercase;
  font-weight: 700;
  color: var(--text-muted);
  letter-spacing: 0.07em;
  padding: 0 18px;
  margin: 8px 0 6px;
}

.nav-menu {
  display: flex;
  flex-direction: column;
  margin-bottom: 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 9px 18px;
  color: var(--text-muted);
  font-size: 0.8125rem;
  font-weight: 500;
  border-left: 3px solid transparent;
  transition: all 0.15s;
  gap: 10px;
}

.nav-item:hover {
  background-color: var(--bg-color);
  color: var(--text-main);
}

.nav-active {
  background-color: #eff6ff;
  border-left-color: var(--primary-color);
  color: var(--primary-color);
  font-weight: 600;
}

.nav-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
  opacity: 0.6;
}
.nav-active .nav-icon { opacity: 1; }
.nav-item:hover .nav-icon { opacity: 0.85; }

.sidebar-footer {
  margin-top: auto;
  padding: 16px 18px;
  border-top: 1px solid var(--border-light);
}

.sidebar-emp-id {
  font-size: 0.7rem;
  color: var(--text-muted);
  font-weight: 500;
  font-variant-numeric: tabular-nums;
}

/* ── Content Region ─────────────────────────────────────────────────────────── */
.content-region {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

/* Page header strip — full width, inside the content region */
.page-header-strip {
  background-color: var(--surface-color);
  border-bottom: 1px solid var(--border-light);
  padding: 14px var(--content-pad);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}

.page-title-block {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.page-title {
  font-size: 1.0625rem;
  font-weight: 700;
  color: var(--text-main);
  margin: 0;
  letter-spacing: -0.01em;
  line-height: 1.3;
}

.page-sub {
  font-size: 0.75rem;
  color: var(--text-muted);
  line-height: 1.3;
}

.page-header-right { display: flex; align-items: center; }

.header-breadcrumb {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.75rem;
}

.crumb-link { color: var(--text-muted); }
.crumb-link:hover { color: var(--primary-color); }
.crumb-sep { color: #94a3b8; }
.crumb-current { color: var(--primary-color); font-weight: 600; }

/* Scrollable content body */
.content-body {
  flex: 1;
  overflow-y: auto;
  padding: var(--content-pad);
}
</style>
