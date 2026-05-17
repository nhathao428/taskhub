import { useEffect, useState } from 'react'
import { Pie, Bar } from 'react-chartjs-2'
import {
  Chart as ChartJS,
  ArcElement,
  Tooltip,
  Legend,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
} from 'chart.js'
import {
  MdPeople,
  MdFolder,
  MdCheckCircle,
  MdAccessTime,
  MdTrendingUp,
  MdOutlinePieChart,
  MdOutlineBarChart,
  MdAssignmentTurnedIn,
  MdPlayArrow,
  MdHourglassEmpty,
  MdEventAvailable,
} from 'react-icons/md'
import api from '../api/axios'
import { useAuth } from '../context/AuthContext'
import { useTranslation } from '../context/LanguageContext'

ChartJS.register(ArcElement, Tooltip, Legend, CategoryScale, LinearScale, BarElement, Title)

function StatCard({ icon: Icon, label, value, gradient, accent }) {
  return (
    <div className="relative overflow-hidden bg-white rounded-2xl shadow-sm border border-gray-100 p-5 hover:shadow-md transition-shadow">
      <div
        className={`absolute -top-4 -right-4 w-24 h-24 rounded-full opacity-10 blur-2xl ${accent}`}
      />
      <div className="relative flex items-start justify-between">
        <div>
          <p className="text-xs font-medium text-gray-500 uppercase tracking-wide mb-1">
            {label}
          </p>
          <p className="text-3xl font-bold text-gray-800">{value}</p>
        </div>
        <div className={`p-2.5 rounded-xl bg-gradient-to-br ${gradient} shadow-md`}>
          <Icon className="text-white text-xl" />
        </div>
      </div>
    </div>
  )
}

function ChartCard({ title, icon: Icon, children, empty }) {
  return (
    <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
      <div className="flex items-center gap-2 mb-4">
        <Icon className="text-indigo-500 text-xl" />
        <h2 className="text-base font-semibold text-gray-700">{title}</h2>
      </div>
      {empty ? (
        <p className="text-gray-400 text-center py-10 text-sm">{empty}</p>
      ) : (
        children
      )}
    </div>
  )
}

export default function Dashboard() {
  const { user } = useAuth()
  const role = user?.role || 'EMPLOYEE'
  if (role === 'EMPLOYEE') {
    return <EmployeeDashboard />
  }
  return <ManagerDashboard />
}

