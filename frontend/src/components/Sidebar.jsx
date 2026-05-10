import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import {
  MdDashboard,
  MdPeople,
  MdFolder,
  MdCheckCircle,
  MdAccessTime,
  MdAutoAwesome,
  MdLogout,
  MdWorkspaces,
} from 'react-icons/md'

const navItems = [
  { to: '/dashboard', icon: MdDashboard, label: 'Bảng điều khiển' },
  { to: '/employees', icon: MdPeople, label: 'Nhân viên' },
  { to: '/projects', icon: MdFolder, label: 'Dự án' },
  { to: '/tasks', icon: MdCheckCircle, label: 'Công việc' },
  { to: '/attendance', icon: MdAccessTime, label: 'Chấm công' },
  { to: '/ai-suggestions', icon: MdAutoAwesome, label: 'AI Gợi ý' },
]

function getInitials(username = '') {
  const parts = username.trim().split(/\s+/)
  if (parts.length === 0 || !parts[0]) return '?'
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase()
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
}

export default function Sidebar() {
  const { logout, user } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <aside className="w-64 bg-slate-900 text-slate-200 flex flex-col min-h-screen relative">
      {/* Brand */}
      <div className="px-6 py-5 border-b border-slate-800">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-indigo-500 via-purple-500 to-pink-500 flex items-center justify-center shadow-lg">
            <MdWorkspaces className="text-white text-xl" />
          </div>
          <div className="leading-tight">
            <h1 className="text-base font-bold text-white">Task Manager</h1>
            <p className="text-[11px] text-slate-400 font-medium tracking-wide uppercase">
              Workspace
            </p>
          </div>
        </div>
      </div>

      {/* Nav */}
      <nav className="flex-1 px-3 py-4 space-y-1">
        {navItems.map(({ to, icon: Icon, label }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) =>
              `group relative flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all ${
                isActive
                  ? 'bg-gradient-to-r from-indigo-600 to-purple-600 text-white shadow-md shadow-indigo-500/25'
                  : 'text-slate-300 hover:bg-slate-800 hover:text-white'
              }`
            }
          >
            {({ isActive }) => (
              <>
                {isActive && (
                  <span className="absolute -left-3 top-1/2 -translate-y-1/2 h-6 w-1 rounded-r-full bg-white" />
                )}
                <Icon className="text-xl flex-shrink-0" />
                <span>{label}</span>
              </>
            )}
          </NavLink>
        ))}
      </nav>

      {/* User + Logout */}
      <div className="p-3 border-t border-slate-800">
        {user && (
          <div className="flex items-center gap-3 px-3 py-2.5 mb-2 rounded-xl bg-slate-800/50">
            <div className="w-9 h-9 rounded-full bg-gradient-to-br from-indigo-400 to-purple-500 flex items-center justify-center text-white text-sm font-bold flex-shrink-0">
              {getInitials(user.username)}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-white truncate">{user.username}</p>
              <p className="text-[11px] text-slate-400 truncate">Đã đăng nhập</p>
            </div>
          </div>
        )}
        <button
          onClick={handleLogout}
          className="flex items-center gap-3 w-full px-3 py-2.5 text-sm font-medium text-slate-300 hover:bg-red-500/10 hover:text-red-400 rounded-xl transition-colors"
        >
          <MdLogout className="text-xl" />
          <span>Đăng xuất</span>
        </button>
      </div>
    </aside>
  )
}
