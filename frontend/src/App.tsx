import { Navigate, Route, Routes } from 'react-router-dom'
import { Layout } from './components/layout'
import { RequireAdmin } from './components/require-admin'
import { AdminResourcesPage } from './pages/admin-resources-page'
import { AdminUsersPage } from './pages/admin-users-page'
import { LoginPage } from './pages/login-page'
import { NotFoundPage } from './pages/not-found-page'
import { ReservationPage } from './pages/reservation-page'
import { ResourceDetailPage } from './pages/resource-detail-page'

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<Navigate to="/reservar" replace />} />
        <Route path="/reservar" element={<ReservationPage />} />
        <Route path="/recursos/:recursoId" element={<ResourceDetailPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route element={<RequireAdmin />}>
          <Route path="/admin" element={<Navigate to="/admin/usuarios" replace />} />
          <Route path="/admin/usuarios" element={<AdminUsersPage />} />
          <Route path="/admin/recursos" element={<AdminResourcesPage />} />
        </Route>
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}
