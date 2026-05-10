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
} from 'react-icons/md'
import api from '../api/axios'

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
    labels: ['Chờ xử lý', 'Đang thực hiện', 'Hoàn thành'],
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
    const dept = emp.department || 'Khác'
    acc[dept] = (acc[dept] || 0) + 1
    return acc
  }, {})

  const deptData = {
    labels: Object.keys(deptMap),
    datasets: [
      {
        label: 'Số nhân viên',
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
            <h2 className="text-xl font-bold">Tổng quan hệ thống</h2>
            <p className="text-sm text-indigo-100 mt-1">
              {tasks.length} công việc đã được tạo, {completedCount} đã hoàn thành
            </p>
          </div>
          <div className="flex items-center gap-3 bg-white/15 backdrop-blur rounded-xl px-4 py-2.5">
            <MdTrendingUp className="text-2xl" />
            <div>
              <p className="text-xs text-indigo-100">Tỷ lệ hoàn thành</p>
              <p className="text-2xl font-bold leading-tight">{completionRate}%</p>
            </div>
          </div>
        </div>
      </div>

      {/* Stat cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          icon={MdPeople}
          label="Nhân viên"
          value={stats.employees}
          gradient="from-indigo-500 to-blue-500"
          accent="bg-indigo-400"
        />
        <StatCard
          icon={MdFolder}
          label="Dự án"
          value={stats.projects}
          gradient="from-blue-500 to-cyan-500"
          accent="bg-blue-400"
        />
        <StatCard
          icon={MdCheckCircle}
          label="Công việc"
          value={stats.tasks}
          gradient="from-emerald-500 to-green-500"
          accent="bg-emerald-400"
        />
        <StatCard
          icon={MdAccessTime}
          label="Chấm công hôm nay"
          value={stats.todayAttendance}
          gradient="from-amber-500 to-orange-500"
          accent="bg-amber-400"
        />
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <ChartCard
          title="Phân bổ trạng thái công việc"
          icon={MdOutlinePieChart}
          empty={tasks.length === 0 ? 'Chưa có công việc' : null}
        >
          <div className="flex justify-center">
            <div style={{ maxWidth: '280px', width: '100%' }}>
              <Pie data={taskStatusData} />
            </div>
          </div>
        </ChartCard>

        <ChartCard
          title="Nhân viên theo phòng ban"
          icon={MdOutlineBarChart}
          empty={Object.keys(deptMap).length === 0 ? 'Chưa có dữ liệu nhân viên' : null}
        >
          <Bar data={deptData} options={barOptions} />
        </ChartCard>
      </div>
    </div>
  )
}
