import { createContext, useContext, useState } from 'react'
import api from '../api/axios'

const AuthContext = createContext(null)

// Tài khoản giả lập cho chế độ dùng thử — hiển thị giao diện cấp quản lý.
const DEMO_USER = { username: 'Khách dùng thử', email: '', role: 'MANAGER' }

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      const saved = localStorage.getItem('user')
      return saved ? JSON.parse(saved) : null
    } catch {
      return null
    }
  })
  const [token, setToken] = useState(() => localStorage.getItem('token'))
  const [isDemo, setIsDemo] = useState(() => {
    try {
      return sessionStorage.getItem('demo') === '1'
    } catch {
      return false
    }
  })

  const enterDemo = () => {
    try { sessionStorage.setItem('demo', '1') } catch { /* ignore */ }
    setIsDemo(true)
  }

  const exitDemo = () => {
    try { sessionStorage.removeItem('demo') } catch { /* ignore */ }
    setIsDemo(false)
  }

  const login = async (email, password) => {
    const response = await api.post('/api/auth/login', { email, password })
    const { token: newToken, username, email: userEmail, role } = response.data.data
    const userData = { username, email: userEmail, role: role || 'EMPLOYEE' }
    localStorage.setItem('token', newToken)
    localStorage.setItem('user', JSON.stringify(userData))
    setToken(newToken)
    setUser(userData)
    exitDemo()
    return response.data
  }

  const register = async (username, email, password) => {
    const response = await api.post('/api/auth/register', { username, email, password })
    return response.data
  }

  const logout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    setToken(null)
    setUser(null)
    exitDemo()
  }

  const isAuthenticated = !!token
  // Trong chế độ demo (chưa đăng nhập), dùng tài khoản giả lập.
  const effectiveUser = isAuthenticated ? user : (isDemo ? DEMO_USER : user)

  return (
    <AuthContext.Provider
      value={{
        user: effectiveUser,
        token,
        isAuthenticated,
        isDemo,
        enterDemo,
        exitDemo,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
