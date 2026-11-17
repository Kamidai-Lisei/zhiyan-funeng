import { http, unwrap } from './http'
import type { Page, Post } from '../types'

export const forumApi = {
  list: (body: Record<string, unknown>) => unwrap<Page<Post>>(http.post('/post/list/page/vo', body)),

  get: (id: number | string) => unwrap<Post>(http.get('/post/get/vo', { params: { id } })),

  add: (body: { title: string; content: string; tags: string[] }) =>
    unwrap<number>(http.post('/post/add', body)),

  edit: (body: { id: number | string; title: string; content: string; tags: string[] }) =>
    unwrap<boolean>(http.post('/post/edit', body)),

  /** 管理员更新（置顶、封禁内容修正等，后端 /post/update 仅管理员） */
  adminUpdate: (body: Record<string, unknown>) =>
    unwrap<boolean>(http.post('/post/update', body)),

  /** 删除（本人或管理员） */
  remove: (id: number | string) => unwrap<boolean>(http.post('/post/delete', { id })),

  thumb: (postId: number | string) => unwrap<boolean>(http.post('/post_thumb/', { postId })),

  favour: (postId: number | string) => unwrap<boolean>(http.post('/post_favour/', { postId })),

  comments: (postId: number | string) => unwrap<any[]>(http.get('/post_comment/list', { params: { postId } })),

  addComment: (body: { postId: number | string; content: string; parentId?: number }) =>
    unwrap<number>(http.post('/post_comment/add', body)),

  removeComment: (id: number | string) => unwrap<boolean>(http.post('/post_comment/delete', { id })),

  commentCount: (postId: number | string) => unwrap<number>(http.get('/post_comment/count', { params: { postId } })),
}
