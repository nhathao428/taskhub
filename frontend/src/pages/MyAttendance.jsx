import { useEffect, useMemo, useState } from 'react'
import api from '../api/axios'
import { MdLogin, MdLogout, MdMyLocation, MdWarning, MdCheckCircle } from 'react-icons/md'
import OfficeMap from '../components/OfficeMap'
import { useTranslation } from '../context/LanguageContext'

function formatDateTime(value, lang) {
  if (!value) return '-'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return value
  return d.toLocaleString(lang === 'en' ? 'en-US' : 'vi-VN')
}

function reviewBadge(status, t) {
  if (status === 'APPROVED') {
    return (
      <span className="inline-flex items-center gap-1 px-2 py-0.5 text-[11px] font-medium rounded-full bg-emerald-100 text-emerald-700">
        <MdCheckCircle /> {t('Đã duyệt')}
      </span>
    )
  }
  if (status === 'PENDING_REVIEW') {
    return (
      <span className="inline-flex items-center gap-1 px-2 py-0.5 text-[11px] font-medium rounded-full bg-amber-100 text-amber-700">
        <MdWarning /> {t('Chờ duyệt')}
      </span>
    )
  }
  if (status === 'REJECTED') {
    return (
      <span className="inline-flex items-center gap-1 px-2 py-0.5 text-[11px] font-medium rounded-full bg-rose-100 text-rose-700">
        ✕ {t('Từ chối')}
      </span>
    )
  }
  return <span className="text-gray-400 text-xs">-</span>
}

