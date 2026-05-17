import { Navigate } from 'react-router-dom'
import Layout from './Layout'
import { useAuth } from '../context/AuthContext'

export default function ProtectedRoute({ children, allowedRoles }) {
  const { isAuthenticated, isDemo, user } = useAuth()
  // Chưa đăng nhập và cũng không ở chế độ dùng thử → về trang đăng nhập.
  if (!isAuthenticated && !isDemo) {
    return <Navigate to="/login" replace />
  }
  if (allowedRoles && allowedRoles.length > 0) {
    const role = user?.role || 'EMPLOYEE'
    if (!allowedRoles.includes(role)) {
      return <Navigate to="/dashboard" replace />
    }
  }
  return <Layout>{children}</Layout>
}
