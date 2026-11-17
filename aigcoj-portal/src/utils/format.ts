import dayjs from 'dayjs'

export function formatDate(value?: string | number | Date, pattern = 'YYYY-MM-DD') {
  if (!value) return '-'
  return dayjs(value).format(pattern)
}

export function fromNow(value?: string | number | Date) {
  if (!value) return ''
  const d = dayjs(value)
  const diff = dayjs().diff(d, 'minute')
  if (diff < 1) return '刚刚'
  if (diff < 60) return `${diff} 分钟前`
  if (diff < 60 * 24) return `${Math.floor(diff / 60)} 小时前`
  if (diff < 60 * 24 * 7) return `${Math.floor(diff / 1440)} 天前`
  return d.format('MM-DD')
}

export function formatSize(bytes?: number) {
  if (!bytes) return '未知大小'
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`
}

export function acceptance(accepted?: number, submit?: number) {
  if (!submit) return '—'
  return `${Math.round(((accepted || 0) / submit) * 100)}%`
}

export function toTags(tags?: string | string[]): string[] {
  if (!tags) return []
  if (Array.isArray(tags)) return tags
  try {
    const parsed = JSON.parse(tags)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return tags.split(',').map((t) => t.trim()).filter(Boolean)
  }
}

export function daysUntil(date: string | number | Date) {
  const target = dayjs(date).startOf('day')
  return Math.max(0, target.diff(dayjs().startOf('day'), 'day'))
}

export function downloadUrl(url?: string) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return url
}

/** 把 Markdown 正文压成一行纯文本摘要，用于列表预览 */
export function plainText(md?: string, limit = 110) {
  if (!md) return ''
  const text = md
    .replace(/```[\s\S]*?```/g, ' ')      // 代码块
    .replace(/!\[[^\]]*\]\([^)]*\)/g, ' ') // 图片
    .replace(/\[([^\]]*)\]\([^)]*\)/g, '$1') // 链接保留文字
    .replace(/^#{1,6}\s*/gm, '')          // 标题井号
    .replace(/[*_`>~|-]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
  return text.length > limit ? `${text.slice(0, limit)}…` : text
}
