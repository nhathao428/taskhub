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
    <div className="relative min-h-screen flex items-center justify-center overflow-hidden bg-gradient-to-br from-indigo-600 via-purple-600 to-pink-500 py-8">
      <div className="absolute -top-32 -right-32 w-96 h-96 bg-white/10 rounded-full blur-3xl" />
      <div className="absolute -bottom-32 -left-32 w-96 h-96 bg-white/10 rounded-full blur-3xl" />

      <div className="absolute top-4 right-4 z-10">
        <LanguageSwitcher variant="dark" />
      </div>

      <div className="relative bg-white rounded-3xl shadow-2xl w-full max-w-4xl mx-4 grid md:grid-cols-2 overflow-hidden">
        {/* Tranh minh họa */}
        <div className="hidden md:flex flex-col justify-center items-center bg-gradient-to-br from-indigo-50 via-purple-50 to-pink-50 p-10">
          <WorkspaceIllustration className="w-full max-w-sm" />
          <h2 className="mt-4 text-lg font-bold text-gray-800 text-center">
            {t('Quản lý công việc, dự án và nhân sự — tích hợp AI.')}
          </h2>
        </div>

        {/* Biểu mẫu */}
        <div className="p-8 md:p-10 flex flex-col justify-center">
          <div className="text-center mb-8">
            <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-gradient-to-br from-indigo-500 via-purple-500 to-pink-500 shadow-lg mb-4">
              <MdWorkspaces className="text-white text-3xl" />
            </div>
            <h1 className="text-2xl font-bold text-gray-800">{t('Chào mừng trở lại')}</h1>
            <p className="text-gray-500 text-sm mt-1">{t('Đăng nhập vào Task Manager')}</p>
          </div>

          {error && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 rounded-xl text-sm">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="flex items-center gap-2 text-sm font-medium text-gray-700 mb-1.5">
                <MdEmail className="text-indigo-500" />
                Email
              </label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="w-full px-4 py-3 border border-gray-200 rounded-xl bg-gray-50 focus:bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent text-sm transition-all"
              />
            </div>

            <div>
              <label className="flex items-center gap-2 text-sm font-medium text-gray-700 mb-1.5">
                <MdLock className="text-indigo-500" />
                {t('Mật khẩu')}
              </label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="w-full px-4 py-3 border border-gray-200 rounded-xl bg-gray-50 focus:bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent text-sm transition-all"
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full flex items-center justify-center gap-2 bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700 disabled:from-gray-300 disabled:to-gray-400 text-white font-semibold py-3 px-4 rounded-xl shadow-md hover:shadow-lg transition-all text-sm"
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

          <p className="text-center text-sm text-gray-500 mt-6">
            {t('Chưa có tài khoản?')}{' '}
            <Link to="/register" className="text-indigo-600 hover:text-purple-600 hover:underline font-semibold transition-colors">
              {t('Đăng ký ngay')}
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}
