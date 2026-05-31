/**
 * StatusBadge — цветной чип статуса проверки.
 * Состояния: pending, running, passed, failed, error, accepted, rejected.
 * Дата создания: 30-05-2025 | Автор: Команда №2
 */
import { clsx } from 'clsx'
const STYLES: Record<string, string> = {
  pending:  'bg-gray-100 text-gray-600',
  running:  'bg-blue-100 text-blue-700 animate-pulse',
  passed:   'bg-green-100 text-green-700',
  failed:   'bg-red-100 text-red-700',
  error:    'bg-orange-100 text-orange-700',
  accepted: 'bg-green-100 text-green-700',
  rejected: 'bg-red-100 text-red-700',
  done:     'bg-green-100 text-green-700',
}
const LABELS: Record<string, string> = {
  pending:'Ожидает', running:'Проверяется', passed:'Пройден',
  failed:'Не пройден', error:'Ошибка', accepted:'Принят',
  rejected:'Отклонён', done:'Готово',
}
export const StatusBadge = ({ status }: { status: string }) => (
  <span className={clsx('inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium', STYLES[status] ?? 'bg-gray-100 text-gray-600')}>
    {status === 'running' && <span className="w-1.5 h-1.5 rounded-full bg-blue-500 mr-1.5 animate-ping" />}
    {LABELS[status] ?? status}
  </span>
)
