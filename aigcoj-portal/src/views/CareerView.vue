<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { forumApi } from '../api/forum'
import { resumeApi } from '../api/resume'
import { useAuthStore } from '../stores/auth'
import { fromNow, plainText } from '../utils/format'
import Icon from '../components/Icon.vue'

const router = useRouter()
const auth = useAuthStore()
const open = ref(0)
const resume = ref<any>(null)
const posts = ref<any[]>([])

const directions = [
  { title: '后端开发', icon: 'code', color: 'blue', skills: ['Java / Go', 'MySQL / Redis', 'Spring Boot', '分布式基础'] },
  { title: '算法工程师', icon: 'chart', color: 'violet', skills: ['数据结构', '机器学习', 'Python', '论文/竞赛'] },
  { title: '前端开发', icon: 'grid', color: 'green', skills: ['JavaScript', 'Vue / React', '工程化', '浏览器原理'] },
  { title: '测试开发', icon: 'shield', color: 'amber', skills: ['自动化测试', 'Linux', '接口测试', 'CI/CD'] },
  { title: '读研深造', icon: 'book', color: 'blue', skills: ['科研兴趣', '论文阅读', '数学基础', '导师方向'] },
  { title: '产品/运营', icon: 'target', color: 'violet', skills: ['需求分析', '数据分析', '沟通表达', '项目经历'] },
]

const questions = [
  { q: '进程和线程的区别是什么？', a: '进程是资源分配的基本单位，拥有独立地址空间；线程是 CPU 调度的基本单位，共享进程资源。线程切换开销更小，但一个线程崩溃可能影响整个进程。' },
  { q: 'TCP 三次握手为什么不是两次？', a: '两次握手无法确认客户端的接收能力，历史 SYN 还可能让服务端建立无效连接、浪费资源。三次握手同时同步双方序号并确认收发能力。' },
  { q: '数据库索引为什么用 B+ 树？', a: 'B+ 树矮胖、磁盘 I/O 少；非叶子结点只存索引，扇出大；叶子结点有序串联，范围查询高效；所有查询都走到叶子，性能稳定。' },
  { q: '死锁的四个必要条件？', a: '互斥、占有并等待、不可剥夺、循环等待。破坏任一条件即可预防死锁；也可以用银行家算法避免，或检测后解除。' },
  { q: 'HashMap 的底层结构？', a: 'JDK 8 是数组 + 链表 + 红黑树。链表长度达到 8 且容量不小于 64 时转红黑树，扩容时重新分布，负载因子默认 0.75。' },
  { q: '介绍一个你最有成就感的项目。', a: '按 STAR 回答：背景与目标、你的职责、关键技术方案、量化结果。重点讲你独立解决的问题和取舍，而不是罗列功能。' },
  { q: '为什么选择我们学校 / 公司？', a: '从学科实力、导师方向、项目匹配、个人规划四个角度回答，避免空泛夸赞。最好能说出具体课程、实验室或业务。' },
  { q: '如果项目重做一遍，你会怎么改进？', a: '承认不足 + 给出具体方案 + 说明预期收益。例如引入缓存、补全监控、拆分模块、增加自动化测试。' },
]

const latestScore = computed(() => resume.value?.score || 0)

onMounted(async () => {
  try {
    const page = await forumApi.list({ current: 1, pageSize: 4, tags: ['复试'] })
    posts.value = page.records || []
  } catch {}
  if (auth.isAuthed) {
    try { resume.value = await resumeApi.latest() } catch {}
  }
})
</script>

