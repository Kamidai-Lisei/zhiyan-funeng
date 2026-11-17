<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { studyApi } from '../api/study'
import { useAuthStore } from '../stores/auth'
import { chapterNeighbors, getChapter, getMarkdown, getModule } from '../content'
import { errorMessage } from '../api/http'
import MarkdownView from '../components/MarkdownView.vue'
import Icon from '../components/Icon.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const key = String(route.params.module)
const slug = String(route.params.slug)

const mod = computed(() => getModule(key))
const chapter = computed(() => getChapter(key, slug))
const content = computed(() => {
  if (!chapter.value) return ''
  const md = getMarkdown(chapter.value.file)
  // 正文首个一级标题与页面标题重复时去掉，避免出现两个同样的 h1
  return md.replace(/^\s*#\s+(.+)\r?\n/, (all, title) => (String(title).trim() === chapter.value!.title ? '' : all))
})
const neighbors = computed(() => chapterNeighbors(key, slug))
const finished = ref(false)
const loading = ref(false)

const load = async () => {
  if (!auth.isAuthed) return
  try {
    const list = await studyApi.progress(key)
    finished.value = list.some((p) => p.slug === slug && p.finished)
  } catch {}
}

const toggle = async () => {
  if (!auth.isAuthed) return Message.info('登录后可记录学习进度')
  loading.value = true
  try {
    await studyApi.saveProgress({ module: key, slug, finished: !finished.value })
    finished.value = !finished.value
    Message.success(finished.value ? '已标记完成' : '已取消完成')
  } catch (e: any) {
    Message.error(errorMessage(e))
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="container" style="max-width: 1080px" v-if="mod && chapter">
      <div class="row between" style="margin-bottom: 14px">
        <button class="btn-ghost" @click="router.push(`/learn/408/${key}`)"><Icon name="arrow-left" :size="15" /> {{ mod.title }}</button>
        <a-button size="small" :type="finished ? 'primary' : 'secondary'" :loading="loading" @click="toggle">
          <Icon name="check" :size="14" /> {{ finished ? '已完成' : '标记完成' }}
        </a-button>
      </div>

      <div class="grid" style="grid-template-columns: minmax(0, 1fr) 220px; gap: 22px; align-items: start">
        <article class="card card-pad" style="padding: 32px 38px">
          <div class="kicker">{{ mod.title }}</div>
          <h1 class="h1" style="font-size: 30px; margin: 8px 0 6px">{{ chapter.title }}</h1>
          <div class="small muted" style="margin-bottom: 22px">预计 {{ chapter.minutes }} 分钟 · 建议配合题库练习</div>
          <MarkdownView :content="content" />

          <div class="row between" style="margin-top: 34px; padding-top: 18px; border-top: 1px solid var(--line)">
            <a-button v-if="neighbors.prev" @click="router.push(`/learn/408/${key}/${neighbors.prev.slug}`)">
              <Icon name="arrow-left" :size="14" /> {{ neighbors.prev.title }}
            </a-button>
            <span v-else />
            <a-button v-if="neighbors.next" type="primary" @click="router.push(`/learn/408/${key}/${neighbors.next.slug}`)">
              {{ neighbors.next.title }} <Icon name="arrow-right" :size="14" />
            </a-button>
          </div>
        </article>

        <aside style="position: sticky; top: 88px; display: grid; gap: 14px">
          <div class="card card-pad">
            <div class="h4" style="margin-bottom: 8px">本章目录</div>
            <div v-for="s in mod.sections" :key="s.slug" class="toc-item" :class="{ active: s.slug === slug }" @click="router.push(`/learn/408/${key}/${s.slug}`)">
              {{ s.title }}
            </div>
          </div>
          <div class="card card-pad" style="background: #fdfaf4">
            <div class="h4">学完自测</div>
            <div class="small muted" style="margin: 6px 0 10px">去题库找对应标签，提交一版代码。</div>
            <a-button long size="small" type="primary" @click="router.push('/problems')">去刷题</a-button>
          </div>
        </aside>
      </div>
    </div>
  </div>
</template>

<style scoped>
.toc-item { padding: 7px 9px; border-radius: 8px; font-size: 12.5px; color: var(--muted); cursor: pointer; line-height: 1.5; }
.toc-item:hover { background: rgba(90, 143, 90, 0.08); color: var(--meadow-deep); }
.toc-item.active { background: var(--brand-soft); color: var(--brand); font-weight: 500; }
@media (max-width: 900px) { .grid[style*="220px"] { grid-template-columns: 1fr !important; } aside { position: static !important; } article { padding: 22px !important; } }
</style>
