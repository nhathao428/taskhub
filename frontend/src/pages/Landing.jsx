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
      <div className="relative overflow-hidden text-white bg-slate-900">
        {/* Aurora mesh */}
        <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top_left,_rgba(99,102,241,0.85),transparent_55%),radial-gradient(ellipse_at_top_right,_rgba(217,70,239,0.55),transparent_55%),radial-gradient(ellipse_at_bottom_right,_rgba(56,189,248,0.45),transparent_55%)]" />
        <div className="absolute -top-32 -right-24 w-[28rem] h-[28rem] bg-fuchsia-500/30 rounded-full blur-3xl" />
        <div className="absolute -bottom-32 -left-24 w-[32rem] h-[32rem] bg-brand-500/40 rounded-full blur-3xl" />
        <div className="absolute inset-0 opacity-[0.04] bg-[linear-gradient(rgba(255,255,255,0.6)_1px,transparent_1px),linear-gradient(90deg,rgba(255,255,255,0.6)_1px,transparent_1px)] bg-[size:40px_40px]" />

        <div className="relative max-w-6xl mx-auto px-6">
          {/* Nav */}
          <div className="flex items-center justify-between py-5">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-brand-600 flex items-center justify-center shadow-brand-glow ring-1 ring-white/10">
                <MdWorkspaces className="text-2xl" />
              </div>
              <span className="text-lg font-bold tracking-tight">Task Manager</span>
            </div>
            <LanguageSwitcher variant="dark" />
          </div>

          {/* Hero content */}
          <div className="py-16 md:py-24 max-w-3xl">
            <h1 className="text-3xl md:text-5xl font-bold leading-tight tracking-tight">
              {t('Quản lý công việc thông minh cho doanh nghiệp nhỏ')}
            </h1>
            <p className="mt-5 text-base md:text-lg text-brand-100 leading-relaxed">
              {t('Giao việc, theo dõi tiến độ, chấm công GPS và để AI gợi ý nhân viên phù hợp — tất cả trong một hệ thống.')}
            </p>
            <div className="mt-8 flex flex-wrap gap-3">
              <button
                onClick={tryDemo}
                className="inline-flex items-center gap-2 bg-white text-brand-700 font-semibold px-6 py-3 rounded-xl shadow-soft-lg hover:shadow-brand-glow transition-shadow"
              >
                <MdPlayArrow className="text-xl" />
                {t('Dùng thử ngay')}
                <MdArrowForward />
              </button>
              <Link
                to="/login"
                className="inline-flex items-center gap-2 bg-white/10 backdrop-blur ring-1 ring-white/20 text-white font-semibold px-6 py-3 rounded-xl hover:bg-white/15 transition-all"
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
            <p className="mt-4 text-sm text-brand-200">
              {t('Chế độ dùng thử cho xem trước giao diện với dữ liệu mẫu — không cần tài khoản.')}
            </p>
          </div>
        </div>
      </div>

      {/* Features */}
      <div className="max-w-6xl mx-auto px-6 py-16">
        <h2 className="text-2xl md:text-3xl font-bold text-slate-900 text-center tracking-tight">
          {t('Tính năng nổi bật')}
        </h2>
        <div className="mt-10 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {FEATURES.map(({ icon: Icon, title, desc }) => (
            <div
              key={title}
              className="bg-white rounded-2xl ring-1 ring-slate-200/70 shadow-soft p-6 hover:shadow-soft-md transition-shadow"
            >
              <div className="w-12 h-12 rounded-xl bg-brand-600 flex items-center justify-center shadow-brand-glow ring-1 ring-white/10">
                <Icon className="text-white text-2xl" />
              </div>
              <h3 className="mt-4 text-lg font-bold text-slate-900 tracking-tight">{t(title)}</h3>
              <p className="mt-2 text-sm text-slate-500 leading-relaxed">{t(desc)}</p>
            </div>
          ))}
        </div>

        {/* CTA */}
        <div className="mt-14 relative overflow-hidden rounded-2xl p-8 text-center text-white shadow-brand-glow bg-slate-900">
          <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top_left,_rgba(99,102,241,0.85),transparent_55%),radial-gradient(ellipse_at_top_right,_rgba(217,70,239,0.55),transparent_55%),radial-gradient(ellipse_at_bottom_right,_rgba(56,189,248,0.45),transparent_55%)]" />
          <div className="absolute -top-24 -right-24 w-72 h-72 bg-fuchsia-500/30 rounded-full blur-3xl" />
          <div className="absolute -bottom-20 -left-20 w-72 h-72 bg-brand-500/30 rounded-full blur-3xl" />
          <div className="absolute inset-0 opacity-[0.04] bg-[linear-gradient(rgba(255,255,255,0.6)_1px,transparent_1px),linear-gradient(90deg,rgba(255,255,255,0.6)_1px,transparent_1px)] bg-[size:32px_32px]" />
          <div className="relative">
            <h3 className="text-xl md:text-2xl font-bold tracking-tight">
              {t('Sẵn sàng trải nghiệm hệ thống?')}
            </h3>
            <p className="mt-2 text-brand-100 text-sm">
              {t('Đăng nhập để dùng đầy đủ, hoặc dùng thử ngay với dữ liệu mẫu.')}
            </p>
            <div className="mt-6 flex flex-wrap justify-center gap-3">
              <button
                onClick={tryDemo}
                className="inline-flex items-center gap-2 bg-white text-brand-700 font-semibold px-6 py-3 rounded-xl shadow-soft-lg hover:shadow-brand-glow transition-shadow"
              >
                <MdPlayArrow className="text-xl" />
                {t('Dùng thử ngay')}
              </button>
              <Link
                to="/login"
                className="inline-flex items-center gap-2 bg-white/10 backdrop-blur ring-1 ring-white/20 text-white font-semibold px-6 py-3 rounded-xl hover:bg-white/15 transition-all"
              >
                <MdLogin className="text-xl" />
                {t('Đăng nhập')}
              </Link>
            </div>
          </div>
        </div>
      </div>

      {/* Footer */}
      <div className="border-t border-slate-200/70 py-6">
        <p className="text-center text-xs text-slate-400">
          Hệ thống Quản lý Công việc cho Doanh nghiệp Nhỏ Tích hợp AI · Đồ án cơ sở 2026
        </p>
      </div>
    </div>
  )
}
