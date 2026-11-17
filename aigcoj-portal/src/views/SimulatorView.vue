<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import { aiApi, type AiMemory, type SimulateScenario } from '@/api/ai'
import { fromNow } from '@/utils/format'
import Icon from '@/components/Icon.vue'
import Floral from '@/components/Floral.vue'
import MarkdownView from '@/components/MarkdownView.vue'
import EmptyState from '@/components/EmptyState.vue'

const form = reactive({
  school: '',
  major: '计算机科学与技术',
  currentScore: 280,
  targetScore: 340,
  dailyHours: 6,
  stage: '强化阶段',
  weakSubjects: '',
})

const stages = ['基础阶段', '强化阶段', '冲刺阶段', '二战调整期']

const loading = ref(false)
const report = ref('')
const scenarios = ref<{ daysLeft: number; rush: SimulateScenario; steady: SimulateScenario; safe: SimulateScenario } | null>(null)
const aiGenerated = ref(true)
const history = ref<AiMemory[]>([])
const historyOpen = ref<AiMemory | null>(null)

async function run() {
  if (!form.school.trim()) {
    Message.warning('先填一下目标院校')
    return
  }
  loading.value = true
  report.value = ''
  scenarios.value = null
  try {
    const r = await aiApi.simulate({ ...form })
    report.value = r.report
    scenarios.value = r.scenarios
    aiGenerated.value = r.aiGenerated
    Message.success(r.aiGenerated ? '推演完成，报告已存入 AI 长期记忆' : 'AI 暂不可用，已用本地推演模型生成')
    history.value = await aiApi.memories('simulate').catch(() => history.value)
  } catch (e: any) {
    Message.error(e?.message || '推演失败')
  } finally {
    loading.value = false
  }
}

function probClass(p: number) {
  if (p >= 65) return 'high'
  if (p >= 40) return 'mid'
  return 'low'
}

onMounted(async () => {
  history.value = await aiApi.memories('simulate').catch(() => [])
})
</script>

