import { useCallback, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import {
  UserSelectionContext,
  type UserSelectionContextValue,
} from './user-selection-context'

const STORAGE_KEY = 'reservas.selectedUserId'

function readStoredUserId(): number | null {
  try {
    const stored = window.localStorage.getItem(STORAGE_KEY)

    if (!stored) {
      return null
    }

    const parsed = Number(stored)
    return Number.isInteger(parsed) && parsed > 0 ? parsed : null
  } catch {
    return null
  }
}

export function UserSelectionProvider({ children }: { children: ReactNode }) {
  const [selectedUserId, setSelectedUserId] = useState<number | null>(readStoredUserId)

  const selectUser = useCallback((userId: number | null) => {
    setSelectedUserId(userId)

    try {
      if (userId === null) {
        window.localStorage.removeItem(STORAGE_KEY)
      } else {
        window.localStorage.setItem(STORAGE_KEY, String(userId))
      }
    } catch {
      return
    }
  }, [])

  const value = useMemo<UserSelectionContextValue>(
    () => ({ selectedUserId, selectUser }),
    [selectedUserId, selectUser],
  )

  return (
    <UserSelectionContext.Provider value={value}>
      {children}
    </UserSelectionContext.Provider>
  )
}
