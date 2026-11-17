<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import { aiApi, type AiMemory } from '@/api/ai'
import { studyApi } from '@/api/study'
import { fromNow } from '@/utils/format'
import Icon from '@/components/Icon.vue'
import Floral from '@/components/Floral.vue'
import MarkdownView from '@/components/MarkdownView.vue'
import EmptyState from '@/components/EmptyState.vue'

const form = reactive({
  stage: '强化阶段',
  dailyMinutes: 360,
  subjects: '数据结构,计算机组成原理,操作系统,计算机网络',
  goal: '',
  targetDate: '2026-12-19',
})

const stages = ['基础阶段', '强化阶段', '冲刺阶段', '复试准备']

const loading = ref(false)
const plan = ref('')
const daysLeft = ref(0)
const aiGenerated = ref(true)
const history = ref<AiMemory[]>([])
const historyOpen = ref<AiMemory | null>(null)

async function generate() {
  loading.value = true
  plan.value = ''
  try {
    const r = await aiApi.generatePlan({ ...form })
    plan.value = r.plan
    daysLeft.value = r.daysLeft
    aiGenerated.value = r.aiGenerated
    Message.success(r.aiGenerated ? '规划已生成并存入 AI 长期记忆' : 'AI 暂不可用，已生成基础版规划')
    history.value = await aiApi.memories('plan').catch(() => history.value)
  } catch (e: any) {
    Message.error(e?.message || '生成失败')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  history.value = await aiApi.memories('plan').catch(() => [])
  const p = await studyApi.plan().catch(() => null)
  if (p) {
    form.targetDate = String(p.targetDate || '2026-12-19').slice(0, 10)
    form.dailyMinutes = p.dailyMinutes || 360
    form.subjects = p.subjects || form.subjects
  }
})
</script>

<template>
  <div class="page">
    <div class="container">
      <section class="fade-up">
        <div class="kicker">个性化学习规划</div>
        <div class="h1" style="margin-top: 8px">计划跟着人走</div>
        <p class="muted" style="margin-top: 6px; max-width: 620px">
          不再是千人一面的复习模板 —— 规划基于你的画像数据、目标日期与每日可投入时间生成，
          覆盖基础、强化、冲刺到复试的完整周期，状态变了就重新生成一份。
        </p>
      </section>

      <div class="plan-grid section fade-up delay-1" style="margin-top: 34px">
        <div class="card card-pad">
          <div class="h3" style="margin-bottom: 16px">规划参数</div>
          <label class="fld"><span>当前阶段</span>
            <a-select v-model="form.stage">
              <a-option v-for="s in stages" :key="s" :value="s">{{ s }}</a-option>
            </a-select>
          </label>
          <label class="fld"><span>目标考试日期</span>
            <a-date-picker v-model="form.targetDate" style="width: 100%" />
          </label>
          <label class="fld"><span>每日可投入（分钟）</span>
            <a-input-number v-model="form.dailyMinutes" :min="30" :max="960" :step="30" style="width: 100%" />
          </label>
          <label class="fld"><span>复习科目</span>
            <input v-model="form.subjects" class="ipt" placeholder="逗号分隔" />
          </label>
          <label class="fld"><span>阶段目标（选填）</span>
            <input v-model="form.goal" class="ipt" placeholder="如：408 冲到 110 分" />
          </label>
          <a-button type="primary" long size="large" :loading="loading" style="margin-top: 6px" @click="generate">
            <Icon name="layers" :size="16" /> 生成我的规划
          </a-button>
        </div>

        <div>
          <div v-if="plan" class="card card-pad">
            <div class="row-between" style="margin-bottom: 14px; align-items: center">
              <span class="tag green">距初试 {{ daysLeft }} 天</span>
              <span class="muted small">{{ aiGenerated ? 'AI 结合画像生成' : '本地模板生成' }}</span>
            </div>
            <MarkdownView :content="plan" />
          </div>
          <div v-else-if="loading" class="card card-pad thinking">
            <Floral variant="wheat" :size="52" color="#c9a227" />
            <p>AI 正在结合你的薄弱点，排布全周期节奏…</p>
          </div>
          <div v-else class="card card-pad">
            <EmptyState title="还没有生成规划" desc="填好左侧参数，让 AI 为你排一份只属于你的复习路线" icon="layers" />
          </div>
        </div>
      </div>

      <section class="section fade-up delay-2">
        <div class="section-head">
          <div>
            <div class="kicker">Memory</div>
            <div class="h2" style="margin-top: 6px">历史规划版本</div>
          </div>
        </div>
        <template v-if="history.length">
          <div class="history-list">
            <div v-for="m in history" :key="m.id" class="card history-item card-hover" @click="historyOpen = m">
              <Icon name="layers" :size="18" />
              <div style="flex: 1; min-width: 0">
                <div class="history-title">{{ m.title }}</div>
                <div class="muted small">{{ fromNow(m.createTime) }}</div>
              </div>
              <Icon name="arrow-right" :size="15" />
            </div>
          </div>
        </template>
        <EmptyState v-else title="暂无历史规划" desc="每次生成都会留档，可以看到计划如何随你的状态演进" />
      </section>

      <a-modal :visible="!!historyOpen" :title="historyOpen?.title" :footer="false" :width="720" @cancel="historyOpen = null" @close="historyOpen = null">
        <MarkdownView :content="historyOpen?.content || ''" />
      </a-modal>

      <hr class="stitched" />
    </div>
  </div>
</template>

<style scoped>
.plan-grid { display: grid; grid-template-columns: 360px 1fr; gap: 22px; align-items: start; }
.fld { display: block; margin-bottom: 14px; }
.fld span { display: block; font-size: 13px; color: var(--ink-2); margin-bottom: 6px; }
.fld :deep(.arco-select-view) { width: 100%; }
.thinking { text-align: center; color: var(--muted); padding: 48px 30px; }
.thinking p { margin-top: 12px; font-size: 14px; }
.history-list { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14px; }
.history-item { display: flex; align-items: center; gap: 12px; padding: 16px 20px; cursor: pointer; color: #8a6a12; }
.history-title { font-size: 14.5px; color: var(--ink-2); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.row-between { display: flex; justify-content: space-between; }
@media (max-width: 900px) {
  .plan-grid, .history-list { grid-template-columns: 1fr; }
}
</style>
