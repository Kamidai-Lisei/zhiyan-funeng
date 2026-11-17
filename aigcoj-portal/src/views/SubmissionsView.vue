<script setup lang="ts">
import { onMounted, reactive, ref, computed } from 'vue'
import { Message } from '@arco-design/web-vue'
import { questionApi } from '@/api/question'
import { fromNow } from '@/utils/format'
import type { QuestionSubmit } from '@/types'

const loading = ref(false)
const records = ref<QuestionSubmit[]>([])
const total = ref(0)
const query = reactive({ current: 1, pageSize: 10, language: '', status: '' })

const STATUS: Record<number, { label: string; cls: string }> = {
  0: { label: '等待判题', cls: 'pending' },
  1: { label: '判题中', cls: 'judging' },
  2: { label: '通过', cls: 'pass' },
  3: { label: '未通过', cls: 'fail' },
}

const pages = computed(() => Math.max(1, Math.ceil(total.value / query.pageSize)))

async function load() {
  loading.value = true
  try {
    const body: Record<string, unknown> = { current: query.current, pageSize: query.pageSize }
    if (query.language) body.language = query.language
    if (query.status !== '') body.status = Number(query.status)
    const page = await questionApi.submissions(body)
    records.value = page.records || []
    total.value = page.total || 0
  } catch (e: any) {
    Message.error(e?.message || '加载提交记录失败')
  } finally {
    loading.value = false
  }
}

function search() {
  query.current = 1
  load()
}

function go(p: number) {
  if (p < 1 || p > pages.value) return
  query.current = p
  load()
}

const detail = ref<QuestionSubmit | null>(null)
const code = ref('')
const codeLoading = ref(false)

async function openDetail(row: QuestionSubmit) {
  detail.value = row
  code.value = row.code || ''
  if (!code.value) {
    codeLoading.value = true
    try {
      code.value = await questionApi.getCode(row.id)
    } catch {
      code.value = '// 代码加载失败'
    } finally {
      codeLoading.value = false
    }
  }
}

const MESSAGE_CN: Record<string, string> = {
  'Accepted': '答案正确',
  'Wrong Answer': '答案错误',
  'Compile Error': '编译错误',
  'Runtime Error': '运行错误',
  'Time Limit Exceeded': '运行超时',
  'Memory Limit Exceeded': '内存超限',
}

function verdict(row: QuestionSubmit) {
  if (row.status === 2) {
    const msg = row.judgeInfo?.message || 'Wrong Answer'
    return MESSAGE_CN[msg] || msg
  }
  if (row.status === 3) return '判题失败'
  return STATUS[row.status ?? 0]?.label || '未知'
}

function scoreOf(row: QuestionSubmit) {
  return row.status === 2 ? row.judgeInfo?.score ?? null : null
}

function verdictCls(row: QuestionSubmit) {
  if (row.status === 2) return row.judgeInfo?.message === 'Accepted' ? 'pass' : 'fail'
  return STATUS[row.status ?? 0]?.cls || ''
}

onMounted(load)
</script>

