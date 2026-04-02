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
import { MdPeople, MdFolder, MdCheckCircle, MdAccessTime } from 'react-icons/md'
import api from '../api/axios'

ChartJS.register(ArcElement, Tooltip, Legend, CategoryScale, LinearScale, BarElement, Title)

function StatCard({ icon: Icon, label, value, color }) {
  return (
    <div className="bg-white rounded-xl shadow-md p-6 flex items-center gap-4">
      <div className={`p-3 rounded-full ${color}`}>
        <Icon className="text-white text-2xl" />
      </div>
      <div>
        <p className="text-sm text-gray-500">{label}</p>
        <p className="text-2xl font-bold text-gray-800">{value}</p>
      </div>
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

  const taskStatusData = {
    labels: ['Chờ xử lý', 'Đang thực hiện', 'Hoàn thành'],
    datasets: [
      {
        data: [
          tasks.filter((t) => ['PENDING', 'Pending', 'pending'].includes(t.status)).length,
          tasks.filter((t) => ['IN_PROGRESS', 'In Progress', 'in_progress'].includes(t.status)).length,
          tasks.filter((t) => ['COMPLETED', 'Completed', 'completed'].includes(t.status)).length,
        ],
        backgroundColor: ['#f59e0b', '#3b82f6', '#10b981'],
        borderWidth: 0,
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
        borderRadius: 6,
      },
    ],
  }

  const barOptions = {
    responsive: true,
    plugins: {
      legend: { display: false },
      title: { display: false },
    },
    scales: {
      y: { beginAtZero: true, ticks: { stepSize: 1 } },
    },
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
      </div>
    )
  }

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-800 mb-6">Bảng điều khiển</h1>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <StatCard icon={MdPeople} label="Tổng nhân viên" value={stats.employees} color="bg-indigo-500" />
        <StatCard icon={MdFolder} label="Tổng dự án" value={stats.projects} color="bg-blue-500" />
        <StatCard icon={MdCheckCircle} label="Tổng công việc" value={stats.tasks} color="bg-green-500" />
        <StatCard icon={MdAccessTime} label="Chấm công hôm nay" value={stats.todayAttendance} color="bg-amber-500" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-xl shadow-md p-6">
          <h2 className="text-lg font-semibold text-gray-700 mb-4">Phân bổ trạng thái công việc</h2>
          <div className="flex justify-center">
            <div style={{ maxWidth: '280px', width: '100%' }}>
              <Pie data={taskStatusData} />
            </div>
          </div>
        </div>

        <div className="bg-white rounded-xl shadow-md p-6">
          <h2 className="text-lg font-semibold text-gray-700 mb-4">Nhân viên theo phòng ban</h2>
          {Object.keys(deptMap).length > 0 ? (
            <Bar data={deptData} options={barOptions} />
          ) : (
            <p className="text-gray-400 text-center py-10">Chưa có dữ liệu nhân viên</p>
          )}
        </div>
      </div>
    </div>
  )
}
