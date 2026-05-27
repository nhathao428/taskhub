import { useState } from 'react'
import {
  MdAutoAwesome,
  MdEmojiEvents,
  MdFormatQuote,
  MdAssignment,
  MdDescription,
  MdArrowForward,
  MdSearchOff,
} from 'react-icons/md'
import api from '../api/axios'
import { useTranslation } from '../context/LanguageContext'

const RANK_THEMES = {
  1: {
    avatar: 'bg-gradient-to-br from-yellow-400 to-orange-500',
    ring: 'ring-2 ring-yellow-300',
    quoteBg: 'from-yellow-50 to-orange-50',
    quoteIcon: 'text-yellow-300',
    medal: 'text-yellow-500',
  },
  2: {
    avatar: 'bg-gradient-to-br from-slate-400 to-gray-500',
    ring: 'ring-2 ring-gray-300',
    quoteBg: 'from-gray-50 to-slate-50',
    quoteIcon: 'text-gray-300',
    medal: 'text-gray-400',
  },
  3: {
    avatar: 'bg-gradient-to-br from-orange-400 to-red-500',
    ring: 'ring-2 ring-orange-300',
    quoteBg: 'from-orange-50 to-red-50',
    quoteIcon: 'text-orange-300',
    medal: 'text-orange-500',
  },
}

const DEFAULT_THEME = {
  avatar: 'bg-brand-600',
  ring: 'ring-1 ring-slate-200/70',
  quoteBg: 'from-brand-50 to-sky-50',
  quoteIcon: 'text-brand-200',
}

function getInitials(first = '', last = '') {
  return (first.charAt(0) + last.charAt(0)).toUpperCase() || '?'
}

function SkeletonCard() {
  return (
    <div className="bg-white rounded-2xl shadow-soft ring-1 ring-slate-200/70 p-6">
      <div className="flex items-start gap-4 mb-4">
        <div className="h-12 w-12 bg-slate-200 rounded-xl animate-pulse" />
        <div className="flex-1 space-y-2">
          <div className="h-3 bg-slate-100 rounded w-12 animate-pulse" />
          <div className="h-5 bg-slate-200 rounded w-32 animate-pulse" />
          <div className="h-3 bg-slate-100 rounded w-20 animate-pulse" />
        </div>
      </div>
      <div className="bg-slate-50 rounded-xl p-4 space-y-2">
        <div className="h-3 bg-slate-200 rounded w-full animate-pulse" />
        <div className="h-3 bg-slate-200 rounded w-5/6 animate-pulse" />
        <div className="h-3 bg-slate-200 rounded w-3/4 animate-pulse" />
      </div>
    </div>
  )
}

function SuggestionCard({ emp, index }) {
  const theme = RANK_THEMES[emp.rank] || DEFAULT_THEME
  const isTop = emp.rank <= 3

  return (
    <div
      className={`relative bg-white rounded-2xl shadow-soft hover:shadow-soft-lg transition-all duration-300 hover:-translate-y-1 p-6 h-full flex flex-col animate-fade-in-up ${
        isTop ? theme.ring : 'ring-1 ring-slate-200/70'
      }`}
      style={{ animationDelay: `${index * 70}ms` }}
    >
      {isTop && (
        <div className="absolute -top-3 -right-3 bg-white rounded-full p-1.5 shadow-soft-md">
          <MdEmojiEvents className={`text-2xl ${theme.medal}`} />
        </div>
      )}

      <div className="flex items-start gap-4 mb-4">
        <div
          className={`flex-shrink-0 w-12 h-12 rounded-xl ${theme.avatar} flex items-center justify-center text-white font-bold shadow-soft ring-1 ring-white/10`}
        >
          {getInitials(emp.firstName, emp.lastName)}
        </div>
        <div className="flex-1 min-w-0">
          <div className="text-[11px] font-semibold text-slate-400 uppercase tracking-[0.12em] mb-0.5">#{emp.rank}</div>
          <h3 className="text-lg font-bold text-slate-900 truncate tracking-tight">
            {emp.firstName} {emp.lastName}
          </h3>
          {emp.department && <p className="text-sm text-slate-500 truncate">{emp.department}</p>}
        </div>
      </div>

      <div
        className={`relative rounded-xl p-4 flex-1 bg-gradient-to-br ${theme.quoteBg}`}
      >
        <MdFormatQuote className={`absolute top-2 left-2 text-3xl ${theme.quoteIcon}`} />
        <p className="relative text-sm text-slate-700 leading-relaxed pl-7 whitespace-pre-line">
          {emp.reasoning}
        </p>
      </div>
    </div>
  )
}

