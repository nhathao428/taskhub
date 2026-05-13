import { useState } from 'react'
import { MdAdd, MdCheck, MdClose, MdWarning } from 'react-icons/md'
import Modal from '../components/Modal'
import api from '../api/axios'
import { useAttendance } from '../hooks/useAttendance'
import { useEmployees } from '../hooks/useEmployees'

function reviewBadge(status) {
  if (status === 'APPROVED') {
    return <span className="px-2 py-0.5 text-[11px] rounded-full bg-emerald-100 text-emerald-700 font-medium">Đã duyệt</span>
  }
  if (status === 'PENDING_REVIEW') {
    return <span className="inline-flex items-center gap-1 px-2 py-0.5 text-[11px] rounded-full bg-amber-100 text-amber-700 font-medium"><MdWarning /> Chờ duyệt</span>
  }
  if (status === 'REJECTED') {
    return <span className="px-2 py-0.5 text-[11px] rounded-full bg-rose-100 text-rose-700 font-medium">Từ chối</span>
  }
  return <span className="text-gray-400 text-xs">-</span>
}

const emptyForm = { employee: '', date: '', checkIn: '', checkOut: '' }

export default function Attendance() {
  const { records, loading, error: fetchError, refetch, logAttendance } = useAttendance()
  const { employees } = useEmployees()
  const [filterEmpId, setFilterEmpId] = useState('')
  const [modalOpen, setModalOpen] = useState(false)
  const [form, setForm] = useState(emptyForm)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const handleFilterChange = (e) => {
    const id = e.target.value
    setFilterEmpId(id)
    refetch(id)
  }

  const getEmpName = (record) => {
    if (record.employee?.firstName) {
      return `${record.employee.firstName} ${record.employee.lastName}`
    }
    const emp = employees.find((e) => (e.employeeId ?? e.id) === (record.employee?.id ?? record.employee))
    return emp ? `${emp.firstName} ${emp.lastName}` : '-'
  }

  const handleSave = async (e) => {
    e.preventDefault()
    setSaving(true)
    setError('')
    const payload = {
      employeeId: Number(form.employee),
      date: form.date,
      checkIn: form.checkIn || null,
      checkOut: form.checkOut || null,
    }
    try {
      await logAttendance(payload)
      setModalOpen(false)
      setForm(emptyForm)
      refetch(filterEmpId)
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.error || 'Chấm công thất bại.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Chấm công</h1>
        <button
          onClick={() => { setForm(emptyForm); setError(''); setModalOpen(true) }}
          className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-lg text-sm font-medium transition-colors"
        >
          <MdAdd className="text-xl" /> Chấm công
        </button>
      </div>

      {fetchError && (
        <div className="p-3 mb-4 bg-red-50 border border-red-200 text-red-600 rounded-lg text-sm">{fetchError}</div>
      )}

      <div className="bg-white rounded-xl shadow-md p-4 mb-4">
        <div className="flex items-center gap-4">
          <label className="text-sm font-medium text-gray-700 whitespace-nowrap">Lọc theo nhân viên:</label>
          <select
            value={filterEmpId}
            onChange={handleFilterChange}
            className="flex-1 max-w-xs px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
          >
            <option value="">Tất cả nhân viên</option>
            {employees.map((emp) => (
              <option key={emp.employeeId ?? emp.id} value={emp.employeeId ?? emp.id}>
                {emp.firstName} {emp.lastName}
              </option>
            ))}
          </select>
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
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Nhân viên</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Ngày</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Giờ vào</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Giờ ra</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Văn phòng</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Khoảng cách</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Trạng thái</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Hành động</th>
              </tr>
            </thead>
            <tbody>
              {records.length === 0 ? (
                <tr>
                  <td colSpan={8} className="text-center py-8 text-gray-400">Chưa có dữ liệu chấm công.</td>
                </tr>
              ) : (
                records.map((rec, idx) => (
                  <tr key={rec.attendanceId ?? rec.id ?? idx} className={idx % 2 === 0 ? 'bg-white hover:bg-gray-50' : 'bg-gray-50 hover:bg-gray-100'}>
                    <td className="px-6 py-4 font-medium text-gray-800">{getEmpName(rec)}</td>
                    <td className="px-6 py-4 text-gray-600">{rec.date ? rec.date.split('T')[0] : '-'}</td>
                    <td className="px-6 py-4 text-gray-600">{rec.checkIn || '-'}</td>
                    <td className="px-6 py-4 text-gray-600">{rec.checkOut || '-'}</td>
                    <td className="px-6 py-4 text-gray-600">{rec.checkInOffice?.name || '-'}</td>
                    <td className="px-6 py-4 text-gray-600">
                      {rec.checkInDistanceMeters != null ? `${rec.checkInDistanceMeters}m` : '-'}
                    </td>
                    <td className="px-6 py-4">{reviewBadge(rec.reviewStatus)}</td>
                    <td className="px-6 py-4">
                      {rec.reviewStatus === 'PENDING_REVIEW' ? (
                        <div className="flex gap-1">
                          <button
                            onClick={async () => {
                              await api.patch(`/api/attendance/${rec.attendanceId}/review`, { status: 'APPROVED' })
                              refetch(filterEmpId)
                            }}
                            title="Duyệt"
                            className="p-1.5 rounded-md bg-emerald-100 hover:bg-emerald-200 text-emerald-700"
                          ><MdCheck /></button>
                          <button
                            onClick={async () => {
                              await api.patch(`/api/attendance/${rec.attendanceId}/review`, { status: 'REJECTED' })
                              refetch(filterEmpId)
                            }}
                            title="Từ chối"
                            className="p-1.5 rounded-md bg-rose-100 hover:bg-rose-200 text-rose-700"
                          ><MdClose /></button>
                        </div>
                      ) : <span className="text-gray-300 text-xs">—</span>}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        )}
      </div>

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title="Chấm công">
        <form onSubmit={handleSave} className="space-y-4">
          {error && <div className="p-3 bg-red-50 border border-red-200 text-red-600 rounded-lg text-sm">{error}</div>}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Nhân viên</label>
            <select
              value={form.employee}
              onChange={(e) => setForm({ ...form, employee: e.target.value })}
              required
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            >
              <option value="">-- Chọn nhân viên --</option>
              {employees.map((emp) => (
                <option key={emp.employeeId ?? emp.id} value={emp.employeeId ?? emp.id}>
                  {emp.firstName} {emp.lastName}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Ngày</label>
            <input
              type="date"
              value={form.date}
              onChange={(e) => setForm({ ...form, date: e.target.value })}
              required
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Giờ vào</label>
              <input
                type="time"
                value={form.checkIn}
                onChange={(e) => setForm({ ...form, checkIn: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Giờ ra</label>
              <input
                type="time"
                value={form.checkOut}
                onChange={(e) => setForm({ ...form, checkOut: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>
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
