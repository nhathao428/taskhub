import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import { LanguageProvider } from './context/LanguageContext'
import ErrorBoundary from './components/ErrorBoundary'
import ProtectedRoute from './components/ProtectedRoute'
import Login from './pages/Login'
import Register from './pages/Register'
import Dashboard from './pages/Dashboard'
import Employees from './pages/Employees'
import Projects from './pages/Projects'
import Tasks from './pages/Tasks'
import Attendance from './pages/Attendance'
import AiSuggestions from './pages/AiSuggestions'
import MyTasks from './pages/MyTasks'
import MyAttendance from './pages/MyAttendance'
import OfficeLocations from './pages/OfficeLocations'
import Users from './pages/Users'

const MANAGER_ROLES = ['MANAGER', 'ADMIN']
const ADMIN_ONLY = ['ADMIN']

export default function App() {
  return (
    <ErrorBoundary>
      <LanguageProvider>
        <AuthProvider>
          <BrowserRouter>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
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
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
          </BrowserRouter>
        </AuthProvider>
      </LanguageProvider>
    </ErrorBoundary>
  )
}
