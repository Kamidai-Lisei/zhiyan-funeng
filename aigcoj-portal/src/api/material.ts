import { http, unwrap } from './http'
import type { Page, Material } from '../types'

export const materialApi = {
  list: (params: { current?: number; pageSize?: number; category?: string; subject?: string; keyword?: string; sort?: string }) =>
    unwrap<Page<Material>>(http.get('/material/list/page', { params })),

  get: (id: number | string) => unwrap<{ material: Material; hasFavour: boolean }>(http.get('/material/get', { params: { id } })),

  categories: () => unwrap<{ name: string; count: number }[]>(http.get('/material/categories')),

  favourIds: () => unwrap<number[]>(http.get('/material/favour/ids')),

  favour: (materialId: number | string) => unwrap<{ favour: boolean }>(http.post('/material/favour', { materialId })),

  myFavour: () => unwrap<Material[]>(http.get('/material/my/favour')),

  download: (id: number | string) => unwrap<{ url: string; title: string }>(http.get('/material/download', { params: { id } })),

  save: (body: Partial<Material>) => unwrap<number>(http.post('/material/save', body)),

  remove: (id: number | string) => unwrap<boolean>(http.post('/material/delete', { id })),

  upload: (file: File, biz = 'material') => {
    const form = new FormData()
    form.append('file', file)
    form.append('biz', biz)
    return unwrap<{ url: string; name: string; size: number; type: string }>(
      http.post('/file/upload', form, { headers: { 'Content-Type': 'multipart/form-data' } }),
    )
  },
}
