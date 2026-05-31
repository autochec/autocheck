/**
 * UploadPage — загрузка решения кандидата: ZIP или Git URL.
 * Дата создания: 30-05-2025 | Автор: Команда №2
 */
import { useState, FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { apiClient } from '../api/client'
import { FileUpload } from '../components/ui/FileUpload'
import { Input } from '../components/ui/Input'
import { Button } from '../components/ui/Button'

export const UploadPage = () => {
  const [assignmentId, setAssignmentId] = useState('')
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [mode, setMode] = useState<'zip'|'git'>('zip')
  const [file, setFile] = useState<File|null>(null)
  const [gitUrl, setGitUrl] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const navigate = useNavigate()

  const canSubmit = assignmentId && name && email && (mode==='zip' ? !!file : !!gitUrl)

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setLoading(true); setError('')
    try {
      let res
      if (mode === 'zip' && file) res = await apiClient.uploadZip(assignmentId, file)
      else res = await apiClient.submitGit({ assignment_id: assignmentId, candidate_name: name, candidate_email: email, git_url: gitUrl })
      if (res?.data) navigate(`/submissions/${(res.data as { id: string }).id}`)
    } catch { setError('Ошибка загрузки. Попробуйте снова.') }
    finally { setLoading(false) }
  }

  return (
    <div className="max-w-lg">
      <h1 className="text-xl font-semibold text-gray-900 mb-6">Загрузить решение</h1>
      <form onSubmit={onSubmit} className="bg-white rounded-xl border border-gray-200 p-6 space-y-4">
        <Input label="ID задания" value={assignmentId} onChange={e=>setAssignmentId(e.target.value)} required />
        <Input label="ФИО кандидата" value={name} onChange={e=>setName(e.target.value)} required />
        <Input label="Email кандидата" type="email" value={email} onChange={e=>setEmail(e.target.value)} required />
        <div className="flex gap-2">
          {(['zip','git'] as const).map(m => (
            <button key={m} type="button" onClick={()=>setMode(m)}
              className={`flex-1 py-2 rounded-lg text-sm font-medium border transition-colors ${mode===m?'bg-blue-50 border-blue-500 text-blue-700':'border-gray-200 text-gray-600'}`}>
              {m === 'zip' ? '📦 ZIP-архив' : '🔗 Git URL'}
            </button>
          ))}
        </div>
        {mode === 'zip' ? <FileUpload onFile={setFile} /> : (
          <Input label="Git URL" type="url" placeholder="https://github.com/..." value={gitUrl} onChange={e=>setGitUrl(e.target.value)} />
        )}
        {error && <p className="text-sm text-red-600">{error}</p>}
        <Button type="submit" loading={loading} disabled={!canSubmit} className="w-full">Отправить на проверку</Button>
      </form>
    </div>
  )
}
