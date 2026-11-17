<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { statsApi } from '@/api/stats'
import { userApi } from '@/api/user'
import { materialApi } from '@/api/material'
import { questionApi } from '@/api/question'
import { forumApi } from '@/api/forum'
import { formatDate, formatSize } from '@/utils/format'
import { useAuthStore } from '@/stores/auth'
import type { Material, Question, UserVO } from '@/types'
import Icon from '../components/Icon.vue'

const router = useRouter()
const auth = useAuthStore()

// ============ 布局状态 ============
const sidebarOpen = ref(true)
type PageKey = 'dashboard' | 'users' | 'posts' | 'materials' | 'questions' | 'roles' | 'settings'
const activePage = ref<PageKey>('dashboard')
const nav: { key: PageKey; icon: string; label: string; desc: string }[] = [
  { key: 'dashboard', icon: 'grid', label: '数据看板', desc: '平台运行数据一览' },
  { key: 'users', icon: 'user', label: '用户管理', desc: '账号与角色' },
  { key: 'posts', icon: 'chat', label: '帖子管理', desc: '社区内容增删' },
  { key: 'materials', icon: 'file', label: '资料管理', desc: '资料库内容' },
  { key: 'questions', icon: 'code', label: '题库管理', desc: '编程题目' },
  { key: 'roles', icon: 'shield', label: '角色权限', desc: '权限分配' },
  { key: 'settings', icon: 'compass', label: '系统设置', desc: '平台参数' },
]
const activeDesc = computed(() => nav.find((n) => n.key === activePage.value)?.desc || '')

// ============ 数据看板 ============
const overview = ref<Record<string, number>>({})
const ranking = ref<any[]>([])
const trend = ref<{ submissions: any[]; checkins: any[] }>({ submissions: [], checkins: [] })
const hot = ref<{ questions: any[]; materials: any[]; posts: any[] }>({ questions: [], materials: [], posts: [] })

const cards = computed(() => [
  { label: '注册用户', value: overview.value.users ?? 0, icon: 'user' },
  { label: '题目总数', value: overview.value.questions ?? 0, icon: 'code' },
  { label: '资料总量', value: overview.value.materials ?? 0, icon: 'file' },
  { label: '提交次数', value: overview.value.submissions ?? 0, icon: 'send' },
  { label: '通过次数', value: overview.value.accepted ?? 0, icon: 'check-circle' },
  { label: '帖子总数', value: overview.value.posts ?? 0, icon: 'chat' },
])

type TrendRow = { day: string; label: string; sub: number; ck: number }
const trendRows = computed<TrendRow[]>(() => {
  const toMap = (list: any[]) => {
    const map = new Map<string, number>()
    ;(list || []).forEach((d: any) => {
      const key = String(d.day || d.date || '').slice(0, 10)
      if (key) map.set(key, Number(d.num ?? d.count ?? d.value ?? 0))
    })
    return map
  }
  const subs = toMap(trend.value.submissions)
  const cks = toMap(trend.value.checkins)
  const rows: TrendRow[] = []
  for (let i = 6; i >= 0; i--) {
    const d = new Date()
    d.setDate(d.getDate() - i)
    const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    rows.push({ day: key, label: key.slice(5), sub: subs.get(key) || 0, ck: cks.get(key) || 0 })
  }
  return rows
})
const maxTrend = computed(() => Math.max(1, ...trendRows.value.flatMap((r) => [r.sub, r.ck])))
function barH(n: number) {
  return `${Math.max(n > 0 ? 6 : 2, (n / maxTrend.value) * 100)}%`
}

async function loadOverview() {
  try {
    const [o, r, t, h] = await Promise.all([
      statsApi.overview(),
      statsApi.ranking(),
      statsApi.trend(),
      statsApi.hot(),
    ])
    overview.value = o || {}
    ranking.value = r || []
    trend.value = t || { submissions: [], checkins: [] }
    hot.value = h || { questions: [], materials: [], posts: [] }
  } catch (e: any) {
    Message.error(e?.message || '加载统计数据失败')
  }
}

// ============ 用户管理 ============
const users = ref<UserVO[]>([])
const userTotal = ref(0)
const userQuery = reactive({ current: 1, pageSize: 10, keyword: '' })

async function loadUsers() {
  try {
    const body: Record<string, unknown> = { current: userQuery.current, pageSize: userQuery.pageSize }
    if (userQuery.keyword) body.userName = userQuery.keyword
    const page = await userApi.listPage(body)
    users.value = page.records || []
    userTotal.value = page.total || 0
  } catch (e: any) {
    Message.error(e?.message || '加载用户失败')
  }
}

async function toggleRole(u: UserVO) {
  const next = u.userRole === 'admin' ? 'user' : 'admin'
  try {
    await userApi.update({ id: u.id, userRole: next })
    u.userRole = next
    Message.success(next === 'admin' ? '已设为管理员' : '已取消管理员')
  } catch (e: any) {
    Message.error(e?.message || '操作失败')
  }
}

