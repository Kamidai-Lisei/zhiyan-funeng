import { http, unwrap } from './http'
import type { Resume } from '../types'

export const resumeApi = {
  latest: () => unwrap<Resume>(http.get('/resume/latest')),

  save: (body: Partial<Resume>) => unwrap<number>(http.post('/resume/save', body)),

  analyze: (id: number | string) => unwrap<{ score: number; result: string }>(http.post('/resume/analyze', { id })),

  analysis: (id: number | string) => unwrap<any>(http.get('/resume/analysis', { params: { id } })),

  templates: () => unwrap<{ id: string; name: string; desc: string }[]>(http.get('/resume/templates')),

  chat: (body: { resumeId: number | string; message: string; history?: { role: string; content: string }[] }) =>
    unwrap<{ reply: string; resumeJson?: string; applied?: boolean }>(http.post('/resume/chat', body)),
}
