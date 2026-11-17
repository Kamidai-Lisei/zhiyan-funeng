import { http, unwrap } from './http'
import type { Page, Question, QuestionSubmit, JudgeInfo } from '../types'

export const questionApi = {
  listPageVo: (body: Record<string, unknown>) =>
    unwrap<Page<Question>>(http.post('/question/list/page/vo', body)),

  getVo: (id: number | string) => unwrap<Question>(http.get('/question/get/vo', { params: { id } })),

  submit: (body: { questionId: number | string; language: string; code: string }) =>
    unwrap<number>(http.post('/question/question_submit/do', body)),

  submissions: (body: Record<string, unknown>) =>
    unwrap<Page<QuestionSubmit>>(http.post('/question/question_submit/list/page', body)),

  getCode: (questionSubmitId: number | string) =>
    unwrap<string>(http.get('/question/question_submit/getCode', { params: { questionSubmitId } })),

  analyze: (questionSubmitId: number | string) =>
    unwrap<string>(http.get('/question/question_submit/analyzeCode', { params: { questionSubmitId } })),

  comments: (id: number | string) =>
    unwrap<any[]>(http.get('/question_comment/getCommentList', { params: { id } })),

  addComment: (body: { questionId: number | string; content: string; parentId?: number }) =>
    unwrap<boolean>(http.post('/question_comment/addComment', {
      questionId: body.questionId,
      content: body.content,
      parentId: body.parentId ?? -1,
      fromId: -1,
    })),

  favourIds: () => unwrap<(number | string)[]>(http.get('/question_favour/ids')),

  toggleFavour: (questionId: number | string) =>
    unwrap<{ favour: boolean }>(http.post('/question_favour/toggle', { questionId })),

  wrongBook: () => unwrap<Question[]>(http.get('/study/wrong-book')),

  /** 我已通过（AC）的题目 id */
  solvedIds: () => unwrap<string[]>(http.get('/question/question_submit/solved')),
}

export function judgeLabel(judgeInfo?: JudgeInfo) {
  return judgeInfo?.message || '等待判题'
}
