import { useEffect, useState } from 'react'
import api from '../api/axios'
import { MdLogin, MdLogout } from 'react-icons/md'

function formatDateTime(value) {
  if (!value) return '-'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return value
  return d.toLocaleString('vi-VN')
}

export default function MyAttendance() {
  const [records, setRecords] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState('')

  const load = async () => {
    try {
      setLoading(true)
      const res = await api.get('/api/attendance/me')
      setRecords(res.data?.data || [])
      setError('')
    } catch (err) {
      setError(err.response?.data?.message || 'Không tải được lịch sử chấm công.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const checkIn = async () => {
    setBusy(true); setMessage('')
    try {
      await api.post('/api/attendance/me/checkin')
      setMessage('Đã chấm công vào ca thành công.')
      await load()
    } catch (err) {
      setMessage(err.response?.data?.message || 'Check-in thất bại.')
    } finally { setBusy(false) }
  }

  const checkOut = async () => {
    setBusy(true); setMessage('')
    try {
      await api.post('/api/attendance/me/checkout')
      setMessage('Đã chấm công ra ca thành công.')
      await load()
    } catch (err) {
      setMessage(err.response?.data?.message || 'Check-out thất bại.')
    } finally { setBusy(false) }
  }

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-800 mb-2">Chấm công của tôi</h1>
      <p className="text-sm text-gray-500 mb-6">Bấm vào ca khi đến và rời nơi làm. Lịch sử được hiển thị bên dưới.</p>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
        <button
          onClick={checkIn}
          disabled={busy}
          className="flex items-center justify-center gap-3 px-6 py-5 rounded-xl bg-gradient-to-r from-emerald-500 to-teal-500 text-white font-semibold shadow-md hover:shadow-lg transition-shadow disabled:opacity-60"
        >
          <MdLogin className="text-2xl" />
          <span>Vào ca (Check-in)</span>
        </button>
        <button
          onClick={checkOut}
          disabled={busy}
          className="flex items-center justify-center gap-3 px-6 py-5 rounded-xl bg-gradient-to-r from-rose-500 to-pink-500 text-white font-semibold shadow-md hover:shadow-lg transition-shadow disabled:opacity-60"
        >
          <MdLogout className="text-2xl" />
          <span>Tan ca (Check-out)</span>
        </button>
      </div>

      {message && (
        <div className="p-3 mb-4 bg-blue-50 border border-blue-200 text-blue-700 rounded-lg text-sm">{message}</div>
      )}
      {error && (
        <div className="p-3 mb-4 bg-red-50 border border-red-200 text-red-600 rounded-lg text-sm">{error}</div>
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
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Ngày</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Giờ vào</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Giờ ra</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Ghi chú</th>
              </tr>
            </thead>
            <tbody>
              {records.length === 0 ? (
                <tr>
                  <td colSpan={4} className="text-center py-8 text-gray-400">Chưa có bản ghi chấm công nào.</td>
                </tr>
              ) : (
                records.map((r, idx) => (
                  <tr key={r.attendanceId ?? r.id ?? idx} className={idx % 2 === 0 ? 'bg-white hover:bg-gray-50' : 'bg-gray-50 hover:bg-gray-100'}>
                    <td className="px-6 py-4 text-gray-800">{r.date ? String(r.date).split('T')[0] : '-'}</td>
                    <td className="px-6 py-4 text-gray-600">{formatDateTime(r.checkIn)}</td>
                    <td className="px-6 py-4 text-gray-600">{formatDateTime(r.checkOut)}</td>
                    <td className="px-6 py-4 text-gray-600">{r.notes || '-'}</td>
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
