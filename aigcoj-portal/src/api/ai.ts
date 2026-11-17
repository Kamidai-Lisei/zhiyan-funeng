import { http, unwrap } from './http'

export interface AiMemory {
  id: number
  memoryType: string
  title: string
  content: string
  createTime: string
}

export interface SimulateScenario {
  name: string
  line: number
  expected: number
  probability: number
}

export const aiApi = {
  ask: (body: { message: string; scene?: string; context?: string }) =>
    unwrap<{ answer: string; scene: string }>(http.post('/ai/assistant', body)),

  /** 择校决策推演：冲稳保多情景对比 */
  simulate: (body: {
    school: string
    major?: string
    currentScore?: number
    targetScore?: number
    dailyHours?: number
    stage?: string
    weakSubjects?: string
  }) =>
    unwrap<{
      report: string
      scenarios: { daysLeft: number; rush: SimulateScenario; steady: SimulateScenario; safe: SimulateScenario }
      memoryId: number
      aiGenerated: boolean
    }>(http.post('/ai/simulate', body)),

  /** 个性化学习规划 */
  generatePlan: (body: {
    stage?: string
    dailyMinutes?: number
    subjects?: string
    goal?: string
    targetDate?: string
  }) => unwrap<{ plan: string; memoryId: number; aiGenerated: boolean; daysLeft: number }>(http.post('/ai/plan/generate', body)),

  /** AI 画像总结 */
  portraitSummary: () =>
    unwrap<{ summary: string; aiGenerated: boolean }>(http.post('/ai/portrait/summary')),

  /** AI 长期记忆 */
  memories: (type?: string) => unwrap<AiMemory[]>(http.get('/ai/memory', { params: { type } })),

  deleteMemory: (id: number) => unwrap<boolean>(http.delete(`/ai/memory/${id}`)),
}

export const searchApi = {
  search: (keyword: string) =>
    unwrap<{ questions: any[]; materials: any[]; posts: any[] }>(http.get('/search', { params: { keyword } })),
}
