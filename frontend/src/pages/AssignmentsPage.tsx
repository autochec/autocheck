/**
 * AssignmentsPage — список тестовых заданий, создание нового.
 * Создание: чекбоксы чекеров + ползунки весов, сумма должна = 100%.
 * Дата создания: 30-05-2025 | Автор: Команда №2
 */
import { useEffect, useState } from 'react'
import { apiClient } from '../api/client'
import { Button } from '../components/ui/Button'
import { Input } from '../components/ui/Input'

const CHECKERS = ['static_analysis','architecture','build','test','documentation','git_practices']

export const AssignmentsPage = () => {
  const [assignments, setAssignments] = useState<{id:string;title:string}[]>([])
  const [showForm, setShowForm] = useState(false)
  const [title, setTitle] = useState('')
  const [desc, setDesc] = useState('')
  const [weights, setWeights] = useState<Record<string,number>>(Object.fromEntries(CHECKERS.map(c=>[c,Math.round(100/CHECKERS.length)])))
  const [enabled, setEnabled] = useState<Record<string,boolean>>(Object.fromEntries(CHECKERS.map(c=>[c,true])))
  const [loading, setLoading] = useState(false)

  const total = Object.entries(weights).filter(([k])=>enabled[k]).reduce((s,[,v])=>s+v,0)
  const canCreate = title && total === 100

  useEffect(() => {
    apiClient.getAssignments().then(r => r.data && setAssignments(r.data as {id:string;title:string}[]))
  }, [])

  const onCreate = async () => {
    setLoading(true)
    const configs = Object.fromEntries(CHECKERS.map(c=>[c,{enabled:enabled[c],weight:weights[c]}]))
    await apiClient.createAssignment({ title, description: desc, checker_configs: configs })
    setShowForm(false); setLoading(false)
    apiClient.getAssignments().then(r => r.data && setAssignments(r.data as {id:string;title:string}[]))
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-xl font-semibold text-gray-900">Тестовые задания</h1>
        <Button onClick={()=>setShowForm(!showForm)}>+ Создать задание</Button>
      </div>

      {showForm && (
        <div className="bg-white rounded-xl border border-gray-200 p-6 mb-6 space-y-4">
          <Input label="Название" value={title} onChange={e=>setTitle(e.target.value)} />
          <Input label="Описание" value={desc} onChange={e=>setDesc(e.target.value)} />
          <div>
            <p className="text-sm font-medium text-gray-700 mb-3">
              Чекеры и веса
              <span className={`ml-2 text-xs ${total===100?'text-green-600':'text-red-600'}`}>
                Сумма: {total}% {total===100?'✓':'(должно быть 100%)'}
              </span>
            </p>
            {CHECKERS.map(c => (
              <div key={c} className="flex items-center gap-3 mb-2">
                <input type="checkbox" checked={enabled[c]} onChange={e=>setEnabled(p=>({...p,[c]:e.target.checked}))} className="accent-blue-600" />
                <span className="text-sm text-gray-700 w-40">{c.replace('_',' ')}</span>
                <input type="range" min={0} max={100} value={weights[c]} disabled={!enabled[c]}
                  onChange={e=>setWeights(p=>({...p,[c]:Number(e.target.value)}))}
                  className="flex-1 accent-blue-600" />
                <span className="text-sm text-gray-600 w-10 text-right">{weights[c]}%</span>
              </div>
            ))}
          </div>
          <Button onClick={onCreate} disabled={!canCreate} loading={loading}>Создать</Button>
        </div>
      )}

      <div className="space-y-2">
        {assignments.map(a => (
          <div key={a.id} className="bg-white rounded-lg border border-gray-200 px-4 py-3 flex justify-between items-center">
            <span className="font-medium text-gray-800">{a.title}</span>
            <span className="text-xs text-gray-400 font-mono">{a.id.slice(0,8)}…</span>
          </div>
        ))}
        {!assignments.length && <p className="text-sm text-gray-400">Заданий пока нет</p>}
      </div>
    </div>
  )
}
