import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { ApiError } from '../api/client'
import { useSession } from '../hooks/use-auth'
import { ErrorState } from './error-state'
import { LoadingState } from './loading-state'

export function RequireAdmin() {
  const { data, isLoading, isError, error, refetch } = useSession()
  const location = useLocation()

  if (isLoading) {
    return <LoadingState message="Verificando la sesión…" />
  }

  if (isError) {
    if (error instanceof ApiError && error.status === 401) {
      return <Navigate to="/login" replace state={{ from: location }} />
    }

    return (
      <ErrorState
        message="No se pudo verificar la sesión"
        error={error}
        onRetry={() => void refetch()}
      />
    )
  }

  if (!data) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  return <Outlet />
}
