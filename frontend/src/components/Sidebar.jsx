import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import {
  MdDashboard,
  MdPeople,
  MdFolder,
  MdCheckCircle,
  MdAccessTime,
  MdSmartToy,
  MdLogout,
} from 'react-icons/md'

const navItems = [
  { to: '/dashboard', icon: MdDashboard, label: 'Bảng điều khiển' },
  { to: '/employees', icon: MdPeople, label: 'Nhân viên' },
  { to: '/projects', icon: MdFolder, label: 'Dự án' },
  { to: '/tasks', icon: MdCheckCircle, label: 'Công việc' },
  { to: '/attendance', icon: MdAccessTime, label: 'Chấm công' },
  { to: '/ai-suggestions', icon: MdSmartToy, label: 'AI Gợi ý' },
]

export default function Sidebar() {
  const { logout, user } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div className="w-64 bg-gray-900 text-white flex flex-col min-h-screen">
      <div className="p-6 border-b border-gray-700">
        <h1 className="text-lg font-bold text-white leading-tight">Quản lý</h1>
        <p className="text-sm font-bold text-white leading-tight">Công việc</p>
        {user && (
          <p className="text-xs text-gray-400 mt-2 truncate">Xin chào, {user.username}</p>
        )}
      </div>

      <nav className="flex-1 py-4">
        {navItems.map(({ to, icon: Icon, label }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) =>
              `flex items-center gap-3 px-6 py-3 text-sm transition-colors duration-150 ${
                isActive
                  ? 'bg-indigo-600 text-white'
                  : 'text-gray-300 hover:bg-gray-800 hover:text-white'
              }`
            }
          >
            <Icon className="text-xl flex-shrink-0" />
            <span>{label}</span>
          </NavLink>
        ))}
      </nav>

      <div className="p-4 border-t border-gray-700">
        <button
          onClick={handleLogout}
          className="flex items-center gap-3 w-full px-4 py-2 text-sm text-gray-300 hover:bg-gray-800 hover:text-white rounded-lg transition-colors duration-150"
        >
          <MdLogout className="text-xl" />
          <span>Đăng xuất</span>
        </button>
      </div>
    </div>
  )
}