function ManagerDashboard() {
  const { t } = useTranslation()
  const [stats, setStats] = useState({ employees: 0, projects: 0, tasks: 0, todayAttendance: 0 })
  const [tasks, setTasks] = useState([])
  const [employees, setEmployees] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [empRes, projRes, taskRes, attRes] = await Promise.all([
          api.get('/api/employees'),
          api.get('/api/projects'),
          api.get('/api/tasks'),
          api.get('/api/attendance'),
        ])

        const today = new Date().toISOString().split('T')[0]
        const todayCount = (attRes.data?.data ?? attRes.data ?? []).filter((a) => {
          const d = a.date ? a.date.split('T')[0] : ''
          return d === today
        }).length

        setStats({
          employees: (empRes.data?.data ?? empRes.data ?? []).length,
          projects: (projRes.data?.data ?? projRes.data ?? []).length,
          tasks: (taskRes.data?.data ?? taskRes.data ?? []).length,
          todayAttendance: todayCount,
        })
        setTasks(taskRes.data?.data ?? taskRes.data ?? [])
        setEmployees(empRes.data?.data ?? empRes.data ?? [])
      } catch (err) {
        console.error('Lỗi khi tải dữ liệu dashboard:', err)
      } finally {
        setLoading(false)
      }
    }
    fetchData()
  }, [])

  const completedCount = tasks.filter((t) =>
    ['COMPLETED', 'Completed', 'completed'].includes(t.status)
  ).length
  const completionRate = tasks.length > 0 ? Math.round((completedCount / tasks.length) * 100) : 0

  const taskStatusData = {
    labels: [t('Chờ xử lý'), t('Đang thực hiện'), t('Hoàn thành')],
    datasets: [
      {
        data: [
          tasks.filter((t) => ['PENDING', 'Pending', 'pending'].includes(t.status)).length,
          tasks.filter((t) => ['IN_PROGRESS', 'In Progress', 'in_progress'].includes(t.status)).length,
          completedCount,
        ],
        backgroundColor: ['#f59e0b', '#3b82f6', '#10b981'],
        borderWidth: 0,
        hoverOffset: 8,
      },
    ],
  }

  const deptMap = employees.reduce((acc, emp) => {
    const dept = emp.department || t('Khác')
    acc[dept] = (acc[dept] || 0) + 1
    return acc
  }, {})

  const deptData = {
    labels: Object.keys(deptMap),
    datasets: [
      {
        label: t('Số nhân viên'),
        data: Object.values(deptMap),
        backgroundColor: '#6366f1',
        borderRadius: 8,
        maxBarThickness: 40,
      },
    ],
  }

  const barOptions = {
    responsive: true,
    plugins: { legend: { display: false }, title: { display: false } },
    scales: { y: { beginAtZero: true, ticks: { stepSize: 1 } } },
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-4 border-indigo-100 border-t-indigo-600" />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Welcome + completion banner */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-indigo-600 via-purple-600 to-pink-500 p-6 text-white shadow-lg">
        <div className="absolute -top-16 -right-16 w-48 h-48 bg-white/10 rounded-full blur-3xl" />
        <div className="absolute -bottom-16 -left-16 w-48 h-48 bg-white/10 rounded-full blur-3xl" />
        <div className="relative flex items-center justify-between flex-wrap gap-4">
          <div>
            <h2 className="text-xl font-bold">{t('Tổng quan hệ thống')}</h2>
            <p className="text-sm text-indigo-100 mt-1">
              {t('{tasks} công việc đã được tạo, {done} đã hoàn thành', {
                tasks: tasks.length,
                done: completedCount,
              })}
            </p>
          </div>
          <div className="flex items-center gap-3 bg-white/15 backdrop-blur rounded-xl px-4 py-2.5">
            <MdTrendingUp className="text-2xl" />
            <div>
              <p className="text-xs text-indigo-100">{t('Tỷ lệ hoàn thành')}</p>
              <p className="text-2xl font-bold leading-tight">{completionRate}%</p>
            </div>
          </div>
        </div>
      </div>

      {/* Stat cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          icon={MdPeople}
          label={t('Nhân viên')}
          value={stats.employees}
          gradient="from-indigo-500 to-blue-500"
          accent="bg-indigo-400"
        />
        <StatCard
          icon={MdFolder}
          label={t('Dự án')}
          value={stats.projects}
          gradient="from-blue-500 to-cyan-500"
          accent="bg-blue-400"
        />
        <StatCard
          icon={MdCheckCircle}
          label={t('Công việc')}
          value={stats.tasks}
          gradient="from-emerald-500 to-green-500"
          accent="bg-emerald-400"
        />
        <StatCard
          icon={MdAccessTime}
          label={t('Chấm công hôm nay')}
          value={stats.todayAttendance}
          gradient="from-amber-500 to-orange-500"
          accent="bg-amber-400"
        />
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <ChartCard
          title={t('Phân bổ trạng thái công việc')}
          icon={MdOutlinePieChart}
          empty={tasks.length === 0 ? t('Chưa có công việc') : null}
        >
          <div className="flex justify-center">
            <div style={{ maxWidth: '280px', width: '100%' }}>
              <Pie data={taskStatusData} />
            </div>
          </div>
        </ChartCard>

        <ChartCard
          title={t('Nhân viên theo phòng ban')}
          icon={MdOutlineBarChart}
          empty={Object.keys(deptMap).length === 0 ? t('Chưa có dữ liệu nhân viên') : null}
        >
          <Bar data={deptData} options={barOptions} />
        </ChartCard>
      </div>
    </div>
  )
}

function EmployeeDashboard() {
  const { user } = useAuth()
  const { t } = useTranslation()
  const [tasks, setTasks] = useState([])
  const [attendance, setAttendance] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [tRes, aRes] = await Promise.all([
          api.get('/api/tasks/me').catch(() => ({ data: { data: [] } })),
          api.get('/api/attendance/me').catch(() => ({ data: { data: [] } })),
        ])
        setTasks(tRes.data?.data ?? tRes.data ?? [])
        setAttendance(aRes.data?.data ?? aRes.data ?? [])
      } finally {
        setLoading(false)
      }
    }
    fetchData()
  }, [])

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-4 border-indigo-100 border-t-indigo-600" />
      </div>
    )
  }

  const pendingCount = tasks.filter((t) => (t.status || '').toLowerCase() === 'pending').length
  const inProgressCount = tasks.filter((t) => (t.status || '').toLowerCase() === 'in_progress').length
  const completedCount = tasks.filter((t) => (t.status || '').toLowerCase() === 'completed').length
  const completionRate = tasks.length > 0 ? Math.round((completedCount / tasks.length) * 100) : 0

  const today = new Date().toISOString().split('T')[0]
  const monthPrefix = today.substring(0, 7)
  const attendanceThisMonth = attendance.filter((a) => (a.date || '').startsWith(monthPrefix)).length

  const myTaskStatusData = {
    labels: [t('Chờ xử lý'), t('Đang thực hiện'), t('Hoàn thành')],
    datasets: [
      {
        data: [pendingCount, inProgressCount, completedCount],
        backgroundColor: ['#f59e0b', '#3b82f6', '#10b981'],
        borderWidth: 0,
        hoverOffset: 8,
      },
    ],
  }

  const upcoming = [...tasks]
    .filter((t) => t.dueDate && (t.status || '').toLowerCase() !== 'completed')
    .sort((a, b) => String(a.dueDate).localeCompare(String(b.dueDate)))
    .slice(0, 5)

  return (
    <div className="space-y-6">
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-emerald-600 via-teal-600 to-cyan-500 p-6 text-white shadow-lg">
        <div className="absolute -top-16 -right-16 w-48 h-48 bg-white/10 rounded-full blur-3xl" />
        <div className="relative flex items-center justify-between flex-wrap gap-4">
          <div>
            <h2 className="text-xl font-bold">{t('Xin chào, {name}!', { name: user?.username })}</h2>
            <p className="text-sm text-emerald-50 mt-1">
              {t('Bạn có {tasks} công việc được giao, {done} đã hoàn thành.', {
                tasks: tasks.length,
                done: completedCount,
              })}
            </p>
          </div>
          <div className="flex items-center gap-3 bg-white/15 backdrop-blur rounded-xl px-4 py-2.5">
            <MdTrendingUp className="text-2xl" />
            <div>
              <p className="text-xs text-emerald-50">{t('Tỷ lệ hoàn thành')}</p>
              <p className="text-2xl font-bold leading-tight">{completionRate}%</p>
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard icon={MdHourglassEmpty} label={t('Chờ xử lý')} value={pendingCount} gradient="from-yellow-500 to-amber-500" accent="bg-yellow-400" />
        <StatCard icon={MdPlayArrow} label={t('Đang thực hiện')} value={inProgressCount} gradient="from-blue-500 to-indigo-500" accent="bg-blue-400" />
        <StatCard icon={MdAssignmentTurnedIn} label={t('Đã hoàn thành')} value={completedCount} gradient="from-emerald-500 to-green-500" accent="bg-emerald-400" />
        <StatCard icon={MdEventAvailable} label={t('Chấm công tháng này')} value={attendanceThisMonth} gradient="from-rose-500 to-pink-500" accent="bg-rose-400" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <ChartCard title={t('Công việc của tôi theo trạng thái')} icon={MdOutlinePieChart} empty={tasks.length === 0 ? t('Bạn chưa có công việc nào') : null}>
          <div className="flex justify-center">
            <div style={{ maxWidth: '280px', width: '100%' }}>
              <Pie data={myTaskStatusData} />
            </div>
          </div>
        </ChartCard>

        <ChartCard title={t('Công việc sắp đến hạn')} icon={MdAccessTime} empty={upcoming.length === 0 ? t('Không có công việc nào sắp đến hạn') : null}>
          <ul className="divide-y divide-gray-100">
            {upcoming.map((t) => (
              <li key={t.taskId ?? t.id} className="py-2 flex items-center justify-between">
                <div className="min-w-0">
                  <p className="text-sm font-medium text-gray-800 truncate">{t.title}</p>
                  <p className="text-xs text-gray-500 truncate">{t.requiredSkills || t.description || '—'}</p>
                </div>
                <span className="ml-3 px-2 py-0.5 rounded-full text-xs font-medium bg-indigo-100 text-indigo-700 whitespace-nowrap">
                  {String(t.dueDate).split('T')[0]}
                </span>
              </li>
            ))}
          </ul>
        </ChartCard>
      </div>
    </div>
  )
}
