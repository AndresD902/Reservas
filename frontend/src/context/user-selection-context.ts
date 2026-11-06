import { createContext } from 'react'

export interface UserSelectionContextValue {
  selectedUserId: number | null
  selectUser: (userId: number | null) => void
}

export const UserSelectionContext =
  createContext<UserSelectionContextValue | null>(null)
