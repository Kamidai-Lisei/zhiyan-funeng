<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import { useAuthStore } from '@/stores/auth'
import { studyApi } from '@/api/study'
import { statsApi } from '@/api/stats'
import { forumApi } from '@/api/forum'
import { materialApi } from '@/api/material'
import Heatmap from '@/components/Heatmap.vue'
import { formatDate, formatSize, fromNow } from '@/utils/format'
import type { Material, Post, StudyOverview, StudyPlan } from '@/types'

const auth = useAuthStore()

const my = ref<StudyOverview | null>(null)
const plan = ref<StudyPlan | null>(null)
const myPosts = ref<Post[]>([])
const favMaterials = ref<Material[]>([])
const rank = ref(0)
const loading = ref(true)

const checkinOpen = ref(false)
const checkinForm = reactive({ duration: 120, content: '' })
const checkinLoading = ref(false)
const todayChecked = ref(false)

const editOpen = ref(false)
const profileForm = reactive({ userName: '', userProfile: '', userAvatar: '' })
const saving = ref(false)

const planOpen = ref(false)
const planForm = reactive({ targetDate: '', dailyMinutes: 180, subjects: '', motto: '' })

const daysLeft = computed(() => {
  const t = plan.value?.targetDate
  if (!t) return 0
  const d = new Date(t)
  return Math.max(0, Math.ceil((d.getTime() - Date.now()) / 86400000))
})

const joined = computed(() => formatDate(auth.user?.createTime, 'YYYY 年 M 月'))

const subjectList = computed(() => {
  const raw = plan.value?.subjects || '数据结构 / 计算机组成原理 / 操作系统 / 计算机网络'
  return raw.split(/[\/,、|]/).map((s) => s.trim()).filter(Boolean)
})

async function load() {
  loading.value = true
  try {
    const tasks: Promise<any>[] = [
      studyApi.overview().then((r) => (my.value = r)),
      studyApi.plan().then((r) => (plan.value = r)),
      studyApi.today().then((r) => (todayChecked.value = !!r)),
      forumApi
        .list({ current: 1, pageSize: 5, userId: auth.user?.id })
        .then((r) => (myPosts.value = r.records || []))
        .catch(() => (myPosts.value = [])),
      materialApi.myFavour().then((r) => (favMaterials.value = r || [])).catch(() => (favMaterials.value = [])),
      statsApi
        .ranking()
        .then((r) => {
          const idx = (r || []).findIndex((u: any) => String(u.id) === String(auth.user?.id))
          rank.value = idx >= 0 ? idx + 1 : 0
        })
        .catch(() => (rank.value = 0)),
    ]
    await Promise.all(tasks)
  } finally {
    loading.value = false
  }
}

async function doCheckin() {
  checkinLoading.value = true
  try {
    await studyApi.checkin({ duration: checkinForm.duration, content: checkinForm.content })
    Message.success('打卡成功，继续保持！')
    checkinOpen.value = false
    checkinForm.content = ''
    studyApi.overview().then((r) => (my.value = r))
    todayChecked.value = true
  } catch (e: any) {
    Message.error(e?.message || '打卡失败')
  } finally {
    checkinLoading.value = false
  }
}

function openEdit() {
  profileForm.userName = auth.user?.userName || ''
  profileForm.userProfile = auth.user?.userProfile || ''
  profileForm.userAvatar = auth.user?.userAvatar || ''
  editOpen.value = true
}