export default function AiSuggestions() {
  const { t } = useTranslation()
  const [taskTitle, setTaskTitle] = useState('')
  const [taskDescription, setTaskDescription] = useState('')
  const [results, setResults] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [searched, setSearched] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    setSearched(true)
    try {
      const res = await api.post('/api/suggestions/recommend', { taskTitle, taskDescription })
      setResults(res.data?.data ?? res.data ?? [])
    } catch (err) {
      setError(err.response?.data?.message || t('Không thể lấy gợi ý.'))
      setResults([])
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      {/* Hero */}
      <div className="relative overflow-hidden rounded-2xl p-8 mb-6 text-white shadow-brand-glow bg-slate-900">
        {/* Aurora mesh */}
        <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top_left,_rgba(99,102,241,0.85),transparent_55%),radial-gradient(ellipse_at_top_right,_rgba(217,70,239,0.55),transparent_55%),radial-gradient(ellipse_at_bottom_right,_rgba(56,189,248,0.45),transparent_55%)]" />
        <div className="absolute -top-24 -right-24 w-72 h-72 bg-fuchsia-500/30 rounded-full blur-3xl" />
        <div className="absolute -bottom-20 -left-20 w-72 h-72 bg-brand-500/30 rounded-full blur-3xl" />
        <div className="absolute inset-0 opacity-[0.04] bg-[linear-gradient(rgba(255,255,255,0.6)_1px,transparent_1px),linear-gradient(90deg,rgba(255,255,255,0.6)_1px,transparent_1px)] bg-[size:32px_32px]" />
        <div className="relative flex items-center gap-3">
          <div className="bg-white/15 backdrop-blur p-2.5 rounded-xl ring-1 ring-white/20">
            <MdAutoAwesome className="text-3xl" />
          </div>
          <div>
            <h1 className="text-2xl font-bold tracking-tight">{t('AI Gợi ý nhân viên')}</h1>
            <p className="text-sm text-brand-100 mt-0.5">
              {t('Phân tích lịch sử và đề xuất người phù hợp nhất')}
            </p>
          </div>
        </div>
      </div>

      {/* Form */}
      <div className="bg-white rounded-2xl shadow-soft ring-1 ring-slate-200/70 p-6 mb-6">
        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className="flex items-center gap-2 text-sm font-medium text-slate-700 mb-2">
              <MdAssignment className="text-brand-600" />
              {t('Tiêu đề công việc')}
            </label>
            <input
              type="text"
              value={taskTitle}
              onChange={(e) => setTaskTitle(e.target.value)}
              required
              className="w-full px-4 py-3 border border-slate-200 rounded-xl text-sm bg-slate-50 focus:bg-white focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent transition-all"
            />
          </div>

          <div>
            <label className="flex items-center gap-2 text-sm font-medium text-slate-700 mb-2">
              <MdDescription className="text-brand-600" />
              {t('Mô tả công việc')}
              <span className="text-slate-400 font-normal text-xs">{t('tùy chọn')}</span>
            </label>
            <textarea
              value={taskDescription}
              onChange={(e) => setTaskDescription(e.target.value)}
              rows={3}
              className="w-full px-4 py-3 border border-slate-200 rounded-xl text-sm bg-slate-50 focus:bg-white focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent transition-all resize-none"
            />
          </div>

          <button
            type="submit"
            disabled={loading || !taskTitle.trim()}
            className="group flex items-center justify-center gap-2 w-full sm:w-auto bg-brand-600 hover:bg-brand-700 disabled:bg-slate-300 disabled:cursor-not-allowed text-white px-6 py-3 rounded-xl text-sm font-semibold shadow-brand-glow disabled:shadow-none transition-colors"
          >
            {loading ? (
              <>
                <div className="animate-spin rounded-full h-4 w-4 border-2 border-white/30 border-t-white" />
                {t('Đang phân tích...')}
              </>
            ) : (
              <>
                <MdAutoAwesome className="text-lg" />
                {t('Phân tích bằng AI')}
                <MdArrowForward className="transition-transform group-hover:translate-x-0.5" />
              </>
            )}
          </button>
        </form>
      </div>

      {/* Error */}
      {error && (
        <div className="mb-4 p-4 bg-red-50 border border-red-200 text-red-700 rounded-xl text-sm flex items-start gap-2">
          <span className="font-medium">{t('Lỗi:')}</span>
          <span>{error}</span>
        </div>
      )}

      {/* Loading */}
      {loading && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[1, 2, 3].map((i) => (
            <SkeletonCard key={i} />
          ))}
        </div>
      )}

      {/* Empty */}
      {!loading && searched && !error && results.length === 0 && (
        <div className="text-center py-16">
          <div className="inline-flex w-20 h-20 rounded-full bg-slate-100 items-center justify-center mb-4">
            <MdSearchOff className="text-4xl text-slate-400" />
          </div>
          <p className="font-medium text-slate-700">{t('Không tìm thấy nhân viên phù hợp')}</p>
          <p className="text-sm text-slate-500 mt-1">{t('Hãy thử mô tả task chi tiết hơn')}</p>
        </div>
      )}

      {/* Results */}
      {!loading && results.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {results.map((emp, idx) => (
            <SuggestionCard key={emp.employeeId} emp={emp} index={idx} />
          ))}
        </div>
      )}
    </div>
  )
}
