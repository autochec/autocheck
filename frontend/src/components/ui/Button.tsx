/**
 * Button — primary, secondary, danger, disabled, loading.
 * Дата создания: 30-05-2025 | Автор: Команда №2
 */
import { clsx } from 'clsx'
import { ButtonHTMLAttributes } from 'react'
interface Props extends ButtonHTMLAttributes<HTMLButtonElement> { variant?: 'primary'|'secondary'|'danger'; loading?: boolean }
const V = { primary:'bg-blue-600 text-white hover:bg-blue-700 focus:ring-blue-500', secondary:'bg-white text-gray-700 border border-gray-300 hover:bg-gray-50 focus:ring-gray-300', danger:'bg-red-600 text-white hover:bg-red-700 focus:ring-red-500' }
export const Button = ({ variant='primary', loading, disabled, children, className, ...p }: Props) => (
  <button disabled={disabled||loading} className={clsx('inline-flex items-center justify-center gap-2 px-4 py-2 rounded-lg text-sm font-medium focus:outline-none focus:ring-2 focus:ring-offset-2 transition-colors', V[variant], (disabled||loading)&&'opacity-50 cursor-not-allowed', className)} {...p}>
    {loading && <svg className="animate-spin h-4 w-4" fill="none" viewBox="0 0 24 24"><circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"/><path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z"/></svg>}
    {children}
  </button>
)
