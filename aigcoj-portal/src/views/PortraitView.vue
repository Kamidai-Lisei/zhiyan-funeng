<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Message, Modal } from '@arco-design/web-vue'
import { studyApi, type Portrait } from '@/api/study'
import { aiApi, type AiMemory } from '@/api/ai'
import { fromNow } from '@/utils/format'
import Icon from '@/components/Icon.vue'
import Floral from '@/components/Floral.vue'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()
const portrait = ref<Portrait | null>(null)
const memories = ref<AiMemory[]>([])
const loading = ref(true)
const summary = ref('')
const summaryLoading = ref(false)

const memoryTypeName: Record<string, string> = {
  portrait: '画像快照',
  simulate: '决策推演',
  plan: '学习规划',
  note: '学习笔记',
}

function masteryColor(m: number) {
  if (m >= 80) return 'var(--meadow)'
  if (m >= 60) return '#c9a227'
  return '#b06b6b'
}

async function load() {
  loading.value = true
  try {
    const [p, m] = await Promise.all([
      studyApi.portrait(),
      aiApi.memories().catch(() => [] as AiMemory[]),
    ])
    portrait.value = p
    memories.value = m
  } catch (e: any) {
    Message.error(e?.message || '画像加载失败')
  } finally {
    loading.value = false
  }
}

async function makeSummary() {
  summaryLoading.value = true
  try {
    const r = await aiApi.portraitSummary()
    summary.value = r.summary
    Message.success(r.aiGenerated ? 'AI 画像总结已生成并存入长期记忆' : 'AI 暂不可用，已生成基础版总结')
    memories.value = await aiApi.memories().catch(() => memories.value)
  } catch (e: any) {
    Message.error(e?.message || '生成失败')
  } finally {
    summaryLoading.value = false
  }
}

function removeMemory(m: AiMemory) {
  Modal.confirm({
    title: '删除这条记忆？',
    content: `「${m.title}」将从你的 AI 长期档案中移除。`,
    okText: '删除',
    cancelText: '再想想',
    async onOk() {
      await aiApi.deleteMemory(m.id)
      memories.value = memories.value.filter((i) => i.id !== m.id)
      Message.success('已删除')
    },
  })
}

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="container" v-loading="loading">
      <!-- 页头 -->
      <section class="fade-up">
        <div class="kicker">AI 长期记忆 · 一人一档</div>
        <div class="row-between" style="align-items: flex-end; flex-wrap: wrap; gap: 14px">
          <div>
            <div class="h1" style="margin-top: 8px">我的考研画像</div>
            <p class="muted" style="margin-top: 6px; max-width: 560px">
              你的每一次刷题、打卡与复盘都会被记住，持续沉淀成这份动态更新的数字画像 ——
              它是决策推演、资源推荐与个性化规划的数据底座。
            </p>
          </div>
          <div class="row gap8">
            <a-button @click="router.push('/simulator')"><Icon name="compass" :size="15" /> 决策推演</a-button>
            <a-button type="primary" @click="router.push('/plan')"><Icon name="layers" :size="15" /> 生成学习规划</a-button>
          </div>
        </div>
      </section>

      <!-- 学情总览 -->
      <section class="section fade-up delay-1" style="margin-top: 36px">
        <div class="stat-grid">
          <div class="card card-pad stat-item">
            <Icon name="code" :size="20" />
            <div><b>{{ portrait?.submitCount ?? 0 }}</b><span>累计提交</span></div>
          </div>
          <div class="card card-pad stat-item">
            <Icon name="check-circle" :size="20" />
            <div><b>{{ portrait?.acceptance ?? 0 }}%</b><span>整体正确率</span></div>
          </div>
          <div class="card card-pad stat-item">
            <Icon name="calendar" :size="20" />
            <div><b>{{ portrait?.checkinDays ?? 0 }}</b><span>打卡天数</span></div>
          </div>
          <div class="card card-pad stat-item">
            <Icon name="clock" :size="20" />
            <div><b>{{ Math.round((portrait?.totalMinutes ?? 0) / 60) }}</b><span>累计学时（小时）</span></div>
          </div>
          <div class="card card-pad stat-item">
            <Icon name="fire" :size="20" />
            <div><b>{{ portrait?.activeDays14 ?? 0 }}</b><span>近两周活跃天</span></div>
          </div>
          <div class="card card-pad stat-item">
            <Icon name="star" :size="20" />
            <div><b>{{ portrait?.favourCount ?? 0 }}</b><span>收藏题目</span></div>
          </div>
        </div>
      </section>

      <div class="portrait-grid section fade-up delay-2">
        <!-- 左：掌握度 + 薄弱点 -->
        <div>
          <div class="card card-pad">
            <div class="h3">知识点掌握度</div>
            <p class="muted small" style="margin: 4px 0 18px">按题目标签聚合你的历次判题结果</p>
            <template v-if="portrait?.tagStats?.length">
              <div v-for="t in portrait.tagStats" :key="t.tag" class="mastery-row">
                <span class="mastery-tag">{{ t.tag }}</span>
                <div class="mastery-bar">
                  <i :style="{ width: t.mastery + '%', background: masteryColor(t.mastery) }" />
                </div>
                <span class="mastery-num">{{ t.mastery }}%</span>
              </div>
            </template>
            <EmptyState v-else title="画像还没开始生长" desc="去题库写几道题，你的掌握度数据就会出现在这里" />
          </div>

          <div class="card card-pad" style="margin-top: 22px">
            <div class="h3">薄弱点与优势</div>
            <div class="divider" />
            <div class="h4" style="color: #8e4f4f">需要靶向补强</div>
            <div class="row gap8" style="flex-wrap: wrap; margin: 8px 0 18px">
              <span v-for="t in portrait?.weakTags || []" :key="t" class="tag rose">{{ t }}</span>
              <span v-if="!portrait?.weakTags?.length" class="muted small">暂未发现明显短板</span>
            </div>
            <div class="h4" style="color: var(--meadow-dark)">保持得不错</div>
            <div class="row gap8" style="flex-wrap: wrap; margin-top: 8px">
              <span v-for="t in portrait?.strongTags || []" :key="t" class="tag green">{{ t }}</span>
              <span v-if="!portrait?.strongTags?.length" class="muted small">继续积累数据</span>
            </div>
          </div>
        </div>

        <!-- 右：AI 总结 + 记忆时间线 -->
        <div>
          <div class="card card-pad summary-card">
            <Floral class="sprig tr" variant="flower" :size="44" color="#5a8f5a" />
            <div class="h3">AI 画像总结</div>
            <p class="muted small" style="margin: 4px 0 14px">让 AI 把你的数据读成一段诚实的学情判断</p>
            <p v-if="summary" class="summary-text">{{ summary }}</p>
            <p v-else class="muted" style="font-size: 14px; line-height: 1.9">
              点击下面的按钮，AI 会基于你的画像数据生成一段学情总结，并存入长期记忆 ——
              下次做决策推演时，它会记得这些。
            </p>
            <a-button type="primary" long :loading="summaryLoading" style="margin-top: 14px" @click="makeSummary">
              <Icon name="sparkles" :size="15" /> 生成我的画像总结
            </a-button>
          </div>

          <div class="card card-pad" style="margin-top: 22px">
            <div class="h3" style="margin-bottom: 6px">AI 长期记忆</div>
            <p class="muted small" style="margin-bottom: 14px">推演报告、规划与画像快照都在这里持续沉淀</p>
            <template v-if="memories.length">
              <div v-for="m in memories.slice(0, 8)" :key="m.id" class="memory-item" @click="m.content && $router.push(m.memoryType === 'simulate' ? '/simulator' : m.memoryType === 'plan' ? '/plan' : '/portrait')">
                <span class="tag" :class="{ violet: m.memoryType === 'simulate', amber: m.memoryType === 'plan', green: m.memoryType === 'portrait' }">
                  {{ memoryTypeName[m.memoryType] || '记忆' }}
                </span>
                <div class="memory-body">
                  <div class="memory-title">{{ m.title }}</div>
                  <div class="muted small">{{ fromNow(m.createTime) }}</div>
                </div>
                <button class="icon-btn" title="删除" @click.stop="removeMemory(m)"><Icon name="trash" :size="14" /></button>
              </div>
            </template>
            <EmptyState v-else title="还没有长期记忆" desc="做一次决策推演或生成一份规划，AI 就开始记住你了" />
          </div>
        </div>
      </div>

      <hr class="stitched" />
    </div>
  </div>
