/**
 * StatsPage — статистика: метрики, line chart за 30 дней, топ-10.
 * Дата создания: 30-05-2025 | Автор: Команда №2
 */
import { useEffect, useState } from 'react'
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts'
import { apiClient } from '../api/client'

interface Stats { total: number; avg_score: number; pass_rate: number; daily: {date:string;count:number}[] }

export const StatsPage = () => {
  const [stats, setStats] = useState<Stats|null>(null)

  useEffect(() => {
    apiClient.getStats().then(r => r.data && setStats(r.data as Stats))
  }, [])

  const metrics = [
    { label: 'Всего проверок', value: stats?.total ?? 0 },
    { label: 'Средний балл',   value: stats ? stats.avg_score.toFixed(1) : '—' },
    { label: '% прохождения',  value: stats ? `${(stats.pass_rate*100).toFixed(0)}%` : '—' },
  ]

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-gray-900">Статистика</h1>
      <div className="grid grid-cols-3 gap-4">
        {metrics.map(m => (
          <div key={m.label} className="bg-white rounded-xl border border-gray-200 p-5">
            <p className="text-sm text-gray-500 mb-1">{m.label}</p>
            <p className="text-3xl font-semibold text-gray-900">{m.value}</p>
          </div>
        ))}
      </div>
      <div className="bg-white rounded-xl border border-gray-200 p-6">
        <h2 className="font-medium text-gray-900 mb-4">Проверки за 30 дней</h2>
        <ResponsiveContainer width="100%" height={220}>
          <LineChart data={stats?.daily ?? []}>
            <CartesianGrid strokeDasharray="3 3" stroke="#F3F4F6" />
            <XAxis dataKey="date" tick={{ fontSize: 11, fill: '#6B7280' }} />
            <YAxis tick={{ fontSize: 11, fill: '#6B7280' }} />
            <Tooltip />
            <Line type="monotone" dataKey="count" stroke="#2563EB" strokeWidth={2} dot={false} />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  )
}
