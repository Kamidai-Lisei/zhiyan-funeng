<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { Message } from '@arco-design/web-vue'
import { studyApi } from '@/api/study'
import { questionApi } from '@/api/question'
import { toTags, acceptance } from '@/utils/format'
import type { Question } from '@/types'

const loading = ref(true)
const list = ref<Question[]>([])
const keyword = ref('')
const favourIds = ref<(number | string)[]>([])

const filtered = computed(() => {
  const k = keyword.value.trim().toLowerCase()
  if (!k) return list.value
  return list.value.filter(
    (q) => q.title.toLowerCase().includes(k) || toTags(q.tags).some((t) => t.toLowerCase().includes(k)),
  )
})

async function load() {
  loading.value = true
  try {
    list.value = (await studyApi.wrongBook()) || []
    try {
      favourIds.value = (await questionApi.favourIds()) || []
    } catch {
      favourIds.value = []
    }
  } catch (e: any) {
    Message.error(e?.message || '加载错题本失败')
  } finally {
    loading.value = false
  }
}

async function toggleFavour(q: Question) {
  try {
    const res = await questionApi.toggleFavour(q.id)
    if (res.favour) {
      favourIds.value = [...new Set([...favourIds.value, q.id])]
      Message.success('已加入收藏')
    } else {
      favourIds.value = favourIds.value.filter((x) => String(x) !== String(q.id))
      Message.info('已取消收藏')
    }
  } catch (e: any) {
    Message.error(e?.message || '操作失败')
  }
}

onMounted(load)
</script>

<template>
  <div class="page container">
    <header class="page-head">
      <div>
        <h1>错题本</h1>
        <p>所有未通过评测的题目会自动收集到这里，逐个攻克它们。</p>
      </div>
      <div class="head-actions">
        <input v-model="keyword" class="sel" placeholder="搜索题目 / 标签" />
        <RouterLink to="/problems" class="btn-primary sm">去题库</RouterLink>
      </div>
    </header>

    <div class="stat-row">
      <div class="mini-stat">
        <b>{{ list.length }}</b>
        <span>累计错题</span>
      </div>
      <div class="mini-stat">
        <b>{{ favourIds.length }}</b>
        <span>已收藏</span>
      </div>
      <div class="mini-stat">
        <b>{{ filtered.length }}</b>
        <span>当前显示</span>
      </div>
    </div>

    <div v-if="loading" class="card loading-box">加载中…</div>
    <div v-else-if="!filtered.length" class="card empty-box">
      <div class="empty-ic">
        <Icon name="check-circle" :size="26" />
      </div>
      <p>{{ keyword ? '没有匹配的错题' : '太棒了，暂时没有错题' }}</p>
      <RouterLink to="/problems" class="btn-primary">继续保持，去刷几道新题 →</RouterLink>
    </div>

    <div v-else class="wrong-grid">
      <article v-for="q in filtered" :key="q.id" class="card wrong-card">
        <header>
          <RouterLink :to="`/problems/${q.id}`" class="title">{{ q.title }}</RouterLink>
          <button class="star" :class="{ on: favourIds.map(String).includes(String(q.id)) }" @click="toggleFavour(q)">
            <Icon name="star" :size="15" />
          </button>
        </header>
        <div class="tags">
          <span v-for="t in toTags(q.tags).slice(0, 4)" :key="t" class="tag">{{ t }}</span>
        </div>
        <footer>
          <span class="muted small">通过率 {{ acceptance(q.acceptedNum, q.submitNum) }}</span>
          <RouterLink :to="`/problems/${q.id}`" class="link">重新挑战 →</RouterLink>
        </footer>
      </article>
    </div>
  </div>
</template>

<style scoped>
.page { display: grid; gap: 18px; }
.page-head { display: flex; align-items: flex-end; justify-content: space-between; gap: 16px; flex-wrap: wrap; }
.page-head h1 { margin: 0; font-size: 30px; color: var(--ink); letter-spacing: 0.01em; line-height: 1.35; }
.page-head p { margin: 8px 0 0; color: var(--muted); font-size: 14.5px; line-height: 1.8; }
.head-actions { display: flex; gap: 10px; align-items: center; }
.sel {
  height: 37px; padding: 0 12px; border-radius: 9px; width: 200px;
  border: 1px solid var(--line); background: var(--cloth-2); font-size: 13px; outline: none;
}
.stat-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; }
.mini-stat {
  background: var(--cloth-2); border: 1px solid var(--line); border-radius: 14px;
  padding: 16px 18px; display: flex; flex-direction: column; gap: 4px;
}
.mini-stat b { font-size: 22px; color: var(--ink); letter-spacing: -0.5px; }
.mini-stat span { font-size: 12.5px; color: var(--muted); }
.loading-box, .empty-box { padding: 64px 20px; text-align: center; color: var(--muted); }
.empty-box p { margin: 12px 0 16px; font-size: 14px; }
.empty-ic {
  width: 56px; height: 56px; margin: 0 auto; border-radius: 50%;
  display: grid; place-items: center; background: rgba(90, 143, 90, 0.12); color: var(--meadow);
}
.wrong-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 14px; }
.wrong-card { padding: 18px 20px; display: flex; flex-direction: column; gap: 12px; transition: 0.18s; }
.wrong-card:hover { transform: translateY(-2px); box-shadow: 0 14px 34px rgba(61, 58, 52, 0.1); }
.wrong-card header { display: flex; justify-content: space-between; gap: 12px; align-items: flex-start; }
.wrong-card .title { font-size: 14.5px; font-weight: 600; color: var(--ink); line-height: 1.45; }
.wrong-card .title:hover { color: var(--brand); }
.star { background: none; border: none; cursor: pointer; color: rgba(139, 115, 85, 0.30); padding: 2px; }
.star.on { color: var(--wheat-deep); }
.tags { display: flex; flex-wrap: wrap; gap: 6px; }
.wrong-card footer { display: flex; justify-content: space-between; align-items: center; margin-top: auto; }
.small { font-size: 12px; }
.link { color: var(--brand); font-size: 13px; font-weight: 500; }
@media (max-width: 720px) {
  .stat-row { grid-template-columns: 1fr; }
  .sel { width: 100%; }
  .head-actions { width: 100%; }
}
</style>