</template>

<style scoped>
.stat-grid { display: grid; grid-template-columns: repeat(6, minmax(0, 1fr)); gap: 16px; }
.stat-item { display: flex; align-items: center; gap: 14px; color: var(--meadow-dark); }
.stat-item b { display: block; font-size: 24px; color: var(--ink); font-weight: 600; }
.stat-item span { font-size: 12px; color: var(--muted); letter-spacing: 0.04em; }
.portrait-grid { display: grid; grid-template-columns: 1.2fr 1fr; gap: 22px; align-items: start; }
.mastery-row { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.mastery-tag { width: 88px; flex: none; font-size: 13px; color: var(--ink-2); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.mastery-bar { flex: 1; height: 10px; border-radius: 999px; background: rgba(139, 115, 85, 0.12); overflow: hidden; }
.mastery-bar i { display: block; height: 100%; border-radius: 999px; transition: width 0.8s ease; }
.mastery-num { width: 42px; flex: none; text-align: right; font-size: 13px; font-weight: 600; color: var(--ink-2); }
.summary-card { position: relative; }
.summary-text { font-size: 14.5px; line-height: 2; color: var(--ink-2); white-space: pre-wrap; }
.memory-item { display: flex; align-items: center; gap: 10px; padding: 10px 6px; border-bottom: 1px dashed rgba(139, 115, 85, 0.16); cursor: pointer; }
.memory-item:last-child { border-bottom: none; }
.memory-body { flex: 1; min-width: 0; }
.memory-title { font-size: 14px; color: var(--ink-2); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.row-between { display: flex; justify-content: space-between; }
.row { display: flex; align-items: center; }
.gap8 { gap: 8px; }
@media (max-width: 900px) {
  .stat-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .portrait-grid { grid-template-columns: 1fr; }
}
</style>
