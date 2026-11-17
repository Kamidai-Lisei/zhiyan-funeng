import { http, unwrap } from './http'

export const statsApi = {
  overview: () => unwrap<Record<string, number>>(http.get('/stats/overview')),
  ranking: () => unwrap<any[]>(http.get('/stats/ranking')),
  trend: () => unwrap<{ submissions: any[]; checkins: any[] }>(http.get('/stats/trend')),
  hot: () => unwrap<{ questions: any[]; materials: any[]; posts: any[] }>(http.get('/stats/hot')),
}
