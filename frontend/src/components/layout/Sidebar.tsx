/**
 * Sidebar — навигационная панель дашборда.
 * Дата создания: 30-05-2025 | Автор: Команда №2
 */
import { NavLink } from 'react-router-dom'
import { clsx } from 'clsx'
const links = [
  { to: '/',             label: 'Проверки',      icon: '📋' },
  { to: '/assignments',  label: 'Задания',        icon: '📝' },
  { to: '/upload',       label: 'Загрузить',      icon: '⬆️' },
  { to: '/stats',        label: 'Статистика',     icon: '📊' },
]
export const Sidebar = () => (
  <aside className="w-56 min-h-screen bg-white border-r border-gray-200 flex flex-col">
    <div className="px-6 py-5 border-b border-gray-100">
      <span className="font-semibold text-gray-900 text-sm">AutoCheckMobile</span>
    </div>
    <nav className="flex-1 px-3 py-4 space-y-1">
      {links.map(l => (
        <NavLink key={l.to} to={l.to} end={l.to==='/'} className={({ isActive }) =>
          clsx('flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors',
            isActive ? 'bg-blue-50 text-blue-700 font-medium' : 'text-gray-600 hover:bg-gray-50')}>
          <span>{l.icon}</span>{l.label}
        </NavLink>
      ))}
    </nav>
  </aside>
)
