/**
 * App — корневой роутер приложения.
 * Защита маршрутов: редирект на /login если нет токена.
 * Дата создания: 30-05-2025 | Автор: Команда №2
 */
import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuthStore } from './store/auth'
import { AppLayout } from './components/layout/AppLayout'
import { LoginPage } from './pages/LoginPage'
import { DashboardPage } from './pages/DashboardPage'
import { SubmissionDetailPage } from './pages/SubmissionDetailPage'
import { UploadPage } from './pages/UploadPage'
import { StatsPage } from './pages/StatsPage'
import { AssignmentsPage } from './pages/AssignmentsPage'

const Protected = ({ children }: { children: React.ReactNode }) => {
  const token = useAuthStore(s => s.token)
  return token ? <>{children}</> : <Navigate to="/login" replace />
}

const App = () => (
  <Routes>
    <Route path="/login" element={<LoginPage />} />
    <Route path="/" element={<Protected><AppLayout /></Protected>}>
      <Route index element={<DashboardPage />} />
      <Route path="submissions/:id" element={<SubmissionDetailPage />} />
      <Route path="upload" element={<UploadPage />} />
      <Route path="stats" element={<StatsPage />} />
      <Route path="assignments" element={<AssignmentsPage />} />
    </Route>
    <Route path="*" element={<Navigate to="/" replace />} />
  </Routes>
)
export default App
