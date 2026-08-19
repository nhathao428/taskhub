import { useCallback, useEffect, useRef, useState } from 'react'
import { MdVideocam, MdVideocamOff, MdCameraAlt } from 'react-icons/md'
import { useTranslation } from '../context/LanguageContext'

/**
 * Bật webcam, chụp ảnh khuôn mặt + chuỗi khung hình để kiểm tra chống giả mạo (chớp mắt).
 *
 * Dùng chung cho 2 việc:
 *   - Đăng ký khuôn mặt (chụp vài ảnh)
 *   - Check-in (chụp 1 ảnh + nhiều khung hình liên tiếp)
 *
 * Ảnh chỉ nằm trong bộ nhớ trình duyệt rồi gửi thẳng lên API — không lưu xuống máy.
 *
 * LƯU Ý: trình duyệt chỉ cho truy cập camera trên HTTPS hoặc localhost. Chạy qua HTTP ở
 * IP LAN (vd 192.168.x.x) sẽ bị chặn — đây là giới hạn bảo mật của trình duyệt.
 */
export default function FaceCapture({
  onCapture,           // (imageBase64, framesBase64[]) => void
  captureFrames = 0,   // số khung hình liên tiếp cho liveness (0 = không cần)
  frameIntervalMs = 200,
  busy = false,
  label,
}) {
  const { t } = useTranslation()
  const videoRef = useRef(null)
  const canvasRef = useRef(null)
  const streamRef = useRef(null)

  const [active, setActive] = useState(false)
  const [error, setError] = useState('')
  const [starting, setStarting] = useState(false)
  const [countdown, setCountdown] = useState(0)

  const stopCamera = useCallback(() => {
    if (streamRef.current) {
      streamRef.current.getTracks().forEach((track) => track.stop())
      streamRef.current = null
    }
    if (videoRef.current) videoRef.current.srcObject = null
    setActive(false)
  }, [])

  // Luôn tắt camera khi rời trang — tránh để đèn webcam sáng mãi.
  useEffect(() => () => stopCamera(), [stopCamera])

  const startCamera = async () => {
    setError(''); setStarting(true)
    if (!navigator.mediaDevices?.getUserMedia) {
      setError(t('Trình duyệt không hỗ trợ camera, hoặc trang đang chạy qua HTTP không bảo mật (cần HTTPS hoặc localhost).'))
      setStarting(false)
      return
    }
    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { width: { ideal: 640 }, height: { ideal: 480 }, facingMode: 'user' },
        audio: false,
      })
      streamRef.current = stream
      if (videoRef.current) {
        videoRef.current.srcObject = stream
        await videoRef.current.play()
      }
      setActive(true)
    } catch (err) {
      if (err?.name === 'NotAllowedError') {
        setError(t('Bạn đã từ chối quyền camera. Cấp lại quyền trong cài đặt trình duyệt rồi thử lại.'))
      } else if (err?.name === 'NotFoundError') {
        setError(t('Không tìm thấy camera nào trên thiết bị.'))
      } else {
        setError(err?.message || t('Không mở được camera.'))
      }
    } finally {
      setStarting(false)
    }
  }

  /** Chụp 1 khung hình hiện tại thành JPEG base64 (bỏ tiền tố data URI). */
  const grabFrame = () => {
    const video = videoRef.current
    const canvas = canvasRef.current
    if (!video || !canvas || !video.videoWidth) return null
    canvas.width = video.videoWidth
    canvas.height = video.videoHeight
    const ctx = canvas.getContext('2d')
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height)
    // quality 0.85: đủ rõ cho nhận diện mà payload không quá nặng
    return canvas.toDataURL('image/jpeg', 0.85).split(',')[1]
  }

  const sleep = (ms) => new Promise((r) => setTimeout(r, ms))

  const handleCapture = async () => {
    if (!active) return
    const main = grabFrame()
    if (!main) {
      setError(t('Chưa lấy được hình từ camera, thử lại sau giây lát.'))
      return
    }

    let frames = []
    if (captureFrames > 0) {
      // Quay liên tiếp vài khung để backend phát hiện chớp mắt.
      for (let i = 0; i < captureFrames; i++) {
        setCountdown(captureFrames - i)
        const f = grabFrame()
        if (f) frames.push(f)
        // Sequential on purpose: the frames must be spaced in time, so
        // awaiting inside the loop is the intended behaviour here.
        await sleep(frameIntervalMs)
      }
      setCountdown(0)
    }
    onCapture?.(main, frames)
  }

  return (
    <div className="rounded-2xl bg-white ring-1 ring-slate-200/70 shadow-soft overflow-hidden">
      <div className="relative bg-slate-900 aspect-[4/3] flex items-center justify-center">
        <video
          ref={videoRef}
          playsInline
          muted
          className={`w-full h-full object-cover ${active ? '' : 'hidden'}`}
          // Lật ngang cho giống gương, người dùng dễ căn mặt hơn
          style={{ transform: 'scaleX(-1)' }}
        />
        {!active && (
          <div className="text-center text-slate-400 px-6">
            <MdVideocamOff className="mx-auto text-4xl mb-2" />
            <p className="text-xs">{t('Camera đang tắt')}</p>
          </div>
        )}
        {active && (
          <div className="absolute inset-0 pointer-events-none flex items-center justify-center">
            <div className="w-40 h-52 border-2 border-emerald-400/70 rounded-[50%]" />
          </div>
        )}
        {countdown > 0 && (
          <div className="absolute inset-0 bg-black/50 flex items-center justify-center">
            <p className="text-white text-sm font-medium text-center px-4">
              {t('Giữ nguyên và chớp mắt tự nhiên…')}
            </p>
          </div>
        )}
      </div>

      <canvas ref={canvasRef} className="hidden" />

      <div className="p-3 space-y-2">
        {error && (
          <p className="text-xs text-rose-600 bg-rose-50 border border-rose-200 rounded-md p-2">{error}</p>
        )}
        <div className="flex gap-2">
          {!active ? (
            <button
              type="button"
              onClick={startCamera}
              disabled={starting}
              className="flex-1 inline-flex items-center justify-center gap-2 px-3 py-2 rounded-lg bg-slate-800 hover:bg-slate-900 text-white text-sm font-medium disabled:opacity-60"
            >
              <MdVideocam /> {starting ? t('Đang mở…') : t('Bật camera')}
            </button>
          ) : (
            <>
              <button
                type="button"
                onClick={handleCapture}
                disabled={busy || countdown > 0}
                className="flex-1 inline-flex items-center justify-center gap-2 px-3 py-2 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white text-sm font-medium disabled:opacity-60"
              >
                <MdCameraAlt /> {label || t('Chụp')}
              </button>
              <button
                type="button"
                onClick={stopCamera}
                className="inline-flex items-center justify-center gap-2 px-3 py-2 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-700 text-sm"
              >
                <MdVideocamOff /> {t('Tắt')}
              </button>
            </>
          )}
        </div>
        {active && captureFrames > 0 && (
          <p className="text-[11px] text-slate-500">
            {t('Nhìn thẳng camera và chớp mắt bình thường khi bấm chụp — hệ thống dùng để phân biệt người thật với ảnh in.')}
          </p>
        )}
      </div>
    </div>
  )
}
