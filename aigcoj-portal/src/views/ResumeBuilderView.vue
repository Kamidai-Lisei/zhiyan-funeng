<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import { resumeApi } from '../api/resume'
import { errorMessage } from '../api/http'
import { useAuthStore } from '../stores/auth'
import type { ResumeContent } from '../types'
import MarkdownView from '../components/MarkdownView.vue'
import Icon from '../components/Icon.vue'
import html2pdf from 'html2pdf.js'

const auth = useAuthStore()

const step = ref<'info' | 'template' | 'preview'>('info')
const id = ref<number | null>(null)
const title = ref('我的简历')
const targetSchool = ref('')
const targetMajor = ref('计算机科学与技术')
const template = ref('classic')
const score = ref(0)
const saving = ref(false)
const loading = ref(true)
const downloading = ref(false)

const form = reactive<ResumeContent>({
  name: auth.displayName,
  phone: '',
  email: '',
  city: '',
  summary: '',
  education: [{ school: '', major: '计算机科学与技术', degree: '本科', time: '2022.09 - 2026.06', gpa: '' }],
  projects: [{ name: '', role: '', time: '', desc: '', tech: '' }],
  skills: ['Java', 'Spring Boot', 'MySQL', '数据结构与算法'],
  awards: [],
})

const skillsText = computed({
  get: () => form.skills.join('，'),
  set: (v: string) => { form.skills = v.split(/[,，]/).map((s) => s.trim()).filter(Boolean) },
})
const awardsText = computed({
  get: () => form.awards.join('\n'),
  set: (v: string) => { form.awards = v.split('\n').map((s) => s.trim()).filter(Boolean) },
})

const templates = [
  { id: 'classic', name: '经典学术', desc: '复试 / 科研方向，稳重清晰', accent: '#3d6b3d' },
  { id: 'modern', name: '现代简约', desc: '互联网求职，重点突出项目', accent: '#2f6f8f' },
  { id: 'tech', name: '技术极简', desc: '后端 / 算法岗，强调技术栈', accent: '#1f2937' },
]

const load = async () => {
  loading.value = true
  try {
    const resume = await resumeApi.latest()
    id.value = resume.id || null
    title.value = resume.title || '我的简历'
    targetSchool.value = resume.targetSchool || ''
    targetMajor.value = resume.targetMajor || '计算机科学与技术'
    template.value = resume.template || 'classic'
    score.value = resume.score || 0
    if (resume.content && resume.content !== '{}') {
      const parsed = JSON.parse(resume.content)
      Object.assign(form, {
        ...parsed,
        education: parsed.education?.length ? parsed.education : form.education,
        projects: parsed.projects?.length ? parsed.projects : form.projects,
        skills: parsed.skills?.length ? parsed.skills : form.skills,
        awards: parsed.awards || [],
      })
    }
  } catch (e: any) {
    Message.error(errorMessage(e))
  } finally {
    loading.value = false
  }
}

const save = async (silent = false) => {
  saving.value = true
  try {
    const newId = await resumeApi.save({
      id: id.value || undefined,
      title: title.value,
      targetSchool: targetSchool.value,
      targetMajor: targetMajor.value,
      template: template.value,
      content: JSON.stringify(form),
      score: score.value,
    })
    id.value = newId
    if (!silent) Message.success('简历已保存')
    return newId
  } catch (e: any) {
    Message.error(errorMessage(e))
    return null
  } finally {
    saving.value = false
  }
}

const nextFromInfo = async () => {
  if (!form.name.trim()) return Message.warning('请填写姓名')
  await save(true)
  step.value = 'template'
}
const nextFromTemplate = async () => {
  await save(true)
  step.value = 'preview'
}

const addProject = () => form.projects.push({ name: '', role: '', time: '', desc: '', tech: '' })
const removeProject = (i: number) => form.projects.splice(i, 1)
const addEducation = () => form.education.push({ school: '', major: '', degree: '本科', time: '', gpa: '' })
const removeEducation = (i: number) => form.education.splice(i, 1)

