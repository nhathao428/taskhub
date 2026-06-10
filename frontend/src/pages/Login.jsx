import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { MdEmail, MdLock, MdWorkspaces, MdLogin } from 'react-icons/md'
import { useAuth } from '../context/AuthContext'
import { useTranslation } from '../context/LanguageContext'
import LanguageSwitcher from '../components/LanguageSwitcher'
import { WorkspaceIllustration } from '../components/Illustrations'

export default function Login() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { login } = useAuth()
  const { t } = useTranslation()
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login(email, password)
      navigate('/dashboard')
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.response?.data?.error ||
          t('Đăng nhập thất bại. Vui lòng kiểm tra lại email và mật khẩu.')
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="relative min-h-screen flex items-center justify-center overflow-hidden py-8 bg-slate-950">
      {/* Aurora mesh background */}
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top_left,_rgba(31,154,150,0.40),transparent_55%),radial-gradient(ellipse_at_bottom,_rgba(19,124,121,0.32),transparent_55%)]" />
      <div className="absolute -top-20 -right-20 w-[28rem] h-[28rem] bg-brand-400/25 rounded-full blur-3xl animate-pulse" style={{ animationDuration: '6s' }} />
      <div className="absolute -bottom-32 -left-32 w-[32rem] h-[32rem] bg-brand-500/35 rounded-full blur-3xl animate-pulse" style={{ animationDuration: '8s' }} />
      {/* Subtle grid texture */}
      <div className="absolute inset-0 opacity-[0.04] bg-[linear-gradient(rgba(255,255,255,0.6)_1px,transparent_1px),linear-gradient(90deg,rgba(255,255,255,0.6)_1px,transparent_1px)] bg-[size:40px_40px]" />

      <div className="absolute top-4 right-4 z-10">
        <LanguageSwitcher variant="dark" />
      </div>

      <div className="relative bg-white/95 backdrop-blur-xl rounded-3xl shadow-2xl ring-1 ring-white/30 w-full max-w-4xl mx-4 grid md:grid-cols-2 overflow-hidden">
        {/* Tranh minh họa */}
        <div className="hidden md:flex flex-col justify-center items-center bg-gradient-to-br from-brand-50 via-brand-100/60 to-white p-10 border-r border-white/40 relative overflow-hidden">
          <div className="absolute -top-12 -right-12 w-40 h-40 bg-brand-200/40 rounded-full blur-2xl" />
          <div className="absolute -bottom-8 -left-8 w-40 h-40 bg-brand-300/30 rounded-full blur-2xl" />
          <WorkspaceIllustration className="w-full max-w-sm relative z-10" />
          <h2 className="mt-6 text-lg font-semibold text-slate-800 text-center leading-snug relative z-10">
            {t('Quản lý công việc, dự án và nhân sự, tích hợp AI.')}
          </h2>
        </div>

        {/* Biểu mẫu */}
        <div className="p-8 md:p-10 flex flex-col justify-center">
          <div className="text-center mb-8">
            <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-brand-600 shadow-brand-glow mb-4">
              <MdWorkspaces className="text-white text-2xl" />
            </div>
            <h1 className="text-2xl font-bold text-slate-900 tracking-tight">{t('Chào mừng trở lại')}</h1>
            <p className="text-slate-500 text-sm mt-1.5">{t('Đăng nhập vào TaskHub')}</p>
          </div>

          {error && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 rounded-xl text-sm">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="flex items-center gap-2 text-sm font-medium text-slate-700 mb-1.5">
                <MdEmail className="text-brand-600" />
                Email
              </label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="w-full px-4 py-3 border border-slate-200 rounded-xl bg-slate-50 focus:bg-white focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent text-sm transition-all"
              />
            </div>

            <div>
              <label className="flex items-center gap-2 text-sm font-medium text-slate-700 mb-1.5">
                <MdLock className="text-brand-600" />
                {t('Mật khẩu')}
              </label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="w-full px-4 py-3 border border-slate-200 rounded-xl bg-slate-50 focus:bg-white focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent text-sm transition-all"
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full flex items-center justify-center gap-2 bg-brand-600 hover:bg-brand-700 disabled:bg-slate-300 text-white font-semibold py-3 px-4 rounded-xl shadow-brand-glow disabled:shadow-none transition-colors text-sm"
            >
              {loading ? (
                <>
                  <div className="animate-spin h-4 w-4 border-2 border-white/30 border-t-white rounded-full" />
                  {t('Đang đăng nhập...')}
                </>
              ) : (
                <>
                  <MdLogin className="text-lg" />
                  {t('Đăng nhập')}
                </>
              )}
            </button>
          </form>

          <p className="text-center text-sm text-slate-500 mt-6">
            {t('Chưa có tài khoản?')}{' '}
            <Link to="/register" className="text-brand-600 hover:text-brand-700 hover:underline font-semibold transition-colors">
              {t('Đăng ký ngay')}
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}