export default function MyAttendance() {
  const { t, lang } = useTranslation()
  const [records, setRecords] = useState([])
  const [offices, setOffices] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState('')
  const [position, setPosition] = useState(null) // [lat, lng]
  const [posError, setPosError] = useState('')
  const [posLoading, setPosLoading] = useState(false)

  const load = async () => {
    try {
      setLoading(true)
      const [rRecords, rOffices] = await Promise.all([
        api.get('/api/attendance/me'),
        api.get('/api/office-locations', { params: { activeOnly: true } }),
      ])
      setRecords(rRecords.data?.data || [])
      setOffices(rOffices.data?.data || [])
      setError('')
    } catch (err) {
      setError(err.response?.data?.message || t('Không tải được dữ liệu.'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const requestLocation = () => {
    setPosError(''); setPosLoading(true)
    if (!('geolocation' in navigator)) {
      setPosError(t('Trình duyệt không hỗ trợ Geolocation API.'))
      setPosLoading(false)
      return
    }
    navigator.geolocation.getCurrentPosition(
      (p) => {
        setPosition([p.coords.latitude, p.coords.longitude])
        setPosLoading(false)
      },
      (err) => {
        setPosError(err.message || t('Không lấy được vị trí. Hãy cấp quyền GPS cho trình duyệt.'))
        setPosLoading(false)
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 5000 },
    )
  }

  // Tự động lấy vị trí lần đầu mở trang
  useEffect(() => { requestLocation() }, [])

  // Tính khoảng cách tới office gần nhất để hiển thị
  const nearest = useMemo(() => {
    if (!position || offices.length === 0) return null
    const [lat, lng] = position
    const distance = (a, b) => {
      const R = 6371000
      const dLat = (b[0] - a[0]) * Math.PI / 180
      const dLng = (b[1] - a[1]) * Math.PI / 180
      const x = Math.sin(dLat / 2) ** 2 +
        Math.cos(a[0] * Math.PI / 180) * Math.cos(b[0] * Math.PI / 180) *
        Math.sin(dLng / 2) ** 2
      return 2 * R * Math.atan2(Math.sqrt(x), Math.sqrt(1 - x))
    }
    let best = null
    for (const o of offices) {
      const d = distance([lat, lng], [o.latitude, o.longitude])
      if (!best || d < best.distance) best = { office: o, distance: d }
    }
    if (!best) return null
    return { ...best, within: best.distance <= (best.office.radiusMeters || 100) }
  }, [position, offices])

  const sendCheckIn = async (path, label) => {
    setBusy(true); setMessage('')
    try {
      const body = position
        ? { latitude: position[0], longitude: position[1], isMocked: false }
        : null
      const res = await api.post(path, body)
      const reviewStatus = res.data?.data?.reviewStatus
      if (reviewStatus === 'PENDING_REVIEW') {
        setMessage(t('{label} thành công nhưng vị trí ngoài vùng cho phép – đã chuyển sang chờ duyệt.', { label }))
      } else {
        setMessage(t('{label} thành công.', { label }))
      }
      await load()
    } catch (err) {
      setMessage(err.response?.data?.message || t('{label} thất bại.', { label }))
    } finally { setBusy(false) }
  }

  const checkIn = () => sendCheckIn('/api/attendance/me/checkin', t('Chấm công vào'))
  const checkOut = () => sendCheckIn('/api/attendance/me/checkout', t('Chấm công ra'))

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-800 mb-2">{t('Chấm công của tôi')}</h1>
      <p className="text-sm text-gray-500 mb-6">
        {t('Hệ thống dùng GPS để xác minh vị trí. Nếu nằm ngoài vùng cho phép, bản ghi sẽ chuyển sang')}
        <span className="font-medium text-amber-600"> {t('trạng thái chờ quản lý duyệt')}</span>.
      </p>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4 mb-6">
        {/* Map */}
        <div className="lg:col-span-2">
          <OfficeMap offices={offices} currentPosition={position} height={380} />
        </div>

        {/* Action card */}
        <div className="flex flex-col gap-3">
          <div className="bg-white rounded-xl p-4 border border-gray-200 shadow-sm">
            <div className="flex items-center justify-between mb-2">
              <p className="text-sm font-semibold text-gray-700">{t('Vị trí hiện tại')}</p>
              <button
                onClick={requestLocation}
                disabled={posLoading}
                className="inline-flex items-center gap-1 text-xs px-2 py-1 rounded-md bg-indigo-50 text-indigo-700 hover:bg-indigo-100 disabled:opacity-50"
              >
                <MdMyLocation /> {t('Cập nhật')}
              </button>
            </div>
            {posLoading && <p className="text-xs text-gray-500">{t('Đang lấy GPS…')}</p>}
            {posError && <p className="text-xs text-rose-600">{posError}</p>}
            {position && (
              <p className="text-xs text-gray-600 break-all">
                {position[0].toFixed(6)}, {position[1].toFixed(6)}
              </p>
            )}
            {nearest && (
              <div className={`mt-2 text-xs rounded-md p-2 border ${
                nearest.within
                  ? 'bg-emerald-50 border-emerald-200 text-emerald-700'
                  : 'bg-amber-50 border-amber-200 text-amber-700'
              }`}>
                {t('Gần nhất:')} <b>{nearest.office.name}</b><br />
                {t('Khoảng cách:')} <b>{Math.round(nearest.distance)}m</b>
                {' '} / {nearest.office.radiusMeters}m
                {nearest.within ? ` ✓ ${t('trong vùng')}` : ` — ${t('ngoài vùng cho phép')}`}
              </div>
            )}
            {!nearest && offices.length === 0 && (
              <p className="text-xs text-gray-400 mt-2">
                {t('Chưa có văn phòng nào được cấu hình. Liên hệ quản lý.')}
              </p>
            )}
          </div>

          <button
            onClick={checkIn}
            disabled={busy}
            className="flex items-center justify-center gap-3 px-6 py-5 rounded-xl bg-gradient-to-r from-emerald-500 to-teal-500 text-white font-semibold shadow-md hover:shadow-lg transition-shadow disabled:opacity-60"
          >
            <MdLogin className="text-2xl" />
            <span>{t('Vào ca (Check-in)')}</span>
          </button>
          <button
            onClick={checkOut}
            disabled={busy}
            className="flex items-center justify-center gap-3 px-6 py-5 rounded-xl bg-gradient-to-r from-rose-500 to-pink-500 text-white font-semibold shadow-md hover:shadow-lg transition-shadow disabled:opacity-60"
          >
            <MdLogout className="text-2xl" />
            <span>{t('Tan ca (Check-out)')}</span>
          </button>
        </div>
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
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">{t('Ngày')}</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">{t('Giờ vào')}</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">{t('Giờ ra')}</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">{t('Văn phòng')}</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">{t('Khoảng cách')}</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase">{t('Trạng thái')}</th>
              </tr>
            </thead>
            <tbody>
              {records.length === 0 ? (
                <tr>
                  <td colSpan={6} className="text-center py-8 text-gray-400">{t('Chưa có bản ghi chấm công nào.')}</td>
                </tr>
              ) : (
                records.map((r, idx) => (
                  <tr key={r.attendanceId ?? idx} className={idx % 2 === 0 ? 'bg-white hover:bg-gray-50' : 'bg-gray-50 hover:bg-gray-100'}>
                    <td className="px-6 py-4 text-gray-800">{r.date ? String(r.date).split('T')[0] : '-'}</td>
                    <td className="px-6 py-4 text-gray-600">{formatDateTime(r.checkIn, lang)}</td>
                    <td className="px-6 py-4 text-gray-600">{formatDateTime(r.checkOut, lang)}</td>
                    <td className="px-6 py-4 text-gray-600">{r.checkInOffice?.name || '-'}</td>
                    <td className="px-6 py-4 text-gray-600">
                      {r.checkInDistanceMeters != null ? `${r.checkInDistanceMeters}m` : '-'}
                    </td>
                    <td className="px-6 py-4">{reviewBadge(r.reviewStatus, t)}</td>
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
