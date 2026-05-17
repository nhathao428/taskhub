import { useState } from 'react'
import { MdManageAccounts } from 'react-icons/md'
import { useUsers } from '../hooks/useUsers'
import { useTranslation } from '../context/LanguageContext'
import { EmptyState } from '../components/Illustrations'

const roleLabel = { ADMIN: 'Quản trị viên', MANAGER: 'Quản lý', EMPLOYEE: 'Nhân viên' }
const roleBadge = {
  ADMIN: 'bg-purple-100 text-purple-700',
  MANAGER: 'bg-indigo-100 text-indigo-700',
  EMPLOYEE: 'bg-gray-100 text-gray-600',
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
        <MdManageAccounts className="text-2xl text-indigo-600" />
        <h1 className="text-2xl font-bold text-gray-800">{t('Người dùng & Phân quyền')}</h1>
      </div>
      <p className="text-sm text-gray-500 mb-5">
        {t('Tài khoản mới đăng ký mặc định là Nhân viên. Quản trị viên đổi vai trò để cấp quyền quản lý.')}
      </p>

      {error && (
        <div className="p-3 mb-4 bg-red-50 border border-red-200 text-red-600 rounded-lg text-sm">
          {error}
        </div>
      )}
      {message && (
        <div className="p-3 mb-4 bg-indigo-50 border border-indigo-200 text-indigo-700 rounded-lg text-sm">
          {message}
        </div>
      )}

      <div className="bg-white rounded-xl shadow-md overflow-hidden">
        {loading ? (
          <div className="flex justify-center py-12">
            <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-600" />
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">{t('Tên đăng nhập')}</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Email</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">{t('Vai trò hiện tại')}</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">{t('Phân quyền')}</th>
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
                    className={idx % 2 === 0 ? 'bg-white hover:bg-gray-50' : 'bg-gray-50 hover:bg-gray-100'}
                  >
                    <td className="px-6 py-4 font-medium text-gray-800">{u.username}</td>
                    <td className="px-6 py-4 text-gray-600">{u.email}</td>
                    <td className="px-6 py-4">
                      <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${roleBadge[u.role] || roleBadge.EMPLOYEE}`}>
                        {roleLabel[u.role] ? t(roleLabel[u.role]) : u.role}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      {u.role === 'ADMIN' ? (
                        <span className="text-gray-400 text-sm">{t('Không thể đổi')}</span>
                      ) : (
                        <select
                          value={u.role}
                          disabled={savingId === u.userId}
                          onChange={(e) => handleRoleChange(u, e.target.value)}
                          className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500 disabled:opacity-60"
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