/** 下载简历 PDF（html2pdf 渲染 A4） */
const downloadPdf = async () => {
  const el = document.getElementById('resume-a4')
  if (!el) return
  downloading.value = true
  try {
    await save(true)
    await html2pdf()
      .set({
        margin: [8, 8, 8, 8],
        filename: `${form.name || '我的'}-简历.pdf`,
        image: { type: 'jpeg', quality: 0.96 },
        html2canvas: { scale: 2, useCORS: true },
        jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' },
        // @ts-ignore
        // @ts-ignore
        pagebreak: { mode: ['avoid-all', 'css', 'legacy'] },
      })
      .from(el)
      .save()
    Message.success('简历 PDF 已下载到本地')
  } catch (e: any) {
    Message.error(e?.message || 'PDF 生成失败')
  } finally {
    downloading.value = false
  }
}

/** AI 修改对话 */
const chatOpen = ref(false)
const chatSending = ref(false)
const chatDraft = ref('')
const chatMessages = ref<{ role: 'user' | 'ai'; content: string }[]>([])
const chatBodyRef = ref<HTMLElement | null>(null)
const aiApplied = ref(false)

const openChat = async () => {
  chatOpen.value = true
  if (!chatMessages.value.length) {
    chatMessages.value.push({
      role: 'ai',
      content: '你好！我是你的 AI 简历顾问。可以告诉我简历哪里不满意，比如：\n- 「项目描述太空洞，帮我改成有量化指标的」\n- 「技能栏想更有针对性，目标是后端开发」\n- 「帮我整体优化一下语言表达」\n\n我会给出修改建议，并可以直接帮你更新简历内容。',
    })
    await scrollChat()
  }
}

const scrollChat = async () => {
  await nextTick()
  if (chatBodyRef.value) chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight
}

