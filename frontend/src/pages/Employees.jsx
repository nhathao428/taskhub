import { useState } from 'react'
import { MdAdd, MdEdit, MdDelete, MdSearch } from 'react-icons/md'
import Modal from '../components/Modal'
import { useEmployees } from '../hooks/useEmployees'
import { useTranslation } from '../context/LanguageContext'
import { EmptyState } from '../components/Illustrations'

const emptyForm = { firstName: '', lastName: '', position: '', department: '', skills: '' }

export default function Employees() {
  const { employees, loading, error: fetchError, createEmployee, updateEmployee, deleteEmployee } = useEmployees()
  const { t } = useTranslation()
  const [search, setSearch] = useState('')
  const [modalOpen, setModalOpen] = useState(false)
  const [editTarget, setEditTarget] = useState(null)
  const [form, setForm] = useState(emptyForm)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const filtered = employees.filter((e) => {
    const q = search.toLowerCase()
    return (
      e.firstName?.toLowerCase().includes(q) ||
      e.lastName?.toLowerCase().includes(q) ||
      e.department?.toLowerCase().includes(q)
    )
  })

  const openAdd = () => {
    setEditTarget(null)
    setForm(emptyForm)
    setError('')
    setModalOpen(true)
  }

  const openEdit = (emp) => {
    setEditTarget(emp)
    setForm({
      firstName: emp.firstName || '',
      lastName: emp.lastName || '',
      position: emp.position || '',
      department: emp.department || '',
      skills: emp.skills || '',
    })
    setError('')
    setModalOpen(true)
  }

  const handleDelete = async (id) => {
    if (!window.confirm(t('Bạn có chắc muốn xóa nhân viên này?'))) return
    try {
      await deleteEmployee(id)
    } catch (err) {
      const msg = err.response?.data?.message || t('Xóa thất bại.')
      alert(msg)
    }
  }

  const handleSave = async (e) => {
    e.preventDefault()
    setSaving(true)
    setError('')
    try {
      if (editTarget) {
        await updateEmployee(editTarget.employeeId ?? editTarget.id, form)
      } else {
        await createEmployee(form)
      }
      setModalOpen(false)
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.error || t('Lưu thất bại.'))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-800">{t('Nhân viên')}</h1>
        <button
          onClick={openAdd}
          className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-lg text-sm font-medium transition-colors"
        >
          <MdAdd className="text-xl" /> {t('Thêm nhân viên')}
        </button>
      </div>

      {fetchError && (
        <div className="p-3 mb-4 bg-red-50 border border-red-200 text-red-600 rounded-lg text-sm">{fetchError}</div>
      )}

      <div className="bg-white rounded-xl shadow-md p-4 mb-4">
        <div className="relative">
          <MdSearch className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 text-xl" />
          <input
            type="text"
            placeholder={t('Tìm kiếm theo tên, phòng ban...')}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
          />
        </div>
      </div>

      <div className="bg-white rounded-xl shadow-md overflow-hidden">
        {loading ? (
          <div className="flex justify-center py-12">
            <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-600" />
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">{t('Họ')}</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">{t('Tên')}</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">{t('Chức vụ')}</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">{t('Phòng ban')}</th>
                <th className="px-6 py-3 text-center text-xs font-semibold text-gray-500 uppercase">{t('Hành động')}</th>
              </tr>
            </thead>
            <tbody>
              {filtered.length === 0 ? (
                <tr>
                  <td colSpan={5} className="py-4">
                    <EmptyState text={t('Không có nhân viên nào.')} />
                  </td>
                </tr>
              ) : (
                filtered.map((emp, idx) => (
                  <tr
                    key={emp.employeeId ?? emp.id}
                    className={idx % 2 === 0 ? 'bg-white hover:bg-gray-50' : 'bg-gray-50 hover:bg-gray-100'}
                  >
                    <td className="px-6 py-4 text-gray-800">{emp.firstName}</td>
                    <td className="px-6 py-4 text-gray-800">{emp.lastName}</td>
                    <td className="px-6 py-4 text-gray-600">{emp.position}</td>
                    <td className="px-6 py-4 text-gray-600">{emp.department}</td>
                    <td className="px-6 py-4 text-center">
                      <button
                        onClick={() => openEdit(emp)}
                        className="inline-flex items-center gap-1 text-indigo-600 hover:text-indigo-800 mr-3 text-sm"
                      >
                        <MdEdit /> {t('Sửa')}
                      </button>
                      <button
                        onClick={() => handleDelete(emp.employeeId ?? emp.id)}
                        className="inline-flex items-center gap-1 text-red-500 hover:text-red-700 text-sm"
                      >
                        <MdDelete /> {t('Xóa')}
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        )}
      </div>

      <Modal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editTarget ? t('Chỉnh sửa nhân viên') : t('Thêm nhân viên mới')}
      >
        <form onSubmit={handleSave} className="space-y-4">
          {error && (
            <div className="p-3 bg-red-50 border border-red-200 text-red-600 rounded-lg text-sm">{error}</div>
          )}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">{t('Họ')}</label>
              <input
                type="text"
                value={form.firstName}
                onChange={(e) => setForm({ ...form, firstName: e.target.value })}
                required
                placeholder={t('Họ')}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">{t('Tên')}</label>
              <input
                type="text"
                value={form.lastName}
                onChange={(e) => setForm({ ...form, lastName: e.target.value })}
                required
                placeholder={t('Tên')}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">{t('Chức vụ')}</label>
            <input
              type="text"
              value={form.position}
              onChange={(e) => setForm({ ...form, position: e.target.value })}
              placeholder={t('VD: Kỹ sư phần mềm')}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">{t('Phòng ban')}</label>
            <input
              type="text"
              value={form.department}
              onChange={(e) => setForm({ ...form, department: e.target.value })}
              placeholder={t('VD: IT, Kế toán, Nhân sự')}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">{t('Kỹ năng')}</label>
            <textarea
              value={form.skills}
              onChange={(e) => setForm({ ...form, skills: e.target.value })}
              rows={2}
              placeholder={t('VD: Java, Spring Boot, PostgreSQL, React')}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 resize-none"
            />
            <p className="text-xs text-gray-500 mt-1">{t('Quản lý nhập tự do, ngăn cách bởi dấu phẩy. AI sẽ dùng để đối chiếu khi gợi ý.')}</p>
          </div>
          <div className="flex gap-3 pt-2">
            <button
              type="button"
              onClick={() => setModalOpen(false)}
              className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg text-sm hover:bg-gray-50"
            >
              {t('Hủy')}
            </button>
            <button
              type="submit"
              disabled={saving}
              className="flex-1 bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-400 text-white px-4 py-2 rounded-lg text-sm font-medium"
            >
              {saving ? t('Đang lưu...') : t('Lưu')}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
