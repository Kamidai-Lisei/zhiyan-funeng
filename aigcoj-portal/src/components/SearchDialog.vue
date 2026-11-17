<script setup lang="ts">
import { nextTick, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { searchApi } from '../api/ai'
import { useUiStore } from '../stores/ui'
import Icon from './Icon.vue'

const ui = useUiStore()
const router = useRouter()
const keyword = ref('')
const loading = ref(false)
const results = ref<{ questions: any[]; materials: any[]; posts: any[] }>({ questions: [], materials: [], posts: [] })
const inputRef = ref<HTMLInputElement>()
let timer: number | undefined

const hot = ['TCP 三次握手', '页面置换', '二叉树', 'Cache', '简历', '复试']

watch(() => ui.searchOpen, async (open) => {
  if (open) {
    await nextTick()
    inputRef.value?.focus()
  } else {
    keyword.value = ''
    results.value = { questions: [], materials: [], posts: [] }
  }
})

watch(keyword, (value) => {
  window.clearTimeout(timer)
  if (!value.trim()) {
    results.value = { questions: [], materials: [], posts: [] }
    return
  }
  timer = window.setTimeout(async () => {
    loading.value = true
    try {
      results.value = await searchApi.search(value.trim())
    } catch {
      results.value = { questions: [], materials: [], posts: [] }
    } finally {
      loading.value = false
    }
  }, 260)
})

const total = () => results.value.questions.length + results.value.materials.length + results.value.posts.length

const open = (path: string) => {
  ui.closeSearch()
  router.push(path)
}

const pickHot = (text: string) => {
  keyword.value = text
}
</script>

<template>
  <div v-if="ui.searchOpen" class="search-overlay" @click.self="ui.closeSearch()">
    <div class="search-box">
      <div class="row" style="position: relative">
        <span style="position: absolute; left: 20px; color: var(--muted-2); display: grid"><Icon name="search" :size="18" /></span>
        <input ref="inputRef" v-model="keyword" class="search-input" style="padding-left: 50px" placeholder="搜索题目、资料、帖子…" @keydown.esc="ui.closeSearch()" />
        <button class="btn-ghost" style="margin-right: 12px" @click="ui.closeSearch()">ESC</button>
      </div>

      <div v-if="!keyword" class="search-results">
        <div class="search-group-title">热门搜索</div>
        <div class="row wrap gap8" style="padding: 6px 12px 14px">
          <span v-for="h in hot" :key="h" class="chip" @click="pickHot(h)">{{ h }}</span>
        </div>
        <div class="search-group-title">快捷入口</div>
        <div class="search-item" @click="open('/problems')"><span>去题库刷一道题</span><Icon name="arrow-right" :size="15" /></div>
        <div class="search-item" @click="open('/materials')"><span>浏览考研资料库</span><Icon name="arrow-right" :size="15" /></div>
        <div class="search-item" @click="open('/learn/408')"><span>复习 408 核心考点</span><Icon name="arrow-right" :size="15" /></div>
      </div>

      <div v-else class="search-results">
        <div v-if="loading" class="small muted" style="padding: 16px">正在搜索…</div>
        <template v-else>
          <template v-if="results.questions.length">
            <div class="search-group-title">题目 · {{ results.questions.length }}</div>
            <div v-for="q in results.questions" :key="q.id" class="search-item" @click="open(`/problems/${q.id}`)">
              <span>{{ q.title }}</span><span class="small muted">题目</span>
            </div>
          </template>
          <template v-if="results.materials.length">
            <div class="search-group-title">资料 · {{ results.materials.length }}</div>
            <div v-for="m in results.materials" :key="m.id" class="search-item" @click="open('/materials')">
              <span>{{ m.title }}</span><span class="small muted">{{ m.category }}</span>
            </div>
          </template>
          <template v-if="results.posts.length">
            <div class="search-group-title">帖子 · {{ results.posts.length }}</div>
            <div v-for="p in results.posts" :key="p.id" class="search-item" @click="open(`/forum/${p.id}`)">
              <span>{{ p.title }}</span><span class="small muted">帖子</span>
            </div>
          </template>
          <div v-if="!total()" class="empty" style="padding: 36px">
            <div class="empty-icon"><Icon name="search" :size="22" /></div>
            <div>没有找到「{{ keyword }}」相关内容</div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>