<template>
  <div class="page">
    <div class="container">
      <section class="career-hero">
        <div>
          <div class="kicker">Career & Interview</div>
          <h1 class="h1" style="margin-top: 8px">从初试到上岸，<br />再到进入理想岗位。</h1>
          <p class="muted" style="margin-top: 10px; max-width: 620px">
            简历工坊、复试流程、面试题库与求职方向，一站补齐考研后半程最容易被忽视的准备。
          </p>
          <div class="row gap12" style="margin-top: 20px">
            <a-button type="primary" @click="router.push('/career/resume')"><Icon name="edit" :size="15" /> 开始写简历</a-button>
            <a-button @click="router.push('/forum')">看复试经验</a-button>
          </div>
        </div>
        <div class="card card-pad" style="background: rgba(255,255,255,.88)">
          <div class="row between">
            <span class="small muted">我的简历评分</span>
            <Icon name="sparkles" :size="16" style="color: var(--timber)" />
          </div>
          <div style="font-size: 42px; font-weight: 600; color: var(--brand-deep); margin: 6px 0">{{ latestScore || '--' }}<span class="small muted" style="font-size: 13px"> / 100</span></div>
          <div class="small muted">{{ latestScore ? '已经超过大多数研友，继续优化项目描述' : '登录后创建简历，AI 帮你逐条诊断' }}</div>
          <a-button long type="primary" style="margin-top: 14px" @click="router.push(auth.isAuthed ? '/career/resume' : '/login')">
            {{ latestScore ? '继续优化' : '创建我的简历' }}
          </a-button>
        </div>
      </section>

      <section class="section">
        <div class="section-head">
          <div>
            <div class="h2">考研后半程时间线</div>
            <div class="desc">初试结束不等于上岸，每一步都要提前准备</div>
          </div>
        </div>
        <div class="timeline card card-pad">
          <div class="tl-item" v-for="(s, i) in [
            { t: '12 月底', title: '初试', desc: '考完先休息两天，估分并整理复试资料清单' },
            { t: '1 月', title: '复试准备启动', desc: '专业课二轮、英语口语、项目复盘，简历初稿' },
            { t: '2 月底', title: '成绩公布', desc: '对照往年分数线，判断复试 / 调剂 / 二战' },
            { t: '3-4 月', title: '复试与调剂', desc: '笔试、机试、综合面试；同步关注调剂系统开放' },
            { t: '4-6 月', title: '拟录取与实习', desc: '确认录取，补基础、找实习或提前联系导师' },
          ]" :key="i">
            <div class="tl-dot" />
            <div class="tl-time">{{ s.t }}</div>
            <div style="flex: 1">
              <div class="h4">{{ s.title }}</div>
              <div class="small muted" style="margin-top: 2px">{{ s.desc }}</div>
            </div>
          </div>
        </div>
      </section>

      <section class="section">
        <div class="section-head">
          <div>
            <div class="h2">面试高频题</div>
            <div class="desc">专业基础 + 项目追问 + 综合面，先会讲再背答案</div>
          </div>
        </div>
        <div class="card">
          <div v-for="(item, i) in questions" :key="i" class="qa-row" @click="open = open === i ? -1 : i">
            <div class="row between">
              <div class="h4">{{ i + 1 }}. {{ item.q }}</div>
              <Icon :name="open === i ? 'close' : 'plus'" :size="15" style="color: var(--muted-2)" />
            </div>
            <div v-if="open === i" class="qa-answer">{{ item.a }}</div>
          </div>
        </div>
      </section>

      <section class="section">
        <div class="section-head">
          <div>
            <div class="h2">就业方向</div>
            <div class="desc">先定方向，再补技能，避免什么都学一点</div>
          </div>
        </div>
        <div class="grid g3">
          <div v-for="d in directions" :key="d.title" class="card card-hover card-pad">
            <div class="stat-icon" :class="d.color"><Icon :name="d.icon" :size="20" /></div>
            <div class="h3" style="margin-top: 10px">{{ d.title }}</div>
            <div class="row wrap gap8" style="margin-top: 10px">
              <span v-for="s in d.skills" :key="s" class="tag">{{ s }}</span>
            </div>
          </div>
        </div>
      </section>

      <section class="section" v-if="posts.length">
        <div class="section-head">
          <div>
            <div class="h2">复试经验帖</div>
            <div class="desc">来自已经上岸的学长学姐</div>
          </div>
          <span class="section-link" @click="router.push('/forum')">更多 <Icon name="arrow-right" :size="14" /></span>
        </div>
        <div class="grid g2">
          <div v-for="p in posts" :key="p.id" class="card card-hover card-pad" style="cursor: pointer" @click="router.push(`/forum/${p.id}`)">
            <div class="h4">{{ p.title }}</div>
            <div class="small muted" style="margin-top: 6px">{{ plainText(p.content, 90) }}</div>
            <div class="row gap12 small muted" style="margin-top: 10px">
              <span>{{ p.user?.userName }}</span><span>{{ fromNow(p.createTime) }}</span>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.career-hero { display: grid; grid-template-columns: 1.45fr 0.55fr; gap: 26px; align-items: center; background: linear-gradient(135deg, #fdfaf4, #ffffff 55%, rgba(90, 143, 90, 0.07)); border: 1px solid #f4eee2; border-radius: 24px; padding: 38px 40px; }
.timeline { padding: 10px 24px; }
.tl-item { display: flex; align-items: flex-start; gap: 16px; padding: 14px 0; position: relative; }
.tl-item:not(:last-child) { border-bottom: 1px solid #f4eee2; }
.tl-dot { width: 9px; height: 9px; border-radius: 50%; background: var(--brand); margin-top: 7px; box-shadow: 0 0 0 4px rgba(90, 143, 90, 0.1); flex-shrink: 0; }
.tl-time { width: 70px; font-size: 12px; color: var(--brand); flex-shrink: 0; margin-top: 3px; }
.qa-row { padding: 16px 20px; border-bottom: 1px solid #f4eee2; cursor: pointer; }
.qa-row:last-child { border-bottom: none; }
.qa-row:hover { background: #fdfaf4; }
.qa-answer { margin-top: 10px; padding: 12px 14px; background: #fdfaf4; border-radius: 10px; font-size: 13px; color: var(--ink-2); line-height: 1.9; }
@media (max-width: 900px) { .career-hero { grid-template-columns: 1fr; padding: 24px; } }
</style>
