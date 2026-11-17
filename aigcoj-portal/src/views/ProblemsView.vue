<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { questionApi } from '../api/question'
import { errorMessage } from '../api/http'
import { acceptance, toTags } from '../utils/format'
import { useAuthStore } from '../stores/auth'
import Icon from '../components/Icon.vue'
import EmptyState from '../components/EmptyState.vue'

const router = useRouter()
const auth = useAuthStore()

const loading = ref(true)
const rows = ref<any[]>([])
const total = ref(0)
const current = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const subject = ref('')
const difficulty = ref('')
const category = ref('')
const favourIds = ref<(number | string)[]>([])
const solvedIds = ref<string[]>([])

const categories = [
  { id: '', label: '全部题目', desc: '查看所有编程题' },
  { id: 'algo', label: '算法竞赛真题', desc: 'ACM / 蓝桥杯风格竞赛真题' },
  { id: 'machine', label: '考研上机真题', desc: '408 各科上机实现题' },
  { id: 'interview', label: '面试真题', desc: '大厂手撕高频题' },
]

const subjects = ['数据结构', '计算机组成原理', '操作系统', '计算机网络', '算法', '字符串']
const levels = ['简单', '中等', '困难']

const load = async () => {
  loading.value = true
  try {
    const tags = [subject.value, difficulty.value].filter(Boolean)
    const page = await questionApi.listPageVo({
      current: current.value,
      pageSize: pageSize.value,
      title: keyword.value || undefined,
      tags: tags.length ? tags : undefined,
      category: category.value || undefined,
      sortField: 'createTime',
      sortOrder: 'desc',
    })
    rows.value = page.records || []
    total.value = Number(page.total || 0)
  } catch (e: any) {
    Message.error(errorMessage(e))
  } finally {
    loading.value = false
  }
}

const pickSubject = (s: string) => {
  subject.value = subject.value === s ? '' : s
  current.value = 1
  load()
}

const pickLevel = (s: string) => {
  difficulty.value = difficulty.value === s ? '' : s
  current.value = 1
  load()
}

const pickCategory = (c: string) => {
  category.value = c
  current.value = 1
  load()
}

const toggleFavour = async (id: number, e: Event) => {
  e.stopPropagation()
  if (!auth.isAuthed) return Message.info('登录后可收藏题目')
  try {
    const res = await questionApi.toggleFavour(id)
    if (res.favour) favourIds.value.push(id)
    else favourIds.value = favourIds.value.filter((x) => String(x) !== String(id))
    Message.success(res.favour ? '已加入收藏' : '已取消收藏')
  } catch (e: any) {
    Message.error(errorMessage(e))
  }
}

const levelClass = (tags: string[]) => {
  if (tags.includes('简单')) return 'green'
  if (tags.includes('困难')) return 'rose'
  return 'amber'
}

onMounted(async () => {
  await load()
  if (auth.isAuthed) {
    try { favourIds.value = await questionApi.favourIds() } catch {}
    try { solvedIds.value = await questionApi.solvedIds() } catch {}
  }
})
</script>

