import { http, unwrap } from './http'
import type { Page, UserVO } from '../types'

export const userApi = {
  listPage: (body: Record<string, unknown>) => unwrap<Page<UserVO>>(http.post('/user/list/page/vo', body)),

  /** 管理员专用：按角色统计（返回 total） */
  adminListPage: (body: Record<string, unknown>) => unwrap<Page<any>>(http.post('/user/list/page', body)),

  update: (body: Record<string, unknown>) => unwrap<boolean>(http.post('/user/update', body)),

  remove: (id: number | string) => unwrap<boolean>(http.post('/user/delete', { id })),
}
