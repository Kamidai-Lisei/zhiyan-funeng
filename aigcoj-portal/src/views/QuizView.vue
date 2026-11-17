<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { quizApi, type QuizQuestion, type QuizResultItem } from '@/api/quiz'
import { errorMessage } from '@/api/http'
import { useAuthStore } from '@/stores/auth'
import Icon from '@/components/Icon.vue'
import Floral from '@/components/Floral.vue'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()
const auth = useAuthStore()

const SUBJECTS = [
  { id: '', label: '全部' },
  { id: 'ds', label: '数据结构' },
  { id: 'co', label: '组成原理' },
  { id: 'os', label: '操作系统' },
  { id: 'cn', label: '计算机网络' },
]
const subjectName = (id: string) => SUBJECTS.find((s) => s.id === id)?.label || id

const loading = ref(true)
const questions = ref<QuizQuestion[]>([])
const subject = ref('')
const source = ref('')
const sources = ref<string[]>([])
const chosen = ref<Record<number, string>>({})
const submitting = ref(false)
const resultMap = ref<Record<number, QuizResultItem>>({})
const score = ref<number | null>(null)
const stats = ref<{ subject: string; total: number; correct: number; accuracy: number }[]>([])

const answeredCount = computed(() => Object.keys(chosen.value).length)
const submitted = computed(() => Object.keys(resultMap.value).length > 0)

async function load() {
  loading.value = true
  try {
    questions.value = await quizApi.questions({
      subject: subject.value || undefined,
      source: source.value || undefined,
    })
    chosen.value = {}
    resultMap.value = {}
    score.value = null
  } catch (e: any) {
    Message.error(errorMessage(e))
  } finally {
    loading.value = false
  }
}

function pick(q: QuizQuestion, opt: string) {
  if (submitted.value) return
  const letter = opt.slice(0, 1)
  chosen.value = { ...chosen.value, [q.id]: letter }
}

async function submit() {
  if (!auth.isAuthed) {
    Message.info('登录后才能提交判分')
    router.push({ path: '/login', query: { redirect: '/learn/quiz' } })
    return
  }
  const answers = Object.entries(chosen.value).map(([questionId, ch]) => ({ questionId: Number(questionId), chosen: ch }))
  if (!answers.length) return Message.warning('先选几道题再提交')
  submitting.value = true
  try {
    const r = await quizApi.submit(answers)
    score.value = r.score
    const map: Record<number, QuizResultItem> = {}
    r.results.forEach((i) => (map[i.questionId] = i))
    resultMap.value = map
    Message.success(`判分完成：${r.correct}/${r.total}，得分 ${r.score}`)
    loadStats()
  } catch (e: any) {
    Message.error(errorMessage(e))
  } finally {
    submitting.value = false
  }
}

function retry() {
  chosen.value = {}
  resultMap.value = {}
  score.value = null
}

function optClass(q: QuizQuestion, opt: string) {
  const letter = opt.slice(0, 1)
  const r = resultMap.value[q.id]
  if (r) {
    if (letter === r.answer) return 'right'
    if (letter === r.chosen && !r.correct) return 'wrong'
    return ''
  }
  return chosen.value[q.id] === letter ? 'picked' : ''
}

async function loadStats() {
  if (!auth.isAuthed) return
  try {
    const r = await quizApi.stats()
    stats.value = r.bySubject
  } catch {}
}

onMounted(async () => {
  try {
    const meta = await quizApi.meta()
    sources.value = Array.from(meta.sources || [])
  } catch {}
  await load()
  loadStats()
})
</script>

