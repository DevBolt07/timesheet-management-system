export const apiClient = {
  async get(url) {
    const res = await fetch(url)
    if (!res.ok) throw new Error(await res.text())
    return (await res.json()).data
  },
  async post(url, data) {
    const res = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    })
    const json = await res.json()
    if (!res.ok || !json.success) throw new Error(json.message || 'Request failed')
    return json.data
  },
  async put(url, data) {
    const res = await fetch(url, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    })
    const json = await res.json()
    if (!res.ok || !json.success) throw new Error(json.message || 'Request failed')
    return json.data
  },
  async delete(url) {
    const res = await fetch(url, {
      method: 'DELETE'
    })
    const json = await res.json()
    if (!res.ok || !json.success) throw new Error(json.message || 'Request failed')
    return json.data
  }
}
