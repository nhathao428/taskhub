import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useTranslation } from '../context/LanguageContext'
import {
  MdDashboard,
  MdPeople,
  MdFolder,
  MdCheckCircle,
  MdAccessTime,
  MdAutoAwesome,
  MdAssignmentInd,
  MdEventAvailable,
  MdLogout,
  MdWorkspaces,
  MdLocationOn,
  MdManageAccounts,
} from 'react-icons/md'

const managerNav = [
  { to: '/dashboard', icon: MdDashboard, label: 'Bảng điều khiển' },
  { to: '/employees', icon: MdPeople, label: 'Nhân viên' },
  { to: '/projects', icon: MdFolder, label: 'Dự án' },
  { to: '/tasks', icon: MdCheckCircle, label: 'Công việc' },
  { to: '/attendance', icon: MdAccessTime, label: 'Chấm công' },
  { to: '/office-locations', icon: MdLocationOn, label: 'Văn phòng' },
  { to: '/ai-suggestions', icon: MdAutoAwesome, label: 'AI Gợi ý' },
]

const employeeNav = [
  { to: '/dashboard', icon: MdDashboard, label: 'Bảng điều khiển' },
  { to: '/projects', icon: MdFolder, label: 'Dự án' },
  { to: '/my-tasks', icon: MdAssignmentInd, label: 'Công việc của tôi' },
  { to: '/my-attendance', icon: MdEventAvailable, label: 'Chấm công của tôi' },
]

const adminNavItem = { to: '/users', icon: MdManageAccounts, label: 'Người dùng' }

function roleLabel(role, t) {
  if (role === 'MANAGER') return t('Quản lý')
  if (role === 'ADMIN') return t('Quản trị viên')
  return t('Nhân viên')
}

function getInitials(username = '') {
  const parts = username.trim().split(/\s+/)
  if (parts.length === 0 || !parts[0]) return '?'
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase()
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
}

export default function Sidebar() {
  const { logout, user, isDemo } = useAuth()
  const { t } = useTranslation()
  const navigate = useNavigate()
  const role = user?.role || 'EMPLOYEE'
  const navItems =
    role === 'EMPLOYEE'
      ? employeeNav
      : role === 'ADMIN'
        ? [...managerNav, adminNavItem]
        : managerNav

  const handleLogout = () => {
    logout()
    navigate(isDemo ? '/' : '/login')
  }

  return (
    <aside className="w-16 md:w-64 bg-slate-900 text-slate-300 flex flex-col min-h-screen relative transition-all duration-300 flex-shrink-0">
      {/* Brand */}
      <div className="px-3 md:px-6 py-5 border-b border-slate-800/80">
        <div className="flex items-center justify-center md:justify-start gap-3">
          <div className="w-10 h-10 rounded-xl bg-brand-600 flex items-center justify-center shadow-brand-glow ring-1 ring-white/10 flex-shrink-0">
            <MdWorkspaces className="text-white text-xl" />
          </div>
          <div className="leading-tight hidden md:block">
            <h1 className="text-base font-bold text-white tracking-tight">TaskHub</h1>
            <p className="text-[10px] text-slate-400 font-semibold tracking-[0.18em] uppercase mt-0.5">
              Workspace
            </p>
          </div>
        </div>
      </div>

      {/* Nav */}
      <nav className="flex-1 px-2 md:px-3 py-4 space-y-1">
        {navItems.map(({ to, icon: Icon, label }) => (
          <NavLink
            key={to}
            to={to}
            title={t(label)}
            className={({ isActive }) =>
              `group relative flex items-center justify-center md:justify-start gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-colors ${
                isActive
                  ? 'bg-brand-600 text-white shadow-soft-md'
                  : 'text-slate-300 hover:bg-slate-800/70 hover:text-white'
              }`
            }
          >
            {({ isActive }) => (
              <>
                {isActive && (
                  <span className="absolute -left-2 md:-left-3 top-1/2 -translate-y-1/2 h-6 w-1 rounded-r-full bg-white" />
                )}
                <Icon className="text-xl flex-shrink-0" />
                <span className="hidden md:block">{t(label)}</span>
              </>
            )}
          </NavLink>
        ))}
      </nav>

      {/* User + Logout */}
      <div className="p-2 md:p-3 border-t border-slate-800/80">
        {user && (
          <div className="flex items-center justify-center md:justify-start gap-3 px-2 md:px-3 py-2.5 mb-2 rounded-xl bg-slate-800/40" title={user.username}>
            <div className="w-9 h-9 rounded-full bg-brand-600 flex items-center justify-center text-white text-sm font-semibold flex-shrink-0 ring-1 ring-white/10">
              {getInitials(user.username)}
            </div>
            <div className="hidden md:block flex-1 min-w-0">
              <p className="text-sm font-semibold text-white truncate">{isDemo ? t('Khách dùng thử') : user.username}</p>
              <p className="text-[11px] text-slate-400 truncate">{isDemo ? t('Chế độ dùng thử') : roleLabel(role, t)}</p>
            </div>
          </div>
        )}
        <button
          onClick={handleLogout}
          title={isDemo ? t('Thoát dùng thử') : t('Đăng xuất')}
          className="flex items-center justify-center md:justify-start gap-3 w-full px-3 py-2.5 text-sm font-medium text-slate-300 hover:bg-red-500/10 hover:text-red-400 rounded-xl transition-colors"
        >
          <MdLogout className="text-xl flex-shrink-0" />
          <span className="hidden md:block">{isDemo ? t('Thoát dùng thử') : t('Đăng xuất')}</span>
        </button>
      </div>
    </aside>
  )
}
