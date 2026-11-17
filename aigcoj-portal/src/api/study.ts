import { http, unwrap } from './http'
import type { Checkin, Question, StudyOverview, StudyPlan } from '../types'

export const studyApi = {
  overview: () => unwrap<StudyOverview>(http.get('/study/overview')),

  plan: () => unwrap<StudyPlan>(http.get('/study/plan')),

  savePlan: (body: StudyPlan) => unwrap<boolean>(http.post('/study/plan', body)),

  checkin: (body: { duration: number; content: string }) => unwrap<boolean>(http.post('/study/checkin', body)),

  today: () => unwrap<Checkin | null>(http.get('/study/checkin/today')),

  calendar: (month: string) => unwrap<string[]>(http.get('/study/checkin/calendar', { params: { month } })),

  progress: (module?: string) => unwrap<any[]>(http.get('/study/progress', { params: { module } })),

  saveProgress: (body: { module: string; slug: string; finished: boolean }) =>
    unwrap<boolean>(http.post('/study/progress', body)),

  wrongBook: () => unwrap<Question[]>(http.get('/study/wrong-book')),

  /** 个人考研画像（一人一档） */
  portrait: () => unwrap<Portrait>(http.get('/study/portrait')),
}

export interface TagStat {
  tag: string
  total: number
  accepted: number
  mastery: number
}

export interface Portrait {
  submitCount: number
  acceptedCount: number
  acceptance: number
  tagStats: TagStat[]
  weakTags: string[]
  strongTags: string[]
  recentWrong: { id: string; title: string }[]
  checkinDays: number
  totalMinutes: number
  activeDays14: number
  favourCount: number
  plan?: { targetDate: string; dailyMinutes: number; subjects: string }
}