async function removeUser(u: UserVO) {
  if (!confirm(`确定删除用户「${u.userName || u.userAccount}」？此操作不可恢复。`)) return
  try {
    await userApi.remove(u.id as number)
    Message.success('已删除')
    loadUsers()
  } catch (e: any) {
    Message.error(e?.message || '删除失败')
  }
}

// ============ 帖子管理（增删） ============
const posts = ref<any[]>([])
const postTotal = ref(0)
const postQuery = reactive({ current: 1, pageSize: 10, keyword: '' })
const postAddOpen = ref(false)
const postForm = reactive({ title: '', content: '', tags: '' })
const posting = ref(false)

async function loadPosts() {
  try {
    const body: Record<string, unknown> = { current: postQuery.current, pageSize: postQuery.pageSize }
    if (postQuery.keyword) body.title = postQuery.keyword
    const page = await forumApi.list(body)
    posts.value = (page.records || []) as any[]
    postTotal.value = page.total || 0
  } catch (e: any) {
    Message.error(e?.message || '加载帖子失败')
  }
}

async function removePost(p: any) {
  if (!confirm(`确定删除帖子「${p.title}」？`)) return
  try {
    await forumApi.remove(p.id)
    Message.success('帖子已删除')
    loadPosts()
  } catch (e: any) {
    Message.error(e?.message || '删除失败')
  }
}

async function addPost() {
  if (!postForm.title.trim() || !postForm.content.trim()) {
    Message.warning('请填写帖子标题与内容')
    return
  }
  posting.value = true
  try {
    await forumApi.add({
      title: postForm.title,
      content: postForm.content,
      tags: postForm.tags ? postForm.tags.split(/[,，]/).map((t) => t.trim()).filter(Boolean) : [],
    })
    Message.success('帖子已发布')
    postAddOpen.value = false
    Object.assign(postForm, { title: '', content: '', tags: '' })
    postQuery.current = 1
    loadPosts()
  } catch (e: any) {
    Message.error(e?.message || '发布失败')
  } finally {
    posting.value = false
  }
}

// ============ 资料管理 ============
const materials = ref<Material[]>([])
const materialQuery = reactive({ current: 1, pageSize: 12, keyword: '' })
const materialTotal = ref(0)
const uploadOpen = ref(false)
const uploading = ref(false)
const uploadForm = reactive({ title: '', category: '408 真题', subject: '', description: '', tags: '', fileUrl: '', fileSize: 0, fileType: 'link' })

async function loadMaterials() {
  try {
    const page = await materialApi.list({
      current: materialQuery.current,
      pageSize: materialQuery.pageSize,
      keyword: materialQuery.keyword,
    })
    materials.value = page.records || []
    materialTotal.value = page.total || 0
  } catch (e: any) {
    Message.error(e?.message || '加载资料失败')
  }
}

async function removeMaterial(m: Material) {
  if (!confirm(`确定删除资料「${m.title}」？`)) return
  try {
    await materialApi.remove(m.id)
    Message.success('已删除')
    loadMaterials()
  } catch (e: any) {
    Message.error(e?.message || '删除失败')
  }
}

/** 上架 / 下架 */
async function toggleStatus(m: Material) {
  const next = m.status === 1 ? 0 : 1
  try {
    await materialApi.save({ ...m, status: next })
    m.status = next
    Message.success(next === 1 ? '已上架' : '已下架')
  } catch (e: any) {
    Message.error(e?.message || '操作失败')
  }
}

function pickFile(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  uploading.value = true
  materialApi
    .upload(file, 'material')
    .then((res) => {
      uploadForm.fileUrl = res.url
      uploadForm.fileSize = res.size
      uploadForm.fileType = (res.name.split('.').pop() || 'pdf').toLowerCase()
      if (!uploadForm.title) uploadForm.title = res.name.replace(/\.[^.]+$/, '')
      Message.success('文件上传成功')
    })
    .catch((err: any) => Message.error(err?.message || '上传失败'))
    .finally(() => (uploading.value = false))
}

async function saveMaterial() {
  if (!uploadForm.title || !uploadForm.fileUrl) {
    Message.warning('请填写标题并填写链接或上传文件')
    return
  }
  try {
    await materialApi.save({ ...uploadForm, subject: uploadForm.subject || undefined })
    Message.success('资料已发布')
    uploadOpen.value = false
    Object.assign(uploadForm, { title: '', description: '', tags: '', fileUrl: '', fileSize: 0, fileType: 'link', subject: '' })
    loadMaterials()
  } catch (e: any) {
    Message.error(e?.message || '发布失败')
  }
}

// ============ 题库管理 ============
const questions = ref<Question[]>([])
const questionQuery = reactive({ current: 1, pageSize: 10, keyword: '' })
const questionTotal = ref(0)

