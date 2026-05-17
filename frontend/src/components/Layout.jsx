import { useLocation } from 'react-router-dom'
import Sidebar from './Sidebar'
import LanguageSwitcher from './LanguageSwitcher'
import { useTranslation } from '../context/LanguageContext'

const ROUTE_TITLES = {
  '/dashboard': 'Bảng điều khiển',
  '/employees': 'Nhân viên',
  '/projects': 'Dự án',
  '/tasks': 'Công việc',
  '/attendance': 'Chấm công',
  '/office-locations': 'Văn phòng',
  '/ai-suggestions': 'AI Gợi ý',
  '/users': 'Người dùng',
  '/my-tasks': 'Công việc của tôi',
  '/my-attendance': 'Chấm công của tôi',
}

export default function Layout({ children }) {
  const { pathname } = useLocation()
  const { t, lang } = useTranslation()
  const titleKey = ROUTE_TITLES[pathname]
  const title = titleKey ? t(titleKey) : 'Task Manager'
  const today = new Date().toLocaleDateString(lang === 'en' ? 'en-US' : 'vi-VN', {
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
          <LanguageSwitcher />
        </header>
        <main className="flex-1 overflow-y-auto p-6">{children}</main>
      </div>
    </div>
  )
}
