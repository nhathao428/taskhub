import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter, Routes, Route } from 'react-router-dom'
import { AuthProvider } from '../context/AuthContext'
import { LanguageProvider } from '../context/LanguageContext'
import ProtectedRoute from '../components/ProtectedRoute'

function renderWithAuth(initialEntries, children) {
  return render(
    <LanguageProvider>
      <AuthProvider>
        <MemoryRouter initialEntries={initialEntries}>
          <Routes>
            <Route path="/login" element={<div>LOGIN_PAGE</div>} />
            <Route path="/dashboard" element={<div>DASHBOARD_PAGE</div>} />
            <Route path="/protected" element={children} />
          </Routes>
        </MemoryRouter>
      </AuthProvider>
    </LanguageProvider>
  )
}

describe('ProtectedRoute', () => {
  it('redirects to /login when user is not authenticated and not in demo mode', () => {
    renderWithAuth(
      ['/protected'],
      <ProtectedRoute>
        <div>SECRET</div>
      </ProtectedRoute>
    )
    expect(screen.getByText('LOGIN_PAGE')).toBeInTheDocument()
    expect(screen.queryByText('SECRET')).not.toBeInTheDocument()
  })

  it('redirects to /dashboard when authenticated user lacks required role', () => {
    localStorage.setItem('token', 'fake-token')
    localStorage.setItem(
      'user',
      JSON.stringify({ username: 'u', email: 'u@x', role: 'EMPLOYEE' })
    )

    renderWithAuth(
      ['/protected'],
      <ProtectedRoute allowedRoles={['ADMIN']}>
        <div>ADMIN_AREA</div>
      </ProtectedRoute>
    )
    expect(screen.getByText('DASHBOARD_PAGE')).toBeInTheDocument()
    expect(screen.queryByText('ADMIN_AREA')).not.toBeInTheDocument()
  })

  it('allows authenticated user with matching role', () => {
    localStorage.setItem('token', 'fake-token')
    localStorage.setItem(
      'user',
      JSON.stringify({ username: 'u', email: 'u@x', role: 'ADMIN' })
    )

    renderWithAuth(
      ['/protected'],
      <ProtectedRoute allowedRoles={['ADMIN']}>
        <div>ADMIN_AREA</div>
      </ProtectedRoute>
    )
    expect(screen.getByText('ADMIN_AREA')).toBeInTheDocument()
  })

  it('allows demo mode access even without authentication', () => {
    sessionStorage.setItem('demo', '1')

    renderWithAuth(
      ['/protected'],
      <ProtectedRoute>
        <div>DEMO_AREA</div>
      </ProtectedRoute>
    )
    expect(screen.getByText('DEMO_AREA')).toBeInTheDocument()
  })

  it('demo user gets MANAGER role automatically', () => {
    sessionStorage.setItem('demo', '1')

    renderWithAuth(
      ['/protected'],
      <ProtectedRoute allowedRoles={['MANAGER', 'ADMIN']}>
        <div>MANAGER_AREA</div>
      </ProtectedRoute>
    )
    expect(screen.getByText('MANAGER_AREA')).toBeInTheDocument()
  })
})