<template>
  <div class="page">
    <div class="container">
      <section class="fade-up">
        <div class="kicker">408 Quiz</div>
        <div class="h1" style="margin-top: 8px">在线习题与真题</div>
        <p class="muted" style="margin-top: 6px; max-width: 620px">
          四本教材的章节练习 + 往年真题改编，在线作答、即时判分、逐题解析 ——
          作答数据会汇入你的个人画像，成为薄弱点诊断的依据。
        </p>
      </section>

      <!-- 筛选 -->
      <div class="card card-pad fade-up delay-1" style="margin: 22px 0 18px">
        <div class="row between wrap gap12">
          <div class="row wrap gap8">
            <span v-for="s in SUBJECTS" :key="s.id" class="chip" :class="{ active: subject === s.id }"
              @click="subject = s.id; load()">{{ s.label }}</span>
          </div>
          <div class="row wrap gap8">
            <span class="small muted">来源</span>
            <span class="chip" :class="{ active: source === '' }" @click="source = ''; load()">全部</span>
            <span v-for="s in sources" :key="s" class="chip" :class="{ active: source === s }" @click="source = s; load()">{{ s }}</span>
          </div>
        </div>
      </div>

      <div class="quiz-grid">
        <!-- 题目区 -->
        <div>
          <div v-if="score !== null" class="card card-pad score-card">
            <Floral class="sprig tr" variant="flower" :size="44" color="#5a8f5a" />
            <div class="score-num" :class="score >= 60 ? 'pass' : 'fail'">{{ score }}<small>分</small></div>
            <div class="muted small" style="margin-top: 4px">
              本次判分 {{ Object.keys(resultMap).filter((k) => resultMap[Number(k)].correct).length }}/{{ Object.keys(resultMap).length }} 题正确
            </div>
            <div class="row gap8" style="margin-top: 14px; justify-content: center">
              <a-button size="small" @click="retry">重新作答</a-button>
              <a-button size="small" type="primary" @click="router.push('/portrait')">看看画像变化</a-button>
            </div>
          </div>

          <div v-if="loading" class="card card-pad"><div v-for="i in 4" :key="i" class="skeleton" style="height: 90px; margin-bottom: 12px" /></div>
          <template v-else-if="questions.length">
            <div v-for="(q, idx) in questions" :key="q.id" class="card card-pad quiz-card">
              <div class="row between wrap gap8">
                <div class="row gap8">
                  <span class="quiz-no">{{ idx + 1 }}</span>
                  <span class="tag">{{ subjectName(q.subject) }}</span>
                  <span class="tag violet">{{ q.chapter }}</span>
                </div>
                <span class="tag" :class="q.source.includes('真题') ? 'amber' : ''">{{ q.source }}</span>
              </div>
              <div class="quiz-stem">{{ q.question }}</div>
              <div class="quiz-options">
                <button v-for="opt in q.options" :key="opt" class="quiz-opt" :class="optClass(q, opt)" @click="pick(q, opt)">
                  {{ opt }}
                  <Icon v-if="resultMap[q.id] && opt.slice(0, 1) === resultMap[q.id].answer" name="check" :size="14" />
                </button>
              </div>
              <div v-if="resultMap[q.id]" class="quiz-analysis">
                <b :style="{ color: resultMap[q.id].correct ? 'var(--meadow-dark)' : '#8e4f4f' }">
                  {{ resultMap[q.id].correct ? '回答正确' : `回答错误，正确答案 ${resultMap[q.id].answer}` }}
                </b>
                <p>{{ resultMap[q.id].analysis }}</p>
              </div>
            </div>

            <div class="card card-pad submit-bar">
              <span class="muted small">已作答 {{ answeredCount }} / {{ questions.length }} 题</span>
              <a-button v-if="!submitted" type="primary" :loading="submitting" @click="submit">
                <Icon name="check-circle" :size="15" /> 提交判分
              </a-button>
              <a-button v-else @click="retry">再做一遍</a-button>
            </div>
          </template>
          <EmptyState v-else title="这个分类下还没有题" desc="换个科目或来源试试" icon="book" />
        </div>

        <!-- 侧栏：我的正确率 -->
        <aside>
          <div class="card card-pad" style="position: sticky; top: 88px">
            <div class="h4" style="margin-bottom: 10px">我的习题正确率</div>
            <template v-if="auth.isAuthed">
              <template v-if="stats.length">
                <div v-for="s in stats" :key="s.subject" class="side-stat">
                  <span>{{ subjectName(s.subject) }}</span>
                  <div class="side-bar"><i :style="{ width: s.accuracy + '%' }" /></div>
                  <b>{{ s.accuracy }}%</b>
                </div>
              </template>
              <p v-else class="muted small" style="line-height: 1.9">还没有作答记录。选一组题提交一次，这里就会出现你的四科正确率。</p>
            </template>
            <p v-else class="muted small" style="line-height: 1.9">登录后自动统计你的作答正确率，并汇入个人画像。</p>
            <div class="divider" />
            <p class="muted small" style="line-height: 1.9">
              错题会留在画像里，决策推演与资源推荐都会参考这些数据。
            </p>
          </div>
        </aside>
      </div>

      <hr class="stitched" />
    </div>
  </div>
