/**
 * DataTable — таблица с сортировкой, пагинацией 50/стр, кликабельными строками.
 * Дата создания: 30-05-2025 | Автор: Команда №2
 */
import { useState } from 'react'
export interface Column<T> { key: keyof T; title: string; render?: (v: T[keyof T], r: T) => React.ReactNode; sortable?: boolean }
interface Props<T extends {id:string}> { columns: Column<T>[]; data: T[]; onRowClick?: (r: T) => void }
const PAGE = 50
export const DataTable = <T extends {id:string}>({ columns, data, onRowClick }: Props<T>) => {
  const [sk, setSk] = useState<keyof T|null>(null)
  const [sd, setSd] = useState<'asc'|'desc'>('asc')
  const [pg, setPg] = useState(0)
  const sorted = [...data].sort((a,b)=>!sk?0:(sd==='asc'?String(a[sk]).localeCompare(String(b[sk])):String(b[sk]).localeCompare(String(a[sk]))))
  const paged = sorted.slice(pg*PAGE,(pg+1)*PAGE)
  const tp = Math.ceil(data.length/PAGE)
  const toggle = (k: keyof T) => { if(sk===k) setSd(d=>d==='asc'?'desc':'asc'); else{setSk(k);setSd('asc')} }
  return (
    <div>
      <div className="overflow-x-auto rounded-xl border border-gray-200 bg-white">
        <table className="min-w-full text-sm">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>{columns.map(c=><th key={String(c.key)} className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase" onClick={()=>c.sortable&&toggle(c.key)}><span className={c.sortable?'cursor-pointer hover:text-gray-900':''}>{c.title}{sk===c.key&&(sd==='asc'?' ↑':' ↓')}</span></th>)}</tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {paged.map(row=><tr key={row.id} onClick={()=>onRowClick?.(row)} className={onRowClick?'hover:bg-gray-50 cursor-pointer transition-colors':''}>
              {columns.map(c=><td key={String(c.key)} className="px-4 py-3 text-gray-700">{c.render?c.render(row[c.key],row):String(row[c.key]??'—')}</td>)}
            </tr>)}
            {!paged.length&&<tr><td colSpan={columns.length} className="px-4 py-8 text-center text-gray-400">Нет данных</td></tr>}
          </tbody>
        </table>
      </div>
      {tp>1&&<div className="flex items-center justify-between mt-3 text-sm text-gray-500">
        <span>Страница {pg+1} из {tp}</span>
        <div className="flex gap-2">
          <button onClick={()=>setPg(p=>Math.max(0,p-1))} disabled={pg===0} className="px-3 py-1 rounded border disabled:opacity-40">←</button>
          <button onClick={()=>setPg(p=>Math.min(tp-1,p+1))} disabled={pg===tp-1} className="px-3 py-1 rounded border disabled:opacity-40">→</button>
        </div>
      </div>}
    </div>
  )
}
