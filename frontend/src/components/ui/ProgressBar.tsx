/**
 * ProgressBar — линейный прогресс. >80% зелёный, 50–80% жёлтый, <50% красный.
 * Дата создания: 30-05-2025 | Автор: Команда №2
 */
interface Props { value: number; className?: string }
export const ProgressBar = ({ value, className = '' }: Props) => {
  const pct = Math.min(100, Math.max(0, value))
  const color = pct > 80 ? '#16A34A' : pct >= 50 ? '#D97706' : '#DC2626'
  return (
    <div className={`w-full bg-gray-200 rounded-full h-2 ${className}`}>
      <div className="h-2 rounded-full transition-all duration-500" style={{ width: `${pct}%`, backgroundColor: color }} />
    </div>
  )
}
