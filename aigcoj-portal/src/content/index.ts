import meta from './408/meta.json'

export interface Chapter {
  slug: string
  title: string
  file: string
  minutes: number
}

export interface Module {
  key: string
  title: string
  desc: string
  color: string
  sections: Chapter[]
}

const files = import.meta.glob('./408/**/*.md', {
  query: '?raw',
  import: 'default',
  eager: true,
}) as Record<string, string>

export const modules: Module[] = Object.entries(meta as Record<string, any>).map(([key, value]) => ({
  key,
  title: value.title,
  desc: value.desc,
  color: value.color,
  sections: value.sections,
}))

export const totalChapters = modules.reduce((sum, m) => sum + m.sections.length, 0)

export function getModule(key: string) {
  return modules.find((m) => m.key === key)
}

export function getChapter(moduleKey: string, slug: string) {
  const mod = getModule(moduleKey)
  return mod?.sections.find((s) => s.slug === slug)
}

export function getMarkdown(file: string) {
  return files[`./408/${file}`] || '# 内容整理中\n\n该章节正在编写，请先浏览其他章节。'
}

export function chapterNeighbors(moduleKey: string, slug: string) {
  const mod = getModule(moduleKey)
  if (!mod) return { prev: undefined, next: undefined }
  const index = mod.sections.findIndex((s) => s.slug === slug)
  return { prev: mod.sections[index - 1], next: mod.sections[index + 1] }
}