async function loadQuestions() {
  try {
    const body: Record<string, unknown> = { current: questionQuery.current, pageSize: questionQuery.pageSize }
    if (questionQuery.keyword) body.title = questionQuery.keyword
    const page = await questionApi.listPageVo(body)
    questions.value = page.records || []
    questionTotal.value = page.total || 0
  } catch (e: any) {
    Message.error(e?.message || '加载题目失败')
  }
}

// ============ 角色权限 ============
const adminList = computed(() => users.value.filter((u) => u.userRole === 'admin'))
const adminCount = ref(0)

async function loadRoles() {
  await loadUsers()
  try {
    // 管理员专用接口，按角色统计总数（不依赖分页条数）
    const page = await userApi.adminListPage({ current: 1, pageSize: 1, userRole: 'admin' })
    adminCount.value = Number(page.total || 0)
  } catch {}
}

const permissionMatrix = [
  { name: '数据看板', admin: true, user: false },
  { name: '用户 / 角色管理', admin: true, user: false },
  { name: '帖子增删改（他人）', admin: true, user: false },
  { name: '资料上架 / 下架 / 删除', admin: true, user: false },
  { name: '题库管理', admin: true, user: false },
  { name: '发布自己的帖子', admin: true, user: true },
  { name: '刷题 / 提交代码', admin: true, user: true },
  { name: '资料浏览 / 收藏 / 下载', admin: true, user: true },
  { name: 'AI 画像与决策推演', admin: true, user: true },
]

// ============ 系统设置 ============
const settings = [
  { group: '基本信息', items: [
    { k: '平台名称', v: '智研赋能 · 个性化考研智能服务平台' },
    { k: '平台口号', v: 'AI 记忆 · 决策推演 · 资源评估，动态进化的 1 对 1 考研服务' },
    { k: '27 考研初试日', v: '2026 年 12 月 19 日' },
  ]},
  { group: 'AI 服务', items: [
    { k: '大模型', v: 'DeepSeek（画像总结 / 决策推演 / 计划生成）' },
    { k: 'AI 记忆库', v: 'ai_memory 表持久化，随使用动态进化' },
  ]},
  { group: '判题引擎', items: [
    { k: '支持语言', v: 'C++ / Java / Python' },
    { k: '沙箱模式', v: '本地沙箱（ProcessBuilder），按用例比例计分' },
  ]},
  { group: '资源来源', items: [
    { k: '资料库真题 / 模拟题', v: '计算机考研杂货铺 csgraduates.com（45 项）' },
    { k: '在线判题题库', v: '28 道编程题 · 24 道 408 选择题' },
  ]},
]

// ============ 切换 ============
function switchTab(t: PageKey) {
  activePage.value = t
  if (t === 'dashboard') loadOverview()
  if (t === 'users') loadUsers()
  if (t === 'posts') loadPosts()
  if (t === 'materials') loadMaterials()
  if (t === 'questions') loadQuestions()
  if (t === 'roles') loadRoles()
}

// 二次权限校验：守卫之外再兜一层，非管理员一律拦截（后端接口另有管理员校验）
onMounted(async () => {
  await auth.hydrate()
  if (!auth.isAdmin) {
    router.replace({ path: '/403', query: { from: 'admin' } })
    return
  }
  loadOverview()
})
</script>