<template>
  <div class="page">
    <div class="container">
      <div class="row between wrap gap16" style="margin-bottom: 18px">
        <div>
          <div class="kicker">Problem Set</div>
          <h1 class="h1" style="margin-top: 6px">题库练习</h1>
          <p class="muted" style="margin-top: 6px">全是在线编程题：Java / Python / C++ 提交评测，按用例计分，AI 分析代码。</p>
        </div>
        <div class="row gap12">
          <a-button @click="router.push('/submissions')"><Icon name="code" :size="15" /> 我的提交</a-button>
          <a-button @click="router.push('/wrong-book')"><Icon name="book" :size="15" /> 错题本</a-button>
        </div>
      </div>

      <div class="card card-pad" style="margin-bottom: 16px">
        <div class="cat-tabs row wrap gap8" style="margin-bottom: 14px">
          <button
            v-for="c in categories"
            :key="c.id"
            class="cat-tab"
            :class="{ active: category === c.id }"
            @click="pickCategory(c.id)"
          >
            <span class="cat-label">{{ c.label }}</span>
            <span class="cat-desc">{{ c.desc }}</span>
          </button>
        </div>
        <div class="row between wrap gap16">
          <div class="row wrap gap8">
            <span class="chip" :class="{ active: !subject && !difficulty }" @click="subject = ''; difficulty = ''; current = 1; load()">全部</span>
            <span v-for="s in subjects" :key="s" class="chip" :class="{ active: subject === s }" @click="pickSubject(s)">{{ s }}</span>
          </div>
          <div class="row gap8">
            <a-input v-model="keyword" placeholder="搜索题目标题" allow-clear style="width: 220px" @press-enter="current = 1; load()" />
            <a-button type="primary" @click="current = 1; load()"><Icon name="search" :size="15" /> 搜索</a-button>
          </div>
        </div>
        <div class="row gap8" style="margin-top: 12px">
          <span class="small muted" style="margin-right: 4px">难度</span>
          <span v-for="l in levels" :key="l" class="chip" :class="{ active: difficulty === l }" @click="pickLevel(l)">{{ l }}</span>
        </div>
      </div>

      <div class="card" style="overflow: hidden">
        <div v-if="loading" style="padding: 20px">
          <div v-for="i in 6" :key="i" class="skeleton" style="height: 46px; margin-bottom: 10px" />
        </div>
        <table v-else-if="rows.length" class="data-table">
          <thead>
            <tr>
              <th style="width: 60px">状态</th>
              <th>题目</th>
              <th style="width: 100px">难度</th>
              <th style="width: 100px">通过率</th>
              <th style="width: 90px">挑战人数</th>
              <th style="width: 70px">收藏</th>
              <th style="width: 100px"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="q in rows" :key="q.id" @click="router.push(`/problems/${q.id}`)">
              <td>
                <span v-if="solvedIds.includes(String(q.id))" class="tag green"><Icon name="check" :size="12" /> 已通过</span>
                <span v-else class="q-dot" :class="toTags(q.tags).includes('简单') ? 'easy' : toTags(q.tags).includes('困难') ? 'hard' : 'mid'" style="margin-left: 12px" />
              </td>
              <td>
                <div class="h4">{{ q.title }}</div>
                <div class="row wrap gap8" style="margin-top: 4px">
                  <span v-for="t in toTags(q.tags).slice(0, 3)" :key="t" class="tag">{{ t }}</span>
                </div>
              </td>
              <td><span class="tag" :class="levelClass(toTags(q.tags))">{{ toTags(q.tags).find((t: string) => levels.includes(t)) || '中等' }}</span></td>
              <td>{{ acceptance(q.acceptedNum, q.submitNum) }}</td>
              <td class="muted">{{ q.submitNum || 0 }}</td>
              <td @click.stop>
                <button class="icon-btn" style="width: 30px; height: 30px" @click="toggleFavour(q.id, $event)">
                  <Icon name="star" :size="14" :style="{ color: favourIds.map(String).includes(String(q.id)) ? 'var(--wheat-deep)' : 'var(--muted-2)' }" />
                </button>
              </td>
              <td><span style="color: var(--brand); font-size: 13px">开始挑战 →</span></td>
            </tr>
          </tbody>
        </table>
        <EmptyState v-else title="没有符合条件的题目" desc="试试清空筛选条件，或者换一个关键词。" icon="search" />

        <div v-if="total > pageSize" class="row between" style="padding: 14px 18px; border-top: 1px solid var(--line)">
          <span class="small muted">共 {{ total }} 道题</span>
          <a-pagination v-model:current="current" :page-size="pageSize" :total="total" size="small" @change="load" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cat-tabs { display: flex; gap: 10px; flex-wrap: wrap; }
.cat-tab {
  display: flex; flex-direction: column; align-items: flex-start; gap: 2px;
  padding: 10px 16px; border-radius: 12px; border: 1px solid var(--line);
  background: var(--paper, #fff); cursor: pointer; transition: all 0.15s;
  min-width: 150px; text-align: left;
}
.cat-tab:hover { border-color: var(--brand); }
.cat-tab.active { border-color: var(--brand); background: rgba(90, 143, 90, 0.08); }
.cat-label { font-size: 14px; font-weight: 600; color: var(--ink, #2c2c2c); }
.cat-tab.active .cat-label { color: var(--brand, #3d6b3d); }
.cat-desc { font-size: 12px; color: var(--muted-2, #888); }
.q-dot { width: 8px; height: 8px; border-radius: 50%; }
.q-dot.easy { background: var(--meadow); box-shadow: 0 0 0 4px rgba(90, 143, 90, 0.1); }
.q-dot.mid { background: var(--wheat-deep); box-shadow: 0 0 0 4px rgba(201, 162, 39, 0.1); }
.q-dot.hard { background: var(--wild-rose-deep); box-shadow: 0 0 0 4px rgba(176, 107, 107, 0.1); }
</style>
