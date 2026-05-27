import axios from 'axios'
import { demoApiResponse } from '../demo/demoData'

// Accept either a full URL ("https://api.example.com") or just a hostname
// ("api.example.com" — useful for platforms like Render that expose
// `fromService.host` without a protocol). Empty value falls back to local dev.
const rawBase = (import.meta.env.VITE_API_BASE_URL || '').trim()
const baseURL = rawBase
  ? (/^https?:\/\//.test(rawBase) ? rawBase : `https://${rawBase}`)
  : 'http://localhost:5000'

// Adapter mặc định của axios (xhr/http/fetch)
const realAdapter = axios.getAdapter(axios.defaults.adapter)

function isDemoMode() {
  try {
    return sessionStorage.getItem('demo') === '1'
  } catch {
    return false
  }
}

// Ở chế độ dùng thử: trả dữ liệu mẫu thay vì gọi backend thật.
// Riêng /api/auth/* và /api/v{N}/auth/* vẫn gọi thật để khách có thể đăng nhập / đăng ký.
function demoAwareAdapter(config) {
  const url = config.url || ''
  if (isDemoMode() && !/\/api\/(v\d+\/)?auth\//.test(url)) {
    return demoApiResponse(config)
  }
  return realAdapter(config)
}

const api = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
  adapter: demoAwareAdapter,
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
