import { http, unwrap } from './http'

export interface QuizQuestion {
  id: number
  subject: string
  chapter: string
  paper: string
  year: number
  type: 'choice' | 'subjective'
  question: string
  options: string[]
}

export interface QuizPaper {
  name: string
  year: number
  kind: '真题' | '模拟' | '章节'
  choice: number
  subjective: number
  total: number
}

export interface SingleResult {
  questionId: number
  type: string
  answer?: string
  answerLong?: string
  analysis?: string
  correct?: boolean
  aiReviewed?: boolean
  aiComment?: string
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
  questions: (params: { subject?: string; paper?: string; year?: number; type?: string }) =>
    unwrap<QuizQuestion[]>(http.get('/quiz/questions', { params })),

  papers: () => unwrap<QuizPaper[]>(http.get('/quiz/papers')),

  meta: () =>
    unwrap<{ bySubject: Record<string, number>; sources: string[]; total: number }>(http.get('/quiz/meta')),

  submit: (answers: { questionId: number; chosen: string }[]) =>
    unwrap<QuizSubmitResult>(http.post('/quiz/submit', { answers })),

  single: (body: { questionId: number; chosen?: string; answerText?: string }) =>
    unwrap<SingleResult>(http.post('/quiz/single', body)),

  stats: () =>
    unwrap<{ answered: number; bySubject: { subject: string; total: number; correct: number; accuracy: number }[] }>(
      http.get('/quiz/stats'),
    ),
}
