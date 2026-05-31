/**
 * FileUpload — drag-and-drop + кнопка выбора. Валидация: .zip, до 50 МБ.
 * Дата создания: 30-05-2025 | Автор: Команда №2
 */
import { useState, DragEvent, ChangeEvent, useRef } from 'react'
import { clsx } from 'clsx'
const MAX = 50*1024*1024
export const FileUpload = ({ onFile }: { onFile: (f: File) => void }) => {
  const [drag, setDrag] = useState(false)
  const [name, setName] = useState<string|null>(null)
  const [err, setErr] = useState<string|null>(null)
  const ref = useRef<HTMLInputElement>(null)
  const handle = (f: File) => {
    if (!f.name.endsWith('.zip')) { setErr('Допустимый формат: .zip'); return }
    if (f.size > MAX) { setErr('Файл превышает 50 МБ'); return }
    setErr(null); setName(f.name); onFile(f)
  }
  return (
    <div>
      <div onDragOver={e=>{e.preventDefault();setDrag(true)}} onDragLeave={()=>setDrag(false)}
        onDrop={e=>{e.preventDefault();setDrag(false);const f=e.dataTransfer.files[0];if(f)handle(f)}}
        onClick={()=>ref.current?.click()}
        className={clsx('border-2 border-dashed rounded-xl p-8 text-center cursor-pointer transition-colors', drag?'border-blue-500 bg-blue-50':'border-gray-300 hover:border-blue-400')}>
        <p className="text-sm text-gray-500">{name ? <span className="text-blue-600 font-medium">{name}</span> : 'Перетащите .zip сюда или нажмите'}</p>
        <p className="text-xs text-gray-400 mt-1">Только .zip, до 50 МБ</p>
      </div>
      {err && <p className="mt-1 text-xs text-red-600">{err}</p>}
      <input ref={ref} type="file" accept=".zip" className="hidden" onChange={(e:ChangeEvent<HTMLInputElement>)=>{const f=e.target.files?.[0];if(f)handle(f)}} />
    </div>
  )
}
