<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { studyApi } from '../api/study'
import { useAuthStore } from '../stores/auth'
import { modules, totalChapters } from '../content'
import Floral from '../components/Floral.vue'

/** 四科各用一枚不同的图标与花饰，避免四张卡长得一样 */
const moduleStyle: Record<string, { icon: string; floral: 'sprig' | 'leaf' | 'wheat' | 'mushroom' }> = {
  ds: { icon: 'grid', floral: 'leaf' },
  co: { icon: 'target', floral: 'wheat' },
  os: { icon: 'clock', floral: 'sprig' },
  cn: { icon: 'link', floral: 'mushroom' },
}
import Icon from '../components/Icon.vue'

const router = useRouter()
const auth = useAuthStore()
const progress = ref<any[]>([])
const loading = ref(true)

const finishedCount = computed(() => progress.value.filter((p) => p.finished).length)
const percent = computed(() => Math.round((finishedCount.value / totalChapters) * 100))

const moduleProgress = (key: string) => {
  const total = modules.find((m) => m.key === key)?.sections.length || 1
  const done = progress.value.filter((p) => p.module === key && p.finished).length
  return { done, total, percent: Math.round((done / total) * 100) }
}

onMounted(async () => {
  if (auth.isAuthed) {
    try { progress.value = await studyApi.progress() } catch {}
  }
  loading.value = false
})
</script>

<template>
  <div class="page">
    <div class="container">
      <section class="learn-hero">
        <div>
          <div class="kicker">408 Master Roadmap</div>
          <h1 class="h1" style="margin-top: 8px">408 学习中心</h1>
          <p class="muted" style="margin-top: 8px; max-width: 620px">
            四科 14 个核心章节，从知识框架到真题考法。每读完一章就勾选进度，配合题库练习形成闭环。
          </p>
          <div class="row gap12" style="margin-top: 18px">
            <a-button type="primary" @click="router.push('/learn/408/ds')">从数据结构开始</a-button>
            <a-button @click="router.push('/learn/quiz')"><Icon name="check-circle" :size="15" /> 在线习题与真题</a-button>
            <a-button @click="router.push('/problems')">去题库练习</a-button>
          </div>
        </div>
        <div class="card card-pad" style="background: rgba(255,255,255,.85)">
          <div class="row between">
            <span class="small muted">总进度</span>
            <span class="h4">{{ percent }}%</span>
          </div>
          <div class="progress-track" style="margin: 10px 0 14px"><div class="progress-bar" :style="{ width: `${percent}%` }" /></div>
          <div class="row between small">
            <span class="muted">已完成 {{ finishedCount }} / {{ totalChapters }} 章</span>
            <span style="color: var(--brand)">{{ auth.isAuthed ? '自动保存' : '登录后记录' }}</span>
          </div>
        </div>
      </section>

      <section class="section">
        <div class="section-head">
          <div>
            <div class="row gap12">
              <div class="h2">四科知识地图</div>
              <Floral variant="sprig" :size="40" color="#5a8f5a" :stroke="1.3" />
            </div>
            <div class="desc">按分值分配时间：数据结构 45 + 计组 45 + OS 35 + 计网 25</div>
          </div>
        </div>
        <div class="grid g2">
          <div v-for="m in modules" :key="m.key" class="card card-hover card-pad" style="cursor: pointer; position: relative; overflow: hidden" @click="router.push(`/learn/408/${m.key}`)">
            <div class="row between">
              <div class="row gap12">
                <div class="stat-icon" :class="m.color"><Icon :name="moduleStyle[m.key]?.icon || 'book'" :size="20" /></div>
                <div>
                  <div class="h3">{{ m.title }}</div>
                  <div class="small muted">{{ m.desc }}</div>
                </div>
              </div>
              <span class="tag blue">{{ moduleProgress(m.key).percent }}%</span>
            </div>
            <div class="progress-track" style="margin: 16px 0 12px">
              <div class="progress-bar" :class="m.color === 'green' ? 'green' : m.color === 'amber' ? 'amber' : ''" :style="{ width: `${moduleProgress(m.key).percent}%` }" />
            </div>
            <div class="row between small muted">
              <span>{{ moduleProgress(m.key).done }} / {{ moduleProgress(m.key).total }} 章</span>
              <span>共 {{ m.sections.reduce((s, c) => s + c.minutes, 0) }} 分钟</span>
            </div>
          </div>
        </div>
      </section>

      <section class="section">
        <div class="grid g3">
          <div class="card card-pad card-hover" style="cursor: pointer" @click="router.push('/learn/quiz')">
            <div class="stat-icon amber"><Icon name="check-circle" :size="20" /></div>
            <div class="h3" style="margin-top: 10px">在线习题与真题</div>
            <div class="small muted" style="margin-top: 6px; line-height: 1.9">
              四科章节练习 + 往年真题改编，在线作答即时判分，正确率自动汇入个人画像。
            </div>
            <span class="section-link" style="margin-top: 10px">开始练习 <Icon name="arrow-right" :size="13" /></span>
          </div>
          <div class="card card-pad">
            <div class="stat-icon blue"><Icon name="target" :size="20" /></div>
            <div class="h3" style="margin-top: 10px">三轮复习法</div>
            <div class="small muted" style="margin-top: 6px; line-height: 1.9">
              第一轮搭框架，第二轮刷真题，第三轮错题回炉。每轮目标不同，别用同一种方式重复。
            </div>
          </div>
          <div class="card card-pad">
            <div class="stat-icon violet"><Icon name="sparkles" :size="20" /></div>
            <div class="h3" style="margin-top: 10px">AI 随时答疑</div>
            <div class="small muted" style="margin-top: 6px; line-height: 1.9">
              右下角 AI 助教可以讲知识点、分析代码、模拟面试，复习卡住时先问一句。
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.learn-hero { display: grid; grid-template-columns: 1.4fr 0.6fr; gap: 24px; align-items: center; background: linear-gradient(135deg, #fdfaf4, #ffffff 60%, rgba(90, 143, 90, 0.07)); border: 1px solid #f4eee2; border-radius: 22px; padding: 34px 38px; }
@media (max-width: 900px) { .learn-hero { grid-template-columns: 1fr; padding: 24px; } }
</style>
