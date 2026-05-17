import { useLocation, Link } from 'react-router-dom'
import { MdInfoOutline } from 'react-icons/md'
import Sidebar from './Sidebar'
import LanguageSwitcher from './LanguageSwitcher'
import { useTranslation } from '../context/LanguageContext'
import { useAuth } from '../context/AuthContext'

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

function DemoBanner() {
  const { t } = useTranslation()
  return (
    <div className="flex items-center justify-center gap-3 bg-amber-500 text-white px-4 py-2 text-sm flex-shrink-0">
      <MdInfoOutline className="text-lg flex-shrink-0" />
      <span className="font-medium">
        {t('Chế độ dùng thử — bạn đang xem dữ liệu mẫu.')}
      </span>
      <Link
        to="/login"
        className="bg-white text-amber-700 font-semibold px-3 py-0.5 rounded-md hover:bg-amber-50 transition-colors"
      >
        {t('Đăng nhập để dùng đầy đủ')}
      </Link>
    </div>
  )
}

export default function Layout({ children }) {
  const { pathname } = useLocation()
  const { t, lang } = useTranslation()
  const { isDemo } = useAuth()
  const titleKey = ROUTE_TITLES[pathname]
  const title = titleKey ? t(titleKey) : 'Task Manager'
  const today = new Date().toLocaleDateString(lang === 'en' ? 'en-US' : 'vi-VN', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })

  return (
    <div className="flex flex-col h-screen bg-gray-50">
      {isDemo && <DemoBanner />}
      <div className="flex flex-1 overflow-hidden">
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
    </div>
  )
}
