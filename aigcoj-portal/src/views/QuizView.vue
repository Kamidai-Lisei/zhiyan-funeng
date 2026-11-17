<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import { quizApi, type QuizQuestion, type QuizPaper, type SingleResult } from '@/api/quiz'
import { errorMessage } from '@/api/http'
import { useAuthStore } from '@/stores/auth'
import Icon from '@/components/Icon.vue'
import EmptyState from '@/components/EmptyState.vue'

const auth = useAuthStore()

const SUBJECTS = [
  { id: '', label: '全部科目' },
  { id: 'ds', label: '数据结构' },
  { id: 'co', label: '计组' },
  { id: 'os', label: '操作系统' },
  { id: 'cn', label: '计网' },
]
const SUBJECT_LABEL: Record<string, string> = { ds: '数据结构', co: '计算机组成原理', os: '操作系统', cn: '计算机网络' }

const mode = ref<'pick' | 'practice' | 'exam'>('pick')
const papers = ref<QuizPaper[]>([])
const selectedPaper = ref<QuizPaper | null>(null)
const subject = ref('')
const loading = ref(false)

const questions = ref<QuizQuestion[]>([])
const idx = ref(0)
const answers = ref<Record<number, string>>({})          // 选择题所选字母
const textAnswers = ref<Record<number, string>>({})      // 大题作答文本
const results = ref<Record<number, SingleResult>>({})    // 已判结果
const judging = ref(false)
const examLeft = ref(0)                                   // 模拟考试剩余秒
const examReport = ref<{ correct: number; total: number; score: number } | null>(null)
let timer: ReturnType<typeof setInterval> | null = null

const stats = ref<{ answered: number; bySubject: { subject: string; total: number; correct: number; accuracy: number }[] } | null>(null)

const cur = computed(() => questions.value[idx.value] || null)
const curResult = computed(() => (cur.value ? results.value[cur.value.id] : null))
const progress = computed(() => {
  if (!questions.value.length) return 0
  const done = questions.value.filter((q) => results.value[q.id] || answers.value[q.id] || textAnswers.value[q.id]).length
  return Math.round((done / questions.value.length) * 100)
})