async function saveProfile() {
  saving.value = true
  try {
    await auth.updateMy({ ...profileForm })
    Message.success('资料已更新')
    editOpen.value = false
  } catch (e: any) {
    Message.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

function openPlan() {
  planForm.targetDate = String(plan.value?.targetDate || '2026-12-19').slice(0, 10)
  planForm.dailyMinutes = plan.value?.dailyMinutes || 180
  planForm.subjects = plan.value?.subjects || ''
  planForm.motto = plan.value?.motto || ''
  planOpen.value = true
}

async function savePlan() {
  saving.value = true
  try {
    await studyApi.savePlan({ ...planForm } as StudyPlan)
    plan.value = await studyApi.plan()
    Message.success('学习计划已更新')
    planOpen.value = false
  } catch (e: any) {
    Message.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function unFavour(id: number | string) {
  try {
    await materialApi.favour(id)
    favMaterials.value = favMaterials.value.filter((m) => String(m.id) !== String(id))
    Message.info('已取消收藏')
  } catch (e: any) {
    Message.error(e?.message || '操作失败')
  }
}

onMounted(load)
</script>

<template>
  <div class="page container">
    <section class="card profile-hero">
      <div class="avatar">{{ auth.initial }}</div>
      <div class="who">
        <div class="name-row">
          <h1>{{ auth.displayName }}</h1>
          <span class="role" :class="{ admin: auth.isAdmin }">{{ auth.isAdmin ? '管理员' : '学员' }}</span>
        </div>
        <p class="acc">@{{ auth.user?.userAccount }} · 加入于 {{ joined }}</p>
        <p class="bio">{{ auth.user?.userProfile || '这个人很懒，还没有写简介。' }}</p>
      </div>
      <div class="hero-actions">
        <button class="btn-ghost sm" @click="openEdit">编辑资料</button>
        <button class="btn-primary sm" :disabled="todayChecked" @click="checkinOpen = true">
          {{ todayChecked ? '今日已打卡' : '今日打卡' }}
        </button>
      </div>
    </section>

    <div class="stat-row">
      <div class="stat-card">
        <span class="lbl">连续打卡</span>
        <b>{{ my?.streak ?? 0 }} <i>天</i></b>
      </div>
      <div class="stat-card">
        <span class="lbl">累计打卡</span>
        <b>{{ my?.checkinDays ?? 0 }} <i>天</i></b>
      </div>
      <div class="stat-card">
        <span class="lbl">学习时长</span>
        <b>{{ Math.round((my?.totalMinutes ?? 0) / 60) }} <i>小时</i></b>
      </div>
      <div class="stat-card">
        <span class="lbl">题目通过</span>
        <b>{{ my?.acceptedCount ?? 0 }} <i>题</i></b>
      </div>
      <div class="stat-card">
        <span class="lbl">排行榜</span>
        <b>{{ rank ? `#${rank}` : '—' }}</b>
      </div>
    </div>

    <div class="grid-2">
      <section class="card block">
        <header class="block-head">
          <b>学习热力图</b>
          <span class="muted small">近 16 周</span>
        </header>
        <Heatmap :dates="my?.recentCheckins || []" :weeks="16" />
        <div class="legend">
          <span class="muted small">少</span>
          <i class="cell l0" /><i class="cell l1" /><i class="cell l2" /><i class="cell l3" /><i class="cell l4" />
          <span class="muted small">多</span>
        </div>
      </section>

      <section class="card block">
        <header class="block-head">
          <b>备考计划</b>
          <button class="link" @click="openPlan">调整</button>
        </header>
        <div class="plan-body">
          <div class="plan-days">
            <b>{{ daysLeft }}</b>
            <span>天后上考场</span>
          </div>
          <ul class="plan-list">
            <li><span>目标日期</span><b>{{ String(plan?.targetDate || '2026-12-19').slice(0, 10) }}</b></li>
            <li><span>每日目标</span><b>{{ plan?.dailyMinutes || 180 }} 分钟</b></li>
            <li class="subjects-row">
              <span>重点科目</span>
              <span class="subject-chips">
                <i v-for="s in subjectList" :key="s" class="subject-chip">{{ s }}</i>
              </span>
            </li>
          </ul>
        </div>
        <p class="motto">“{{ plan?.motto || '每天进步一点点，坚持带来大改变。' }}”</p>
        <RouterLink to="/learn/408" class="btn-ghost sm full">继续学习 408 →</RouterLink>
      </section>
    </div>

    <div class="grid-2">
      <section class="card block">
        <header class="block-head">
          <b>我的帖子</b>
          <RouterLink to="/forum/new" class="link">发帖</RouterLink>
        </header>
        <ul v-if="myPosts.length" class="list">
          <li v-for="p in myPosts" :key="p.id">
            <RouterLink :to="`/forum/${p.id}`" class="li-title">{{ p.title }}</RouterLink>
            <span class="muted small">{{ fromNow(p.createTime) }}</span>
          </li>
        </ul>
        <p v-else class="muted empty-tip">还没有发过帖子，去论坛聊聊你的备考心得吧。</p>
      </section>

      <section class="card block">
        <header class="block-head">
          <b>资料收藏</b>
          <RouterLink to="/materials" class="link">资料库</RouterLink>
        </header>
        <ul v-if="favMaterials.length" class="list">
          <li v-for="m in favMaterials.slice(0, 6)" :key="m.id">
            <RouterLink :to="`/materials?id=${m.id}`" class="li-title">{{ m.title }}</RouterLink>
            <span class="muted small">{{ formatSize(m.fileSize) }}</span>
            <button class="link danger" @click="unFavour(m.id)">移除</button>
          </li>
        </ul>
        <p v-else class="muted empty-tip">收藏夹是空的，去资料库挑几份 PDF 吧。</p>
      </section>
    </div>

    <div class="quick-links">
      <RouterLink to="/submissions" class="quick">
        <Icon name="code" :size="18" /><span>我的提交</span>
      </RouterLink>
      <RouterLink to="/wrong-book" class="quick">
        <Icon name="target" :size="18" /><span>错题本</span>
      </RouterLink>
      <RouterLink to="/career/resume" class="quick">
        <Icon name="file" :size="18" /><span>AI 简历诊断</span>
      </RouterLink>
      <RouterLink to="/forum" class="quick">
        <Icon name="chat" :size="18" /><span>考研论坛</span>
      </RouterLink>
    </div>

    <a-modal v-model:visible="checkinOpen" title="今日打卡" :footer="false" :width="420">
      <div class="modal-form">
        <label>学习时长（分钟）</label>
        <a-input-number v-model="checkinForm.duration" :min="10" :max="900" :step="10" style="width: 100%" />
        <label>今天学了什么？</label>
        <a-textarea v-model="checkinForm.content" :auto-size="{ minRows: 3 }" placeholder="例如：数据结构图论 + 2 道题" />
        <a-button long type="primary" :loading="checkinLoading" @click="doCheckin">完成打卡</a-button>
      </div>
    </a-modal>

    <a-modal v-model:visible="editOpen" title="编辑个人资料" :footer="false" :width="440">
      <div class="modal-form">
        <label>昵称</label>
        <a-input v-model="profileForm.userName" placeholder="展示在论坛与排行榜" />
        <label>个性签名</label>
        <a-textarea v-model="profileForm.userProfile" :auto-size="{ minRows: 3 }" placeholder="一句话介绍自己" />
        <label>头像地址</label>
        <a-input v-model="profileForm.userAvatar" placeholder="https://…（选填）" />
        <a-button long type="primary" :loading="saving" @click="saveProfile">保存</a-button>
      </div>
    </a-modal>

    <a-modal v-model:visible="planOpen" title="调整备考计划" :footer="false" :width="440">
      <div class="modal-form">
        <label>考试日期</label>
        <a-input v-model="planForm.targetDate" placeholder="2026-12-19" />
        <label>每日目标时长（分钟）</label>
        <a-input-number v-model="planForm.dailyMinutes" :min="30" :max="720" :step="30" style="width: 100%" />
        <label>重点科目</label>
        <a-input v-model="planForm.subjects" placeholder="用 / 分隔" />
        <label>激励语</label>
        <a-input v-model="planForm.motto" placeholder="写一句给自己看的话" />
        <a-button long type="primary" :loading="saving" @click="savePlan">保存计划</a-button>
      </div>
    </a-modal>
  </div>
</template>

<style scoped>
.page { display: grid; gap: 18px; }
.profile-hero { display: flex; align-items: center; gap: 20px; padding: 24px 26px; flex-wrap: wrap; }
.avatar {
  width: 72px; height: 72px; border-radius: 999px; flex: none;
  display: grid; place-items: center; font-size: 26px; font-weight: 600; color: #fffdf8;
  background: linear-gradient(150deg, var(--meadow), #7fae7c);
  box-shadow: 0 10px 24px rgba(90, 143, 90, 0.28);
}
.who { flex: 1; min-width: 220px; }
.name-row { display: flex; align-items: center; gap: 10px; }
.name-row h1 { margin: 0; font-size: 22px; color: var(--ink); letter-spacing: -0.4px; }
.role { font-size: 11.5px; padding: 2px 9px; border-radius: 999px; background: #f4f0e4; color: var(--meadow); font-weight: 600; }
.role.admin { background: rgba(245, 215, 95, 0.26); color: #8a6a12; }
.acc { margin: 5px 0 0; font-size: 12.5px; color: var(--muted); }
.bio { margin: 8px 0 0; font-size: 13.5px; color: var(--ink-2); }
.hero-actions { display: flex; gap: 10px; }
.stat-row { display: grid; grid-template-columns: repeat(5, 1fr); gap: 14px; }
.stat-card {
  background: var(--cloth-2); border: 1px solid var(--line); border-radius: 14px; padding: 16px 18px;
  display: flex; flex-direction: column; gap: 6px;
}
.stat-card .lbl { font-size: 12.5px; color: var(--muted); }
.stat-card b { font-size: 24px; color: var(--ink); letter-spacing: -0.5px; }
.stat-card i { font-size: 12px; font-style: normal; color: var(--muted); font-weight: 500; }
.grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.block { padding: 20px 22px; display: flex; flex-direction: column; gap: 14px; }
.block-head { display: flex; align-items: center; justify-content: space-between; }
.block-head b { font-size: 14.5px; color: var(--ink); }
.legend { display: flex; align-items: center; gap: 5px; }
.legend .cell { width: 11px; height: 11px; border-radius: 3px; background: #f0e9dc; display: inline-block; }
.legend .l1 { background: #e4eddf; } .legend .l2 { background: #d7e5d3; }
.legend .l3 { background: #8ab98a; } .legend .l4 { background: var(--meadow); }
.plan-body { display: flex; gap: 20px; align-items: center; flex-wrap: wrap; }
.plan-days {
  width: 108px; height: 108px; flex: none; border-radius: 18px; display: grid; place-content: center;
  text-align: center; gap: 2px; background: linear-gradient(160deg, #f4f0e4, #e4eddf); border: 1px solid #e4eddf;
}
.plan-days b { font-size: 28px; color: var(--meadow-deep); letter-spacing: -1px; }
.plan-days span { font-size: 11.5px; color: #3b6ed8; }
.plan-list { list-style: none; margin: 0; padding: 0; display: grid; gap: 9px; flex: 1; min-width: 200px; }
.plan-list li { display: flex; justify-content: space-between; gap: 12px; font-size: 13px; }
.plan-list span { color: var(--muted); }
.plan-list b { color: var(--ink); font-weight: 600; text-align: right; }
.subjects-row { align-items: flex-start; }
.subject-chips { display: flex; flex-wrap: wrap; gap: 5px; justify-content: flex-end; max-width: 62%; }
.subject-chip {
  font-style: normal; font-size: 11.5px; padding: 2px 8px; border-radius: 999px;
  background: #f4f0e4; color: var(--meadow-deep); font-weight: 500;
}
.motto { margin: 0; font-size: 13px; color: var(--ink-2); background: #f7f2e8; border-left: 3px solid rgba(139, 115, 85, 0.30); padding: 10px 14px; border-radius: 0 10px 10px 0; font-style: italic; }
.list { list-style: none; margin: 0; padding: 0; display: grid; gap: 2px; }
.list li { display: flex; align-items: center; gap: 12px; padding: 10px 0; border-bottom: 1px dashed var(--line); }
.list li:last-child { border-bottom: none; }
.li-title { flex: 1; font-size: 13.5px; color: var(--ink); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.li-title:hover { color: var(--brand); }
.link { color: var(--brand); font-size: 13px; background: none; border: none; cursor: pointer; padding: 0; }
.link.danger { color: var(--wild-rose-deep); font-size: 12px; }
.empty-tip { margin: 0; font-size: 13px; padding: 12px 0; }
.quick-links { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; }
.quick {
  background: var(--cloth-2); border: 1px solid var(--line); border-radius: 14px; padding: 18px;
  display: flex; align-items: center; gap: 10px; color: var(--ink); font-size: 13.5px; font-weight: 500;
  transition: 0.18s;
}
.quick:hover { transform: translateY(-2px); border-color: #d7e5d3; color: var(--brand); box-shadow: 0 12px 28px rgba(61, 58, 52, 0.09); }
.modal-form { display: grid; gap: 8px; }
.modal-form label { font-size: 12.5px; color: var(--ink-2); font-weight: 600; margin-top: 6px; }
.modal-form a-button { margin-top: 10px; }
.full { width: 100%; text-align: center; }
@media (max-width: 1024px) {
  .stat-row { grid-template-columns: repeat(3, 1fr); }
  .quick-links { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 760px) {
  .grid-2 { grid-template-columns: 1fr; }
  .stat-row { grid-template-columns: repeat(2, 1fr); }
}
</style>
