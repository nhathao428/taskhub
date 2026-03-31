import { useState } from 'react'
import { MdAdd, MdEdit, MdDelete } from 'react-icons/md'
import Modal from '../components/Modal'
import { useProjects } from '../hooks/useProjects'

const emptyForm = { name: '', description: '', startDate: '', endDate: '', status: 'ACTIVE' }

const statusConfig = {
  ACTIVE: { label: 'Hoạt động', cls: 'bg-green-100 text-green-700' },
  ON_TRACK: { label: 'Đúng tiến độ', cls: 'bg-green-100 text-green-700' },
  IN_PROGRESS: { label: 'Đang thực hiện', cls: 'bg-yellow-100 text-yellow-700' },
  COMPLETED: { label: 'Hoàn thành', cls: 'bg-gray-100 text-gray-600' },
  OVERDUE: { label: 'Quá hạn', cls: 'bg-red-100 text-red-600' },
}

function StatusBadge({ status }) {
  const cfg = statusConfig[status] || { label: status, cls: 'bg-gray-100 text-gray-600' }
  return (
    <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${cfg.cls}`}>
      {cfg.label}
    </span>
  )
}

export default function Projects() {
  const { projects, loading, error: fetchError, createProject, updateProject, deleteProject } = useProjects()
  const [modalOpen, setModalOpen] = useState(false)
  const [editTarget, setEditTarget] = useState(null)
  const [form, setForm] = useState(emptyForm)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const openAdd = () => {
    setEditTarget(null)
    setForm(emptyForm)
    setError('')
    setModalOpen(true)
  }

  const openEdit = (proj) => {
    setEditTarget(proj)
    setForm({
      name: proj.name || '',
      description: proj.description || '',
      startDate: proj.startDate ? proj.startDate.split('T')[0] : '',
      endDate: proj.endDate ? proj.endDate.split('T')[0] : '',
      status: proj.status || 'ACTIVE',
    })
    setError('')
    setModalOpen(true)
  }

  const handleDelete = async (id) => {
    if (!window.confirm('Bạn có chắc muốn xóa dự án này?')) return
    try {
      await deleteProject(id)
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
        await updateProject(editTarget.projectId ?? editTarget.id, form)
      } else {
        await createProject(form)
      }
      setModalOpen(false)
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.error || 'Lưu thất bại.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Dự án</h1>
        <button
          onClick={openAdd}
          className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-lg text-sm font-medium transition-colors"
        >
          <MdAdd className="text-xl" /> Thêm dự án
        </button>
      </div>

      {fetchError && (
        <div className="p-3 mb-4 bg-red-50 border border-red-200 text-red-600 rounded-lg text-sm">{fetchError}</div>
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
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Tên dự án</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Mô tả</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Ngày bắt đầu</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Ngày kết thúc</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Trạng thái</th>
                <th className="px-6 py-3 text-center text-xs font-semibold text-gray-500 uppercase">Hành động</th>
              </tr>
            </thead>
            <tbody>
              {projects.length === 0 ? (
                <tr>
                  <td colSpan={6} className="text-center py-8 text-gray-400">Chưa có dự án nào.</td>
                </tr>
              ) : (
                projects.map((proj, idx) => (
                  <tr key={proj.projectId ?? proj.id} className={idx % 2 === 0 ? 'bg-white hover:bg-gray-50' : 'bg-gray-50 hover:bg-gray-100'}>
                    <td className="px-6 py-4 font-medium text-gray-800">{proj.name}</td>
                    <td className="px-6 py-4 text-gray-600 max-w-xs truncate">{proj.description}</td>
                    <td className="px-6 py-4 text-gray-600">{proj.startDate ? proj.startDate.split('T')[0] : '-'}</td>
                    <td className="px-6 py-4 text-gray-600">{proj.endDate ? proj.endDate.split('T')[0] : '-'}</td>
                    <td className="px-6 py-4"><StatusBadge status={proj.status} /></td>
                    <td className="px-6 py-4 text-center">
                      <button
                        onClick={() => openEdit(proj)}
                        className="inline-flex items-center gap-1 text-indigo-600 hover:text-indigo-800 mr-3 text-sm"
                      >
                        <MdEdit /> Sửa
                      </button>
                      <button
                        onClick={() => handleDelete(proj.projectId ?? proj.id)}
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

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editTarget ? 'Chỉnh sửa dự án' : 'Thêm dự án mới'}>
        <form onSubmit={handleSave} className="space-y-4">
          {error && <div className="p-3 bg-red-50 border border-red-200 text-red-600 rounded-lg text-sm">{error}</div>}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Tên dự án</label>
            <input
              type="text"
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              required
              placeholder="Nhập tên dự án"
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Mô tả</label>
            <textarea
              value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
              rows={3}
              placeholder="Mô tả ngắn về dự án"
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 resize-none"
            />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Ngày bắt đầu</label>
              <input
                type="date"
                value={form.startDate}
                onChange={(e) => setForm({ ...form, startDate: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Ngày kết thúc</label>
              <input
                type="date"
                value={form.endDate}
                onChange={(e) => setForm({ ...form, endDate: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Trạng thái</label>
            <select
              value={form.status}
              onChange={(e) => setForm({ ...form, status: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            >
              <option value="ACTIVE">Hoạt động</option>
              <option value="ON_TRACK">Đúng tiến độ</option>
              <option value="IN_PROGRESS">Đang thực hiện</option>
              <option value="COMPLETED">Hoàn thành</option>
              <option value="OVERDUE">Quá hạn</option>
            </select>
          </div>
          <div className="flex gap-3 pt-2">
            <button type="button" onClick={() => setModalOpen(false)} className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg text-sm hover:bg-gray-50">Hủy</button>
            <button type="submit" disabled={saving} className="flex-1 bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-400 text-white px-4 py-2 rounded-lg text-sm font-medium">
              {saving ? 'Đang lưu...' : 'Lưu'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
