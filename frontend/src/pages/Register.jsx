import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { MdEmail, MdLock, MdPerson, MdWorkspaces, MdPersonAdd } from 'react-icons/md'
import { useAuth } from '../context/AuthContext'
import { useTranslation } from '../context/LanguageContext'
import LanguageSwitcher from '../components/LanguageSwitcher'
import { WorkspaceIllustration } from '../components/Illustrations'

export default function Register() {
  const [form, setForm] = useState({ username: '', email: '', password: '', confirm: '' })
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [loading, setLoading] = useState(false)
  const { register } = useAuth()
  const { t } = useTranslation()
  const navigate = useNavigate()

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    if (form.password !== form.confirm) {
      setError(t('Mật khẩu xác nhận không khớp.'))
      return
    }
    setLoading(true)
    try {
      await register(form.username, form.email, form.password)
      setSuccess(t('Đăng ký thành công! Đang chuyển đến trang đăng nhập...'))
      setTimeout(() => navigate('/login'), 1500)
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.response?.data?.error ||
          t('Đăng ký thất bại. Vui lòng thử lại.')
      )
    } finally {
      setLoading(false)
    }
  }

  const fields = [
    { name: 'username', label: 'Tên người dùng', icon: MdPerson, type: 'text' },
    { name: 'email', label: 'Email', icon: MdEmail, type: 'email' },
    { name: 'password', label: 'Mật khẩu', icon: MdLock, type: 'password' },
    { name: 'confirm', label: 'Xác nhận mật khẩu', icon: MdLock, type: 'password' },
  ]

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
            <h1 className="text-2xl font-bold text-gray-800">{t('Tạo tài khoản')}</h1>
            <p className="text-gray-500 text-sm mt-1">{t('Tham gia Task Manager hôm nay')}</p>
          </div>

          {error && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 rounded-xl text-sm">
              {error}
            </div>
          )}
          {success && (
            <div className="mb-4 p-3 bg-green-50 border border-green-200 text-green-700 rounded-xl text-sm">
              {success}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            {fields.map(({ name, label, icon: Icon, type }) => (
              <div key={name}>
                <label className="flex items-center gap-2 text-sm font-medium text-gray-700 mb-1.5">
                  <Icon className="text-indigo-500" />
                  {label === 'Email' ? label : t(label)}
                </label>
                <input
                  type={type}
                  name={name}
                  value={form[name]}
                  onChange={handleChange}
                  required
                  className="w-full px-4 py-3 border border-gray-200 rounded-xl bg-gray-50 focus:bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent text-sm transition-all"
                />
              </div>
            ))}

            <button
              type="submit"
              disabled={loading}
              className="w-full flex items-center justify-center gap-2 bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700 disabled:from-gray-300 disabled:to-gray-400 text-white font-semibold py-3 px-4 rounded-xl shadow-md hover:shadow-lg transition-all text-sm"
            >
              {loading ? (
                <>
                  <div className="animate-spin h-4 w-4 border-2 border-white/30 border-t-white rounded-full" />
                  {t('Đang đăng ký...')}
                </>
              ) : (
                <>
                  <MdPersonAdd className="text-lg" />
                  {t('Đăng ký')}
                </>
              )}
            </button>
          </form>

          <p className="text-center text-sm text-gray-500 mt-6">
            {t('Đã có tài khoản?')}{' '}
            <Link to="/login" className="text-indigo-600 hover:text-purple-600 hover:underline font-semibold transition-colors">
              {t('Đăng nhập')}
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}
