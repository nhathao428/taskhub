import { useEffect, useState } from 'react'
import { MdAdd, MdCheck, MdClose, MdWarning, MdImage } from 'react-icons/md'
import Modal from '../components/Modal'
import api from '../api/axios'
import { useAttendance } from '../hooks/useAttendance'
import { useEmployees } from '../hooks/useEmployees'
import { useTranslation } from '../context/LanguageContext'
import { EmptyState } from '../components/Illustrations'

function reviewBadge(status, t) {
  if (status === 'APPROVED') {
    return <span className="px-2 py-0.5 text-[11px] rounded-full bg-emerald-100 text-emerald-700 font-medium">{t('Đã duyệt')}</span>
  }
  if (status === 'PENDING_REVIEW') {
    return <span className="inline-flex items-center gap-1 px-2 py-0.5 text-[11px] rounded-full bg-amber-100 text-amber-700 font-medium"><MdWarning /> {t('Chờ duyệt')}</span>
  }
  if (status === 'REJECTED') {
    return <span className="px-2 py-0.5 text-[11px] rounded-full bg-rose-100 text-rose-700 font-medium">{t('Từ chối')}</span>
  }
  return <span className="text-slate-400 text-xs">-</span>
}

const emptyForm = { employee: '', date: '', checkIn: '', checkOut: '' }

