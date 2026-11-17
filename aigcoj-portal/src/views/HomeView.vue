<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { statsApi } from '../api/stats'
import { studyApi } from '../api/study'
import { useAuthStore } from '../stores/auth'
import Icon from '../components/Icon.vue'
import Floral from '../components/Floral.vue'

const router = useRouter()
const auth = useAuthStore()

const overview = ref<Record<string, number>>({})
const my = ref<any>(null)
const plan = ref<any>(null)

const targetDate = computed(() => plan.value?.targetDate || '2026-12-19')
const days = computed(() => Math.max(0, dayjs(targetDate.value).startOf('day').diff(dayjs().startOf('day'), 'day')))
const weeks = computed(() => Math.floor(days.value / 7))
const months = computed(() => Math.max(0, dayjs(targetDate.value).diff(dayjs(), 'month')))

/** 首页只放入口：三大 AI 引擎 + 学习场景链接，内容都在各自场景里 */
const engines = [
  {
    path: '/portrait',
    title: '我的画像',
    desc: '一人一档：刷题、打卡、错题持续沉淀成动态更新的考研数字画像。',
    icon: 'chart',
    mark: '',
    floral: 'sprig',
  },
  {
    path: '/simulator',
    title: '决策推演',
    desc: '冲稳保多情景择校模拟，上岸概率、提分预估都有数据依据。',
    icon: 'compass',
    mark: 'wheat',
    floral: 'wheat',
  },
  {
    path: '/plan',
    title: '学习规划',
    desc: '一人一策：基于画像生成基础到复试的全周期计划，状态变了就跟着变。',
    icon: 'layers',
    mark: 'rose',
    floral: 'flower',
  },
]

const scenes = [
  { path: '/problems', label: '题库', desc: '写完即评测，错题自动入本' },
  { path: '/learn/408', label: '408 精讲', desc: '四科笔记 + 在线习题与真题' },
  { path: '/materials', label: '资料库', desc: '资源价值引擎智能排序' },
  { path: '/forum', label: '研友论坛', desc: '择校、进度、心态都有人回应' },
  { path: '/career', label: '就业与复试', desc: '复试流程与求职方向' },
  { path: '/career/resume', label: '简历工坊', desc: '结构化填写，AI 逐句挑毛病' },
]

const go = (path: string) => router.push(path)

onMounted(async () => {
  const tasks: Promise<any>[] = [statsApi.overview().then((r) => (overview.value = r))]
  if (auth.isAuthed) {
    tasks.push(studyApi.overview().then((r) => (my.value = r)))
    tasks.push(studyApi.plan().then((r) => (plan.value = r)))
  }
  await Promise.allSettled(tasks)
})
</script>

<template>
  <div class="page">
    <div class="container">
      <!-- ============ 一页封面 ============ -->
      <section class="hero fade-up">
        <div>
          <div class="hero-badge"><span class="dot" /> AI 长期记忆 · 决策推演 · 资源评估</div>
          <h1 class="hero-title">智研赋能，<br />一间 <em>有记忆</em> 的自习室。</h1>
          <p class="hero-sub">
            它记住你的每次刷题与错题，读懂你的薄弱点；择校时替你推演不同选择的上岸概率，
            再把当下最适配的资料排到你面前。一人一档，一人一策，一人一资源。
          </p>
          <div class="hero-actions">
            <a-button type="primary" size="large" @click="go(auth.isAuthed ? '/portrait' : '/register')">
              {{ auth.isAuthed ? '看看我的画像' : '建立我的画像' }} <Icon name="arrow-right" :size="16" />
            </a-button>
            <a-button size="large" @click="go('/simulator')">做一次择校推演</a-button>
          </div>
          <div class="hero-meta">
            <div><strong>{{ overview.questions || 16 }} 道</strong><span>在线题目</span></div>
            <div><strong>14 章</strong><span>408 精讲</span></div>
            <div><strong>{{ overview.materials || 13 }} 份</strong><span>考研资料</span></div>
          </div>
        </div>

        <div class="hero-side">
          <div class="countdown-card">
            <Floral class="sprig tr" variant="flower" :size="46" color="#5a8f5a" />
            <div class="countdown-label">距离初试</div>
            <div class="countdown-num">{{ days }}<small>天</small></div>
            <div class="countdown-grid">
              <div class="countdown-cell"><b>{{ months }}</b><span>个月</span></div>
              <div class="countdown-cell"><b>{{ weeks }}</b><span>周</span></div>
              <div class="countdown-cell"><b>{{ days * 24 }}</b><span>小时</span></div>
            </div>
            <div class="divider" style="margin: 18px 0 14px" />
            <div v-if="auth.isAuthed" class="small muted">
              今天已学 <b style="color: var(--ink)">{{ my?.todayMinutes || 0 }}</b> 分钟，连续
              <b style="color: var(--ink)">{{ my?.streak || 0 }}</b> 天。
            </div>
            <div v-else class="small muted">
              登录后可以记下每天的复习时间，也会记住你读到哪一章。
            </div>
          </div>
        </div>
      </section>

      <!-- ============ 三大 AI 引擎 ============ -->
      <section class="section fade-up delay-1">
        <div class="section-head" style="margin-bottom: 26px">
          <div>
            <div class="kicker">AI Engines</div>
            <div class="h2" style="margin-top: 6px">三件为你一个人服务的事</div>
          </div>
          <span class="section-link" @click="go('/me')">我的学习台 <Icon name="arrow-right" :size="14" /></span>
        </div>

        <div class="entry-grid">
          <a v-for="e in engines" :key="e.path" class="entry-card" @click="go(e.path)">
            <Floral class="sprig tr" :variant="e.floral as any" :size="54" color="#8b7355" :stroke="1.3" />
            <div class="entry-mark" :class="e.mark"><Icon :name="e.icon" :size="22" /></div>
            <div class="entry-title">{{ e.title }}</div>
            <div class="entry-desc">{{ e.desc }}</div>
            <span class="entry-go">走进去 <Icon name="arrow-right" :size="13" /></span>
          </a>
        </div>

        <!-- ============ 学习场景链接 ============ -->
        <div class="scene-links">
          <a v-for="s in scenes" :key="s.path" class="scene-link" @click="go(s.path)">
            <div>
              <b>{{ s.label }}</b>
              <span>{{ s.desc }}</span>
            </div>
            <Icon name="arrow-right" :size="15" />
          </a>
        </div>

        <div class="note-line">
          <span>距 {{ dayjs(targetDate).format('M 月 D 日') }} 还有 {{ days }} 天</span>
          <i class="sep" />
          <span>{{ overview.posts || 7 }} 篇研友经验</span>
          <i class="sep" />
          <span>慢慢来，比较快</span>
        </div>
      </section>

      <!-- ============ 一句引言 ============ -->
      <section class="quote-block fade-up delay-2">
        <Floral class="sprig tl rot-l" variant="sprig" :size="72" color="#5a8f5a" :stroke="1.2" />
        <Floral class="sprig br rot-r" variant="wheat" :size="68" color="#c9a227" :stroke="1.2" />
        <p>“让 AI 从一次性问答工具，变成持续理解你、辅助决策并伴随你成长的智能学习伙伴。”</p>
        <span>—— 智研赋能 · 项目愿景</span>
      </section>

      <hr class="stitched" />
    </div>
  </div>
</template>
