<script setup>
defineProps({
  data: {
    type: Array,
    required: true,
    default: () => []
  }
})

const formatTime = (timeString) => {
  if (!timeString) return '';
  const [hours, minutes] = timeString.split(':');
  let h = parseInt(hours, 10);
  const ampm = h >= 12 ? 'PM' : 'AM';
  h = h % 12 || 12;
  const paddedH = h.toString().padStart(2, '0');
  return `${paddedH}:${minutes} ${ampm}`;
}
</script>

<template>
  <div class="table-container">
    <table class="data-table">
      <thead>
        <tr>
          <th>Date</th>
          <th>Task</th>
          <th>Time Range</th>
          <th>Description</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="data.length === 0">
          <td colspan="4" class="empty-state">No timesheet entries found.</td>
        </tr>
        <tr v-for="entry in data" :key="entry.id" class="data-row">
          <td>{{ entry.date }}</td>
          <td><span class="badge">{{ entry.task }}</span></td>
          <td>{{ formatTime(entry.startTime) }} - {{ formatTime(entry.endTime) }}</td>
          <td>{{ entry.description }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.table-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
  overflow: hidden;
  margin-top: 20px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 18px 24px;
  text-align: left;
  border-bottom: 1px solid var(--border-color);
}

th {
  background-color: #f8fafc;
  color: #64748b;
  font-weight: 600;
  text-transform: uppercase;
  font-size: 0.85rem;
  letter-spacing: 0.05em;
}

.data-row {
  transition: all 0.1s ease;
}

.data-row:hover {
  background-color: #f1f5f9;
  transform: scale(1.001);
  box-shadow: inset 3px 0 0 var(--accent-color);
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #94a3b8;
  font-style: italic;
}

.badge {
  background-color: #e0f2fe;
  color: #0369a1;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
  letter-spacing: 0.03em;
  display: inline-block;
}
</style>