</template>

<style scoped>
.quiz-grid { display: grid; grid-template-columns: minmax(0, 1fr) 280px; gap: 20px; align-items: start; }
.score-card { text-align: center; position: relative; margin-bottom: 18px; }
.score-num { font-size: 44px; font-weight: 600; }
.score-num small { font-size: 16px; margin-left: 2px; }
.score-num.pass { color: var(--meadow-dark); }
.score-num.fail { color: #8e4f4f; }
.quiz-card { margin-bottom: 16px; }
.quiz-no {
  width: 26px; height: 26px; border-radius: 8px; background: var(--brand-soft); color: var(--meadow-deep);
  display: inline-flex; align-items: center; justify-content: center; font-size: 13px; font-weight: 600;
}
.quiz-stem { margin: 14px 0 12px; font-size: 15px; line-height: 1.9; color: var(--ink); }
.quiz-options { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.quiz-opt {
  text-align: left; padding: 10px 14px; border-radius: 14px; font-size: 14px; cursor: pointer;
  border: 1.5px solid rgba(139, 115, 85, 0.2); background: #fffdf8; color: var(--ink-2);
  display: flex; align-items: center; justify-content: space-between; gap: 8px;
  transition: border-color 0.3s, background 0.3s, transform 0.3s;
}
.quiz-opt:hover { border-color: rgba(90, 143, 90, 0.45); transform: translateY(-1px); }
.quiz-opt.picked { border-color: var(--meadow); background: var(--brand-soft); color: var(--meadow-deep); font-weight: 500; }
.quiz-opt.right { border-color: var(--meadow); background: rgba(90, 143, 90, 0.12); color: var(--meadow-dark); font-weight: 600; }
.quiz-opt.wrong { border-color: #b06b6b; background: rgba(212, 160, 160, 0.16); color: #8e4f4f; }
.quiz-analysis {
  margin-top: 14px; padding: 12px 16px; border-radius: 12px; background: #fdfaf4;
  border: 1px dashed rgba(139, 115, 85, 0.22); font-size: 13.5px;
}
.quiz-analysis p { margin-top: 4px; color: var(--ink-2); line-height: 1.8; }
.submit-bar { display: flex; align-items: center; justify-content: space-between; }
.side-stat { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; font-size: 13px; color: var(--ink-2); }
.side-stat span { width: 62px; flex: none; }
.side-stat b { width: 40px; text-align: right; flex: none; }
.side-bar { flex: 1; height: 8px; border-radius: 999px; background: rgba(139, 115, 85, 0.12); overflow: hidden; }
.side-bar i { display: block; height: 100%; border-radius: 999px; background: var(--meadow); }
@media (max-width: 900px) {
  .quiz-grid { grid-template-columns: 1fr; }
  .quiz-options { grid-template-columns: 1fr; }
}
@media (prefers-reduced-motion: reduce) { .quiz-opt:hover { transform: none; } }
</style>
