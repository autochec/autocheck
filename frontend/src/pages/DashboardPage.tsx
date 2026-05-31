/**
 * DashboardPage — главный дашборд Эксперта.
 * DataTable проверок, live-поиск по ФИО, фильтры по статусу.
 * Дата создания: 30-05-2025 | Автор: Команда №2
 */
import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { apiClient } from '../api/client'
import { DataTable, Column } from '../components/ui/DataTable'
import { StatusBadge } from '../components/ui/StatusBadge'
import { Input } from '../components/ui/Input'

interface SubmissionRow { id: string; status: string; score: number | null; assignment_id: string }

const STATUS_OPTS = ['', 'pending', 'running', 'done', 'error']

export const DashboardPage = () => {
  const [rows, setRows] = useState<SubmissionRow[]>([])
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('')
  const navigate = useNavigate()

  useEffect(() => {
    apiClient.getSubmissions().then(res => { if (res.data) setRows(res.data as SubmissionRow[]) })
  }, [])

  const filtered = rows.filter(r =>
    (statusFilter ? r.status === statusFilter : true) &&
    (search ? r.id.includes(search) || r.assignment_id.includes(search) : true)
  )

  const columns: Column<SubmissionRow>[] = [
    { key: 'id',            title: 'ID',       sortable: true },
    { key: 'assignment_id', title: 'Задание',  sortable: true },
    { key: 'status',        title: 'Статус',   render: v => <StatusBadge status={String(v)} /> },
    { key: 'score',         title: 'Балл',     sortable: true, render: v => v != null ? `${Number(v).toFixed(1)}/100` : '—' },
  ]

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-xl font-semibold text-gray-900">Проверки</h1>
        <span className="text-sm text-gray-500">{filtered.length} проверок</span>
      </div>
      <div className="flex gap-3 mb-4">
        <Input placeholder="Поиск по ID или заданию…" value={search} onChange={e=>setSearch(e.target.value)} className="max-w-xs" />
        <select value={statusFilter} onChange={e=>setStatusFilter(e.target.value)}
          className="px-3 py-2 rounded-lg border border-gray-300 text-sm text-gray-700 focus:ring-2 focus:ring-blue-500">
          <option value="">Все статусы</option>
          {STATUS_OPTS.filter(Boolean).map(s => <option key={s} value={s}>{s}</option>)}
        </select>
      </div>
      <DataTable columns={columns} data={filtered} onRowClick={r => navigate(`/submissions/${r.id}`)} />
    </div>
  )
}
