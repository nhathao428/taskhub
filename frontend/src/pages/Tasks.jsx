import { useEffect, useState } from 'react'
import { MdAdd, MdEdit, MdDelete } from 'react-icons/md'
import api from '../api/axios'
import Modal from '../components/Modal'

const emptyForm = {
  title: '',
  description: '',
  dueDate: '',
  status: 'PENDING',
  project: null,
  assignedTo: null,
}

const statusConfig = {
  PENDING: { label: 'Chờ xử lý', cls: 'bg-yellow-100 text-yellow-700' },
  Pending: { label: 'Chờ xử lý', cls: 'bg-yellow-100 text-yellow-700' },
  IN_PROGRESS: { label: 'Đang thực hiện', cls: 'bg-blue-100 text-blue-700' },
  'In Progress': { label: 'Đang thực hiện', cls: 'bg-blue-100 text-blue-700' },
  COMPLETED: { label: 'Hoàn thành', cls: 'bg-green-100 text-green-700' },
  Completed: { label: 'Hoàn thành', cls: 'bg-green-100 text-green-700' },
}

function StatusBadge({ status }) {
  const cfg = statusConfig[status] || { label: status, cls: 'bg-gray-100 text-gray-600' }
  return <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${cfg.cls}`}>{cfg.label}</span>
}

export default function Tasks() {
  const [tasks, setTasks] = useState([])
  const [projects, setProjects] = useState([])
  const [employees, setEmployees] = useState([])
  const [loading, setLoading] = useState(true)
  const [modalOpen, setModalOpen] = useState(false)
  const [editTarget, setEditTarget] = useState(null)
  const [form, setForm] = useState(emptyForm)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const fetchAll = async () => {
    try {
      const [taskRes, projRes, empRes] = await Promise.all([
        api.get('/api/tasks'),
        api.get('/api/projects'),
        api.get('/api/employees'),
      ])
      setTasks(taskRes.data || [])
      setProjects(projRes.data || [])
      setEmployees(empRes.data || [])
    } catch {
      setError('Không thể tải dữ liệu.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchAll() }, [])

  const openAdd = () => {
    setEditTarget(null)
    setForm(emptyForm)
    setError('')
    setModalOpen(true)
  }

  const openEdit = (task) => {
    setEditTarget(task)
    setForm({
      title: task.title || '',
      description: task.description || '',
      dueDate: task.dueDate ? task.dueDate.split('T')[0] : '',
      status: task.status || 'PENDING',
      project: task.project?.id || task.project || null,
      assignedTo: task.assignedTo?.id || task.assignedTo || null,
    })
    setError('')
    setModalOpen(true)
  }

  const handleDelete = async (id) => {
    if (!window.confirm('Bạn có chắc muốn xóa công việc này?')) return
    try {
      await api.delete(`/api/tasks/${id}`)
      fetchAll()
    } catch {
      alert('Xóa thất bại.')
    }
  }

  const handleSave = async (e) => {
    e.preventDefault()
    setSaving(true)
    setError('')
    const payload = {
      title: form.title,
      description: form.description,
      dueDate: form.dueDate,
      status: form.status,
      project: form.project ? { id: Number(form.project) } : null,
      assignedTo: form.assignedTo ? { id: Number(form.assignedTo) } : null,
    }
    try {
      if (editTarget) {
        await api.put(`/api/tasks/${editTarget.id}`, payload)
      } else {
        await api.post('/api/tasks', payload)
      }
      setModalOpen(false)
      fetchAll()
    } catch (err) {
      setError(err.response?.data?.message || 'Lưu thất bại.')
    } finally {
      setSaving(false)
    }
  }

  const getProjectName = (task) => {
    if (task.project?.name) return task.project.name
    const proj = projects.find((p) => p.id === task.project)
    return proj?.name || '-'
  }

  const getAssigneeName = (task) => {
    if (task.assignedTo?.firstName) {
      return `${task.assignedTo.firstName} ${task.assignedTo.lastName}`
    }
    const emp = employees.find((e) => e.id === task.assignedTo)
    return emp ? `${emp.firstName} ${emp.lastName}` : '-'
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Công việc</h1>
        <button
          onClick={openAdd}
          className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-lg text-sm font-medium transition-colors"
        >
          <MdAdd className="text-xl" /> Thêm công việc
        </button>
      </div>

      <div className="bg-white rounded-xl shadow-md overflow-hidden">
        {loading ? (
          <div className="flex justify-center py-12">
            <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-600" />
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Tiêu đề</th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Mô tả</th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Hạn chót</th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Trạng thái</th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Dự án</th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Phân công</th>
                  <th className="px-6 py-3 text-center text-xs font-semibold text-gray-500 uppercase">Hành động</th>
                </tr>
              </thead>
              <tbody>
                {tasks.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="text-center py-8 text-gray-400">Chưa có công việc nào.</td>
                  </tr>
                ) : (
                  tasks.map((task, idx) => (
                    <tr key={task.id} className={idx % 2 === 0 ? 'bg-white hover:bg-gray-50' : 'bg-gray-50 hover:bg-gray-100'}>
                      <td className="px-6 py-4 font-medium text-gray-800">{task.title}</td>
                      <td className="px-6 py-4 text-gray-600 max-w-xs truncate">{task.description}</td>
                      <td className="px-6 py-4 text-gray-600">{task.dueDate ? task.dueDate.split('T')[0] : '-'}</td>
                      <td className="px-6 py-4"><StatusBadge status={task.status} /></td>
                      <td className="px-6 py-4 text-gray-600">{getProjectName(task)}</td>
                      <td className="px-6 py-4 text-gray-600">{getAssigneeName(task)}</td>
                      <td className="px-6 py-4 text-center">
                        <button onClick={() => openEdit(task)} className="inline-flex items-center gap-1 text-indigo-600 hover:text-indigo-800 mr-3 text-sm">
                          <MdEdit /> Sửa
                        </button>
                        <button onClick={() => handleDelete(task.id)} className="inline-flex items-center gap-1 text-red-500 hover:text-red-700 text-sm">
                          <MdDelete /> Xóa
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editTarget ? 'Chỉnh sửa công việc' : 'Thêm công việc mới'}>
        <form onSubmit={handleSave} className="space-y-4">
          {error && <div className="p-3 bg-red-50 border border-red-200 text-red-600 rounded-lg text-sm">{error}</div>}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Tiêu đề</label>
            <input
              type="text"
              value={form.title}
              onChange={(e) => setForm({ ...form, title: e.target.value })}
              required
              placeholder="Nhập tiêu đề công việc"
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Mô tả</label>
            <textarea
              value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
              rows={2}
              placeholder="Mô tả công việc"
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 resize-none"
            />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Hạn chót</label>
              <input
                type="date"
                value={form.dueDate}
                onChange={(e) => setForm({ ...form, dueDate: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Trạng thái</label>
              <select
                value={form.status}
                onChange={(e) => setForm({ ...form, status: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              >
                <option value="PENDING">Chờ xử lý</option>
                <option value="IN_PROGRESS">Đang thực hiện</option>
                <option value="COMPLETED">Hoàn thành</option>
              </select>
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Dự án</label>
            <select
              value={form.project || ''}
              onChange={(e) => setForm({ ...form, project: e.target.value || null })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            >
              <option value="">-- Chọn dự án --</option>
              {projects.map((p) => (
                <option key={p.id} value={p.id}>{p.name}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Phân công cho</label>
            <select
              value={form.assignedTo || ''}
              onChange={(e) => setForm({ ...form, assignedTo: e.target.value || null })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            >
              <option value="">-- Chọn nhân viên --</option>
              {employees.map((emp) => (
                <option key={emp.id} value={emp.id}>{emp.firstName} {emp.lastName}</option>
              ))}
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
