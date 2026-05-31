/**
 * useAuthStore — глобальное состояние аутентификации (Zustand).
 * State management отделён от компонентов.
 *
 * Дата создания: 30-05-2025
 * Автор: Команда №2
 */
import { create } from 'zustand'
import { apiClient } from '../api/client'

interface AuthState {
  token: string | null
  role: string | null
  isLoading: boolean
  error: string | null
  login: (email: string, password: string) => Promise<void>
  logout: () => void
}

export const useAuthStore = create<AuthState>((set) => ({
  token: localStorage.getItem('access_token'),
  role: localStorage.getItem('user_role'),
  isLoading: false,
  error: null,

  login: async (email, password) => {
    set({ isLoading: true, error: null })
    try {
      const res = await apiClient.login(email, password)
      if (res.data) {
        const token = (res.data as { access_token: string }).access_token
        localStorage.setItem('access_token', token)
        set({ token, isLoading: false })
      }
    } catch (e: unknown) {
      const msg = (e as { response?: { data?: { error?: string } } })
        ?.response?.data?.error ?? 'Ошибка входа'
      set({ error: msg, isLoading: false })
    }
  },

  logout: () => {
    localStorage.removeItem('access_token')
    localStorage.removeItem('user_role')
    set({ token: null, role: null })
  },
}))
