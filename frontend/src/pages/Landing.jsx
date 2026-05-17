import { Link, useNavigate, Navigate } from 'react-router-dom'
import {
  MdWorkspaces, MdLogin, MdPersonAdd, MdPlayArrow, MdArrowForward,
  MdChecklist, MdAutoAwesome, MdLocationOn, MdShield, MdLanguage,
} from 'react-icons/md'
import { useAuth } from '../context/AuthContext'
import { useTranslation } from '../context/LanguageContext'
import LanguageSwitcher from '../components/LanguageSwitcher'

const FEATURES = [
  { icon: MdChecklist, title: 'Quản lý công việc & dự án',
    desc: 'Giao việc, theo dõi tiến độ, hạn chót và trạng thái.' },
  { icon: MdAutoAwesome, title: 'AI gợi ý nhân viên',
    desc: 'Google Gemini phân tích và đề xuất người phù hợp nhất.' },
  { icon: MdLocationOn, title: 'Chấm công GPS',
    desc: 'Xác thực vị trí bằng bản đồ, hạn chế chấm công gian lận.' },
  { icon: MdShield, title: 'Phân quyền & bảo mật',
    desc: 'Ba vai trò, xác thực JWT, dữ liệu được bảo vệ nhiều lớp.' },
  { icon: MdWorkspaces, title: 'Dashboard trực quan',
    desc: 'Biểu đồ thống kê công việc và nhân sự theo thời gian thực.' },
  { icon: MdLanguage, title: 'Song ngữ Việt / Anh',
    desc: 'Chuyển đổi ngôn ngữ giao diện tức thì, mọi lúc.' },
]

export default function Landing() {
  const { isAuthenticated, enterDemo } = useAuth()
  const { t } = useTranslation()
  const navigate = useNavigate()

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />
  }

  const tryDemo = () => {
    enterDemo()
    navigate('/dashboard')
  }

  return (
    <div className="min-h-screen bg-white">
      {/* Hero */}
      <div className="relative overflow-hidden bg-gradient-to-br from-indigo-600 via-purple-600 to-pink-500 text-white">
        <div className="absolute -top-32 -right-24 w-96 h-96 bg-white/10 rounded-full blur-3xl" />
        <div className="absolute -bottom-32 -left-24 w-96 h-96 bg-white/10 rounded-full blur-3xl" />

        <div className="relative max-w-6xl mx-auto px-6">
          {/* Nav */}
          <div className="flex items-center justify-between py-5">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-white/15 backdrop-blur flex items-center justify-center">
                <MdWorkspaces className="text-2xl" />
              </div>
              <span className="text-lg font-bold">Task Manager</span>
            </div>
            <LanguageSwitcher variant="dark" />
          </div>

          {/* Hero content */}
          <div className="py-16 md:py-24 max-w-3xl">
            <h1 className="text-3xl md:text-5xl font-bold leading-tight">
              {t('Quản lý công việc thông minh cho doanh nghiệp nhỏ')}
            </h1>
            <p className="mt-5 text-base md:text-lg text-indigo-100 leading-relaxed">
              {t('Giao việc, theo dõi tiến độ, chấm công GPS và để AI gợi ý nhân viên phù hợp — tất cả trong một hệ thống.')}
            </p>
            <div className="mt-8 flex flex-wrap gap-3">
              <button
                onClick={tryDemo}
                className="inline-flex items-center gap-2 bg-white text-indigo-700 font-semibold px-6 py-3 rounded-xl shadow-lg hover:shadow-xl transition-all"
              >
                <MdPlayArrow className="text-xl" />
                {t('Dùng thử ngay')}
                <MdArrowForward />
              </button>
              <Link
                to="/login"
                className="inline-flex items-center gap-2 bg-white/15 backdrop-blur border border-white/30 text-white font-semibold px-6 py-3 rounded-xl hover:bg-white/25 transition-all"
              >
                <MdLogin className="text-xl" />
                {t('Đăng nhập')}
              </Link>
              <Link
                to="/register"
                className="inline-flex items-center gap-2 text-white/90 font-semibold px-4 py-3 rounded-xl hover:text-white transition-all"
              >
                <MdPersonAdd className="text-xl" />
                {t('Đăng ký')}
              </Link>
            </div>
            <p className="mt-4 text-sm text-indigo-200">
              {t('Chế độ dùng thử cho xem trước giao diện với dữ liệu mẫu — không cần tài khoản.')}
            </p>
          </div>
        </div>
      </div>

      {/* Features */}
      <div className="max-w-6xl mx-auto px-6 py-16">
        <h2 className="text-2xl md:text-3xl font-bold text-gray-800 text-center">
          {t('Tính năng nổi bật')}
        </h2>
        <div className="mt-10 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {FEATURES.map(({ icon: Icon, title, desc }) => (
            <div
              key={title}
              className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 hover:shadow-md transition-shadow"
            >
              <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center shadow-md">
                <Icon className="text-white text-2xl" />
              </div>
              <h3 className="mt-4 text-lg font-bold text-gray-800">{t(title)}</h3>
              <p className="mt-2 text-sm text-gray-500 leading-relaxed">{t(desc)}</p>
            </div>
          ))}
        </div>

        {/* CTA */}
        <div className="mt-14 relative overflow-hidden rounded-2xl bg-gradient-to-r from-indigo-600 to-purple-600 p-8 text-center text-white">
          <h3 className="text-xl md:text-2xl font-bold">
            {t('Sẵn sàng trải nghiệm hệ thống?')}
          </h3>
          <p className="mt-2 text-indigo-100 text-sm">
            {t('Đăng nhập để dùng đầy đủ, hoặc dùng thử ngay với dữ liệu mẫu.')}
          </p>
          <div className="mt-6 flex flex-wrap justify-center gap-3">
            <button
              onClick={tryDemo}
              className="inline-flex items-center gap-2 bg-white text-indigo-700 font-semibold px-6 py-3 rounded-xl shadow-lg hover:shadow-xl transition-all"
            >
              <MdPlayArrow className="text-xl" />
              {t('Dùng thử ngay')}
            </button>
            <Link
              to="/login"
              className="inline-flex items-center gap-2 bg-white/15 backdrop-blur border border-white/30 text-white font-semibold px-6 py-3 rounded-xl hover:bg-white/25 transition-all"
            >
              <MdLogin className="text-xl" />
              {t('Đăng nhập')}
            </Link>
          </div>
        </div>
      </div>

      {/* Footer */}
      <div className="border-t border-gray-100 py-6">
        <p className="text-center text-xs text-gray-400">
          Hệ thống Quản lý Công việc cho Doanh nghiệp Nhỏ Tích hợp AI · Đồ án cơ sở 2026
        </p>
      </div>
    </div>
  )
}