<template>
  <div class="page container">
    <header class="page-head">
      <div>
        <h1>我的提交</h1>
        <p>每一次提交都会被记录，失败也是进步的台阶。</p>
      </div>
      <RouterLink to="/problems" class="btn-ghost">去刷题 →</RouterLink>
    </header>

    <div class="card toolbar">
      <select v-model="query.language" class="sel" @change="search">
        <option value="">全部语言</option>
        <option value="java">Java</option>
        <option value="cpp">C++</option>
        <option value="python">Python</option>
      </select>
      <select v-model="query.status" class="sel" @change="search">
        <option value="">全部状态</option>
        <option value="2">通过</option>
        <option value="3">未通过</option>
        <option value="0">等待判题</option>
        <option value="1">判题中</option>
      </select>
      <span class="muted">共 {{ total }} 条提交</span>
      <button class="btn-ghost sm" @click="load">刷新</button>
    </div>

    <div v-if="loading" class="card loading-box">加载中…</div>
    <div v-else-if="!records.length" class="card empty-box">
      <p>还没有提交记录</p>
      <RouterLink to="/problems" class="btn-primary">去题库挑一道题</RouterLink>
    </div>

    <div v-else class="card table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th style="width: 62px">#</th>
            <th>题目</th>
            <th style="width: 96px">语言</th>
            <th style="width: 150px">结果</th>
            <th style="width: 70px">得分</th>
            <th style="width: 150px">耗时 / 内存</th>
            <th style="width: 120px">提交时间</th>
            <th style="width: 70px" />
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in records" :key="row.id">
            <td class="mono muted">{{ row.id }}</td>
            <td>
              <RouterLink :to="`/problems/${row.questionId}`" class="q-title">
                {{ row.questionVO?.title || `题目 #${row.questionId}` }}
              </RouterLink>
            </td>
            <td><span class="chip tiny">{{ row.language }}</span></td>
            <td>
              <span class="verdict" :class="verdictCls(row)">
                {{ verdict(row) }}
              </span>
            </td>
            <td class="mono">
              <b v-if="scoreOf(row) != null" :style="{ color: scoreOf(row)! >= 60 ? 'var(--meadow-dark)' : '#8e4f4f' }">{{ scoreOf(row) }}</b>
              <span v-else class="muted">—</span>
            </td>
            <td class="mono muted small" style="white-space: nowrap">
              {{ row.judgeInfo?.time != null ? row.judgeInfo.time + ' ms' : '—' }} /
              {{ row.judgeInfo?.memory != null ? row.judgeInfo.memory + ' KB' : '—' }}
            </td>
            <td class="muted small">{{ fromNow(row.createTime) }}</td>
            <td>
              <button class="link" @click="openDetail(row)">代码</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="pages > 1" class="pager">
      <button class="btn-ghost sm" :disabled="query.current === 1" @click="go(query.current - 1)">上一页</button>
      <span class="muted">{{ query.current }} / {{ pages }}</span>
      <button class="btn-ghost sm" :disabled="query.current === pages" @click="go(query.current + 1)">下一页</button>
    </div>

    <div v-if="detail" class="modal-mask" @click.self="detail = null">
      <div class="modal">
        <header>
          <b>{{ detail.questionVO?.title || `提交 #${detail.id}` }}</b>
          <button class="link" @click="detail = null">关闭</button>
        </header>
        <div class="meta">
          <span class="chip tiny">{{ detail.language }}</span>
          <span class="verdict" :class="STATUS[detail.status ?? 0]?.cls">{{ verdict(detail) }}</span>
          <span class="muted small">{{ detail.createTime }}</span>
        </div>
        <pre class="code-box">{{ codeLoading ? '加载代码中…' : code }}</pre>
        <footer>
          <RouterLink :to="`/problems/${detail.questionId}`" class="btn-primary sm">打开题目</RouterLink>
        </footer>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page { display: grid; gap: 18px; }
.page-head { display: flex; align-items: flex-end; justify-content: space-between; gap: 16px; flex-wrap: wrap; }
.page-head h1 { margin: 0; font-size: 30px; color: var(--ink); letter-spacing: 0.01em; line-height: 1.35; }
.page-head p { margin: 8px 0 0; color: var(--muted); font-size: 14.5px; line-height: 1.8; }
.toolbar { display: flex; align-items: center; gap: 12px; padding: 14px 18px; flex-wrap: wrap; }
.sel {
  height: 36px; padding: 0 12px; border-radius: 9px;
  border: 1px solid var(--line); background: var(--cloth-2); color: var(--ink); font-size: 13px; outline: none;
}
.toolbar .muted { margin-left: auto; font-size: 12.5px; }
.loading-box, .empty-box { padding: 60px 20px; text-align: center; color: var(--muted); }
.empty-box p { margin: 0 0 14px; }
.table-wrap { padding: 4px 4px 8px; overflow-x: auto; }
.q-title { color: var(--ink); font-weight: 600; font-size: 13.5px; }
.q-title:hover { color: var(--brand); }
.verdict { font-size: 12.5px; font-weight: 600; }
.verdict.pass { color: var(--meadow-deep); }
.verdict.fail { color: var(--wild-rose-deep); }
.verdict.pending, .verdict.judging { color: var(--wheat-deep); }
.mono { font-family: ui-monospace, Menlo, monospace; }
.small { font-size: 12px; }
.link { background: none; border: none; color: var(--brand); font-size: 13px; cursor: pointer; padding: 0; }
.pager { display: flex; align-items: center; justify-content: center; gap: 12px; font-size: 13px; }
.modal-mask {
  position: fixed; inset: 0; background: rgba(61, 58, 52, 0.45);
  display: grid; place-items: center; z-index: 90; padding: 20px;
}
.modal {
  width: min(760px, 100%); max-height: 82vh; background: var(--cloth-2); border-radius: 16px;
  padding: 20px 22px; display: flex; flex-direction: column; gap: 12px;
  box-shadow: 0 24px 60px rgba(61, 58, 52, 0.28);
}
.modal header { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
.modal .meta { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.code-box {
  flex: 1; overflow: auto; background: var(--ink); color: rgba(139, 115, 85, 0.22); border-radius: 12px;
  padding: 16px; font-size: 12.5px; line-height: 1.65; font-family: ui-monospace, Menlo, monospace;
  margin: 0; white-space: pre;
}
.modal footer { display: flex; justify-content: flex-end; }
</style>
