import { http, unwrap } from './http'
import type { LoginUserVO, UserVO } from '../types'

export const authApi = {
  login: (body: { userAccount: string; userPassword: string }) =>
    unwrap<LoginUserVO>(http.post('/user/login', body)),

  register: (body: { userAccount: string; userPassword: string; checkPassword: string }) =>
    unwrap<number>(http.post('/user/register', body)),

  logout: () => unwrap<boolean>(http.post('/user/logout')),

  me: () => unwrap<LoginUserVO>(http.get('/user/get/login')),

  updateMy: (body: Partial<UserVO>) => unwrap<boolean>(http.post('/user/update/my', body)),
}
