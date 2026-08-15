import { useEffect, useMemo, useState } from 'react'
import api from '../api/axios'
import { MdLogin, MdLogout, MdMyLocation, MdWarning, MdCheckCircle, MdFace, MdDelete } from 'react-icons/md'
import OfficeMap from '../components/OfficeMap'
import FaceCapture from '../components/FaceCapture'
import { useTranslation } from '../context/LanguageContext'
import { EmptyState } from '../components/Illustrations'

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
  return <span className="text-slate-400 text-xs">-</span>
}

/** Hiển thị kết quả nhận diện khuôn mặt của một lần chấm công. */
function faceBadge(record, t) {
  const { faceVerified, faceSimilarity, livenessPassed } = record
  // Lần check-in không dùng khuôn mặt -> cả 3 trường đều null
  if (faceVerified == null && livenessPassed == null) {
    return <span className="text-slate-400 text-xs">-</span>
  }
  if (livenessPassed === false) {
    return (
      <span className="inline-flex items-center gap-1 px-2 py-0.5 text-[11px] font-medium rounded-full bg-rose-100 text-rose-700">
        <MdWarning /> {t('Nghi giả mạo')}
      </span>
    )
  }
  if (faceVerified === true) {
    return (
      <span className="inline-flex items-center gap-1 px-2 py-0.5 text-[11px] font-medium rounded-full bg-emerald-100 text-emerald-700">
        <MdCheckCircle /> {faceSimilarity != null ? faceSimilarity.toFixed(2) : t('Khớp')}
      </span>
    )
  }
  return (
    <span className="inline-flex items-center gap-1 px-2 py-0.5 text-[11px] font-medium rounded-full bg-amber-100 text-amber-700">
      <MdWarning /> {t('Không khớp')}
      {faceSimilarity != null && ` (${faceSimilarity.toFixed(2)})`}
    </span>
  )
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

  // --- Nhận diện khuôn mặt ---
  // faceStatus.featureEnabled = backend có bật tính năng không (phụ thuộc BIOMETRIC_KEY).
  // Nếu tắt, toàn bộ phần khuôn mặt bị ẩn và check-in chạy bằng GPS như cũ.
  const [faceStatus, setFaceStatus] = useState(null)
  const [showEnroll, setShowEnroll] = useState(false)
  const [enrollShots, setEnrollShots] = useState([])
  const [faceMessage, setFaceMessage] = useState('')
  const [useFaceForCheckIn, setUseFaceForCheckIn] = useState(true)

  const loadFaceStatus = async () => {
    try {
      const res = await api.get('/api/face/me')
      setFaceStatus(res.data?.data || null)
    } catch {
      // Backend cũ chưa có endpoint này -> coi như tắt tính năng, không báo lỗi ồn ào.
      setFaceStatus(null)
    }
  }

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

  useEffect(() => { load(); loadFaceStatus() }, [])

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

  /**
   * Gửi check-in / check-out.
   * face = { image, frames } nếu có dùng nhận diện khuôn mặt, ngược lại null.
   */
  const sendCheckIn = async (path, label, face = null) => {
    setBusy(true); setMessage('')
    try {
      const body = {}
      if (position) {
        body.latitude = position[0]
        body.longitude = position[1]
        body.isMocked = false
      }
      if (face?.image) {
        body.faceImageBase64 = face.image
        if (face.frames?.length) body.livenessFramesBase64 = face.frames
      }
      const res = await api.post(path, Object.keys(body).length ? body : null)
      const data = res.data?.data
      const reviewStatus = data?.reviewStatus

      if (reviewStatus === 'PENDING_REVIEW') {
        // Nói rõ LÝ DO bị chuyển chờ duyệt thay vì thông báo chung chung.
        let reason = t('vị trí ngoài vùng cho phép')
        if (data?.livenessPassed === false) {
          reason = t('không phát hiện được chớp mắt (nghi dùng ảnh/video)')
        } else if (data?.faceVerified === false) {
          reason = t('khuôn mặt không khớp với người đã đăng ký')
        }
        setMessage(t('{label} đã ghi nhận nhưng chuyển sang chờ duyệt: ', { label }) + reason + '.')
      } else {
        setMessage(t('{label} thành công.', { label }))
      }
      await load()
    } catch (err) {
      setMessage(err.response?.data?.message || t('{label} thất bại.', { label }))
    } finally { setBusy(false) }
  }

  const faceEnabled = !!faceStatus?.featureEnabled
  const faceEnrolled = !!faceStatus?.enrolled
  // Chỉ dùng khuôn mặt khi backend bật + đã đăng ký + người dùng không tắt đi.
  const faceActive = faceEnabled && faceEnrolled && useFaceForCheckIn

  const checkIn = (face = null) => sendCheckIn('/api/attendance/me/checkin', t('Chấm công vào'), face)
  const checkOut = () => sendCheckIn('/api/attendance/me/checkout', t('Chấm công ra'))

  /** Chụp xong ở chế độ check-in -> gửi luôn kèm ảnh. */
  const handleCheckInCapture = (image, frames) => checkIn({ image, frames })

  /** Chụp xong ở chế độ đăng ký -> gom lại, đủ số ảnh thì bấm gửi. */
  const handleEnrollCapture = (image) => {
    setEnrollShots((prev) => (prev.length >= 5 ? prev : [...prev, image]))
    setFaceMessage('')
  }

  const submitEnroll = async () => {
    if (enrollShots.length === 0) return
    setBusy(true); setFaceMessage('')
    try {
      const res = await api.post('/api/face/me/enroll', { imagesBase64: enrollShots })
      setFaceMessage(res.data?.data?.message || t('Đăng ký khuôn mặt thành công.'))
      setEnrollShots([])
      setShowEnroll(false)
      await loadFaceStatus()
    } catch (err) {
      setFaceMessage(err.response?.data?.message || t('Đăng ký khuôn mặt thất bại.'))
    } finally { setBusy(false) }
  }

  const deleteMyFace = async () => {
    setBusy(true); setFaceMessage('')
    try {
      await api.delete('/api/face/me')
      setFaceMessage(t('Đã xoá dữ liệu khuôn mặt của bạn.'))
      await loadFaceStatus()
    } catch (err) {
      setFaceMessage(err.response?.data?.message || t('Xoá thất bại.'))
    } finally { setBusy(false) }
  }

  return (
    <div>
      <h1 className="text-2xl font-bold text-slate-900 tracking-tight mb-2">{t('Chấm công của tôi')}</h1>
      <p className="text-sm text-slate-500 mb-6">
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
          <div className="bg-white rounded-2xl p-4 ring-1 ring-slate-200/70 shadow-soft">
            <div className="flex items-center justify-between mb-2">
              <p className="text-sm font-semibold text-slate-700">{t('Vị trí hiện tại')}</p>
              <button
                onClick={requestLocation}
                disabled={posLoading}
                className="inline-flex items-center gap-1 text-xs px-2 py-1 rounded-md bg-emerald-50 text-emerald-700 hover:bg-emerald-100 disabled:opacity-50"
              >
                <MdMyLocation /> {t('Cập nhật')}
              </button>
            </div>
            {posLoading && <p className="text-xs text-slate-500">{t('Đang lấy GPS…')}</p>}
            {posError && <p className="text-xs text-rose-600">{posError}</p>}
            {position && (
              <p className="text-xs text-slate-600 break-all">
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
              <p className="text-xs text-slate-400 mt-2">
                {t('Chưa có văn phòng nào được cấu hình. Liên hệ quản lý.')}
              </p>
            )}
          </div>

          {/* Khối nhận diện khuôn mặt — chỉ hiện khi backend bật tính năng */}
          {faceEnabled && (
            <div className="bg-white rounded-2xl p-4 ring-1 ring-slate-200/70 shadow-soft">
              <div className="flex items-center justify-between mb-2">
                <p className="text-sm font-semibold text-slate-700 inline-flex items-center gap-1">
                  <MdFace className="text-lg" /> {t('Khuôn mặt')}
                </p>
                {faceEnrolled ? (
                  <span className="inline-flex items-center gap-1 px-2 py-0.5 text-[11px] font-medium rounded-full bg-emerald-100 text-emerald-700">
                    <MdCheckCircle /> {t('Đã đăng ký')}
                  </span>
                ) : (
                  <span className="inline-flex items-center gap-1 px-2 py-0.5 text-[11px] font-medium rounded-full bg-slate-100 text-slate-600">
                    {t('Chưa đăng ký')}
                  </span>
                )}
              </div>

              {!faceEnrolled && (
                <p className="text-xs text-slate-500 mb-2">
                  {t('Đăng ký khuôn mặt để check-in an toàn hơn, tránh bị chấm công hộ.')}
                </p>
              )}

              {faceEnrolled && (
                <label className="flex items-center gap-2 text-xs text-slate-600 mb-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={useFaceForCheckIn}
                    onChange={(e) => setUseFaceForCheckIn(e.target.checked)}
                    className="rounded border-slate-300 text-emerald-600 focus:ring-emerald-500"
                  />
                  {t('Dùng khuôn mặt khi check-in')}
                </label>
              )}

              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={() => { setShowEnroll((v) => !v); setEnrollShots([]); setFaceMessage('') }}
                  className="flex-1 text-xs px-2 py-1.5 rounded-md bg-slate-100 hover:bg-slate-200 text-slate-700"
                >
                  {showEnroll ? t('Đóng') : (faceEnrolled ? t('Đăng ký lại') : t('Đăng ký ngay'))}
                </button>
                {faceEnrolled && (
                  <button
                    type="button"
                    onClick={deleteMyFace}
                    disabled={busy}
                    className="inline-flex items-center gap-1 text-xs px-2 py-1.5 rounded-md bg-rose-50 hover:bg-rose-100 text-rose-600 disabled:opacity-50"
                    title={t('Xoá dữ liệu khuôn mặt của bạn')}
                  >
                    <MdDelete /> {t('Xoá')}
                  </button>
                )}
              </div>

              {faceMessage && (
                <p className="mt-2 text-xs text-slate-600 bg-slate-50 border border-slate-200 rounded-md p-2">
                  {faceMessage}
                </p>
              )}
            </div>
          )}

          {/* Đăng ký khuôn mặt: chụp 3-5 ảnh rồi gửi */}
          {faceEnabled && showEnroll && (
            <div className="bg-white rounded-2xl p-3 ring-1 ring-slate-200/70 shadow-soft">
              <p className="text-xs text-slate-500 mb-2">
                {t('Chụp 3-5 ảnh, mỗi ảnh đổi góc mặt hoặc ánh sáng một chút để nhận diện ổn định hơn.')}
              </p>
              <FaceCapture
                onCapture={handleEnrollCapture}
                busy={busy}
                label={t('Chụp ảnh {n}/5', { n: enrollShots.length + 1 })}
              />
              <div className="flex items-center justify-between mt-2">
                <span className="text-xs text-slate-500">
                  {t('Đã chụp: {n} ảnh', { n: enrollShots.length })}
                </span>
                <button
                  type="button"
                  onClick={submitEnroll}
                  disabled={busy || enrollShots.length === 0}
                  className="text-xs px-3 py-1.5 rounded-md bg-emerald-600 hover:bg-emerald-700 text-white disabled:opacity-50"
                >
                  {t('Lưu đăng ký')}
                </button>
              </div>
            </div>
          )}

          {/* Check-in bằng khuôn mặt thay cho nút thường */}
          {faceActive ? (
            <div className="bg-white rounded-2xl p-3 ring-1 ring-slate-200/70 shadow-soft">
              <p className="text-sm font-semibold text-slate-700 mb-2 inline-flex items-center gap-1">
                <MdLogin /> {t('Vào ca bằng khuôn mặt')}
              </p>
              <FaceCapture
                onCapture={handleCheckInCapture}
                captureFrames={faceStatus?.livenessRequired ? 8 : 0}
                busy={busy}
                label={t('Chụp & vào ca')}
              />
            </div>
          ) : (
            <button
              onClick={() => checkIn()}
              disabled={busy}
              className="flex items-center justify-center gap-3 px-6 py-5 rounded-2xl bg-emerald-600 hover:bg-emerald-700 text-white font-semibold shadow-soft-md hover:shadow-soft-lg transition-shadow disabled:opacity-60"
            >
              <MdLogin className="text-2xl" />
              <span>{t('Vào ca (Check-in)')}</span>
            </button>
          )}
          <button
            onClick={checkOut}
            disabled={busy}
            className="flex items-center justify-center gap-3 px-6 py-5 rounded-2xl bg-rose-500 hover:bg-rose-600 text-white font-semibold shadow-soft-md hover:shadow-soft-lg transition-shadow disabled:opacity-60"
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

      <div className="bg-white rounded-2xl shadow-soft ring-1 ring-slate-200/70 overflow-hidden">
        {loading ? (
          <div className="flex justify-center py-12">
            <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-emerald-600" />
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-slate-50 border-b border-slate-200">
              <tr>
                <th className="px-6 py-3 text-left text-[11px] font-semibold text-slate-500 uppercase tracking-[0.12em]">{t('Ngày')}</th>
                <th className="px-6 py-3 text-left text-[11px] font-semibold text-slate-500 uppercase tracking-[0.12em]">{t('Giờ vào')}</th>
                <th className="px-6 py-3 text-left text-[11px] font-semibold text-slate-500 uppercase tracking-[0.12em]">{t('Giờ ra')}</th>
                <th className="px-6 py-3 text-left text-[11px] font-semibold text-slate-500 uppercase tracking-[0.12em]">{t('Văn phòng')}</th>
                <th className="px-6 py-3 text-left text-[11px] font-semibold text-slate-500 uppercase tracking-[0.12em]">{t('Khoảng cách')}</th>
                {faceEnabled && (
                  <th className="px-6 py-3 text-left text-[11px] font-semibold text-slate-500 uppercase tracking-[0.12em]">{t('Khuôn mặt')}</th>
                )}
                <th className="px-6 py-3 text-left text-[11px] font-semibold text-slate-500 uppercase tracking-[0.12em]">{t('Trạng thái')}</th>
              </tr>
            </thead>
            <tbody>
              {records.length === 0 ? (
                <tr>
                  <td colSpan={faceEnabled ? 7 : 6} className="py-4"><EmptyState text={t('Chưa có bản ghi chấm công nào.')} /></td>
                </tr>
              ) : (
                records.map((r, idx) => (
                  <tr key={r.attendanceId ?? idx} className={idx % 2 === 0 ? 'bg-white hover:bg-slate-50' : 'bg-slate-50/60 hover:bg-slate-100'}>
                    <td className="px-6 py-4 text-slate-900">{r.date ? String(r.date).split('T')[0] : '-'}</td>
                    <td className="px-6 py-4 text-slate-600">{formatDateTime(r.checkIn, lang)}</td>
                    <td className="px-6 py-4 text-slate-600">{formatDateTime(r.checkOut, lang)}</td>
                    <td className="px-6 py-4 text-slate-600">{r.checkInOffice?.name || '-'}</td>
                    <td className="px-6 py-4 text-slate-600">
                      {r.checkInDistanceMeters != null ? `${r.checkInDistanceMeters}m` : '-'}
                    </td>
                    {faceEnabled && (
                      <td className="px-6 py-4">{faceBadge(r, t)}</td>
                    )}
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
