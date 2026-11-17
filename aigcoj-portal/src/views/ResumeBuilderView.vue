<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import { resumeApi } from '../api/resume'
import { errorMessage } from '../api/http'
import { useAuthStore } from '../stores/auth'
import type { ResumeContent } from '../types'
import MarkdownView from '../components/MarkdownView.vue'
import Icon from '../components/Icon.vue'

const auth = useAuthStore()

const id = ref<number | null>(null)
const title = ref('我的简历')
const targetSchool = ref('')
const targetMajor = ref('计算机科学与技术')
const template = ref('classic')
const score = ref(0)
const saving = ref(false)
const analyzing = ref(false)
const analyzeOpen = ref(false)
const analysis = ref('')
const loading = ref(true)

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
  { id: 'classic', name: '经典学术', desc: '复试 / 科研' },
  { id: 'modern', name: '现代简约', desc: '互联网求职' },
  { id: 'tech', name: '技术极简', desc: '后端 / 算法' },
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

const analyze = async () => {
  const rid = await save(true)
  if (!rid) return
  analyzeOpen.value = true
  analyzing.value = true
  analysis.value = ''
  try {
    const res = await resumeApi.analyze(rid)
    score.value = res.score
    analysis.value = res.result
  } catch (e: any) {
    analysis.value = errorMessage(e)
  } finally {
    analyzing.value = false
  }
}

const addProject = () => form.projects.push({ name: '', role: '', time: '', desc: '', tech: '' })
const removeProject = (i: number) => form.projects.splice(i, 1)
const addEducation = () => form.education.push({ school: '', major: '', degree: '本科', time: '', gpa: '' })
const removeEducation = (i: number) => form.education.splice(i, 1)

const escapeHtml = (s: string) => s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')

