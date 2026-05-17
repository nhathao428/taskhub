import { useState } from 'react'
import { MdAdd, MdEdit, MdDelete } from 'react-icons/md'
import Modal from '../components/Modal'
import { useTasks } from '../hooks/useTasks'
import { useProjects } from '../hooks/useProjects'
import { useEmployees } from '../hooks/useEmployees'
import { useTranslation } from '../context/LanguageContext'

const emptyForm = {
  title: '',
  description: '',
  requiredSkills: '',
  dueDate: '',
  status: 'PENDING',
  project: null,
  assignedTo: null,
}

const statusConfig = {
  PENDING: { label: 'Chờ xử lý', cls: 'bg-yellow-100 text-yellow-700' },
  Pending: { label: 'Chờ xử lý', cls: 'bg-yellow-100 text-yellow-700' },
  pending: { label: 'Chờ xử lý', cls: 'bg-yellow-100 text-yellow-700' },
  IN_PROGRESS: { label: 'Đang thực hiện', cls: 'bg-blue-100 text-blue-700' },
  'In Progress': { label: 'Đang thực hiện', cls: 'bg-blue-100 text-blue-700' },
  in_progress: { label: 'Đang thực hiện', cls: 'bg-blue-100 text-blue-700' },
  COMPLETED: { label: 'Hoàn thành', cls: 'bg-green-100 text-green-700' },
  Completed: { label: 'Hoàn thành', cls: 'bg-green-100 text-green-700' },
  completed: { label: 'Hoàn thành', cls: 'bg-green-100 text-green-700' },
}