<template>
  <div class="page">
    <div class="container">
      <section class="fade-up">
        <div class="kicker">AI 学习决策模拟器</div>
        <div class="h1" style="margin-top: 8px">择校决策推演</div>
        <p class="muted" style="margin-top: 6px; max-width: 620px">
          把「冲多高、稳在哪、保什么」从拍脑袋变成多情景量化对比 ——
          推演会读取你的个人画像数据，不同选择的上岸概率一目了然。
        </p>
      </section>

      <div class="sim-grid section fade-up delay-1" style="margin-top: 34px">
        <!-- 表单 -->
        <div class="card card-pad sim-form">
          <div class="h3" style="margin-bottom: 16px">推演参数</div>
          <label class="fld"><span>目标院校 *</span>
            <input v-model="form.school" class="ipt" placeholder="如：浙江大学" />
          </label>
          <label class="fld"><span>目标专业</span>
            <input v-model="form.major" class="ipt" placeholder="计算机科学与技术" />
          </label>
          <div class="fld-row">
            <label class="fld"><span>当前估分</span>
              <a-input-number v-model="form.currentScore" :min="0" :max="500" />
            </label>
            <label class="fld"><span>目标分数线</span>
              <a-input-number v-model="form.targetScore" :min="0" :max="500" />
            </label>
          </div>
          <div class="fld-row">
            <label class="fld"><span>每日可投入（小时）</span>
              <a-input-number v-model="form.dailyHours" :min="1" :max="16" />
            </label>
            <label class="fld"><span>备考阶段</span>
              <a-select v-model="form.stage">
                <a-option v-for="s in stages" :key="s" :value="s">{{ s }}</a-option>
              </a-select>
            </label>
          </div>
          <label class="fld"><span>自认薄弱科目（选填）</span>
            <input v-model="form.weakSubjects" class="ipt" placeholder="如：组成原理、英语阅读" />
          </label>
          <div class="tip muted small">
            <Icon name="sparkles" :size="13" /> 推演会自动附上你的画像数据（正确率、薄弱标签、学习投入），越用越准
          </div>
          <a-button type="primary" long size="large" :loading="loading" style="margin-top: 14px" @click="run">
            <Icon name="compass" :size="16" /> 开始推演
          </a-button>
        </div>

        <!-- 结果 -->
        <div>
          <div v-if="scenarios" class="scenario-grid">
            <div v-for="s in [scenarios.rush, scenarios.steady, scenarios.safe]" :key="s.name" class="card card-pad scenario">
              <div class="scenario-name">{{ s.name }}</div>
              <div class="scenario-prob" :class="probClass(s.probability)">{{ s.probability }}<small>%</small></div>
              <div class="muted small">目标线 {{ s.line }} 分 · 预计可达 {{ s.expected }} 分</div>
            </div>
          </div>
          <div v-if="scenarios" class="muted small" style="margin: 10px 2px 0">
            距初试约 {{ scenarios.daysLeft }} 天 · {{ aiGenerated ? '报告由 AI 结合你的画像生成' : '报告由本地推演模型生成' }}
          </div>

          <div v-if="report" class="card card-pad" style="margin-top: 18px">
            <MarkdownView :content="report" />
          </div>
          <div v-else-if="loading" class="card card-pad thinking">
            <Floral variant="sprig" :size="52" color="#5a8f5a" />
            <p>AI 正在读取你的画像，推演不同情景…</p>
          </div>
          <div v-else class="card card-pad">
            <EmptyState title="还没有推演结果" desc="填好左侧参数，让数据替你做一次关键决策" icon="compass" />
          </div>
        </div>
      </div>

      <!-- 历史推演 -->
      <section class="section fade-up delay-2">
        <div class="section-head">
          <div>
            <div class="kicker">Memory</div>
            <div class="h2" style="margin-top: 6px">历史推演记录</div>
          </div>
        </div>
        <template v-if="history.length">
          <div class="history-list">
            <div v-for="m in history" :key="m.id" class="card history-item card-hover" @click="historyOpen = m">
              <Icon name="compass" :size="18" />
              <div style="flex: 1; min-width: 0">
                <div class="history-title">{{ m.title }}</div>
                <div class="muted small">{{ fromNow(m.createTime) }}</div>
              </div>
              <Icon name="arrow-right" :size="15" />
            </div>
          </div>
        </template>
        <EmptyState v-else title="暂无历史推演" desc="每一次推演都会被记住，方便你对比不同时期的判断" />
      </section>

      <a-modal :visible="!!historyOpen" :title="historyOpen?.title" :footer="false" :width="720" @cancel="historyOpen = null" @close="historyOpen = null">
        <MarkdownView :content="historyOpen?.content || ''" />
      </a-modal>

      <hr class="stitched" />
    </div>
  </div>
</template>

<style scoped>
.sim-grid { display: grid; grid-template-columns: 380px 1fr; gap: 22px; align-items: start; }
.fld { display: block; margin-bottom: 14px; }
.fld span { display: block; font-size: 13px; color: var(--ink-2); margin-bottom: 6px; }
.fld :deep(.arco-input-number), .fld :deep(.arco-select-view) { width: 100%; }
.fld-row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.tip { display: flex; align-items: center; gap: 6px; margin-top: 4px; }
.scenario-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; }
.scenario { text-align: center; }
.scenario-name { font-size: 13px; color: var(--muted); letter-spacing: 0.08em; }
.scenario-prob { font-size: 40px; font-weight: 600; margin: 8px 0 4px; }
.scenario-prob small { font-size: 16px; }
.scenario-prob.high { color: var(--meadow-dark); }
.scenario-prob.mid { color: #8a6a12; }
.scenario-prob.low { color: #8e4f4f; }
.thinking { text-align: center; color: var(--muted); padding: 48px 30px; }
.thinking p { margin-top: 12px; font-size: 14px; }
.history-list { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14px; }
.history-item { display: flex; align-items: center; gap: 12px; padding: 16px 20px; cursor: pointer; color: var(--meadow-dark); }
.history-title { font-size: 14.5px; color: var(--ink-2); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
@media (max-width: 900px) {
  .sim-grid, .history-list { grid-template-columns: 1fr; }
  .scenario-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 10px; }
}
</style>
