import { useState } from 'react'
import { MdAdd, MdClose, MdSmartToy } from 'react-icons/md'
import api from '../api/axios'

function ProgressBar({ label, value }) {
  const pct = Math.round((value || 0) * 100)
  return (
    <div className="mb-2">
      <div className="flex justify-between text-xs text-gray-600 mb-0.5">
        <span>{label}</span>
        <span className="font-medium">{pct}%</span>
      </div>
      <div className="w-full bg-gray-200 rounded-full h-2">
        <div
          className="bg-indigo-500 h-2 rounded-full transition-all duration-500"
          style={{ width: `${pct}%` }}
        />
      </div>
    </div>
  )
}

function SkeletonCard() {
  return (
    <div className="bg-white rounded-xl shadow-md p-6 border border-gray-100 animate-pulse">
      <div className="flex items-start justify-between mb-4">
        <div className="space-y-2">
          <div className="h-5 bg-gray-200 rounded w-32" />
          <div className="h-3 bg-gray-100 rounded w-20" />
        </div>
        <div className="h-10 w-10 bg-gray-200 rounded-full" />
      </div>
      <div className="space-y-3 mb-4">
        {[1, 2, 3, 4].map((i) => (
          <div key={i} className="space-y-1">
            <div className="h-3 bg-gray-100 rounded w-full" />
            <div className="h-2 bg-gray-200 rounded w-full" />
          </div>
        ))}
      </div>
      <div className="h-12 bg-indigo-50 rounded-lg" />
    </div>
  )
}

function SuggestionCard({ emp }) {
  const overallPct = Math.round((emp.overallScore || 0) * 100)
  return (
    <div className="bg-white rounded-xl shadow-md p-6 border border-gray-100 hover:shadow-lg transition-shadow">
      <div className="flex items-start justify-between mb-4">
        <div>
          <h3 className="text-lg font-bold text-gray-800">
            {emp.firstName} {emp.lastName}
          </h3>
          <p className="text-sm text-gray-500">{emp.department}</p>
        </div>
        <div className="text-center">
          <div
            className={`text-2xl font-bold ${
              overallPct >= 80
                ? 'text-green-600'
                : overallPct >= 60
                ? 'text-yellow-600'
                : 'text-red-500'
            }`}
          >
            {overallPct}%
          </div>
          <p className="text-xs text-gray-500">Điểm tổng</p>
        </div>
      </div>

      <div className="mb-4">
        <ProgressBar label="Kỹ năng phù hợp" value={emp.skillMatchScore} />
        <ProgressBar label="Khối lượng công việc" value={emp.workloadScore} />
        <ProgressBar label="Hiệu suất" value={emp.performanceScore} />
        <ProgressBar label="Chuyên cần" value={emp.attendanceScore} />
      </div>

      {emp.reasoning && (
        <div className="bg-indigo-50 border border-indigo-100 rounded-lg p-3">
          <p className="text-xs text-indigo-700 leading-relaxed">{emp.reasoning}</p>
        </div>
      )}
    </div>
  )
}