function StatusBadge({ status }) {
  const { t } = useTranslation()
  const cfg = statusConfig[status]
  const label = cfg ? t(cfg.label) : status
  const cls = cfg ? cfg.cls : 'bg-gray-100 text-gray-600'
  return <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${cls}`}>{label}</span>
}

export default function Tasks() {
  const { tasks, loading, error: taskError, createTask, updateTask, deleteTask } = useTasks()
  const { projects } = useProjects()
  const { employees } = useEmployees()
  const { t } = useTranslation()
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

  const getTaskProjectId = (task) =>
    task.project?.projectId ?? task.project?.id ?? task.project ?? null

  const getTaskAssigneeId = (task) =>
    task.assignedTo?.employeeId ?? task.assignedTo?.id ?? task.assignedTo ?? null

  const openEdit = (task) => {
    setEditTarget(task)
    setForm({
      title: task.title || '',
      description: task.description || '',
      requiredSkills: task.requiredSkills || '',
      dueDate: task.dueDate ? task.dueDate.split('T')[0] : '',
      status: task.status || 'PENDING',
      project: getTaskProjectId(task),
      assignedTo: getTaskAssigneeId(task),
    })
    setError('')
    setModalOpen(true)
  }

  const handleDelete = async (id) => {
    if (!window.confirm(t('Bạn có chắc muốn xóa công việc này?'))) return
    try {
      await deleteTask(id)
    } catch (err) {
      const msg = err.response?.data?.message || t('Xóa thất bại.')
      alert(msg)
    }
  }

  const handleSave = async (e) => {
    e.preventDefault()
    setSaving(true)
    setError('')
    const payload = {
      title: form.title,
      description: form.description || null,
      requiredSkills: form.requiredSkills || null,
      dueDate: form.dueDate || null,
      status: form.status,
      projectId: form.project ? Number(form.project) : null,
      assignedToId: form.assignedTo ? Number(form.assignedTo) : null,
    }
    try {
      if (editTarget) {
        await updateTask(editTarget.taskId ?? editTarget.id, payload)
      } else {
        await createTask(payload)
      }
      setModalOpen(false)
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.error || t('Lưu thất bại.'))
    } finally {
      setSaving(false)
    }
  }

  const getProjectName = (task) => {
    if (task.project?.name) return task.project.name
    const proj = projects.find((p) => (p.projectId ?? p.id) === task.project)
    return proj?.name || '-'
  }

  const getAssigneeName = (task) => {
    if (task.assignedTo?.firstName) {
      return `${task.assignedTo.firstName} ${task.assignedTo.lastName}`
    }
    const emp = employees.find((e) => (e.employeeId ?? e.id) === task.assignedTo)
    return emp ? `${emp.firstName} ${emp.lastName}` : '-'
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-800">{t('Công việc')}</h1>
        <button
          onClick={openAdd}
          className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-lg text-sm font-medium transition-colors"
        >
          <MdAdd className="text-xl" /> {t('Thêm công việc')}
        </button>
      </div>

      {taskError && (
        <div className="p-3 mb-4 bg-red-50 border border-red-200 text-red-600 rounded-lg text-sm">{taskError}</div>
      )}

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
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">{t('Tiêu đề')}</th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">{t('Mô tả')}</th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">{t('Hạn chót')}</th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">{t('Trạng thái')}</th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">{t('Dự án')}</th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">{t('Phân công')}</th>
                  <th className="px-6 py-3 text-center text-xs font-semibold text-gray-500 uppercase">{t('Hành động')}</th>
                </tr>
              </thead>
              <tbody>
                {tasks.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="text-center py-8 text-gray-400">{t('Chưa có công việc nào.')}</td>
                  </tr>
                ) : (
                  tasks.map((task, idx) => (
                    <tr key={task.taskId ?? task.id} className={idx % 2 === 0 ? 'bg-white hover:bg-gray-50' : 'bg-gray-50 hover:bg-gray-100'}>
                      <td className="px-6 py-4 font-medium text-gray-800">{task.title}</td>
                      <td className="px-6 py-4 text-gray-600 max-w-xs truncate">{task.description}</td>
                      <td className="px-6 py-4 text-gray-600">{task.dueDate ? task.dueDate.split('T')[0] : '-'}</td>
                      <td className="px-6 py-4"><StatusBadge status={task.status} /></td>
                      <td className="px-6 py-4 text-gray-600">{getProjectName(task)}</td>
                      <td className="px-6 py-4 text-gray-600">{getAssigneeName(task)}</td>
                      <td className="px-6 py-4 text-center">
                        <button onClick={() => openEdit(task)} className="inline-flex items-center gap-1 text-indigo-600 hover:text-indigo-800 mr-3 text-sm">
                          <MdEdit /> {t('Sửa')}
                        </button>
                        <button onClick={() => handleDelete(task.taskId ?? task.id)} className="inline-flex items-center gap-1 text-red-500 hover:text-red-700 text-sm">
                          <MdDelete /> {t('Xóa')}
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

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editTarget ? t('Chỉnh sửa công việc') : t('Thêm công việc mới')}>
        <form onSubmit={handleSave} className="space-y-4">
          {error && <div className="p-3 bg-red-50 border border-red-200 text-red-600 rounded-lg text-sm">{error}</div>}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">{t('Tiêu đề')}</label>
            <input
              type="text"
              value={form.title}
              onChange={(e) => setForm({ ...form, title: e.target.value })}
              required
              placeholder={t('Nhập tiêu đề công việc')}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">{t('Mô tả')}</label>
            <textarea
              value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
              rows={2}
              placeholder={t('Mô tả công việc')}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 resize-none"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">{t('Kỹ năng yêu cầu')}</label>
            <input
              type="text"
              value={form.requiredSkills}
              onChange={(e) => setForm({ ...form, requiredSkills: e.target.value })}
              placeholder={t('VD: Java, Spring Boot, PostgreSQL')}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
            <p className="text-xs text-gray-500 mt-1">{t('Quản lý nhập tự do, AI sẽ đối chiếu với kỹ năng của nhân viên khi gợi ý.')}</p>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">{t('Hạn chót')}</label>
              <input
                type="date"
                value={form.dueDate}
                onChange={(e) => setForm({ ...form, dueDate: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">{t('Trạng thái')}</label>
              <select
                value={form.status}
                onChange={(e) => setForm({ ...form, status: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              >
                <option value="PENDING">{t('Chờ xử lý')}</option>
                <option value="IN_PROGRESS">{t('Đang thực hiện')}</option>
                <option value="COMPLETED">{t('Hoàn thành')}</option>
              </select>
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">{t('Dự án')}</label>
            <select
              value={form.project || ''}
              onChange={(e) => setForm({ ...form, project: e.target.value || null })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            >
              <option value="">{t('-- Chọn dự án --')}</option>
              {projects.map((p) => (
                <option key={p.projectId ?? p.id} value={p.projectId ?? p.id}>{p.name}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">{t('Phân công cho')}</label>
            <select
              value={form.assignedTo || ''}
              onChange={(e) => setForm({ ...form, assignedTo: e.target.value || null })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            >
              <option value="">{t('-- Chọn nhân viên --')}</option>
              {employees.map((emp) => (
                <option key={emp.employeeId ?? emp.id} value={emp.employeeId ?? emp.id}>{emp.firstName} {emp.lastName}</option>
              ))}
            </select>
          </div>
          <div className="flex gap-3 pt-2">
            <button type="button" onClick={() => setModalOpen(false)} className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg text-sm hover:bg-gray-50">{t('Hủy')}</button>
            <button type="submit" disabled={saving} className="flex-1 bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-400 text-white px-4 py-2 rounded-lg text-sm font-medium">
              {saving ? t('Đang lưu...') : t('Lưu')}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
