<script setup>
defineProps({
  data: {
    type: Array,
    required: true,
    default: () => []
  }
})

const emit = defineEmits(['delete-entry'])

const formatTime = (timeString) => {
  if (!timeString) return '';
  const [hours, minutes] = timeString.split(':');
  let h = parseInt(hours, 10);
  const ampm = h >= 12 ? 'PM' : 'AM';
  h = h % 12 || 12;
  const paddedH = h.toString().padStart(2, '0');
  return `${paddedH}:${minutes} ${ampm}`;
}

const statusClass = (status) => {
  switch (status) {
    case 'APPROVED': return 'status-approved';
    case 'REJECTED': return 'status-rejected';
    default: return 'status-pending';
  }
}

const getInitials = (taskStr) => {
  if (!taskStr) return 'T';
  return taskStr.substring(0, 1).toUpperCase();
}
</script>

<template>
  <div class="table-frame">
    <table class="data-table">
      <thead>
        <tr>
          <th width="35%">Task & Description</th>
          <th width="20%">Date & Time</th>
          <th width="15%">Role Context</th>
          <th width="15%">Status</th>
          <th width="15%" class="text-right">Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="data.length === 0">
          <td colspan="5" class="empty-state">
            <div class="empty-icon">📝</div>
            <p>No timesheet entries found globally.</p>
          </td>
        </tr>
        <tr v-for="entry in data" :key="entry.id" class="data-row">
          
          <!-- Avatar + Text Composition -->
          <td>
            <div class="cell-composite">
              <div class="cell-avatar">{{ getInitials(entry.task) }}</div>
              <div class="cell-text">
                <div class="cell-title">{{ entry.task }}</div>
                <div class="cell-subtitle">{{ entry.description || 'No specific details added.' }}</div>
              </div>
            </div>
          </td>

          <!-- Date & Time Combos -->
          <td>
            <div class="cell-text">
              <div class="cell-title">{{ entry.date }}</div>
              <div class="cell-subtitle">{{ formatTime(entry.startTime) }} - {{ formatTime(entry.endTime) }}</div>
            </div>
          </td>

          <!-- Team / Context Placeholder -->
          <td>
            <div class="role-badge">Staff Contributor</div>
          </td>

          <!-- Status Indicator -->
          <td>
            <span :class="['status-badge', statusClass(entry.status)]">
              <span class="status-dot"></span>
              {{ entry.status || 'PENDING' }}
            </span>
          </td>

          <!-- Actions -->
          <td class="text-right actions-cell">
            <button class="action-btn text-link" title="Edit row">Edit</button>
            <button 
              class="action-btn text-danger" 
              @click="emit('delete-entry', entry.id)"
              :disabled="entry.status !== 'PENDING'"
              title="Delete (Pending only)"
            >
              Delete
            </button>
            <button class="action-btn icon-btn">⋮</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.table-frame {
  width: 100%;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-subtle);
  vertical-align: middle;
}

th {
  text-align: left;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  background-color: var(--bg-surface);
}

.text-right {
  text-align: right;
}

.data-row {
  background-color: #ffffff;
}

.data-row:hover {
  background-color: #f8fafc;
}

/* Cell Composition block */
.cell-composite {
  display: flex;
  align-items: center;
  gap: 12px;
}

.cell-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: #eff6ff;
  color: var(--primary-blue);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.875rem;
}

.cell-text {
  display: flex;
  flex-direction: column;
}

.cell-title {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-main);
}

.cell-subtitle {
  font-size: 0.75rem;
  color: var(--text-muted);
  margin-top: 2px;
  max-width: 280px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Role badge */
.role-badge {
  font-size: 0.8125rem;
  color: var(--text-muted);
  background-color: #f3f4f6;
  padding: 4px 8px;
  border-radius: 4px;
  display: inline-block;
}

/* Visual Status Badges */
.status-badge {
  display: inline-flex;
  align-items: center;
  font-size: 0.8125rem;
  font-weight: 500;
  padding: 4px 10px;
  border-radius: 12px;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 6px;
}

.status-pending { 
  background-color: var(--status-pending-bg);
  color: var(--status-pending-text);
}
.status-pending .status-dot { background-color: var(--status-pending-text); }

.status-approved { 
  background-color: var(--status-approved-bg);
  color: var(--status-approved-text);
}
.status-approved .status-dot { background-color: var(--status-approved-text); }

.status-rejected { 
  background-color: var(--status-rejected-bg);
  color: var(--status-rejected-text);
}
.status-rejected .status-dot { background-color: var(--status-rejected-text); }

/* Actions */
.actions-cell {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
}

.action-btn {
  font-size: 0.8125rem;
  font-weight: 500;
  padding: 4px 6px;
}

.text-link {
  color: var(--primary-blue);
}
.text-link:hover { text-decoration: underline; }

.text-danger {
  color: #dc2626;
}
.text-danger:hover:not(:disabled) { text-decoration: underline; cursor: pointer; }
.text-danger:disabled {
  color: #9ca3af;
  cursor: not-allowed;
}

.icon-btn {
  color: #6b7280;
  font-size: 1.1rem;
  padding: 4px;
}
.icon-btn:hover { background-color: #f3f4f6; border-radius: 4px; }

.empty-state {
  text-align: center;
  padding: 64px 24px;
  color: var(--text-muted);
}

.empty-icon {
  font-size: 2rem;
  margin-bottom: 8px;
  opacity: 0.5;
}
</style>
