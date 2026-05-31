/**
 * ScoreCard — крупный балл, цветовая индикация, название задания, статус.
 * Дата создания: 30-05-2025 | Автор: Команда №2
 */
import { ProgressBar } from './ProgressBar'
import { StatusBadge } from './StatusBadge'
interface Props { score: number | null; status: string; assignmentTitle: string; candidateName: string }
export const ScoreCard = ({ score, status, assignmentTitle, candidateName }: Props) => {
  const color = score == null ? '#6B7280' : score > 80 ? '#16A34A' : score >= 50 ? '#D97706' : '#DC2626'
  return (
    <div className="bg-white rounded-xl border border-gray-200 p-6 shadow-sm">
      <div className="flex items-start justify-between mb-4">
        <div>
          <p className="text-sm text-gray-500 mb-1">{assignmentTitle}</p>
          <p className="font-medium text-gray-900">{candidateName}</p>
        </div>
        <StatusBadge status={status} />
      </div>
      <div className="flex items-end gap-2 mb-3">
        <span className="text-5xl font-semibold" style={{ color }}>{score != null ? score.toFixed(1) : '—'}</span>
        <span className="text-gray-400 mb-1">/100</span>
      </div>
      {score != null && <ProgressBar value={score} />}
    </div>
  )
}
