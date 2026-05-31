/**
 * Input — default, focused, filled, error, disabled + сообщение об ошибке под полем.
 * Дата создания: 30-05-2025 | Автор: Команда №2
 */
import { clsx } from 'clsx'
import { InputHTMLAttributes, forwardRef } from 'react'
interface Props extends InputHTMLAttributes<HTMLInputElement> { label?: string; error?: string }
export const Input = forwardRef<HTMLInputElement, Props>(({ label, error, className, ...p }, ref) => (
  <div className="w-full">
    {label && <label className="block text-sm font-medium text-gray-700 mb-1">{label}</label>}
    <input ref={ref} className={clsx('w-full px-3 py-2 rounded-lg border text-sm outline-none transition-colors focus:ring-2 focus:ring-blue-500 focus:border-blue-500', error ? 'border-red-500' : 'border-gray-300', p.disabled && 'bg-gray-50 cursor-not-allowed opacity-60', className)} {...p} />
    {error && <p className="mt-1 text-xs text-red-600">{error}</p>}
  </div>
))
Input.displayName = 'Input'
