export const apiClient = {
  getHeaders() {
    const context = localStorage.getItem('erp_auth_context')
    const headers = { 'Content-Type': 'application/json' }
    
    if (context) {
       try {
           const parsed = JSON.parse(context)
           if (parsed?.id) headers['X-ERP-User-Id'] = String(parsed.id)
       } catch (e) { console.error('Silent ERP Auth Context Parsing Failure') }
    }
    return headers
  },
  async get(url) {
    const res = await fetch(url, { headers: this.getHeaders() })
    const json = await res.json()
    if (!res.ok || (json.success === false)) throw new Error(json.message || 'Request failed')
    return json.data
  },
  async post(url, data) {
    const res = await fetch(url, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(data)
    })
    const json = await res.json()
    if (!res.ok || !json.success) throw new Error(json.message || 'Request failed')
    return json.data
  },
  async put(url, data) {
    const res = await fetch(url, {
      method: 'PUT',
      headers: this.getHeaders(),
      body: JSON.stringify(data)
    })
    const json = await res.json()
    if (!res.ok || !json.success) throw new Error(json.message || 'Request failed')
    return json.data
  },
  async delete(url) {
    const res = await fetch(url, {
      method: 'DELETE',
      headers: this.getHeaders()
    })
    const json = await res.json()
    if (!res.ok || !json.success) throw new Error(json.message || 'Request failed')
    return json.data
  }
}