const printResume = () => {
  const accent = template.value === 'modern' ? 'var(--meadow-deep)' : template.value === 'tech' ? 'var(--ink)' : 'var(--meadow-deep)'
  const edu = form.education.map((e) => `<div class="item"><div class="row"><b>${escapeHtml(e.school)}</b><span>${escapeHtml(e.time)}</span></div><div class="muted">${escapeHtml(e.major)} · ${escapeHtml(e.degree)} ${e.gpa ? '· GPA ' + escapeHtml(e.gpa) : ''}</div></div>`).join('')
  const projects = form.projects.map((p) => `<div class="item"><div class="row"><b>${escapeHtml(p.name)}</b><span>${escapeHtml(p.time)}</span></div><div class="muted">${escapeHtml(p.role)} ${p.tech ? '· ' + escapeHtml(p.tech) : ''}</div><p>${escapeHtml(p.desc)}</p></div>`).join('')
  const html = `<!doctype html><html><head><meta charset="utf-8"><title>${escapeHtml(form.name)}的简历</title>
  <style>body{font-family:"Microsoft YaHei",sans-serif;color:#1f2937;max-width:780px;margin:36px auto;line-height:1.7;padding:0 24px}
  h1{font-size:28px;margin:0} .contact{color:var(--muted);font-size:13px;margin-top:6px}
  .sec{font-size:14px;font-weight:700;color:${accent};border-bottom:2px solid ${accent}33;padding-bottom:4px;margin:22px 0 10px;letter-spacing:1px}
  .item{margin-bottom:12px}.row{display:flex;justify-content:space-between}.muted{color:var(--muted);font-size:13px}
  p{margin:6px 0;font-size:13px;white-space:pre-wrap}.tag{display:inline-block;background:#f4eee2;border-radius:4px;padding:2px 8px;margin:3px 6px 3px 0;font-size:12px}
  @media print{body{margin:0}}</style></head><body>
  <h1>${escapeHtml(form.name)}</h1>
  <div class="contact">${escapeHtml(form.phone)} ${form.email ? ' · ' + escapeHtml(form.email) : ''} ${form.city ? ' · ' + escapeHtml(form.city) : ''} · 意向：${escapeHtml(targetSchool.value || '')} ${escapeHtml(targetMajor.value || '')}</div>
  ${form.summary ? `<div class="sec">个人简介</div><p>${escapeHtml(form.summary)}</p>` : ''}
  <div class="sec">教育经历</div>${edu || '<p class="muted">待补充</p>'}
  <div class="sec">项目经历</div>${projects || '<p class="muted">待补充</p>'}
  <div class="sec">专业技能</div><div>${form.skills.map((s) => `<span class="tag">${escapeHtml(s)}</span>`).join('')}</div>
  ${form.awards.length ? `<div class="sec">获奖与证书</div><ul>${form.awards.map((a) => `<li>${escapeHtml(a)}</li>`).join('')}</ul>` : ''}
  </body></html>`
  const win = window.open('', '_blank')
  if (!win) return Message.warning('请允许浏览器弹出窗口')
  win.document.write(html)
  win.document.close()
  setTimeout(() => win.print(), 400)
}

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="container">
      <div class="row between wrap gap12" style="margin-bottom: 18px">
        <div>
          <div class="kicker">Resume Studio</div>
          <h1 class="h1" style="margin-top: 6px">简历工坊</h1>
          <p class="muted" style="margin-top: 6px">结构化编辑 + 实时预览 + AI 逐条诊断，导出可直接打印的 PDF。</p>
        </div>
        <div class="row gap8">
          <a-button :loading="saving" @click="save()"><Icon name="check" :size="15" /> 保存</a-button>
          <a-button type="primary" :loading="analyzing" @click="analyze"><Icon name="sparkles" :size="15" /> AI 诊断</a-button>
          <a-button @click="printResume"><Icon name="download" :size="15" /> 导出 PDF</a-button>
        </div>
      </div>

      <div class="score-strip card card-pad" style="margin-bottom: 16px">
        <div class="row gap16 wrap">
          <div class="score-ring">
            <div class="score-num">{{ score || '--' }}</div>
          </div>
          <div style="flex: 1; min-width: 220px">
            <div class="h3">简历竞争力评分</div>
            <div class="small muted" style="margin-top: 4px">
              {{ score ? 'AI 根据内容完整度、项目量化、技能匹配与表达给出建议' : '完成简历后点击「AI 诊断」，获取逐条修改建议' }}
            </div>
            <div class="progress-track" style="margin-top: 10px"><div class="progress-bar" :style="{ width: `${score}%` }" /></div>
          </div>
          <div class="row gap8">
            <span v-for="t in templates" :key="t.id" class="chip" :class="{ active: template === t.id }" @click="template = t.id">
              {{ t.name }}
            </span>
          </div>
        </div>
      </div>

      <div v-if="loading" class="card card-pad"><div class="skeleton" style="height: 400px" /></div>

      <div v-else class="resume-grid">
        <div style="display: grid; gap: 14px">
          <div class="card card-pad">
            <div class="h4" style="margin-bottom: 10px">基本信息</div>
            <div class="grid g2">
              <div><div class="small muted">姓名</div><a-input v-model="form.name" /></div>
              <div><div class="small muted">电话</div><a-input v-model="form.phone" placeholder="138xxxxxxxx" /></div>
              <div><div class="small muted">邮箱</div><a-input v-model="form.email" placeholder="you@example.com" /></div>
              <div><div class="small muted">城市</div><a-input v-model="form.city" placeholder="杭州" /></div>
              <div><div class="small muted">目标院校</div><a-input v-model="targetSchool" placeholder="例如：浙江大学" /></div>
              <div><div class="small muted">目标专业</div><a-input v-model="targetMajor" /></div>
            </div>
            <div style="margin-top: 10px">
              <div class="small muted">个人简介</div>
              <a-textarea v-model="form.summary" :auto-size="{ minRows: 3 }" placeholder="一句话说明你的方向、优势与目标" />
            </div>
          </div>

          <div class="card card-pad">
            <div class="row between" style="margin-bottom: 10px">
              <div class="h4">教育经历</div>
              <a-button size="mini" @click="addEducation"><Icon name="plus" :size="12" /> 添加</a-button>
            </div>
            <div v-for="(e, i) in form.education" :key="i" style="padding: 12px; border: 1px solid var(--line); border-radius: 10px; margin-bottom: 10px">
              <div class="grid g2">
                <a-input v-model="e.school" placeholder="学校" />
                <a-input v-model="e.major" placeholder="专业" />
                <a-input v-model="e.degree" placeholder="学历" />
                <a-input v-model="e.time" placeholder="2022.09 - 2026.06" />
              </div>
              <div class="row gap8" style="margin-top: 8px">
                <a-input v-model="e.gpa" placeholder="GPA / 排名（可选）" />
                <a-button size="small" status="danger" @click="removeEducation(i)"><Icon name="trash" :size="13" /></a-button>
              </div>
            </div>
          </div>

          <div class="card card-pad">
            <div class="row between" style="margin-bottom: 10px">
              <div class="h4">项目经历</div>
              <a-button size="mini" @click="addProject"><Icon name="plus" :size="12" /> 添加项目</a-button>
            </div>
            <div v-for="(p, i) in form.projects" :key="i" style="padding: 12px; border: 1px solid var(--line); border-radius: 10px; margin-bottom: 10px">
              <div class="grid g2">
                <a-input v-model="p.name" placeholder="项目名称" />
                <a-input v-model="p.role" placeholder="你的角色" />
                <a-input v-model="p.time" placeholder="2025.03 - 2025.06" />
                <a-input v-model="p.tech" placeholder="技术栈：Spring Boot / Vue" />
              </div>
              <a-textarea v-model="p.desc" :auto-size="{ minRows: 3 }" placeholder="背景 + 你做了什么 + 量化结果（STAR）" style="margin-top: 8px" />
              <div class="row between" style="margin-top: 8px">
                <span class="small muted">建议写出 1-2 个数字结果</span>
                <a-button size="small" status="danger" @click="removeProject(i)"><Icon name="trash" :size="13" /></a-button>
              </div>
            </div>
          </div>

          <div class="card card-pad">
            <div class="h4" style="margin-bottom: 10px">技能与获奖</div>
            <div class="small muted">技能（逗号分隔）</div>
            <a-input v-model="skillsText" placeholder="Java，Spring Boot，MySQL" />
            <div class="small muted" style="margin-top: 10px">获奖与证书（每行一条）</div>
            <a-textarea v-model="awardsText" :auto-size="{ minRows: 3 }" placeholder="校级一等奖学金&#10;CET-6 560" />
          </div>
        </div>

        <div style="position: sticky; top: 88px">
          <div class="resume-paper" :class="`tpl-${template}`">
            <div class="row between">
              <div>
                <div class="resume-name">{{ form.name || '你的姓名' }}</div>
                <div class="small muted" style="margin-top: 4px">
                  {{ form.phone || '电话' }} · {{ form.email || '邮箱' }} · {{ form.city || '城市' }}
                </div>
              </div>
              <div style="text-align: right">
                <div class="small" style="color: var(--brand-deep)">意向</div>
                <div class="h4">{{ targetSchool || '目标院校' }}</div>
                <div class="small muted">{{ targetMajor }}</div>
              </div>
            </div>

            <div v-if="form.summary" class="resume-sec-title">个人简介</div>
            <div v-if="form.summary" class="small" style="line-height: 1.8; color: var(--ink-2)">{{ form.summary }}</div>

            <div class="resume-sec-title">教育经历</div>
            <div v-for="(e, i) in form.education" :key="i" style="margin-bottom: 8px">
              <div class="resume-line"><b>{{ e.school || '学校名称' }}</b><span>{{ e.time }}</span></div>
              <div class="small muted">{{ e.major }} · {{ e.degree }} {{ e.gpa ? `· GPA ${e.gpa}` : '' }}</div>
            </div>

            <div class="resume-sec-title">项目经历</div>
            <div v-for="(p, i) in form.projects" :key="i" style="margin-bottom: 12px">
              <div class="resume-line"><b>{{ p.name || '项目名称' }}</b><span>{{ p.time }}</span></div>
              <div class="small muted">{{ p.role }} {{ p.tech ? `· ${p.tech}` : '' }}</div>
              <div class="small" style="margin-top: 4px; color: var(--ink-2); white-space: pre-wrap; line-height: 1.7">{{ p.desc || '项目描述与量化结果' }}</div>
            </div>

            <div class="resume-sec-title">专业技能</div>
            <div class="row wrap gap8">
              <span v-for="s in form.skills" :key="s" class="tag">{{ s }}</span>
            </div>

            <template v-if="form.awards.length">
              <div class="resume-sec-title">获奖与证书</div>
              <ul style="padding-left: 18px; font-size: 13px; color: var(--ink-2)">
                <li v-for="a in form.awards" :key="a">{{ a }}</li>
              </ul>
            </template>
          </div>
          <div class="small muted" style="margin-top: 8px; text-align: center">预览会实时更新，导出后可另存为 PDF</div>
        </div>
      </div>
    </div>

    <a-modal v-model:visible="analyzeOpen" title="AI 简历诊断" :footer="false" :width="720">
      <div v-if="analyzing" style="padding: 40px; text-align: center" class="muted">正在逐条审阅你的简历…</div>
      <div v-else style="max-height: 62vh; overflow-y: auto">
        <div class="row gap12" style="margin-bottom: 14px; padding: 12px 14px; background: #fdfaf4; border-radius: 10px">
          <div class="score-ring" style="width: 56px; height: 56px"><div class="score-num" style="font-size: 20px">{{ score }}</div></div>
          <div class="small muted" style="align-self: center">根据内容完整度、项目量化、技能匹配与表达综合评估</div>
        </div>
        <MarkdownView :content="analysis" />
      </div>
    </a-modal>
  </div>
</template>

<style scoped>
.score-strip { background: linear-gradient(135deg, #fdfaf4, #ffffff); }
.score-ring { width: 68px; height: 68px; border-radius: 50%; background: conic-gradient(var(--meadow) var(--p, 0%), rgba(139, 115, 85, 0.18) 0); display: grid; place-items: center; position: relative; }
.score-ring::before { content: ''; position: absolute; inset: 6px; background: var(--cloth-2); border-radius: 50%; }
.score-num { position: relative; font-size: 22px; font-weight: 600; color: var(--brand-deep); }
.resume-paper.tpl-modern { border-top: 4px solid var(--meadow-deep); }
.resume-paper.tpl-tech { border-top: 4px solid var(--ink); }
.resume-paper.tpl-modern .resume-sec-title { color: var(--meadow-dark); border-color: #99f6e4; }
.resume-paper.tpl-tech .resume-sec-title { color: var(--ink); border-color: rgba(139, 115, 85, 0.30); }
@media (max-width: 1024px) { .resume-grid { grid-template-columns: 1fr; } div[style*="sticky"] { position: static !important; } }
</style>
