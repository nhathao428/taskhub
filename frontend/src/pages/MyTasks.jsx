import { useEffect, useState } from 'react'
import api from '../api/axios'
import { MdAssignmentTurnedIn, MdHourglassEmpty, MdPlayArrow } from 'react-icons/md'
import { useTranslation } from '../context/LanguageContext'

const statusConfig = {
  pending: { label: 'Chờ xử lý', cls: 'bg-yellow-100 text-yellow-700' },
  in_progress: { label: 'Đang thực hiện', cls: 'bg-blue-100 text-blue-700' },
  completed: { label: 'Hoàn thành', cls: 'bg-green-100 text-green-700' },
}

function StatusBadge({ status }) {
  const { t } = useTranslation()
  const key = (status || '').toLowerCase()
  const cfg = statusConfig[key]
  const label = cfg ? t(cfg.label) : status
  const cls = cfg ? cfg.cls : 'bg-gray-100 text-gray-600'
  return <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${cls}`}>{label}</span>
}

export default function MyTasks() {
  const { t } = useTranslation()
  const [tasks, setTasks] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [updatingId, setUpdatingId] = useState(null)

  const load = async () => {
    try {
      setLoading(true)
      const res = await api.get('/api/tasks/me')
      setTasks(res.data?.data || [])
      setError('')
    } catch (err) {
      setError(err.response?.data?.message || t('Không tải được danh sách công việc của bạn.'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const changeStatus = async (taskId, newStatus) => {
    setUpdatingId(taskId)
    try {
      await api.patch(`/api/tasks/${taskId}/status`, { status: newStatus })
      await load()
    } catch (err) {
      alert(err.response?.data?.message || t('Cập nhật trạng thái thất bại.'))
    } finally {
      setUpdatingId(null)
    }
  }

  const counts = {
    pending: tasks.filter((t) => (t.status || '').toLowerCase() === 'pending').length,
    in_progress: tasks.filter((t) => (t.status || '').toLowerCase() === 'in_progress').length,
    completed: tasks.filter((t) => (t.status || '').toLowerCase() === 'completed').length,
  }

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-800 mb-2">{t('Công việc của tôi')}</h1>
      <p className="text-sm text-gray-500 mb-6">{t('Các nhiệm vụ được quản lý phân cho bạn. Bạn có thể cập nhật trạng thái công việc trực tiếp.')}</p>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
        <div className="bg-white rounded-xl shadow p-4 flex items-center gap-3">
          <div className="p-3 rounded-lg bg-yellow-100 text-yellow-700"><MdHourglassEmpty className="text-xl" /></div>
          <div>
            <p className="text-xs text-gray-500 uppercase">{t('Chờ xử lý')}</p>
            <p className="text-xl font-bold text-gray-800">{counts.pending}</p>
          </div>
        </div>
        <div className="bg-white rounded-xl shadow p-4 flex items-center gap-3">
          <div className="p-3 rounded-lg bg-blue-100 text-blue-700"><MdPlayArrow className="text-xl" /></div>
          <div>
            <p className="text-xs text-gray-500 uppercase">{t('Đang thực hiện')}</p>
            <p className="text-xl font-bold text-gray-800">{counts.in_progress}</p>
          </div>
        </div>
        <div className="bg-white rounded-xl shadow p-4 flex items-center gap-3">
          <div className="p-3 rounded-lg bg-green-100 text-green-700"><MdAssignmentTurnedIn className="text-xl" /></div>
          <div>
            <p className="text-xs text-gray-500 uppercase">{t('Hoàn thành')}</p>
            <p className="text-xl font-bold text-gray-800">{counts.completed}</p>
          </div>
        </div>
      </div>

      {error && (
        <div className="p-3 mb-4 bg-red-50 border border-red-200 text-red-600 rounded-lg text-sm">{error}</div>
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
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">{t('Kỹ năng yêu cầu')}</th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">{t('Hạn chót')}</th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">{t('Trạng thái')}</th>
                  <th className="px-6 py-3 text-center text-xs font-semibold text-gray-500 uppercase">{t('Cập nhật')}</th>
                </tr>
              </thead>
              <tbody>
                {tasks.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="text-center py-8 text-gray-400">{t('Bạn chưa được giao công việc nào.')}</td>
                  </tr>
                ) : (
                  tasks.map((task, idx) => (
                    <tr key={task.taskId ?? task.id} className={idx % 2 === 0 ? 'bg-white hover:bg-gray-50' : 'bg-gray-50 hover:bg-gray-100'}>
                      <td className="px-6 py-4 font-medium text-gray-800">{task.title}</td>
                      <td className="px-6 py-4 text-gray-600 max-w-xs truncate">{task.description || '-'}</td>
                      <td className="px-6 py-4 text-gray-600 max-w-xs truncate">{task.requiredSkills || '-'}</td>
                      <td className="px-6 py-4 text-gray-600">{task.dueDate ? task.dueDate.split('T')[0] : '-'}</td>
                      <td className="px-6 py-4"><StatusBadge status={task.status} /></td>
                      <td className="px-6 py-4 text-center">
                        <select
                          disabled={updatingId === (task.taskId ?? task.id)}
                          value={(task.status || 'pending').toLowerCase()}
                          onChange={(e) => changeStatus(task.taskId ?? task.id, e.target.value)}
                          className="px-2 py-1 border border-gray-300 rounded-lg text-xs focus:outline-none focus:ring-2 focus:ring-indigo-500"
                        >
                          <option value="pending">{t('Chờ xử lý')}</option>
                          <option value="in_progress">{t('Đang thực hiện')}</option>
                          <option value="completed">{t('Hoàn thành')}</option>
                        </select>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}