const sendChat = async () => {
  const text = chatDraft.value.trim()
  if (!text) return
  if (!auth.isAuthed) return Message.info('请先登录')
  chatMessages.value.push({ role: 'user', content: text })
  chatDraft.value = ''
  await scrollChat()
  chatSending.value = true
  try {
    const rid = await save(true)
    if (!rid) throw new Error('保存失败')
    const history = chatMessages.value.slice(0, -1).map((m) => ({ role: m.role === 'ai' ? 'assistant' : 'user', content: m.content }))
    const res = await resumeApi.chat({ resumeId: rid, message: text, history })
    chatMessages.value.push({ role: 'ai', content: res.reply })
    if (res.applied && res.resumeJson) {
      const parsed = JSON.parse(res.resumeJson)
      Object.assign(form, {
        ...parsed,
        education: parsed.education?.length ? parsed.education : form.education,
        projects: parsed.projects?.length ? parsed.projects : form.projects,
        skills: parsed.skills?.length ? parsed.skills : form.skills,
        awards: parsed.awards || [],
      })
      await save(true)
      aiApplied.value = true
      Message.success('AI 已按建议更新了简历内容，可在右侧预览查看')
    }
    await scrollChat()
  } catch (e: any) {
    chatMessages.value.push({ role: 'ai', content: '抱歉，刚才处理失败了：' + errorMessage(e) + '。请稍后重试。' })
    await scrollChat()
  } finally {
    chatSending.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="container resume-page">
      <div class="row between wrap gap16" style="margin-bottom: 18px">
        <div>
          <div class="kicker">Resume Studio</div>
          <h1 class="h1" style="margin-top: 6px">简历工坊</h1>
          <p class="muted" style="margin-top: 6px">三步生成简历：填写信息 → 选择风格模板 → 在线预览并下载 PDF；AI 顾问随时帮你打磨。</p>
        </div>
        <div class="row gap12">
          <span v-if="aiApplied" class="tag green"><Icon name="sparkles" :size="12" /> AI 已优化</span>
          <span v-if="score" class="tag blue">AI 评分 {{ score }}</span>
          <a-button :loading="saving" @click="save()"><Icon name="file" :size="15" /> 保存</a-button>
        </div>
      </div>

      <!-- 步骤条 -->
      <div class="r-steps card card-pad">
        <div v-for="(s, i) in [{ k: 'info', t: '1. 填写基本信息' }, { k: 'template', t: '2. 选择简历模板' }, { k: 'preview', t: '3. 预览与下载' }]" :key="s.k"
             class="r-step" :class="{ active: step === s.k, done: ['info', 'template', 'preview'].indexOf(step) > i }" @click="step !== 'preview' && (step = s.k as any)">
          {{ s.t }}
        </div>
      </div>

      <!-- Step 1: 基本信息 -->
      <div v-if="step === 'info'" class="card card-pad r-form">
        <div class="grid g2">
          <div><label class="r-label">姓名</label><a-input v-model="form.name" placeholder="真实姓名" /></div>
          <div><label class="r-label">所在城市</label><a-input v-model="form.city" placeholder="如：武汉" /></div>
          <div><label class="r-label">手机号</label><a-input v-model="form.phone" placeholder="招聘方联系方式" /></div>
          <div><label class="r-label">邮箱</label><a-input v-model="form.email" placeholder="name@example.com" /></div>
          <div><label class="r-label">目标院校</label><a-input v-model="targetSchool" placeholder="复试/求职意向院校或公司" /></div>
          <div><label class="r-label">目标专业/岗位</label><a-input v-model="targetMajor" placeholder="如：计算机科学与技术 / 后端开发" /></div>
        </div>

        <div class="divider" />
        <div class="row between"><span class="h3">教育经历</span><a-button size="small" @click="addEducation"><Icon name="plus" :size="14" /> 添加</a-button></div>
        <div v-for="(e, i) in form.education" :key="i" class="r-block">
          <div class="grid g2">
            <div><label class="r-label">学校</label><a-input v-model="e.school" placeholder="学校名称" /></div>
            <div><label class="r-label">专业</label><a-input v-model="e.major" /></div>
            <div><label class="r-label">学历 / 时间</label>
              <div class="row gap8">
                <a-input v-model="e.degree" style="width: 100px" />
                <a-input v-model="e.time" style="flex: 1" placeholder="2022.09 - 2026.06" />
              </div>
            </div>
            <div><label class="r-label">GPA / 排名（选填）</label><a-input v-model="e.gpa" placeholder="3.6 / 前 10%" />
              <a-button v-if="form.education.length > 1" size="mini" status="danger" style="margin-top: 6px" @click="removeEducation(i)">删除</a-button>
            </div>
          </div>
        </div>

        <div class="divider" />
        <div class="row between"><span class="h3">项目 / 实践经历</span><a-button size="small" @click="addProject"><Icon name="plus" :size="14" /> 添加</a-button></div>
        <div v-for="(p, i) in form.projects" :key="i" class="r-block">
          <div class="grid g2">
            <div><label class="r-label">项目名称</label><a-input v-model="p.name" placeholder="如：智研赋能考研平台" /></div>
            <div><label class="r-label">担任角色</label><a-input v-model="p.role" placeholder="独立开发 / 前端负责人" /></div>
            <div><label class="r-label">时间</label><a-input v-model="p.time" placeholder="2025.09 - 至今" /></div>
            <div><label class="r-label">技术栈</label><a-input v-model="p.tech" placeholder="Vue3 · Spring Boot · MySQL" /></div>
          </div>
          <label class="r-label">项目描述（建议含量化指标）</label>
          <a-textarea v-model="p.desc" :auto-size="{ minRows: 3 }" placeholder="背景 → 你做了什么 → 用了什么技术 → 取得什么结果（数字说话）" />
          <a-button v-if="form.projects.length > 1" size="mini" status="danger" style="margin-top: 8px" @click="removeProject(i)">删除此项目</a-button>
        </div>

        <div class="divider" />
        <div class="grid g2">
          <div><label class="r-label">专业技能（逗号分隔）</label><a-textarea v-model="skillsText" :auto-size="{ minRows: 2 }" placeholder="Java，Spring Boot，MySQL，数据结构与算法" /></div>
          <div><label class="r-label">荣誉奖项（每行一条）</label><a-textarea v-model="awardsText" :auto-size="{ minRows: 2 }" placeholder="蓝桥杯省一等奖&#10;校级奖学金" /></div>
        </div>
        <label class="r-label">个人简介</label>
        <a-textarea v-model="form.summary" :auto-size="{ minRows: 3 }" placeholder="2-3 句话：专业背景 + 核心技能 + 求职意向" />
        <div class="row" style="justify-content: flex-end; margin-top: 16px">
          <a-button type="primary" size="large" :loading="saving" @click="nextFromInfo">下一步：选择模板 <Icon name="arrow-right" :size="15" /></a-button>
        </div>
      </div>

      <!-- Step 2: 模板 -->
      <div v-else-if="step === 'template'" class="card card-pad">
        <span class="h3">选择你喜欢的简历风格</span>
        <p class="small muted" style="margin-top: 4px">不同模板突出不同重点，随时可以回来更换。</p>
        <div class="tpl-grid">
          <button v-for="t in templates" :key="t.id" class="tpl-card" :class="{ active: template === t.id }" @click="template = t.id">
            <div class="tpl-mini" :style="{ borderTopColor: t.accent }">
              <div class="tpl-mini-name" :style="{ color: t.accent }">{{ form.name || '姓名' }}</div>
              <div class="tpl-mini-line" /><div class="tpl-mini-line short" />
              <div class="tpl-mini-sec" :style="{ background: t.accent }" />
              <div class="tpl-mini-line" /><div class="tpl-mini-line short" />
              <div class="tpl-mini-sec" :style="{ background: t.accent, width: '46%' }" />
              <div class="tpl-mini-line" /><div class="tpl-mini-line short" />
            </div>
            <div class="h4" style="margin-top: 10px">{{ t.name }}</div>
            <div class="small muted" style="margin-top: 2px">{{ t.desc }}</div>
          </button>
        </div>
        <div class="row" style="justify-content: flex-end; margin-top: 16px">
          <a-button size="large" @click="step = 'info'"><Icon name="arrow-left" :size="15" /> 上一步</a-button>
          <a-button type="primary" size="large" :loading="saving" @click="nextFromTemplate">下一步：预览简历 <Icon name="arrow-right" :size="15" /></a-button>
        </div>
      </div>

      <!-- Step 3: 预览 -->
      <div v-else class="preview-layout">
        <div class="card preview-side">
          <div class="h4" style="margin-bottom: 10px">完成你的简历</div>
          <p class="small muted">PDF 为 A4 排版，可直接打印或作为复试/求职附件投递。</p>
          <div class="side-actions">
            <a-button long type="primary" :loading="downloading" @click="downloadPdf"><Icon name="download" :size="15" /> 下载简历 PDF</a-button>
            <a-button long @click="openChat"><Icon name="sparkles" :size="15" /> 向 AI 提出修改建议</a-button>
            <a-button long @click="step = 'template'">更换模板风格</a-button>
            <a-button long @click="step = 'info'">继续编辑内容</a-button>
          </div>
          <div class="divider" />
          <div class="small muted" style="line-height: 1.9">
            <b>AI 顾问能做什么：</b><br />
            · 按你的修改意见直接改写简历内容<br />
            · 补充量化指标与岗位关键词<br />
            · 提供复试 / 求职进一步指导
          </div>
        </div>

        <!-- A4 纸预览 -->
        <div class="preview-stage">
          <div id="resume-a4" class="resume-a4" :class="'tpl-' + template">
            <div class="ra-head">
              <div class="ra-name">{{ form.name || '姓名' }}</div>
              <div class="ra-contact">
                {{ [form.phone, form.email, form.city].filter(Boolean).join(' · ') }}
                <template v-if="targetSchool || targetMajor"> · 意向：{{ [targetSchool, targetMajor].filter(Boolean).join(' ') }}</template>
              </div>
            </div>
            <div v-if="form.summary" class="ra-sec">个人简介</div>
            <p v-if="form.summary" class="ra-text">{{ form.summary }}</p>
            <div class="ra-sec">教育经历</div>
            <div v-for="e in form.education" :key="e.school + e.time" class="ra-item">
              <div class="ra-row"><b>{{ e.school }}</b><span>{{ e.time }}</span></div>
              <div class="ra-sub">{{ e.major }} · {{ e.degree }}{{ e.gpa ? ' · GPA ' + e.gpa : '' }}</div>
            </div>
            <div class="ra-sec">项目与实践经历</div>
            <div v-for="p in form.projects" :key="p.name + p.time" class="ra-item">
              <div class="ra-row"><b>{{ p.name }}</b><span>{{ p.time }}</span></div>
              <div class="ra-sub">{{ p.role }}{{ p.tech ? ' · ' + p.tech : '' }}</div>
              <p class="ra-text">{{ p.desc }}</p>
            </div>
            <div class="ra-sec">专业技能</div>
            <div class="ra-tags"><span v-for="s in form.skills" :key="s" class="ra-tag">{{ s }}</span></div>
            <div v-if="form.awards.length" class="ra-sec">荣誉奖项</div>
            <ul v-if="form.awards.length" class="ra-list"><li v-for="a in form.awards" :key="a">{{ a }}</li></ul>
          </div>
        </div>
      </div>
    </div>

    <!-- AI 修改对话 -->
    <a-modal v-model:visible="chatOpen" title="AI 简历顾问" :footer="false" :width="620" unmount-on-close>
      <div class="chat-wrap">
        <div ref="chatBodyRef" class="chat-list">
          <div v-for="(m, i) in chatMessages" :key="i" class="ai-msg" :class="m.role">
            <MarkdownView v-if="m.role === 'ai'" :content="m.content" />
            <div v-else class="ai-user-bubble">{{ m.content }}</div>
          </div>
          <div v-if="chatSending" class="small muted" style="padding: 8px">AI 顾问正在阅读你的简历…</div>
        </div>
        <div class="ai-input">
          <a-textarea v-model="chatDraft" placeholder="描述你对简历的修改意见，例如「项目一缺少量化数据，帮我改写」" :auto-size="{ minRows: 2, maxRows: 4 }" @keydown.enter.exact.prevent="sendChat" />
          <a-button type="primary" :loading="chatSending" style="margin-top: 8px" @click="sendChat"><Icon name="send" :size="14" /> 发送给 AI</a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<style scoped>
.resume-page { max-width: 1200px; }
.r-steps { display: flex; gap: 8px; margin-bottom: 16px; }
.r-step {
  flex: 1; text-align: center; padding: 10px; border-radius: 10px; font-size: 14px;
  color: var(--muted-2, #999); border: 1.5px solid transparent; cursor: pointer;
}
.r-step.active { color: var(--brand, #3d6b3d); border-color: var(--brand, #3d6b3d); background: rgba(90, 143, 90, 0.07); font-weight: 700; }
.r-step.done { color: var(--brand, #3d6b3d); }
.r-label { display: block; font-size: 12.5px; color: var(--muted); margin: 12px 0 5px; }
.r-block { border: 1px dashed rgba(139, 115, 85, 0.25); border-radius: 12px; padding: 4px 14px 14px; margin-top: 10px; }
.tpl-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-top: 14px; }
.tpl-card {
  border: 2px solid var(--line, #e5e0d4); border-radius: 14px; padding: 14px; cursor: pointer;
  background: #fff; text-align: center; transition: 0.15s;
}
.tpl-card:hover { transform: translateY(-2px); }
.tpl-card.active { border-color: var(--brand, #3d6b3d); box-shadow: 0 8px 20px rgba(61, 107, 61, 0.14); }
.tpl-mini { border: 1px solid #eee; border-top: 4px solid; border-radius: 8px; padding: 10px; background: #fcfcf9; }
.tpl-mini-name { font-size: 13px; font-weight: 800; }
.tpl-mini-line { height: 5px; background: #e5e5e0; border-radius: 3px; margin-top: 6px; }
.tpl-mini-line.short { width: 60%; }
.tpl-mini-sec { height: 6px; width: 30%; border-radius: 3px; margin-top: 12px; }
.preview-layout { display: grid; grid-template-columns: 300px minmax(0, 1fr); gap: 18px; align-items: start; }
.preview-side { position: sticky; top: 88px; }
.side-actions { display: grid; gap: 8px; margin-top: 12px; }
.preview-stage {
  background: repeating-conic-gradient(#f7f4ec 0% 25%, #fdfcf7 0% 50%) 50% / 24px 24px;
  border-radius: 16px; padding: 26px; display: flex; justify-content: center;
}
.resume-a4 {
  width: 794px; min-height: 1050px; background: #fff; box-shadow: 0 10px 40px rgba(60, 50, 30, 0.18);
  padding: 46px 52px; color: #1f2937; font-size: 13.5px; line-height: 1.75;
  border-top: 6px solid #3d6b3d;
}
.resume-a4.tpl-modern { border-top-color: #2f6f8f; }
.resume-a4.tpl-tech { border-top-color: #1f2937; }
.ra-head { border-bottom: 2px solid #3d6b3d; padding-bottom: 14px; }
.tpl-modern .ra-head { border-color: #2f6f8f; }
.tpl-tech .ra-head { border-color: #1f2937; }
.ra-name { font-size: 26px; font-weight: 800; letter-spacing: 1px; color: #3d6b3d; }
.tpl-modern .ra-name { color: #2f6f8f; }
.tpl-tech .ra-name { color: #1f2937; }
.ra-contact { font-size: 12.5px; color: #6b7280; margin-top: 6px; }
.ra-sec {
  font-size: 14.5px; font-weight: 800; color: #3d6b3d; letter-spacing: 2px;
  margin: 18px 0 8px; padding-bottom: 4px; border-bottom: 1.5px solid rgba(61, 107, 61, 0.25);
}
.tpl-modern .ra-sec { color: #2f6f8f; border-color: rgba(47, 111, 143, 0.25); }
.tpl-tech .ra-sec { color: #1f2937; border-color: rgba(31, 41, 55, 0.25); }
.ra-item { margin-bottom: 10px; }
.ra-row { display: flex; justify-content: space-between; font-size: 13.5px; }
.ra-sub { color: #6b7280; font-size: 12.5px; margin-top: 2px; }
.ra-text { margin: 4px 0 0; white-space: pre-wrap; color: #374151; }
.ra-tags { display: flex; flex-wrap: wrap; gap: 6px; }
.ra-tag { background: #f4eee2; border-radius: 5px; padding: 3px 10px; font-size: 12.5px; color: #4b4030; }
.ra-list { margin: 0; padding-left: 20px; color: #374151; }
.chat-wrap { display: flex; flex-direction: column; height: 60vh; }
.chat-list { flex: 1; overflow-y: auto; padding: 4px; }
.ai-msg { margin-bottom: 12px; }
.ai-msg.ai :deep(.markdown-body) { background: #f7f5ee; border-radius: 12px; padding: 10px 14px; }
.ai-user-bubble { background: #3d6b3d; color: #fff; border-radius: 12px; padding: 9px 14px; display: inline-block; max-width: 90%; }
.ai-input { border-top: 1px solid #eee; padding-top: 10px; margin-top: 8px; }
@media (max-width: 1024px) {
  .tpl-grid, .preview-layout { grid-template-columns: 1fr; }
  .resume-a4 { transform: scale(0.8); transform-origin: top center; }
}
</style>
