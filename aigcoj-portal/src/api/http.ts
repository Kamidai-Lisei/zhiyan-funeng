import axios from 'axios'

export type BaseResponse<T> = {
  code: number
  data: T
  message: string
}

export const http = axios.create({
  baseURL: '/api',
  withCredentials: true,
  timeout: 120_000,
})

http.interceptors.response.use(
  (resp) => {
    const body = resp.data as BaseResponse<unknown>
    if (body && typeof body.code === 'number') {
      if (body.code === 0) return resp
      return Promise.reject(Object.assign(new Error(body.message || '请求失败'), { code: body.code, body }))
    }
    return resp
  },
  (err) => {
    const message = err?.response?.data?.message || err?.message || '网络异常'
    return Promise.reject(Object.assign(new Error(message), { code: err?.response?.status }))
  },
)

export async function unwrap<T>(p: Promise<{ data: BaseResponse<T> }>): Promise<T> {
  const resp = await p
  return resp.data.data
}

export function errorMessage(e: any) {
  if (!e) return '操作失败'
  if (e.code === 40100 || e.code === 401002 || e.code === 401004) return '请先登录后再操作'
  return e.message || '操作失败'
}
