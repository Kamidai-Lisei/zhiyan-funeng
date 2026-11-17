<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { studyApi } from '../api/study'
import { useAuthStore } from '../stores/auth'
import { getModule } from '../content'
import { errorMessage } from '../api/http'
import Icon from '../components/Icon.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const key = String(route.params.module)
const mod = computed(() => getModule(key))
const progress = ref<any[]>([])
const loading = ref(true)

const done = (slug: string) => progress.value.some((p) => p.module === key && p.slug === slug && p.finished)
const doneCount = computed(() => (mod.value?.sections || []).filter((s) => done(s.slug)).length)
const percent = computed(() => Math.round((doneCount.value / (mod.value?.sections.length || 1)) * 100))

const load = async () => {
  if (auth.isAuthed) {
    try { progress.value = await studyApi.progress(key) } catch {}
  }
  loading.value = false
}

const toggle = async (slug: string, e: Event) => {
  e.stopPropagation()
  if (!auth.isAuthed) return Message.info('登录后可记录学习进度')
  const next = !done(slug)
  try {
    await studyApi.saveProgress({ module: key, slug, finished: next })
    if (next) progress.value.push({ module: key, slug, finished: 1 })
    else progress.value = progress.value.filter((p) => !(p.module === key && p.slug === slug))
  } catch (e: any) {
    Message.error(errorMessage(e))
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="container" v-if="mod">
      <div class="row between" style="margin-bottom: 16px">
        <button class="btn-ghost" @click="router.push('/learn/408')"><Icon name="arrow-left" :size="15" /> 学习中心</button>
        <span class="small muted">408 · {{ mod.title }}</span>
      </div>

      <div class="card card-pad" style="background: linear-gradient(135deg, #fdfaf4, #ffffff); margin-bottom: 18px">
        <div class="row between wrap gap16">
          <div>
            <div class="kicker">Chapter List</div>
            <h1 class="h1" style="font-size: 28px; margin-top: 6px">{{ mod.title }}</h1>
            <p class="muted" style="margin-top: 6px">{{ mod.desc }}</p>
          </div>
          <div style="min-width: 220px">
            <div class="row between small"><span class="muted">完成进度</span><span>{{ percent }}%</span></div>
            <div class="progress-track" style="margin-top: 8px"><div class="progress-bar" :style="{ width: `${percent}%` }" /></div>
            <div class="small muted" style="margin-top: 6px">{{ doneCount }} / {{ mod.sections.length }} 章</div>
          </div>
        </div>
      </div>

      <div class="grid" style="grid-template-columns: minmax(0, 1fr) 280px; gap: 18px; align-items: start">
        <div class="card">
          <div v-for="(s, i) in mod.sections" :key="s.slug" class="chapter-row" @click="router.push(`/learn/408/${key}/${s.slug}`)">
            <div class="chapter-index" :class="{ done: done(s.slug) }">{{ done(s.slug) ? '✓' : i + 1 }}</div>
            <div style="flex: 1">
              <div class="h4">{{ s.title }}</div>
              <div class="small muted" style="margin-top: 3px">预计 {{ s.minutes }} 分钟 · 笔记 + 考点</div>
            </div>
            <button class="btn-ghost" style="font-size: 12px" @click="toggle(s.slug, $event)">{{ done(s.slug) ? '已完成' : '标记完成' }}</button>
            <Icon name="arrow-right" :size="16" style="color: var(--muted-2)" />
          </div>
        </div>

        <aside style="display: grid; gap: 14px; position: sticky; top: 88px">
          <div class="card card-pad">
            <div class="h4">本科目建议</div>
            <div class="small muted" style="margin-top: 8px; line-height: 1.9">
              · 先看目录，建立框架再进细节<br />
              · 每个知识点配 1-2 道题验证<br />
              · 大题步骤写在纸上，别只看答案<br />
              · 错题回到对应章节重读
            </div>
          </div>
          <div class="card card-pad">
            <div class="h4">继续学习</div>
            <a-button long type="primary" style="margin-top: 10px" @click="router.push(`/learn/408/${key}/${mod.sections[0].slug}`)">
              从第一章开始
            </a-button>
            <a-button long style="margin-top: 8px" @click="router.push('/problems')">做对应练习</a-button>
          </div>
        </aside>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chapter-row { display: flex; align-items: center; gap: 14px; padding: 16px 20px; border-bottom: 1px solid #f4eee2; cursor: pointer; }
.chapter-row:last-child { border-bottom: none; }
.chapter-row:hover { background: #fdfaf4; }
.chapter-index { width: 30px; height: 30px; border-radius: 9px; background: #f4eee2; display: grid; place-items: center; font-size: 13px; color: var(--muted); flex-shrink: 0; }
.chapter-index.done { background: rgba(90, 143, 90, 0.12); color: var(--meadow-dark); }
@media (max-width: 900px) { .grid[style*="280px"] { grid-template-columns: 1fr !important; } aside { position: static !important; } }
</style>