<template>
  <div class="admin-shell">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ collapsed: !sidebarOpen }">
      <div class="side-head">
        <div class="logo-row">
          <div class="logo-badge"><Icon name="shield" :size="16" /></div>
          <span v-if="sidebarOpen" class="logo-text">智研赋能 · 后台</span>
        </div>
        <nav class="side-nav">
          <button
            v-for="n in nav"
            :key="n.key"
            class="side-item"
            :class="{ on: activePage === n.key }"
            :title="n.label"
            @click="switchTab(n.key)"
          >
            <Icon :name="n.icon" :size="18" />
            <span v-if="sidebarOpen">{{ n.label }}</span>
          </button>
        </nav>
      </div>
      <div class="side-foot" :class="{ hide: !sidebarOpen }">
        <button class="side-item back" @click="router.push('/')">
          <Icon name="arrow-left" :size="18" />
          <span>返回平台</span>
        </button>
      </div>
    </aside>

    <!-- 主区域 -->
    <div class="main" :class="{ collapsed: !sidebarOpen }">
      <header class="topbar">
        <div class="tb-left">
          <button class="tb-btn" @click="sidebarOpen = !sidebarOpen" aria-label="切换侧边栏">
            <Icon name="menu" :size="18" />
          </button>
          <div>
            <b>{{ nav.find((n) => n.key === activePage)?.label }}</b>
            <span class="tb-desc">{{ activeDesc }}</span>
          </div>
        </div>
        <div class="tb-right">
          <span class="chip gold"><Icon name="shield" :size="13" style="vertical-align: -2px" /> 管理员</span>
          <div class="avatar">A</div>
        </div>
      </header>

      <main class="content">
        <!-- 数据看板 -->
        <template v-if="activePage === 'dashboard'">
          <div class="metric-grid">
            <div v-for="c in cards" :key="c.label" class="metric">
              <span class="ic"><Icon :name="c.icon" :size="16" /></span>
              <div>
                <b>{{ c.value }}</b>
                <span>{{ c.label }}</span>
              </div>
            </div>
          </div>

          <div class="grid-2">
            <section class="panel">
              <header class="panel-head"><b>近 7 天活跃</b><span class="muted">提交 / 打卡</span></header>
              <div class="chart-bars">
                <div v-for="d in trendRows" :key="d.day" class="bar-col" :title="`${d.day} 提交 ${d.sub} 次 / 打卡 ${d.ck} 次`">
                  <div class="bar-pair">
                    <i class="bar sub" :style="{ height: barH(d.sub) }" />
                    <i class="bar ck" :style="{ height: barH(d.ck) }" />
                  </div>
                  <span>{{ d.label }}</span>
                </div>
              </div>
              <div class="legend">
                <span><i class="dot sub" />代码提交</span>
                <span><i class="dot ck" />学习打卡</span>
              </div>
            </section>

            <section class="panel">
              <header class="panel-head"><b>刷题排行榜</b><span class="muted">Top 10</span></header>
              <ul class="rank-list">
                <li v-for="(u, i) in ranking" :key="u.id">
                  <i class="rank" :class="`r${i + 1}`">{{ i + 1 }}</i>
                  <span class="uname">{{ u.userName || u.userAccount || '匿名用户' }}</span>
                  <b>{{ u.acCount ?? u.acceptedNum ?? 0 }} <em>AC</em></b>
                </li>
                <li v-if="!ranking.length" class="muted small">暂无数据</li>
              </ul>
            </section>
          </div>

          <div class="grid-3">
            <section class="panel">
              <header class="panel-head"><b>热门题目</b></header>
              <ul class="hot-list">
                <li v-for="q in hot.questions" :key="q.id">
                  <a :href="`/problems/${q.id}`" @click.prevent="router.push(`/problems/${q.id}`)">{{ q.title }}</a>
                  <span class="muted small">{{ q.submitNum ?? 0 }} 次提交</span>
                </li>
                <li v-if="!hot.questions.length" class="muted small">暂无数据</li>
              </ul>
            </section>
            <section class="panel">
              <header class="panel-head"><b>热门资料</b></header>
              <ul class="hot-list">
                <li v-for="m in hot.materials" :key="m.id">
                  <span class="one-line">{{ m.title }}</span>
                  <span class="muted small">{{ m.downloadNum ?? 0 }} 下载</span>
                </li>
                <li v-if="!hot.materials.length" class="muted small">暂无数据</li>
              </ul>
            </section>
            <section class="panel">
              <header class="panel-head"><b>热门帖子</b></header>
              <ul class="hot-list">
                <li v-for="p in hot.posts" :key="p.id">
                  <a :href="`/forum/${p.id}`" @click.prevent="router.push(`/forum/${p.id}`)">{{ p.title }}</a>
                  <span class="muted small">{{ p.thumbNum ?? 0 }} 赞</span>
                </li>
                <li v-if="!hot.posts.length" class="muted small">暂无数据</li>
              </ul>
            </section>
          </div>
        </template>

        <!-- 用户管理 -->
        <template v-else-if="activePage === 'users'">
          <div class="toolbar">
            <input v-model="userQuery.keyword" class="ipt" placeholder="按昵称搜索" @keyup.enter="loadUsers" />
            <button class="btn primary sm" @click="loadUsers">搜索</button>
            <span class="muted count">共 {{ userTotal }} 位用户</span>
          </div>
          <div class="panel table-wrap">
            <table class="data-table">
              <thead>
                <tr><th style="width: 70px">ID</th><th>账号</th><th>昵称</th><th style="width: 110px">角色</th><th style="width: 130px">注册时间</th><th style="width: 130px">操作</th></tr>
              </thead>
              <tbody>
                <tr v-for="u in users" :key="u.id">
                  <td class="mono muted">{{ u.id }}</td>
                  <td>{{ u.userAccount }}</td>
                  <td>{{ u.userName || '—' }}</td>
                  <td><span class="chip tiny" :class="{ gold: u.userRole === 'admin' }">{{ u.userRole === 'admin' ? '管理员' : '普通用户' }}</span></td>
                  <td class="muted small">{{ formatDate(u.createTime) }}</td>
                  <td class="ops">
                    <button class="link" @click="toggleRole(u)">切换角色</button>
                    <button class="link danger" @click="removeUser(u)">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </template>

        <!-- 帖子管理 -->
        <template v-else-if="activePage === 'posts'">
          <div class="toolbar">
            <input v-model="postQuery.keyword" class="ipt" placeholder="按标题搜索" @keyup.enter="loadPosts" />
            <button class="btn primary sm" @click="loadPosts">搜索</button>
            <button class="btn ghost sm" @click="postAddOpen = true"><Icon name="plus" :size="14" /> 发布帖子</button>
            <span class="muted count">共 {{ postTotal }} 篇帖子</span>
          </div>
          <div class="panel table-wrap">
            <table class="data-table">
              <thead>
                <tr><th style="width: 70px">ID</th><th>标题</th><th style="width: 100px">作者</th><th style="width: 90px">点赞</th><th style="width: 130px">发布时间</th><th style="width: 130px">操作</th></tr>
              </thead>
              <tbody>
                <tr v-for="p in posts" :key="p.id">
                  <td class="mono muted">{{ p.id }}</td>
                  <td class="one-line">
                    <a :href="`/forum/${p.id}`" class="p-title" @click.prevent="router.push(`/forum/${p.id}`)">{{ p.title }}</a>
                  </td>
                  <td class="muted small">{{ p.authorName || p.user?.userName || '—' }}</td>
                  <td class="muted small">{{ p.thumbNum ?? 0 }}</td>
                  <td class="muted small">{{ formatDate(p.createTime) }}</td>
                  <td class="ops">
                    <button class="link" @click="router.push(`/forum/${p.id}`)">查看</button>
                    <button class="link danger" @click="removePost(p)">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </template>

        <!-- 资料管理 -->
        <template v-else-if="activePage === 'materials'">
          <div class="toolbar">
            <input v-model="materialQuery.keyword" class="ipt" placeholder="按标题搜索" @keyup.enter="loadMaterials" />
            <button class="btn primary sm" @click="loadMaterials">搜索</button>
            <button class="btn ghost sm" @click="uploadOpen = true"><Icon name="plus" :size="14" /> 新增资料</button>
            <span class="muted count">共 {{ materialTotal }} 份</span>
          </div>
          <div class="panel table-wrap">
            <table class="data-table">
              <thead>
                <tr><th style="width: 70px">ID</th><th>标题</th><th style="width: 110px">分类</th><th style="width: 80px">类型</th><th style="width: 90px">大小</th><th style="width: 100px">浏览/下载</th><th style="width: 80px">状态</th><th style="width: 160px">操作</th></tr>
              </thead>
              <tbody>
                <tr v-for="m in materials" :key="m.id">
                  <td class="mono muted">{{ m.id }}</td>
                  <td class="one-line" :title="m.title">{{ m.title }}</td>
                  <td><span class="chip tiny">{{ m.category }}</span></td>
                  <td class="muted small">{{ m.fileType === 'link' ? '在线' : (m.fileType || 'pdf').toUpperCase() }}</td>
                  <td class="muted small">{{ m.fileType === 'link' ? '—' : formatSize(m.fileSize) }}</td>
                  <td class="muted small">{{ m.viewNum ?? 0 }} / {{ m.downloadNum ?? 0 }}</td>
                  <td><span class="chip tiny" :class="m.status === 1 ? 'green' : 'gray'">{{ m.status === 1 ? '上架' : '下架' }}</span></td>
                  <td class="ops">
                    <button class="link" @click="toggleStatus(m)">{{ m.status === 1 ? '下架' : '上架' }}</button>
                    <button class="link danger" @click="removeMaterial(m)">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </template>

        <!-- 题库管理 -->
        <template v-else-if="activePage === 'questions'">
          <div class="toolbar">
            <input v-model="questionQuery.keyword" class="ipt" placeholder="按标题搜索" @keyup.enter="loadQuestions" />
            <button class="btn primary sm" @click="loadQuestions">搜索</button>
            <span class="muted count">共 {{ questionTotal }} 道题</span>
          </div>
          <div class="panel table-wrap">
            <table class="data-table">
              <thead>
                <tr><th style="width: 70px">ID</th><th>标题</th><th style="width: 110px">提交</th><th style="width: 110px">通过</th><th style="width: 130px">操作</th></tr>
              </thead>
              <tbody>
                <tr v-for="q in questions" :key="q.id">
                  <td class="mono muted">{{ q.id }}</td>
                  <td><a :href="`/problems/${q.id}`" class="p-title" @click.prevent="router.push(`/problems/${q.id}`)">{{ q.title }}</a></td>
                  <td class="muted small">{{ q.submitNum ?? 0 }}</td>
                  <td class="muted small">{{ q.acceptedNum ?? 0 }}</td>
                  <td class="ops"><button class="link" @click="router.push(`/problems/${q.id}`)">查看</button></td>
                </tr>
              </tbody>
            </table>
          </div>
        </template>

        <!-- 角色权限 -->
        <template v-else-if="activePage === 'roles'">
          <div class="role-cards">
            <section class="panel role-card">
              <div class="role-ic gold"><Icon name="shield" :size="20" /></div>
              <div>
                <b>{{ adminCount }}</b>
                <span>管理员账号</span>
              </div>
              <p class="small muted">拥有后台全部权限：内容增删、用户角色、平台管理。专属账号 admin。</p>
            </section>
            <section class="panel role-card">
              <div class="role-ic"><Icon name="user" :size="20" /></div>
              <div>
                <b>{{ userTotal }}</b>
                <span>注册用户</span>
              </div>
              <p class="small muted">可刷题、看资料、发帖、使用 AI 画像与决策推演。</p>
            </section>
          </div>
          <div class="panel">
            <header class="panel-head"><b>权限矩阵</b><span class="muted">admin / user</span></header>
            <table class="data-table">
              <thead><tr><th>能力</th><th style="width: 90px">管理员</th><th style="width: 90px">普通用户</th></tr></thead>
              <tbody>
                <tr v-for="p in permissionMatrix" :key="p.name">
                  <td>{{ p.name }}</td>
                  <td><Icon v-if="p.admin" name="check" :size="15" class="ok" /></td>
                  <td><Icon v-if="p.user" name="check" :size="15" class="ok" /><span v-else class="muted">—</span></td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="panel">
            <header class="panel-head"><b>本页可见的管理员</b><span class="muted">前 10 条中</span></header>
            <ul class="hot-list">
              <li v-for="u in adminList" :key="u.id">
                <span>{{ u.userName || u.userAccount }}</span>
                <span class="muted small">{{ u.userAccount }}</span>
              </li>
              <li v-if="!adminList.length" class="muted small">本页暂无管理员，可在「用户管理」中切换角色</li>
            </ul>
          </div>
        </template>

        <!-- 系统设置 -->
        <template v-else>
          <section v-for="g in settings" :key="g.group" class="panel">
            <header class="panel-head"><b>{{ g.group }}</b></header>
            <ul class="set-list">
              <li v-for="it in g.items" :key="it.k">
                <span class="set-k">{{ it.k }}</span>
                <span class="set-v">{{ it.v }}</span>
              </li>
            </ul>
          </section>
        </template>
      </main>
    </div>

    <!-- 发布帖子弹窗 -->
    <a-modal v-model:visible="postAddOpen" title="发布帖子（管理员）" :footer="false" :width="560">
      <div class="modal-form">
        <label>标题</label>
        <a-input v-model="postForm.title" placeholder="帖子标题" />
        <label>内容（支持 Markdown）</label>
        <a-textarea v-model="postForm.content" :auto-size="{ minRows: 8 }" placeholder="正文内容" />
        <label>标签（逗号分隔）</label>
        <a-input v-model="postForm.tags" placeholder="经验,408,复习方法" />
        <a-button long type="primary" :loading="posting" style="margin-top: 14px" @click="addPost">发布帖子</a-button>
      </div>
    </a-modal>

    <!-- 新增资料弹窗 -->
    <a-modal v-model:visible="uploadOpen" title="新增资料（管理员）" :footer="false" :width="520">
      <div class="modal-form">
        <label>资料标题</label>
        <a-input v-model="uploadForm.title" placeholder="例如：2025 年 408 真题（在线做题）" />
        <label>资料类型</label>
        <a-select v-model="uploadForm.fileType">
          <a-option value="link">在线链接（外部学习页面）</a-option>
          <a-option value="pdf">上传文件</a-option>
        </a-select>
        <div v-if="uploadForm.fileType === 'link'">
          <label>链接地址</label>
          <a-input v-model="uploadForm.fileUrl" placeholder="https://www.csgraduates.com/study_methods/..." />
        </div>
        <div v-else>
          <label>上传文件</label>
          <input type="file" accept=".pdf,.doc,.docx,.zip,.ppt,.pptx" @change="pickFile" />
          <p v-if="uploadForm.fileUrl" class="ok-tip">已上传：{{ uploadForm.fileUrl }}</p>
        </div>
        <div class="grid2">
          <div>
            <label>分类</label>
            <a-select v-model="uploadForm.category">
              <a-option value="408 真题">408 真题</a-option>
              <a-option value="408 模拟题">408 模拟题</a-option>
              <a-option value="408 习题">408 习题</a-option>
              <a-option value="408 大纲">408 大纲</a-option>
              <a-option value="408">408 专业课</a-option>
              <a-option value="方法论">方法论</a-option>
              <a-option value="数学">数学</a-option>
              <a-option value="英语">英语</a-option>
              <a-option value="政治">政治</a-option>
              <a-option value="复试">复试</a-option>
              <a-option value="经验">经验</a-option>
            </a-select>
          </div>
          <div>
            <label>408 科目（可选）</label>
            <a-select v-model="uploadForm.subject" allow-clear>
              <a-option value="ds">数据结构</a-option>
              <a-option value="co">计组</a-option>
              <a-option value="os">操作系统</a-option>
              <a-option value="cn">计网</a-option>
            </a-select>
          </div>
        </div>
        <label>简介</label>
        <a-textarea v-model="uploadForm.description" :auto-size="{ minRows: 2 }" placeholder="一句话描述这份资料" />
        <label>标签（逗号分隔）</label>
        <a-input v-model="uploadForm.tags" placeholder="真题,高频,冲刺" />
        <a-button long type="primary" :loading="uploading" style="margin-top: 14px" @click="saveMaterial">发布资料</a-button>
      </div>
    </a-modal>
  </div>
