<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const router = useRouter()
const authStore = useAuthStore()

const isConnecting = ref(false)
const form = ref({ username: '', password: '' })
const loginError = ref('')

const handleLogin = async () => {
  if (isConnecting.value) return
  isConnecting.value = true
  loginError.value = ''
  
  try {
     await authStore.loginAsDemo(form.value.username, form.value.password)
     // Navigate directly to the role-specific default workspace.
     // homeRoute is a computed in authStore: ADMIN→/app/admin, HOD→/app/review, STAFF→/app/history
     router.push(authStore.homeRoute)
  } catch (e) {
     loginError.value = e.message
     console.error(e)
  } finally {
     isConnecting.value = false
  }
}
</script>

<template>
  <div class="landing-page">
    <main class="page-body">
      <div class="intro-section">
        <h1>Welcome back</h1>
        <p>Log in securely to access your timesheet records and academic evaluation workflows.</p>
      </div>

      <div class="login-container">
        <form @submit.prevent="handleLogin" class="login-form">
          <div class="form-group">
            <label>Enterprise Username</label>
            <input type="text" v-model="form.username" placeholder="e.g. dipak.pawar" required />
          </div>
          <div class="form-group">
            <label>Password</label>
            <input type="password" v-model="form.password" placeholder="Enter your password" required />
          </div>
          
          <div v-if="loginError" class="error-msg">
            {{ loginError }}
          </div>

          <button type="submit" class="blue-btn" :disabled="isConnecting">
            <span v-if="isConnecting">Authenticating...</span>
            <span v-else>Secure Login</span>
          </button>
        </form>

        <div class="demo-credentials-box">
          <h4>Integration Test Accounts</h4>
          <p>This is a simulated demo environment simulating a deployed ERP SSO workflow. Use the active sandbox credentials below to test capabilities:</p>
          <ul class="demo-list">
            <li><b>Staff Member:</b> dipak.pawar<br/><span>pw: <code>staff123</code></span></li>
            <li><b>Staff Member:</b> kavita.deshmukh<br/><span>pw: <code>staff123</code></span></li>
            <li><b>Staff Member:</b> rahul.verma<br/><span>pw: <code>staff123</code></span></li>
            <li><b>HOD/Manager:</b> dr.sharma<br/><span>pw: <code>manager123</code></span></li>
            <li><b>System Admin:</b> system.admin<br/><span>pw: <code>admin123</code></span></li>
          </ul>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.landing-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
}

.page-body {
  width: 100%;
  max-width: 450px;
  text-align: center;
}

.intro-section {
  margin-bottom: 2rem;
}

.intro-section h1 {
  font-size: 2.25rem;
  color: var(--text-dark);
  margin-bottom: 0.5rem;
}

.intro-section p {
  color: var(--text-muted);
  font-size: 1.05rem;
  line-height: 1.5;
}

.login-container {
  width: 100%;
}

.login-form {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  text-align: left;
}

.form-group label {
  display: block;
  font-weight: 600;
  margin-bottom: 5px;
  color: var(--text-dark);
}

.form-group input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  transition: border-color 0.2s;
}

.form-group input:focus {
  border-color: var(--primary-color);
  outline: none;
}

.error-msg {
  color: #ef4444;
  font-size: 0.9rem;
  background: #fef2f2;
  border: 1px solid #fca5a5;
  padding: 8px;
  border-radius: 6px;
  text-align: center;
}

.blue-btn {
  background: var(--primary-color);
  color: white;
  border: none;
  padding: 12px;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  margin-top: 5px;
  transition: opacity 0.2s;
}

.blue-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.demo-credentials-box {
  margin-top: 2rem;
  background: #f8f9fc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 1.25rem;
  text-align: left;
}

.demo-credentials-box h4 {
  margin-bottom: 0.5rem;
  font-size: 1rem;
  color: var(--text-dark);
}

.demo-credentials-box p {
  font-size: 0.85rem;
  color: var(--text-muted);
  margin-bottom: 1rem;
}

.demo-list {
  font-size: 0.85rem;
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 0.75rem;
}

.demo-list li {
  background: white;
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.demo-list code {
  background: #e2e8f0;
  padding: 2px 6px;
  border-radius: 4px;
}
</style>
