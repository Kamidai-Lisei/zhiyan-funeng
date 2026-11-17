import { http, unwrap } from './http'

export interface QuizQuestion {
  id: number
  subject: string
  chapter: string
  source: string
  question: string
  options: string[]
}

export interface QuizResultItem {
  questionId: number
  chosen: string
  answer: string
  correct: boolean
  analysis: string
}

export interface QuizSubmitResult {
  total: number
  correct: number
  score: number
  results: QuizResultItem[]
}

export const quizApi = {
  questions: (params: { subject?: string; source?: string }) =>
    unwrap<QuizQuestion[]>(http.get('/quiz/questions', { params })),

  meta: () =>
    unwrap<{ bySubject: Record<string, number>; sources: string[]; total: number }>(http.get('/quiz/meta')),

  submit: (answers: { questionId: number; chosen: string }[]) =>
    unwrap<QuizSubmitResult>(http.post('/quiz/submit', { answers })),

  stats: () =>
    unwrap<{ answered: number; bySubject: { subject: string; total: number; correct: number; accuracy: number }[] }>(
      http.get('/quiz/stats'),
    ),
}
