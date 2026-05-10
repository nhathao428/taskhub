import { useLocation } from 'react-router-dom'
import Sidebar from './Sidebar'

const ROUTE_TITLES = {
  '/dashboard': 'Bảng điều khiển',
  '/employees': 'Nhân viên',
  '/projects': 'Dự án',
  '/tasks': 'Công việc',
  '/attendance': 'Chấm công',
  '/ai-suggestions': 'AI Gợi ý',
}

export default function Layout({ children }) {
  const { pathname } = useLocation()
  const title = ROUTE_TITLES[pathname] || 'Task Manager'
  const today = new Date().toLocaleDateString('vi-VN', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar />
      <div className="flex-1 flex flex-col overflow-hidden">
        <header className="bg-white border-b border-gray-200 px-6 py-3 flex items-center justify-between flex-shrink-0">
          <div>
            <h2 className="text-lg font-semibold text-gray-800">{title}</h2>
            <p className="text-xs text-gray-500 capitalize">{today}</p>
          </div>
        </header>
        <main className="flex-1 overflow-y-auto p-6">{children}</main>
      </div>
    </div>
  )
}
