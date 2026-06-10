import { lazy, Suspense } from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import { LanguageProvider } from './context/LanguageContext'
import ErrorBoundary from './components/ErrorBoundary'
import ProtectedRoute from './components/ProtectedRoute'
// Landing/Login/Register giữ eager: first paint trên route public, không cần chờ chunk.
import Landing from './pages/Landing'
import Login from './pages/Login'
import Register from './pages/Register'
import ForgotPassword from './pages/ForgotPassword'
import ResetPassword from './pages/ResetPassword'

// Wrap dynamic import() với fallback hiển thị nút "Tải lại" khi chunk load fail
// (network drop, deploy mới invalidate hash, etc.) — tránh user gặp blank screen.
function lazyWithRetry(loader) {
  return lazy(() =>
    loader().catch(() => ({
      default: () => (
        <div className="min-h-screen flex items-center justify-center bg-slate-50 p-6">
          <div className="max-w-sm text-center">
            <p className="text-slate-700 mb-3">
              Không tải được trang. Mạng có thể bị gián đoạn.
            </p>
            <button
              onClick={() => window.location.reload()}
              className="px-4 py-2 rounded-lg bg-brand-600 text-white text-sm font-semibold"
            >
              Tải lại
            </button>
          </div>
        </div>
      ),
    }))
  )
}

const Dashboard = lazyWithRetry(() => import('./pages/Dashboard'))
const Employees = lazyWithRetry(() => import('./pages/Employees'))
const Projects = lazyWithRetry(() => import('./pages/Projects'))
const Tasks = lazyWithRetry(() => import('./pages/Tasks'))
const Attendance = lazyWithRetry(() => import('./pages/Attendance'))
const AiSuggestions = lazyWithRetry(() => import('./pages/AiSuggestions'))
const MyTasks = lazyWithRetry(() => import('./pages/MyTasks'))
const MyAttendance = lazyWithRetry(() => import('./pages/MyAttendance'))
const OfficeLocations = lazyWithRetry(() => import('./pages/OfficeLocations'))
const Users = lazyWithRetry(() => import('./pages/Users'))

const MANAGER_ROLES = ['MANAGER', 'ADMIN']
const ADMIN_ONLY = ['ADMIN']

function RouteFallback() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50">
      <div className="flex items-center gap-3 text-slate-500">
        <span className="h-3 w-3 rounded-full bg-brand-500 animate-pulse" />
        <span className="text-sm">Đang tải...</span>
      </div>
    </div>
  )
}

export default function App() {
  return (
    <ErrorBoundary>
      <LanguageProvider>
        <AuthProvider>
          <BrowserRouter>
          <Suspense fallback={<RouteFallback />}>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/forgot-password" element={<ForgotPassword />} />
            <Route path="/reset-password" element={<ResetPassword />} />
            <Route
              path="/dashboard"
              element={
                <ProtectedRoute>
                  <Dashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="/employees"
              element={
                <ProtectedRoute allowedRoles={MANAGER_ROLES}>
                  <Employees />
                </ProtectedRoute>
              }
            />
            <Route
              path="/projects"
              element={
                <ProtectedRoute>
                  <Projects />
                </ProtectedRoute>
              }
            />
            <Route
              path="/tasks"
              element={
                <ProtectedRoute allowedRoles={MANAGER_ROLES}>
                  <Tasks />
                </ProtectedRoute>
              }
            />
            <Route
              path="/attendance"
              element={
                <ProtectedRoute allowedRoles={MANAGER_ROLES}>
                  <Attendance />
                </ProtectedRoute>
              }
            />
            <Route
              path="/office-locations"
              element={
                <ProtectedRoute allowedRoles={MANAGER_ROLES}>
                  <OfficeLocations />
                </ProtectedRoute>
              }
            />
            <Route
              path="/ai-suggestions"
              element={
                <ProtectedRoute allowedRoles={MANAGER_ROLES}>
                  <AiSuggestions />
                </ProtectedRoute>
              }
            />
            <Route
              path="/users"
              element={
                <ProtectedRoute allowedRoles={ADMIN_ONLY}>
                  <Users />
                </ProtectedRoute>
              }
            />
            <Route
              path="/my-tasks"
              element={
                <ProtectedRoute>
                  <MyTasks />
                </ProtectedRoute>
              }
            />
            <Route
              path="/my-attendance"
              element={
                <ProtectedRoute>
                  <MyAttendance />
                </ProtectedRoute>
              }
            />
            <Route path="/" element={<Landing />} />
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
          </Suspense>
          </BrowserRouter>
        </AuthProvider>
      </LanguageProvider>
    </ErrorBoundary>
  )
}
