import { useState } from 'react'
import { MdManageAccounts } from 'react-icons/md'
import { useUsers } from '../hooks/useUsers'
import { useTranslation } from '../context/LanguageContext'
import { EmptyState } from '../components/Illustrations'

const roleLabel = { ADMIN: 'Quản trị viên', MANAGER: 'Quản lý', EMPLOYEE: 'Nhân viên' }
const roleBadge = {
  ADMIN: 'bg-fuchsia-100 text-fuchsia-700',
  MANAGER: 'bg-brand-100 text-brand-700',
  EMPLOYEE: 'bg-slate-100 text-slate-600',
}

export default function Users() {
  const { users, loading, error, updateRole } = useUsers()
  const { t } = useTranslation()
  const [savingId, setSavingId] = useState(null)
  const [message, setMessage] = useState('')

  const handleRoleChange = async (user, role) => {
    if (role === user.role) return
    setSavingId(user.userId)
    setMessage('')
    try {
      await updateRole(user.userId, role)
      setMessage(
        t('Đã đổi vai trò của "{name}" thành {role}.', {
          name: user.username,
          role: t(roleLabel[role]),
        })
      )
    } catch (err) {
      setMessage(err.response?.data?.message || t('Đổi vai trò thất bại.'))
    } finally {
      setSavingId(null)
    }
  }

  return (
    <div>
      <div className="flex items-center gap-2 mb-1">
        <MdManageAccounts className="text-2xl text-brand-600" />
        <h1 className="text-2xl font-bold text-slate-900 tracking-tight">{t('Người dùng & Phân quyền')}</h1>
      </div>
      <p className="text-sm text-slate-500 mb-5">
        {t('Tài khoản mới đăng ký mặc định là Nhân viên. Quản trị viên đổi vai trò để cấp quyền quản lý.')}
      </p>

      {error && (
        <div className="p-3 mb-4 bg-red-50 border border-red-200 text-red-600 rounded-lg text-sm">
          {error}
        </div>
      )}
      {message && (
        <div className="p-3 mb-4 bg-brand-50 border border-brand-200 text-brand-700 rounded-lg text-sm">
          {message}
        </div>
      )}

      <div className="bg-white rounded-2xl shadow-soft ring-1 ring-slate-200/70 overflow-hidden">
        {loading ? (
          <div className="flex justify-center py-12">
            <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-brand-600" />
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-slate-50 border-b border-slate-200">
              <tr>
                <th className="px-6 py-3 text-left text-[11px] font-semibold text-slate-500 uppercase tracking-[0.12em]">{t('Tên đăng nhập')}</th>
                <th className="px-6 py-3 text-left text-[11px] font-semibold text-slate-500 uppercase tracking-[0.12em]">Email</th>
                <th className="px-6 py-3 text-left text-[11px] font-semibold text-slate-500 uppercase tracking-[0.12em]">{t('Vai trò hiện tại')}</th>
                <th className="px-6 py-3 text-left text-[11px] font-semibold text-slate-500 uppercase tracking-[0.12em]">{t('Phân quyền')}</th>
              </tr>
            </thead>
            <tbody>
              {users.length === 0 ? (
                <tr>
                  <td colSpan={4} className="py-4"><EmptyState text={t('Chưa có người dùng nào.')} /></td>
                </tr>
              ) : (
                users.map((u, idx) => (
                  <tr
                    key={u.userId}
                    className={idx % 2 === 0 ? 'bg-white hover:bg-slate-50' : 'bg-slate-50/60 hover:bg-slate-100'}
                  >
                    <td className="px-6 py-4 font-medium text-slate-900">{u.username}</td>
                    <td className="px-6 py-4 text-slate-600">{u.email}</td>
                    <td className="px-6 py-4">
                      <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${roleBadge[u.role] || roleBadge.EMPLOYEE}`}>
                        {roleLabel[u.role] ? t(roleLabel[u.role]) : u.role}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      {u.role === 'ADMIN' ? (
                        <span className="text-slate-400 text-sm">{t('Không thể đổi')}</span>
                      ) : (
                        <select
                          value={u.role}
                          disabled={savingId === u.userId}
                          onChange={(e) => handleRoleChange(u, e.target.value)}
                          className="px-3 py-1.5 border border-slate-200 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-brand-500 disabled:opacity-60"
                        >
                          <option value="EMPLOYEE">{t('Nhân viên')}</option>
                          <option value="MANAGER">{t('Quản lý')}</option>
                        </select>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        )}
      </div>
    </div>
  )
}
