/**
 * AppLayout — обёртка с Sidebar для всех аутентифицированных страниц.
 * Дата создания: 30-05-2025 | Автор: Команда №2
 */
import { Outlet } from 'react-router-dom'
import { Sidebar } from './Sidebar'
export const AppLayout = () => (
  <div className="flex min-h-screen bg-gray-50">
    <Sidebar />
    <main className="flex-1 p-6 overflow-auto">
      <Outlet />
    </main>
  </div>
)
