import { useEffect, useState } from 'react'
import { MdAdd, MdEdit, MdDelete, MdSearch } from 'react-icons/md'
import api from '../api/axios'
import Modal from '../components/Modal'

const emptyForm = { firstName: '', lastName: '', position: '', department: '' }

export default function Employees() {
  const [employees, setEmployees] = useState([])
  const [filtered, setFiltered] = useState([])
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)
  const [modalOpen, setModalOpen] = useState(false)
  const [editTarget, setEditTarget] = useState(null)
  const [form, setForm] = useState(emptyForm)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const fetchEmployees = async () => {
    try {
      const res = await api.get('/api/employees')
      setEmployees(res.data || [])
      setFiltered(res.data || [])
    } catch {
      setError('Không thể tải danh sách nhân viên.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchEmployees() }, [])

  useEffect(() => {
    const q = search.toLowerCase()
    setFiltered(
      employees.filter(
        (e) =>
          e.firstName?.toLowerCase().includes(q) ||
          e.lastName?.toLowerCase().includes(q) ||
          e.department?.toLowerCase().includes(q)
      )
    )
  }, [search, employees])

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
    })
    setError('')
    setModalOpen(true)
  }

  const handleDelete = async (id) => {
    if (!window.confirm('Bạn có chắc muốn xóa nhân viên này?')) return
    try {
      await api.delete(`/api/employees/${id}`)
      fetchEmployees()
    } catch {
      alert('Xóa thất bại.')
    }
  }

  const handleSave = async (e) => {
    e.preventDefault()
    setSaving(true)
    setError('')
    try {
      if (editTarget) {
        await api.put(`/api/employees/${editTarget.id}`, form)
      } else {
        await api.post('/api/employees', form)
      }
      setModalOpen(false)
      fetchEmployees()
    } catch (err) {
      setError(err.response?.data?.message || 'Lưu thất bại.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Nhân viên</h1>
        <button
          onClick={openAdd}
          className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-lg text-sm font-medium transition-colors"
        >
          <MdAdd className="text-xl" /> Thêm nhân viên
        </button>
      </div>

      <div className="bg-white rounded-xl shadow-md p-4 mb-4">
        <div className="relative">
          <MdSearch className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 text-xl" />
          <input
            type="text"
            placeholder="Tìm kiếm theo tên, phòng ban..."
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
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Họ</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Tên</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Chức vụ</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Phòng ban</th>
                <th className="px-6 py-3 text-center text-xs font-semibold text-gray-500 uppercase">Hành động</th>
              </tr>
            </thead>
            <tbody>
              {filtered.length === 0 ? (
                <tr>
                  <td colSpan={5} className="text-center py-8 text-gray-400">
                    Không có nhân viên nào.
                  </td>
                </tr>
              ) : (
                filtered.map((emp, idx) => (
                  <tr
                    key={emp.id}
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
                        <MdEdit /> Sửa
                      </button>
                      <button
                        onClick={() => handleDelete(emp.id)}
                        className="inline-flex items-center gap-1 text-red-500 hover:text-red-700 text-sm"
                      >
                        <MdDelete /> Xóa
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
        title={editTarget ? 'Chỉnh sửa nhân viên' : 'Thêm nhân viên mới'}
      >
        <form onSubmit={handleSave} className="space-y-4">
          {error && (
            <div className="p-3 bg-red-50 border border-red-200 text-red-600 rounded-lg text-sm">{error}</div>
          )}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Họ</label>
              <input
                type="text"
                value={form.firstName}
                onChange={(e) => setForm({ ...form, firstName: e.target.value })}
                required
                placeholder="Họ"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Tên</label>
              <input
                type="text"
                value={form.lastName}
                onChange={(e) => setForm({ ...form, lastName: e.target.value })}
                required
                placeholder="Tên"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Chức vụ</label>
            <input
              type="text"
              value={form.position}
              onChange={(e) => setForm({ ...form, position: e.target.value })}
              placeholder="VD: Kỹ sư phần mềm"
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Phòng ban</label>
            <input
              type="text"
              value={form.department}
              onChange={(e) => setForm({ ...form, department: e.target.value })}
              placeholder="VD: IT, Kế toán, Nhân sự"
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>
          <div className="flex gap-3 pt-2">
            <button
              type="button"
              onClick={() => setModalOpen(false)}
              className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg text-sm hover:bg-gray-50"
            >
              Hủy
            </button>
            <button
              type="submit"
              disabled={saving}
              className="flex-1 bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-400 text-white px-4 py-2 rounded-lg text-sm font-medium"
            >
              {saving ? 'Đang lưu...' : 'Lưu'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
