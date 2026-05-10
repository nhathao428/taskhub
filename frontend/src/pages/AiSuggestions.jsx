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

const RANK_THEMES = {
  1: {
    avatar: 'bg-gradient-to-br from-yellow-400 to-orange-500',
    ring: 'ring-2 ring-yellow-300',
    quoteBg: 'from-yellow-50 to-orange-50',
    quoteIcon: 'text-yellow-300',
    medal: 'text-yellow-500',
    label: 'Vàng',
  },
  2: {
    avatar: 'bg-gradient-to-br from-slate-400 to-gray-500',
    ring: 'ring-2 ring-gray-300',
    quoteBg: 'from-gray-50 to-slate-50',
    quoteIcon: 'text-gray-300',
    medal: 'text-gray-400',
    label: 'Bạc',
  },
  3: {
    avatar: 'bg-gradient-to-br from-orange-400 to-red-500',
    ring: 'ring-2 ring-orange-300',
    quoteBg: 'from-orange-50 to-red-50',
    quoteIcon: 'text-orange-300',
    medal: 'text-orange-500',
    label: 'Đồng',
  },
}

const DEFAULT_THEME = {
  avatar: 'bg-gradient-to-br from-indigo-400 to-purple-500',
  ring: 'border border-gray-100',
  quoteBg: 'from-indigo-50 to-blue-50',
  quoteIcon: 'text-indigo-200',
}

function getInitials(first = '', last = '') {
  return (first.charAt(0) + last.charAt(0)).toUpperCase() || '?'
}

function SkeletonCard() {
  return (
    <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
      <div className="flex items-start gap-4 mb-4">
        <div className="h-12 w-12 bg-gray-200 rounded-xl animate-pulse" />
        <div className="flex-1 space-y-2">
          <div className="h-3 bg-gray-100 rounded w-12 animate-pulse" />
          <div className="h-5 bg-gray-200 rounded w-32 animate-pulse" />
          <div className="h-3 bg-gray-100 rounded w-20 animate-pulse" />
        </div>
      </div>
      <div className="bg-gray-50 rounded-xl p-4 space-y-2">
        <div className="h-3 bg-gray-200 rounded w-full animate-pulse" />
        <div className="h-3 bg-gray-200 rounded w-5/6 animate-pulse" />
        <div className="h-3 bg-gray-200 rounded w-3/4 animate-pulse" />
      </div>
    </div>
  )
}

function SuggestionCard({ emp, index }) {
  const theme = RANK_THEMES[emp.rank] || DEFAULT_THEME
  const isTop = emp.rank <= 3

  return (
    <div
      className={`relative bg-white rounded-2xl shadow-sm hover:shadow-lg transition-all duration-300 hover:-translate-y-1 p-6 h-full flex flex-col animate-fade-in-up ${
        isTop ? theme.ring : 'border border-gray-100'
      }`}
      style={{ animationDelay: `${index * 70}ms` }}
    >
      {isTop && (
        <div className="absolute -top-3 -right-3 bg-white rounded-full p-1.5 shadow-md">
          <MdEmojiEvents className={`text-2xl ${theme.medal}`} />
        </div>
      )}

      <div className="flex items-start gap-4 mb-4">
        <div
          className={`flex-shrink-0 w-12 h-12 rounded-xl ${theme.avatar} flex items-center justify-center text-white font-bold shadow-sm`}
        >
          {getInitials(emp.firstName, emp.lastName)}
        </div>
        <div className="flex-1 min-w-0">
          <div className="text-xs font-semibold text-gray-400 mb-0.5">#{emp.rank}</div>
          <h3 className="text-lg font-bold text-gray-800 truncate">
            {emp.firstName} {emp.lastName}
          </h3>
          {emp.department && <p className="text-sm text-gray-500 truncate">{emp.department}</p>}
        </div>
      </div>

      <div
        className={`relative rounded-xl p-4 flex-1 bg-gradient-to-br ${theme.quoteBg}`}
      >
        <MdFormatQuote className={`absolute top-2 left-2 text-3xl ${theme.quoteIcon}`} />
        <p className="relative text-sm text-gray-700 leading-relaxed pl-7 whitespace-pre-line">
          {emp.reasoning}
        </p>
      </div>
    </div>
  )
}

export default function AiSuggestions() {
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
      setError(err.response?.data?.message || 'Không thể lấy gợi ý.')
      setResults([])
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      {/* Hero */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-indigo-600 via-purple-600 to-pink-500 p-8 mb-6 shadow-lg">
        <div className="absolute -top-16 -right-16 w-48 h-48 bg-white/10 rounded-full blur-3xl" />
        <div className="absolute -bottom-16 -left-16 w-48 h-48 bg-white/10 rounded-full blur-3xl" />
        <div className="relative flex items-center gap-3 text-white">
          <div className="bg-white/20 backdrop-blur p-2.5 rounded-xl">
            <MdAutoAwesome className="text-3xl" />
          </div>
          <div>
            <h1 className="text-2xl font-bold">AI Gợi ý nhân viên</h1>
            <p className="text-sm text-indigo-100 mt-0.5">
              Phân tích lịch sử và đề xuất người phù hợp nhất
            </p>
          </div>
        </div>
      </div>

      {/* Form */}
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 mb-6">
        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className="flex items-center gap-2 text-sm font-medium text-gray-700 mb-2">
              <MdAssignment className="text-indigo-500" />
              Tiêu đề công việc
            </label>
            <input
              type="text"
              value={taskTitle}
              onChange={(e) => setTaskTitle(e.target.value)}
              required
              className="w-full px-4 py-3 border border-gray-200 rounded-xl text-sm bg-gray-50 focus:bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all"
            />
          </div>

          <div>
            <label className="flex items-center gap-2 text-sm font-medium text-gray-700 mb-2">
              <MdDescription className="text-indigo-500" />
              Mô tả công việc
              <span className="text-gray-400 font-normal text-xs">tùy chọn</span>
            </label>
            <textarea
              value={taskDescription}
              onChange={(e) => setTaskDescription(e.target.value)}
              rows={3}
              className="w-full px-4 py-3 border border-gray-200 rounded-xl text-sm bg-gray-50 focus:bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all resize-none"
            />
          </div>

          <button
            type="submit"
            disabled={loading || !taskTitle.trim()}
            className="group flex items-center justify-center gap-2 w-full sm:w-auto bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700 disabled:from-gray-300 disabled:to-gray-400 disabled:cursor-not-allowed text-white px-6 py-3 rounded-xl text-sm font-semibold shadow-md hover:shadow-lg transition-all"
          >
            {loading ? (
              <>
                <div className="animate-spin rounded-full h-4 w-4 border-2 border-white/30 border-t-white" />
                Đang phân tích...
              </>
            ) : (
              <>
                <MdAutoAwesome className="text-lg" />
                Phân tích bằng AI
                <MdArrowForward className="transition-transform group-hover:translate-x-0.5" />
              </>
            )}
          </button>
        </form>
      </div>

      {/* Error */}
      {error && (
        <div className="mb-4 p-4 bg-red-50 border border-red-200 text-red-700 rounded-xl text-sm flex items-start gap-2">
          <span className="font-medium">Lỗi:</span>
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
          <div className="inline-flex w-20 h-20 rounded-full bg-gray-100 items-center justify-center mb-4">
            <MdSearchOff className="text-4xl text-gray-400" />
          </div>
          <p className="font-medium text-gray-700">Không tìm thấy nhân viên phù hợp</p>
          <p className="text-sm text-gray-500 mt-1">Hãy thử mô tả task chi tiết hơn</p>
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