export default function Attendance() {
  const { records, loading, error: fetchError, refetch, logAttendance } = useAttendance()
  const { employees } = useEmployees()
  const { t } = useTranslation()
  const [filterEmpId, setFilterEmpId] = useState('')
  const [modalOpen, setModalOpen] = useState(false)
  const [form, setForm] = useState(emptyForm)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  // --- Xem ảnh check-in bị nghi vấn (đối chiếu bằng mắt) ---
  // Ảnh chỉ tồn tại với lần không khớp mặt / trượt liveness, và tự xoá sau hạn lưu trữ.
  const [captureView, setCaptureView] = useState(null)   // attendanceId đang xem
  const [captureUrl, setCaptureUrl] = useState('')
  const [captureError, setCaptureError] = useState('')

  useEffect(() => {
    if (!captureView) {
      // Thu hồi blob URL để không giữ ảnh sinh trắc học trong bộ nhớ trình duyệt.
      if (captureUrl) URL.revokeObjectURL(captureUrl)
      setCaptureUrl(''); setCaptureError('')
      return
    }
    let revoked = false
    let url = ''
    ;(async () => {
      try {
        setCaptureError('')
        const res = await api.get(`/api/face/capture/${captureView}`, { responseType: 'blob' })
        url = URL.createObjectURL(res.data)
        if (!revoked) setCaptureUrl(url)
      } catch (err) {
        // Backend trả 401 cho cả trường hợp thiếu quyền (hành vi chung của dự án), nên
        // gộp 401/403 vào cùng một thông báo.
        const status = err.response?.status
        setCaptureError(
          status === 403 || status === 401
            ? t('Bạn không có quyền xem ảnh này (chỉ quản lý xem được).')
            : t('Không có ảnh cho lần chấm công này (chỉ lần bị nghi vấn mới lưu, và ảnh sẽ tự xoá sau hạn lưu trữ).')
        )
      }
    })()
    return () => { revoked = true; if (url) URL.revokeObjectURL(url) }
  }, [captureView])

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
      setError(err.response?.data?.message || err.response?.data?.error || t('Chấm công thất bại.'))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-slate-900 tracking-tight">{t('Chấm công')}</h1>
        <button
          onClick={() => { setForm(emptyForm); setError(''); setModalOpen(true) }}
          className="flex items-center gap-2 bg-brand-600 hover:bg-brand-700 text-white px-4 py-2 rounded-lg text-sm font-medium shadow-brand-glow transition-colors"
        >
          <MdAdd className="text-xl" /> {t('Ghi nhận chấm công')}
        </button>
      </div>

      {fetchError && (
        <div className="p-3 mb-4 bg-red-50 border border-red-200 text-red-600 rounded-lg text-sm">{fetchError}</div>
      )}

      <div className="bg-white rounded-2xl shadow-soft ring-1 ring-slate-200/70 p-4 mb-4">
        <div className="flex items-center gap-4">
          <label className="text-sm font-medium text-slate-700 whitespace-nowrap">{t('Lọc theo nhân viên:')}</label>
          <select
            value={filterEmpId}
            onChange={handleFilterChange}
            className="flex-1 max-w-xs px-3 py-2 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
          >
            <option value="">{t('Tất cả nhân viên')}</option>
            {employees.map((emp) => (
              <option key={emp.employeeId ?? emp.id} value={emp.employeeId ?? emp.id}>
                {emp.firstName} {emp.lastName}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="bg-white rounded-2xl shadow-soft ring-1 ring-slate-200/70 overflow-hidden">
        {loading ? (
          <div className="flex justify-center py-12">
            <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-brand-600" />
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-slate-50 border-b border-slate-200">
              <tr>
                <th className="px-6 py-3 text-left text-[11px] font-semibold text-slate-500 uppercase tracking-[0.12em]">{t('Nhân viên')}</th>
                <th className="px-6 py-3 text-left text-[11px] font-semibold text-slate-500 uppercase tracking-[0.12em]">{t('Ngày')}</th>
                <th className="px-6 py-3 text-left text-[11px] font-semibold text-slate-500 uppercase tracking-[0.12em]">{t('Giờ vào')}</th>
                <th className="px-6 py-3 text-left text-[11px] font-semibold text-slate-500 uppercase tracking-[0.12em]">{t('Giờ ra')}</th>
                <th className="px-6 py-3 text-left text-[11px] font-semibold text-slate-500 uppercase tracking-[0.12em]">{t('Văn phòng')}</th>
                <th className="px-6 py-3 text-left text-[11px] font-semibold text-slate-500 uppercase tracking-[0.12em]">{t('Khoảng cách')}</th>
                <th className="px-6 py-3 text-left text-[11px] font-semibold text-slate-500 uppercase tracking-[0.12em]">{t('Trạng thái')}</th>
                <th className="px-6 py-3 text-left text-[11px] font-semibold text-slate-500 uppercase tracking-[0.12em]">{t('Hành động')}</th>
              </tr>
            </thead>
            <tbody>
              {records.length === 0 ? (
                <tr>
                  <td colSpan={8} className="py-4"><EmptyState text={t('Chưa có dữ liệu chấm công.')} /></td>
                </tr>
              ) : (
                records.map((rec, idx) => (
                  <tr key={rec.attendanceId ?? rec.id ?? idx} className={idx % 2 === 0 ? 'bg-white hover:bg-slate-50' : 'bg-slate-50/60 hover:bg-slate-100'}>
                    <td className="px-6 py-4 font-medium text-slate-900">{getEmpName(rec)}</td>
                    <td className="px-6 py-4 text-slate-600">{rec.date ? rec.date.split('T')[0] : '-'}</td>
                    <td className="px-6 py-4 text-slate-600">{rec.checkIn || '-'}</td>
                    <td className="px-6 py-4 text-slate-600">{rec.checkOut || '-'}</td>
                    <td className="px-6 py-4 text-slate-600">{rec.checkInOffice?.name || '-'}</td>
                    <td className="px-6 py-4 text-slate-600">
                      {rec.checkInDistanceMeters != null ? `${rec.checkInDistanceMeters}m` : '-'}
                    </td>
                    <td className="px-6 py-4">
                      {reviewBadge(rec.reviewStatus, t)}
                      {/* Kết quả nhận diện khuôn mặt, chỉ hiện với bản ghi có dùng */}
                      {(rec.faceVerified != null || rec.livenessPassed != null) && (
                        <div className="mt-1 text-[11px]">
                          {rec.livenessPassed === false ? (
                            <span className="text-rose-600">{t('Nghi giả mạo (không chớp mắt)')}</span>
                          ) : rec.faceVerified === false ? (
                            <span className="text-amber-600">
                              {t('Mặt không khớp')}
                              {rec.faceSimilarity != null && ` (${rec.faceSimilarity.toFixed(2)})`}
                            </span>
                          ) : (
                            <span className="text-emerald-600">
                              {t('Mặt khớp')}
                              {rec.faceSimilarity != null && ` (${rec.faceSimilarity.toFixed(2)})`}
                            </span>
                          )}
                        </div>
                      )}
                    </td>
                    <td className="px-6 py-4">
                      {/* Xem ảnh đối chiếu — chỉ tồn tại với lần bị nghi vấn */}
                      {(rec.faceVerified === false || rec.livenessPassed === false) && (
                        <button
                          onClick={() => setCaptureView(rec.attendanceId)}
                          title={t('Xem ảnh lúc check-in để đối chiếu')}
                          className="mb-1 inline-flex items-center gap-1 px-2 py-1 rounded-md bg-slate-100 hover:bg-slate-200 text-slate-700 text-[11px]"
                        >
                          <MdImage /> {t('Xem ảnh')}
                        </button>
                      )}
                      {rec.reviewStatus === 'PENDING_REVIEW' ? (
                        <div className="flex gap-1">
                          <button
                            onClick={async () => {
                              await api.patch(`/api/attendance/${rec.attendanceId}/review`, { status: 'APPROVED' })
                              refetch(filterEmpId)
                            }}
                            title={t('Duyệt')}
                            className="p-1.5 rounded-md bg-emerald-100 hover:bg-emerald-200 text-emerald-700"
                          ><MdCheck /></button>
                          <button
                            onClick={async () => {
                              await api.patch(`/api/attendance/${rec.attendanceId}/review`, { status: 'REJECTED' })
                              refetch(filterEmpId)
                            }}
                            title={t('Từ chối')}
                            className="p-1.5 rounded-md bg-rose-100 hover:bg-rose-200 text-rose-700"
                          ><MdClose /></button>
                        </div>
                      ) : <span className="text-slate-300 text-xs">—</span>}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        )}
      </div>

      {/* Ảnh lúc check-in để quản lý tự đối chiếu đúng/sai người */}
      <Modal
        isOpen={!!captureView}
        onClose={() => setCaptureView(null)}
        title={t('Ảnh lúc chấm công')}
      >
        <div className="space-y-3">
          {captureError && (
            <div className="p-3 bg-amber-50 border border-amber-200 text-amber-700 rounded-lg text-sm">
              {captureError}
            </div>
          )}
          {!captureError && !captureUrl && (
            <div className="flex justify-center py-8">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-600" />
            </div>
          )}
          {captureUrl && (
            <>
              <img src={captureUrl} alt={t('Ảnh chấm công')} className="w-full rounded-lg" />
              <p className="text-xs text-slate-500">
                {t('Ảnh này chỉ được lưu vì lần chấm công bị nghi vấn, đã mã hoá trong cơ sở dữ liệu và sẽ tự động xoá sau thời hạn lưu trữ. So với ảnh nhân viên đã đăng ký để quyết định duyệt hay từ chối.')}
              </p>
            </>
          )}
        </div>
      </Modal>

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={t('Ghi nhận chấm công')}>
        <form onSubmit={handleSave} className="space-y-4">
          {error && <div className="p-3 bg-red-50 border border-red-200 text-red-600 rounded-lg text-sm">{error}</div>}
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">{t('Nhân viên')}</label>
            <select
              value={form.employee}
              onChange={(e) => setForm({ ...form, employee: e.target.value })}
              required
              className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
            >
              <option value="">{t('-- Chọn nhân viên --')}</option>
              {employees.map((emp) => (
                <option key={emp.employeeId ?? emp.id} value={emp.employeeId ?? emp.id}>
                  {emp.firstName} {emp.lastName}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">{t('Ngày')}</label>
            <input
              type="date"
              value={form.date}
              onChange={(e) => setForm({ ...form, date: e.target.value })}
              required
              className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
            />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">{t('Giờ vào')}</label>
              <input
                type="time"
                value={form.checkIn}
                onChange={(e) => setForm({ ...form, checkIn: e.target.value })}
                className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">{t('Giờ ra')}</label>
              <input
                type="time"
                value={form.checkOut}
                onChange={(e) => setForm({ ...form, checkOut: e.target.value })}
                className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
              />
            </div>
          </div>
          <div className="flex gap-3 pt-2">
            <button type="button" onClick={() => setModalOpen(false)} className="flex-1 px-4 py-2 border border-slate-200 text-slate-700 rounded-lg text-sm hover:bg-slate-50">{t('Hủy')}</button>
            <button type="submit" disabled={saving} className="flex-1 bg-brand-600 hover:bg-brand-700 disabled:bg-brand-300 text-white px-4 py-2 rounded-lg text-sm font-medium shadow-brand-glow disabled:shadow-none">
              {saving ? t('Đang lưu...') : t('Lưu')}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
