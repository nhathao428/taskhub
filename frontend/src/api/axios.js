import axios from 'axios'

// Accept either a full URL ("https://api.example.com") or just a hostname
// ("api.example.com" — useful for platforms like Render that expose
// `fromService.host` without a protocol). Empty value falls back to local dev.
const rawBase = (import.meta.env.VITE_API_BASE_URL || '').trim()
const baseURL = rawBase
  ? (/^https?:\/\//.test(rawBase) ? rawBase : `https://${rawBase}`)
  : 'http://localhost:5000'

const api = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
})

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default api
