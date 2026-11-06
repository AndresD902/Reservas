import { useContext } from 'react'
import {
  UserSelectionContext,
  type UserSelectionContextValue,
} from '../context/user-selection-context'

export function useUserSelection(): UserSelectionContextValue {
  const context = useContext(UserSelectionContext)

  if (!context) {
    throw new Error('useUserSelection debe usarse dentro de UserSelectionProvider')
  }

  return context
}
