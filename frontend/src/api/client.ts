/**
 * ApiClient — единственная точка HTTP-взаимодействия с бэкендом.
 * Все компоненты и хуки используют только этот модуль, не axios напрямую.
 * Обработка 401/403/422/500 централизована здесь.
 *
 * Дата создания: 30-05-2025
 * Автор: Команда №2
 */
import axios, { AxiosInstance, AxiosError } from 'axios'

const BASE_URL = import.meta.env.VITE_API_URL ?? '/api/v1'

/** Стандартный формат ответа бэкенда */
export interface ApiResponse<T = unknown> {
  data: T | null
  error: string | null
  meta: Record<string, unknown> | null
}

class ApiClient {
  private readonly http: AxiosInstance

  constructor() {
    this.http = axios.create({
      baseURL: BASE_URL,
      headers: { 'Content-Type': 'application/json' },
    })

    // Request interceptor — добавляет Bearer-токен
    this.http.interceptors.request.use((config) => {
      const token = localStorage.getItem('access_token')
      if (token) config.headers.Authorization = `Bearer ${token}`
      return config
    })

    // Response interceptor — централизованная обработка ошибок
    this.http.interceptors.response.use(
      (res) => res,
      (err: AxiosError<ApiResponse>) => {
        if (err.response?.status === 401) {
          // Истёкшая сессия — редирект на логин
          localStorage.removeItem('access_token')
          window.location.href = '/login'
        }
        return Promise.reject(err)
      }
    )
  }

  // ── Auth ────────────────────────────────────────────────────────────────

  async login(email: string, password: string) {
    const res = await this.http.post<ApiResponse<{ access_token: string }>>('/auth/login', { email, password })
    return res.data
  }

  async register(email: string, fullName: string, password: string, role = 'candidate') {
    const res = await this.http.post<ApiResponse>('/auth/register', { email, full_name: fullName, password, role })
    return res.data
  }

  async logout() {
    return this.http.post('/auth/logout')
  }

  async getProfile() {
    const res = await this.http.get<ApiResponse>('/auth/profile')
    return res.data
  }

  // ── Assignments ─────────────────────────────────────────────────────────

  async getAssignments() {
    const res = await this.http.get<ApiResponse>('/assignments')
    return res.data
  }

  async createAssignment(payload: unknown) {
    const res = await this.http.post<ApiResponse>('/assignments', payload)
    return res.data
  }

  async getAssignment(id: string) {
    const res = await this.http.get<ApiResponse>(`/assignments/${id}`)
    return res.data
  }

  async updateAssignment(id: string, payload: unknown) {
    const res = await this.http.put<ApiResponse>(`/assignments/${id}`, payload)
    return res.data
  }

  async deleteAssignment(id: string) {
    return this.http.delete(`/assignments/${id}`)
  }

  // ── Submissions ─────────────────────────────────────────────────────────

  async getSubmissions() {
    const res = await this.http.get<ApiResponse>('/submissions')
    return res.data
  }

  async getSubmission(id: string) {
    const res = await this.http.get<ApiResponse>(`/submissions/${id}`)
    return res.data
  }

  async submitGit(payload: { assignment_id: string; candidate_name: string; candidate_email: string; git_url: string }) {
    const res = await this.http.post<ApiResponse>('/submissions', payload)
    return res.data
  }

  async uploadZip(assignmentId: string, file: File) {
    const form = new FormData()
    form.append('file', file)
    const res = await this.http.post<ApiResponse>(`/submissions/upload?assignment_id=${assignmentId}`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    return res.data
  }

  async getSubmissionStatus(id: string) {
    const res = await this.http.get<ApiResponse>(`/submissions/${id}/status`)
    return res.data
  }

  async getSubmissionResults(id: string) {
    const res = await this.http.get<ApiResponse>(`/submissions/${id}/results`)
    return res.data
  }

  async rerunSubmission(id: string) {
    const res = await this.http.post<ApiResponse>(`/submissions/${id}/rerun`)
    return res.data
  }

  async setVerdict(id: string, verdict: 'accepted' | 'rejected', comment = '') {
    const res = await this.http.put<ApiResponse>(`/submissions/${id}/verdict`, { verdict, comment })
    return res.data
  }

  async getAiReview(id: string) {
    const res = await this.http.get<ApiResponse>(`/submissions/${id}/ai-review`)
    return res.data
  }

  // ── Candidates ──────────────────────────────────────────────────────────

  async getCandidates() {
    const res = await this.http.get<ApiResponse>('/candidates')
    return res.data
  }

  // ── Reports ─────────────────────────────────────────────────────────────

  async getStats() {
    const res = await this.http.get<ApiResponse>('/reports/stats')
    return res.data
  }
}

/** Единственный экземпляр API-клиента для всего приложения */
export const apiClient = new ApiClient()