export default function AiSuggestions() {
  const [taskTitle, setTaskTitle] = useState('')
  const [taskDescription, setTaskDescription] = useState('')
  const [skillInput, setSkillInput] = useState('')
  const [skills, setSkills] = useState([])
  const [results, setResults] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [searched, setSearched] = useState(false)

  const addSkill = (e) => {
    if (e.key === 'Enter' || e.key === ',') {
      e.preventDefault()
      const s = skillInput.trim()
      if (s && !skills.includes(s)) {
        setSkills([...skills, s])
      }
      setSkillInput('')
    }
  }

  const removeSkill = (skill) => {
    setSkills(skills.filter((s) => s !== skill))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    setSearched(true)
    try {
      const res = await api.post('/api/suggestions/recommend', {
        taskTitle,
        taskDescription,
        requiredSkills: skills,
      })
      setResults(res.data || [])
    } catch (err) {
      setError(
        err.response?.data?.message || 'Không thể lấy gợi ý. Vui lòng thử lại.'
      )
      setResults([])
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <div className="flex items-center gap-3 mb-6">
        <MdSmartToy className="text-3xl text-indigo-600" />
        <h1 className="text-2xl font-bold text-gray-800">AI Gợi ý nhân viên</h1>
      </div>

      <div className="bg-white rounded-xl shadow-md p-6 mb-6">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Tiêu đề công việc
            </label>
            <input
              type="text"
              value={taskTitle}
              onChange={(e) => setTaskTitle(e.target.value)}
              required
              placeholder="VD: Phát triển API RESTful cho ứng dụng di động"
              className="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Mô tả công việc
              <span className="ml-1 text-gray-400 font-normal">(tùy chọn — giúp AI gợi ý chính xác hơn)</span>
            </label>
            <textarea
              value={taskDescription}
              onChange={(e) => setTaskDescription(e.target.value)}
              rows={3}
              placeholder="VD: Xây dựng các endpoint REST API với Spring Boot, tích hợp JWT authentication và kết nối PostgreSQL..."
              className="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 resize-none"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Kỹ năng yêu cầu
              <span className="ml-1 text-gray-400 font-normal">(nhấn Enter hoặc dấu phẩy để thêm)</span>
            </label>
            <div className="flex flex-wrap gap-2 p-3 border border-gray-300 rounded-lg min-h-[44px] focus-within:ring-2 focus-within:ring-indigo-500 focus-within:border-transparent">
              {skills.map((skill) => (
                <span
                  key={skill}
                  className="inline-flex items-center gap-1 bg-indigo-100 text-indigo-700 text-sm px-3 py-1 rounded-full"
                >
                  {skill}
                  <button type="button" onClick={() => removeSkill(skill)} className="hover:text-indigo-900">
                    <MdClose className="text-base" />
                  </button>
                </span>
              ))}
              <input
                type="text"
                value={skillInput}
                onChange={(e) => setSkillInput(e.target.value)}
                onKeyDown={addSkill}
                placeholder={skills.length === 0 ? 'VD: Java, React, SQL...' : ''}
                className="flex-1 min-w-[120px] outline-none text-sm bg-transparent"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-400 text-white px-6 py-2.5 rounded-lg text-sm font-medium transition-colors"
          >
            {loading ? (
              <>
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white" />
                AI đang phân tích...
              </>
            ) : (
              <>
                <MdSmartToy className="text-xl" />
                Gợi ý nhân viên phù hợp
              </>
            )}
          </button>
        </form>
      </div>

      {error && (
        <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 rounded-lg text-sm">
          {error}
        </div>
      )}

      {loading && (
        <div>
          <h2 className="text-lg font-semibold text-gray-700 mb-4">AI đang phân tích dữ liệu...</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[1, 2, 3].map((i) => (
              <SkeletonCard key={i} />
            ))}
          </div>
        </div>
      )}

      {!loading && searched && results.length === 0 && !error && (
        <div className="text-center py-12 text-gray-400">
          <MdSmartToy className="text-5xl mx-auto mb-2 opacity-30" />
          <p>Không tìm thấy nhân viên phù hợp.</p>
        </div>
      )}

      {!loading && results.length > 0 && (
        <div>
          <h2 className="text-lg font-semibold text-gray-700 mb-4">
            Top {results.length} nhân viên phù hợp
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {results.slice(0, 5).map((emp, idx) => (
              <div key={emp.employeeId || idx} className="relative">
                {idx === 0 && (
                  <div className="absolute -top-2 -right-2 z-10 bg-yellow-400 text-yellow-900 text-xs font-bold px-2 py-0.5 rounded-full">
                    Tốt nhất
                  </div>
                )}
                <SuggestionCard emp={emp} />
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