</template>

<style scoped>
/* ============ 外壳：全屏独立后台 ============ */
.admin-shell { position: fixed; inset: 0; z-index: 300; display: flex; background: #f5f6f8; color: #1f2329; font-size: 14px; }
.sidebar {
  width: 224px; background: #17181c; color: #9aa0a8; display: flex; flex-direction: column;
  transition: width 0.2s; overflow: hidden; flex: none;
}
.sidebar.collapsed { width: 0; }
.side-head { padding: 20px 16px; flex: 1; overflow: hidden; }
.logo-row { display: flex; align-items: center; gap: 10px; margin-bottom: 24px; white-space: nowrap; }
.logo-badge { width: 32px; height: 32px; border-radius: 9px; background: #5c8a54; display: grid; place-items: center; color: #fff; flex: none; }
.logo-text { font-weight: 600; color: #fff; font-size: 13.5px; letter-spacing: 0.02em; }
.side-nav { display: flex; flex-direction: column; gap: 4px; }
.side-item {
  display: flex; align-items: center; gap: 11px; padding: 10px 12px; border: none; background: none;
  border-radius: 9px; font-size: 13px; color: #9aa0a8; cursor: pointer; white-space: nowrap; width: 100%;
  transition: 0.15s; text-align: left;
}
.side-item:hover { color: #fff; background: rgba(255, 255, 255, 0.06); }
.side-item.on { color: #fff; background: rgba(255, 255, 255, 0.12); }
.side-foot { padding: 16px; border-top: 1px solid rgba(255, 255, 255, 0.08); }
.side-foot.hide { display: none; }

.main { flex: 1; min-width: 0; display: flex; flex-direction: column; }
.topbar {
  height: 56px; background: #fff; border-bottom: 1px solid #e5e7eb;
  display: flex; align-items: center; justify-content: space-between; padding: 0 20px; flex: none;
}
.tb-left { display: flex; align-items: center; gap: 12px; }
.tb-left b { font-size: 14px; color: #1f2329; }
.tb-desc { font-size: 12px; color: #9aa0a8; margin-left: 8px; }
.tb-btn { padding: 7px; border: none; background: none; border-radius: 8px; cursor: pointer; color: #6b7280; display: grid; place-items: center; }
.tb-btn:hover { background: #f3f4f6; }
.tb-right { display: flex; align-items: center; gap: 12px; }
.avatar { width: 32px; height: 32px; border-radius: 999px; background: rgba(92, 138, 84, 0.15); color: #4e7a3f; display: grid; place-items: center; font-weight: 600; font-size: 13px; }

.content { padding: 22px 24px 40px; overflow-y: auto; flex: 1; display: flex; flex-direction: column; gap: 16px; }

/* ============ 面板与卡片 ============ */
.panel { background: #fff; border: 1px solid #e5e7eb; border-radius: 14px; padding: 18px 20px; display: flex; flex-direction: column; gap: 12px; }
.panel-head { display: flex; align-items: center; justify-content: space-between; }
.panel-head b { font-size: 14px; color: #1f2329; }
.metric-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 14px; }
.metric { background: #fff; border: 1px solid #e5e7eb; border-radius: 14px; padding: 16px; display: flex; gap: 12px; align-items: center; }
.metric .ic { width: 36px; height: 36px; border-radius: 10px; display: grid; place-items: center; background: rgba(92, 138, 84, 0.12); color: #5c8a54; flex: none; }
.metric b { display: block; font-size: 20px; color: #1f2329; letter-spacing: -0.5px; }
.metric span { font-size: 12px; color: #9aa0a8; }
.grid-2 { display: grid; grid-template-columns: 1.15fr 1fr; gap: 16px; }
.grid-3 { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }

.chart-bars { display: flex; align-items: flex-end; gap: 12px; height: 130px; padding: 8px 4px 0; border-bottom: 1px dashed #e5e7eb; }
.bar-col { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: flex-end; gap: 6px; height: 100%; min-width: 0; }
.bar-pair { display: flex; align-items: flex-end; justify-content: center; gap: 4px; width: 100%; height: 100%; }
.bar-pair .bar { width: 9px; border-radius: 5px 5px 2px 2px; display: block; }
.bar.sub { background: #8fbc85; }
.bar.ck { background: #e3c878; }
.bar-col span { font-size: 10px; color: #9aa0a8; }
.legend { display: flex; gap: 16px; font-size: 12px; color: #6b7280; }
.legend .dot { display: inline-block; width: 8px; height: 8px; border-radius: 3px; margin-right: 6px; }
.dot.sub { background: #8fbc85; } .dot.ck { background: #e3c878; }

.rank-list, .hot-list, .set-list { list-style: none; margin: 0; padding: 0; display: grid; gap: 9px; }
.rank-list li { display: flex; align-items: center; gap: 10px; font-size: 13px; }
.rank { width: 22px; height: 22px; border-radius: 7px; display: grid; place-items: center; font-size: 11px; font-style: normal; background: #f3f4f6; color: #9aa0a8; font-weight: 700; }
.rank.r1 { background: rgba(245, 215, 95, 0.28); color: #8a6a12; }
.rank.r2 { background: rgba(31, 35, 41, 0.08); color: #4b5563; }
.rank.r3 { background: rgba(245, 180, 95, 0.24); color: #9a5b1f; }
.uname { flex: 1; color: #1f2329; }
.rank-list b { color: #4e7a3f; font-size: 13px; }
.rank-list em { font-style: normal; font-size: 10.5px; color: #9aa0a8; }
.hot-list li { display: flex; justify-content: space-between; gap: 10px; font-size: 13px; align-items: center; }
.hot-list a { color: #1f2329; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; text-decoration: none; cursor: pointer; }
.hot-list a:hover { color: #5c8a54; }
.one-line { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.set-list li { display: flex; gap: 14px; padding: 8px 0; border-bottom: 1px dashed #f0f1f3; font-size: 13px; }
.set-list li:last-child { border-bottom: none; }
.set-k { color: #6b7280; width: 160px; flex: none; }
.set-v { color: #1f2329; flex: 1; }

/* ============ 工具栏与表格 ============ */
.toolbar { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.ipt { height: 36px; padding: 0 12px; border-radius: 9px; border: 1px solid #e5e7eb; background: #fff; font-size: 13px; outline: none; min-width: 220px; }
.ipt:focus { border-color: #8fbc85; }
.count { margin-left: auto; font-size: 12.5px; }
.table-wrap { padding: 4px 4px 8px; overflow-x: auto; }
.data-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.data-table th { text-align: left; padding: 10px 12px; color: #9aa0a8; font-weight: 500; font-size: 12px; border-bottom: 1px solid #f0f1f3; }
.data-table td { padding: 11px 12px; border-bottom: 1px solid #f7f8f9; color: #1f2329; }
.data-table tr:last-child td { border-bottom: none; }
.data-table tr:hover td { background: #fafbfc; }
.p-title { color: #1f2329; font-weight: 600; text-decoration: none; cursor: pointer; }
.p-title:hover { color: #5c8a54; }
.mono { font-family: ui-monospace, Menlo, monospace; font-size: 12px; }
.small { font-size: 12px; }
.ops { display: flex; gap: 10px; }
.link { background: none; border: none; color: #5c8a54; font-size: 12.5px; cursor: pointer; padding: 0; }
.link:hover { text-decoration: underline; }
.link.danger { color: #c25656; }
.ok { color: #5c8a54; }

/* chips & buttons */
.chip { display: inline-flex; align-items: center; gap: 4px; padding: 3px 10px; border-radius: 999px; font-size: 12px; background: #f3f4f6; color: #6b7280; }
.chip.gold { background: rgba(245, 215, 95, 0.28); color: #8a6a12; }
.chip.tiny { padding: 2px 8px; font-size: 11.5px; }
.chip.green { background: rgba(92, 138, 84, 0.14); color: #4e7a3f; }
.chip.gray { background: #f3f4f6; color: #9aa0a8; }
.btn { border: 1px solid transparent; border-radius: 9px; cursor: pointer; font-size: 13px; transition: 0.15s; }
.btn.primary { background: #5c8a54; color: #fff; padding: 8px 16px; }
.btn.primary:hover { background: #4e7a3f; }
.btn.ghost { background: #fff; border-color: #e5e7eb; color: #1f2329; padding: 8px 16px; display: inline-flex; align-items: center; gap: 6px; }
.btn.ghost:hover { border-color: #8fbc85; color: #4e7a3f; }
.btn.sm { padding: 8px 16px; }

/* roles */
.role-cards { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.role-card { flex-direction: row; align-items: center; gap: 14px; }
.role-card b { display: block; font-size: 22px; color: #1f2329; }
.role-card > div > span { font-size: 12.5px; color: #9aa0a8; }
.role-card p { margin: 0; }
.role-ic { width: 44px; height: 44px; border-radius: 12px; display: grid; place-items: center; background: #f3f4f6; color: #6b7280; flex: none; }
.role-ic.gold { background: rgba(245, 215, 95, 0.3); color: #8a6a12; }

/* modal */
.modal-form { display: grid; gap: 8px; }
.modal-form label { font-size: 12.5px; color: #4b5563; font-weight: 600; margin-top: 8px; }
.ok-tip { font-size: 12px; color: #4e7a3f; margin: 0; word-break: break-all; }
.grid2 { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }

@media (max-width: 1180px) {
  .metric-grid { grid-template-columns: repeat(3, 1fr); }
  .grid-3 { grid-template-columns: 1fr; }
  .role-cards { grid-template-columns: 1fr; }
}
@media (max-width: 860px) {
  .grid-2 { grid-template-columns: 1fr; }
  .metric-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
