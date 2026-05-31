/**
 * SubmissionDetailPage — карточка проверки.
 * ScoreCard, детализация чекеров, AI-анализ (graceful), вердикт.
 * Дата создания: 30-05-2025 | Автор: Команда №2
 */
import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { apiClient } from '../api/client'
import { ScoreCard } from '../components/ui/ScoreCard'
import { StatusBadge } from '../components/ui/StatusBadge'
import { ProgressBar } from '../components/ui/ProgressBar'
import { Button } from '../components/ui/Button'

interface CheckResultRow { checker: string; status: string; score: number; message: string }
interface Submission { id: string; status: string; score: number|null; verdict: string|null }

export const SubmissionDetailPage = () => {
  const { id } = useParams<{ id: string }>()
  const [sub, setSub] = useState<Submission|null>(null)
  const [results, setResults] = useState<CheckResultRow[]>([])
  const [aiReview, setAiReview] = useState<string|null>(null)
  const [verdict, setVerdict] = useState<'accepted'|'rejected'|null>(null)
  const [loading, setLoading] = useState(false)
  const [expanded, setExpanded] = useState<string|null>(null)

  useEffect(() => {
    if (!id) return
    apiClient.getSubmission(id).then(r => r.data && setSub(r.data as Submission))
    apiClient.getSubmissionResults(id).then(r => r.data && setResults(r.data as CheckResultRow[]))
  }, [id])

  const loadAI = async () => {
    if (!id) return
    setLoading(true)
    const r = await apiClient.getAiReview(id)
    setAiReview((r.data as { review: string })?.review ?? 'AI-анализ недоступен')
    setLoading(false)
  }

  const submitVerdict = async (v: 'accepted'|'rejected') => {
    if (!id) return
    await apiClient.setVerdict(id, v)
    setVerdict(v)
  }

  if (!sub) return <div className="p-6 text-gray-400">Загрузка…</div>

  return (
    <div className="max-w-3xl space-y-6">
      <ScoreCard score={sub.score} status={sub.status} assignmentTitle="Задание" candidateName={`#${id?.slice(0,8)}`} />

      <div className="bg-white rounded-xl border border-gray-200 p-6">
        <h2 className="font-medium text-gray-900 mb-4">Результаты чекеров</h2>
        <div className="space-y-3">
          {results.map(r => (
            <div key={r.checker} className="border border-gray-100 rounded-lg overflow-hidden">
              <button onClick={() => setExpanded(expanded === r.checker ? null : r.checker)}
                className="w-full flex items-center justify-between px-4 py-3 text-sm hover:bg-gray-50">
                <div className="flex items-center gap-3">
                  <span className="font-medium text-gray-700 capitalize">{r.checker.replace('_',' ')}</span>
                  <StatusBadge status={r.status} />
                </div>
                <div className="flex items-center gap-4">
                  <span className="font-semibold text-gray-900">{r.score.toFixed(1)}</span>
                  <span className="text-gray-400">{expanded === r.checker ? '▲' : '▼'}</span>
                </div>
              </button>
              {expanded === r.checker && (
                <div className="px-4 pb-4 text-xs text-gray-600 border-t border-gray-100 pt-3">
                  <p className="mb-2">{r.message}</p>
                  <ProgressBar value={r.score} />
                </div>
              )}
            </div>
          ))}
          {!results.length && <p className="text-sm text-gray-400">Результаты ещё не готовы</p>}
        </div>
      </div>

      <div className="bg-white rounded-xl border border-gray-200 p-6">
        <div className="flex items-center justify-between mb-3">
          <h2 className="font-medium text-gray-900">AI-анализ</h2>
          {!aiReview && <Button variant="secondary" onClick={loadAI} loading={loading}>Запросить</Button>}
        </div>
        {aiReview
          ? <p className="text-sm text-gray-700 whitespace-pre-wrap">{aiReview}</p>
          : <p className="text-sm text-gray-400 italic">Нажмите «Запросить» для получения AI-анализа</p>
        }
      </div>

      {!sub.verdict && sub.status === 'done' && (
        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <h2 className="font-medium text-gray-900 mb-4">Вынести вердикт</h2>
          <div className="flex gap-3">
            <Button onClick={() => submitVerdict('accepted')}>✓ Принять</Button>
            <Button variant="danger" onClick={() => submitVerdict('rejected')}>✗ Отклонить</Button>
          </div>
        </div>
      )}
      {(sub.verdict || verdict) && (
        <div className="flex justify-center">
          <StatusBadge status={sub.verdict ?? verdict ?? ''} />
        </div>
      )}
    </div>
  )
}