const loadPapers = async () => {
  loading.value = true
  try {
    papers.value = await quizApi.papers()
  } catch (e: any) {
    Message.error(errorMessage(e))
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  if (!auth.isAuthed) return
  try {
    stats.value = await quizApi.stats()
  } catch {}
}

const realPapers = computed(() => papers.value.filter((p) => p.kind === '真题'))
const mockPapers = computed(() => papers.value.filter((p) => p.kind === '模拟'))
const chapterPapers = computed(() => papers.value.filter((p) => p.kind === '章节'))

const openPaper = async (p: QuizPaper, asExam: boolean) => {
  loading.value = true
  selectedPaper.value = p
  try {
    questions.value = await quizApi.questions({ paper: p.name, subject: p.kind === '章节' && subject.value ? subject.value : undefined })
    if (!questions.value.length) {
      Message.warning('该卷暂无题目，换个筛选试试')
      loading.value = false
      return
    }
    idx.value = 0
    answers.value = {}
    textAnswers.value = {}
    results.value = {}
    examReport.value = null
    mode.value = asExam ? 'exam' : 'practice'
    if (asExam) startTimer()
  } catch (e: any) {
    Message.error(errorMessage(e))
  } finally {
    loading.value = false
  }
}

const startTimer = () => {
  stopTimer()
  examLeft.value = 180 * 60
  timer = setInterval(() => {
    examLeft.value--
    if (examLeft.value <= 0) {
      stopTimer()
      Message.warning('时间到，自动交卷')
      submitExam()
    }
  }, 1000)
}
const stopTimer = () => {
  if (timer) clearInterval(timer)
  timer = null
}
const fmtTime = (s: number) => {
  const h = Math.floor(s / 3600)
  const m = Math.floor((s % 3600) / 60)
  const sec = s % 60
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(sec).padStart(2, '0')}`
}

/** 练习模式：单题提交判分（大题走 AI 评阅） */
const submitOne = async () => {
  const q = cur.value
  if (!q) return
  if (!auth.isAuthed) return Message.info('登录后可作答并保存记录')
  if (q.type === 'choice' && !answers.value[q.id]) return Message.warning('请先选择答案')
  if (q.type === 'subjective' && !textAnswers.value[q.id]?.trim()) return Message.warning('请先作答')
  judging.value = true
  try {
    const res = await quizApi.single({
      questionId: q.id,
      chosen: q.type === 'choice' ? answers.value[q.id] : undefined,
      answerText: q.type === 'subjective' ? textAnswers.value[q.id] : undefined,
    })
    results.value = { ...results.value, [q.id]: res }
  } catch (e: any) {
    Message.error(errorMessage(e))
  } finally {
    judging.value = false
  }
}

const submitExam = async () => {
  stopTimer()
  if (!auth.isAuthed) return Message.info('登录后可提交试卷')
  const choiceQs = questions.value.filter((q) => q.type === 'choice')
  const filled = choiceQs.filter((q) => answers.value[q.id]).length
  if (!filled) return Message.warning('还没有作答任何选择题')
  try {
    const res = await quizApi.submit(choiceQs.filter((q) => answers.value[q.id]).map((q) => ({ questionId: q.id, chosen: answers.value[q.id] })))
    examReport.value = { correct: res.correct, total: res.total, score: res.score }
    // 把选择题结果写进 results 供逐题回看
    for (const r of res.results) {
      results.value = { ...results.value, [r.questionId]: { questionId: r.questionId, type: 'choice', answer: r.answer, analysis: r.analysis, correct: r.correct } }
    }
    Message.success(`交卷成功：选择题正确 ${res.correct}/${res.total}`)
  } catch (e: any) {
    Message.error(errorMessage(e))
  }
}

const backToPick = () => {
  stopTimer()
  mode.value = 'pick'
  selectedPaper.value = null
  examReport.value = null
  loadStats()
}

const go = (i: number) => {
  if (i >= 0 && i < questions.value.length) idx.value = i
}

onMounted(() => {
  loadPapers()
  loadStats()
})
onUnmounted(stopTimer)
</script>

<template>
  <div class="page">
    <div class="container quiz-page">
      <!-- 选择卷子 -->
      <template v-if="mode === 'pick'">
        <div class="row between wrap gap16" style="margin-bottom: 18px">
          <div>
            <div class="kicker">408 Practice</div>
            <h1 class="h1" style="margin-top: 6px">408 在线刷题</h1>
            <p class="muted" style="margin-top: 6px">
              历年真题 · 全真模拟 · 章节精练：逐题作答即时判分，大题由 AI 逐句评阅；支持 180 分钟整卷限时模拟。
            </p>
          </div>
          <div class="card card-pad" v-if="stats" style="min-width: 240px">
            <div class="small muted">我的刷题记录</div>
            <div class="h2" style="margin: 4px 0">{{ stats.answered }} 题次</div>
            <div class="row wrap gap8">
              <span v-for="s in stats.bySubject" :key="s.subject" class="tag">
                {{ SUBJECT_LABEL[s.subject] || s.subject }} {{ s.accuracy }}%
              </span>
            </div>
          </div>
        </div>

        <div class="qz-group" v-for="(group, gi) in [
            { title: '历年真题卷', icon: 'file', list: realPapers, tint: 'green' },
            { title: '全真模拟卷', icon: 'sparkles', list: mockPapers, tint: 'amber' },
            { title: '章节精练', icon: 'book', list: chapterPapers, tint: 'blue' },
          ]" :key="gi">
          <div class="qz-group-head">
            <Icon :name="group.icon" :size="17" />
            <span class="h3">{{ group.title }}</span>
          </div>
          <div v-if="group.title === '章节精练'" class="row gap8" style="margin: 8px 0 4px">
            <span v-for="s in SUBJECTS" :key="s.id" class="chip" :class="{ active: subject === s.id }" @click="subject = s.id">
              {{ s.label }}
            </span>
          </div>
          <div class="qz-papers">
            <button v-for="p in group.list" :key="p.name" class="qz-paper-card" @click="openPaper(p, false)">
              <div class="qz-paper-name">{{ p.name }}</div>
              <div class="small muted" style="margin-top: 4px">
                选择题 {{ p.choice }} · 大题 {{ p.subjective }} · 共 {{ p.total }} 题
              </div>
              <div class="row gap8" style="margin-top: 10px">
                <span class="qz-mode-btn" @click.stop="openPaper(p, false)"><Icon name="edit" :size="13" /> 逐题练习</span>
                <span v-if="p.kind !== '章节'" class="qz-mode-btn exam" @click.stop="openPaper(p, true)">
                  <Icon name="clock" :size="13" /> 整卷模拟
                </span>
              </div>
            </button>
            <EmptyState v-if="!group.list.length && !loading" title="暂无试卷" icon="book" />
          </div>
        </div>
      </template>

      <!-- 练习 / 考试模式 -->
      <template v-else>
        <div class="row between wrap gap12" style="margin-bottom: 14px">
          <div class="row gap12">
            <button class="btn-ghost" @click="backToPick"><Icon name="arrow-left" :size="15" /> 返回选卷</button>
            <span class="h3">{{ selectedPaper?.name }}</span>
            <span class="tag">{{ mode === 'exam' ? '整卷模拟' : '逐题练习' }}</span>
          </div>
          <div class="row gap8">
            <span v-if="mode === 'exam'" class="qz-timer" :class="{ urgent: examLeft < 300 }">
              <Icon name="clock" :size="14" /> {{ fmtTime(examLeft) }}
            </span>
            <span class="small muted">{{ progress }}% 已作答</span>
            <a-button v-if="mode === 'exam'" type="primary" size="small" @click="submitExam">交卷</a-button>
          </div>
        </div>

        <div class="problem-layout">
          <!-- 左：题目 -->
          <div class="problem-pane">
            <div class="problem-pane-head">
              <div class="row gap8">
                <span class="h3">第 {{ idx + 1 }} / {{ questions.length }} 题</span>
                <span class="tag">{{ SUBJECT_LABEL[cur?.subject || ''] || cur?.subject }}</span>
                <span class="tag" :class="cur?.type === 'subjective' ? 'blue' : 'green'">
                  {{ cur?.type === 'subjective' ? '综合应用题' : '单项选择题' }}
                </span>
                <span v-if="cur?.chapter" class="small muted">{{ cur.chapter }}</span>
              </div>
            </div>
            <div class="problem-scroll">
              <template v-if="cur">
                <div class="qz-stem">{{ cur.question }}</div>
                <div v-if="cur.type === 'choice'" class="qz-options">
                  <label
                    v-for="o in cur.options"
                    :key="o"
                    class="qz-option"
                    :class="{
                      picked: answers[cur.id] === o[0],
                      right: curResult?.type === 'choice' && curResult?.answer === o[0],
                      wrong: curResult?.type === 'choice' && answers[cur.id] === o[0] && !curResult?.correct,
                    }"
                  >
                    <input type="radio" :name="'q' + cur.id" :value="o[0]" v-model="answers[cur.id]" :disabled="!!curResult" />
                    <span>{{ o }}</span>
                  </label>
                </div>
                <div v-else class="qz-subjective">
                  <a-textarea
                    v-model="textAnswers[cur.id]"
                    placeholder="写下你的解题思路、步骤与结论（AI 将对照参考答案逐点评阅）…"
                    :auto-size="{ minRows: 8, maxRows: 16 }"
                    :disabled="!!curResult"
                  />
                </div>

                <!-- 判分结果 -->
                <div v-if="curResult" class="qz-verdict" :class="curResult.correct === true ? 'ok' : curResult.aiReviewed ? 'ai' : 'bad'">
                  <template v-if="curResult.aiReviewed">
                    <div class="qz-verdict-title"><Icon name="sparkles" :size="15" /> AI 评阅结果</div>
                    <pre class="qz-verdict-text">{{ curResult.aiComment }}</pre>
                  </template>
                  <template v-else>
                    <div class="qz-verdict-title" :class="curResult.correct ? 'ok' : 'bad'">
                      {{ curResult.correct ? '✓ 回答正确' : `× 回答错误，正确答案是 ${curResult.answer}` }}
                    </div>
                  </template>
                  <div v-if="curResult.analysis" class="qz-analysis">
                    <div class="qz-analysis-head"><Icon name="book" :size="14" /> 详细解析</div>
                    <div class="qz-analysis-body">{{ curResult.analysis }}</div>
                  </div>
                  <div v-if="curResult.answerLong" class="qz-analysis">
                    <div class="qz-analysis-head"><Icon name="file" :size="14" /> 参考答案要点</div>
                    <div class="qz-analysis-body">{{ curResult.answerLong }}</div>
                  </div>
                </div>

                <div class="row between" style="margin-top: 18px">
                  <a-button :disabled="idx === 0" @click="go(idx - 1)"><Icon name="arrow-left" :size="14" /> 上一题</a-button>
                  <div class="row gap8">
                    <a-button v-if="!curResult" type="primary" :loading="judging" @click="submitOne">
                      <Icon name="check" :size="14" />
                      {{ cur.type === 'subjective' ? '提交给 AI 评阅' : '提交本题' }}
                    </a-button>
                    <a-button v-if="idx < questions.length - 1" type="outline" @click="go(idx + 1)">下一题 <Icon name="arrow-right" :size="14" /></a-button>
                  </div>
                </div>
              </template>
            </div>
          </div>

          <!-- 右：答题卡 -->
          <div class="problem-pane" style="max-width: 330px">
            <div class="problem-pane-head"><span class="h4">答题卡</span></div>
            <div style="padding: 16px; overflow-y: auto; flex: 1">
              <div class="qz-grid">
                <button
                  v-for="(q, i) in questions"
                  :key="q.id"
                  class="qz-cell"
                  :class="{
                    cur: i === idx,
                    done: !!results[q.id] || (q.type === 'choice' && answers[q.id]) || (q.type === 'subjective' && textAnswers[q.id]),
                    right: results[q.id]?.correct === true,
                    wrong: results[q.id] && results[q.id].correct === false && !results[q.id].aiReviewed,
                    subj: q.type === 'subjective',
                  }"
                  @click="go(i)"
                >
                  {{ i + 1 }}
                </button>
              </div>
              <div class="small muted" style="margin-top: 12px; line-height: 2">
                <span class="qz-legend ok" /> 回答正确
                <span class="qz-legend bad" /> 回答错误
                <span class="qz-legend subj" /> 大题（AI 评阅）
                <span class="qz-legend done" /> 已作答
              </div>

              <!-- 模拟考试报告 -->
              <div v-if="examReport" class="qz-report">
                <div class="h3">交卷报告</div>
                <div class="qz-report-score">{{ examReport.score }}<span>分</span></div>
                <div class="small muted">选择题正确 {{ examReport.correct }} / {{ examReport.total }}</div>
                <div class="small" style="margin-top: 8px">
                  大题可在逐题练习模式中提交给 AI 评阅，或查看左侧解析。
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.quiz-page { max-width: 1280px; }
.qz-group { margin-bottom: 22px; }
.qz-group-head { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; color: var(--brand, #3d6b3d); }
.qz-papers { display: grid; grid-template-columns: repeat(auto-fill, minmax(230px, 1fr)); gap: 12px; }
.qz-paper-card {
  text-align: left; padding: 14px 16px; border-radius: 14px; cursor: pointer;
  border: 1px solid var(--line, #e5e0d4); background: var(--cloth-2, #fffdf8); transition: all 0.15s;
}
.qz-paper-card:hover { border-color: var(--brand, #3d6b3d); transform: translateY(-2px); box-shadow: 0 6px 18px rgba(61, 107, 61, 0.12); }
.qz-paper-name { font-weight: 700; font-size: 14.5px; color: var(--ink, #2c2c2c); }
.qz-mode-btn {
  display: inline-flex; align-items: center; gap: 4px; font-size: 12px;
  padding: 4px 10px; border-radius: 99px; background: rgba(90, 143, 90, 0.12); color: #3d6b3d;
}
.qz-mode-btn.exam { background: rgba(201, 162, 39, 0.14); color: #8a6d1a; }
.qz-stem { font-size: 15.5px; line-height: 1.9; color: var(--ink, #2c2c2c); margin-bottom: 14px; white-space: pre-wrap; }
.qz-options { display: flex; flex-direction: column; gap: 8px; }
.qz-option {
  display: flex; gap: 10px; align-items: flex-start; padding: 11px 14px; border-radius: 10px;
  border: 1.5px solid var(--line, #e5e0d4); cursor: pointer; transition: all 0.12s; font-size: 14.5px; line-height: 1.7;
}
.qz-option:hover { border-color: var(--brand, #3d6b3d); }
.qz-option.picked { border-color: #3d6b3d; background: rgba(90, 143, 90, 0.07); }
.qz-option.right { border-color: #4a9a4a; background: rgba(74, 154, 74, 0.1); }
.qz-option.wrong { border-color: #c25b5b; background: rgba(194, 91, 91, 0.08); }
.qz-option input { margin-top: 4px; accent-color: #3d6b3d; }
.qz-verdict { margin-top: 16px; padding: 14px 16px; border-radius: 12px; border: 1.5px dashed; }
.qz-verdict.ok { border-color: #4a9a4a; background: rgba(74, 154, 74, 0.06); }
.qz-verdict.bad { border-color: #c25b5b; background: rgba(194, 91, 91, 0.05); }
.qz-verdict.ai { border-color: #8a6d1a; background: rgba(201, 162, 39, 0.06); }
.qz-verdict-title { font-weight: 700; display: flex; align-items: center; gap: 6px; margin-bottom: 8px; }
.qz-verdict-title.ok { color: #2f7d2f; }
.qz-verdict-title.bad { color: #a84040; }
.qz-verdict-text { font-family: inherit; font-size: 13.5px; line-height: 1.8; white-space: pre-wrap; margin: 0; color: var(--ink, #2c2c2c); }
.qz-analysis { margin-top: 10px; background: rgba(255, 255, 255, 0.75); border-radius: 10px; padding: 10px 14px; }
.qz-analysis-head { font-size: 13px; font-weight: 700; color: #3d6b3d; display: flex; align-items: center; gap: 5px; margin-bottom: 4px; }
.qz-analysis-body { font-size: 13.5px; line-height: 1.85; color: #444; white-space: pre-wrap; }
.qz-timer {
  display: inline-flex; align-items: center; gap: 6px; font-weight: 700; font-variant-numeric: tabular-nums;
  padding: 5px 14px; border-radius: 99px; background: rgba(61, 107, 61, 0.1); color: #3d6b3d; font-size: 15px;
}
.qz-timer.urgent { background: rgba(194, 91, 91, 0.12); color: #a84040; animation: qz-pulse 1s infinite; }
@keyframes qz-pulse { 50% { opacity: 0.6; } }
.qz-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(40px, 1fr)); gap: 7px; }
.qz-cell {
  height: 38px; border-radius: 9px; border: 1px solid var(--line, #e5e0d4); background: #fff;
  font-size: 13px; font-weight: 600; cursor: pointer; color: #666; position: relative;
}
.qz-cell.done { background: rgba(90, 143, 90, 0.14); color: #3d6b3d; border-color: rgba(90, 143, 90, 0.4); }
.qz-cell.right { background: rgba(74, 154, 74, 0.25); color: #1f5c1f; }
.qz-cell.wrong { background: rgba(194, 91, 91, 0.2); color: #8f2f2f; }
.qz-cell.subj::after { content: 'AI'; position: absolute; top: -5px; right: -4px; font-size: 9px; background: #c9a227; color: #fff; border-radius: 99px; padding: 0 4px; }
.qz-cell.cur { outline: 2px solid #3d6b3d; outline-offset: 1px; }
.qz-legend { display: inline-block; width: 10px; height: 10px; border-radius: 3px; margin-right: 4px; vertical-align: -1px; }
.qz-legend.ok { background: rgba(74, 154, 74, 0.5); }
.qz-legend.bad { background: rgba(194, 91, 91, 0.45); }
.qz-legend.done { background: rgba(90, 143, 90, 0.25); }
.qz-legend.subj { background: #c9a227; }
.qz-report { margin-top: 16px; padding: 14px; border-radius: 12px; background: rgba(61, 107, 61, 0.08); text-align: center; }
.qz-report-score { font-size: 34px; font-weight: 800; color: #3d6b3d; }
.qz-report-score span { font-size: 14px; font-weight: 600; }
</style>
