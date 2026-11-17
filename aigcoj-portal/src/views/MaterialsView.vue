<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import { materialApi } from '../api/material'
import { studyApi } from '../api/study'
import { errorMessage } from '../api/http'
import { useAuthStore } from '../stores/auth'
import { formatSize, toTags } from '../utils/format'
import Icon from '../components/Icon.vue'
import EmptyState from '../components/EmptyState.vue'

const auth = useAuthStore()

const loading = ref(true)
const rows = ref<any[]>([])
const total = ref(0)
const current = ref(1)
const pageSize = ref(12)
const category = ref('')
const subject = ref('')
const keyword = ref('')
const sort = ref('new')
const categories = ref<{ name: string; count: number }[]>([])
const favourIds = ref<(number | string)[]>([])
const stats = ref<any>({})

const cats = [
  { id: '', label: '全部资料' },
  { id: '408 真题', label: '408 历年真题' },
  { id: '408 模拟题', label: '408 模拟题' },
  { id: '408 习题', label: '408 经典练习题' },
  { id: '408 大纲', label: '408 考研大纲' },
  { id: '408', label: '408 专业课' },
  { id: '方法论', label: '复习方法论' },
  { id: '数学', label: '考研数学' },
  { id: '英语', label: '考研英语' },
  { id: '政治', label: '考研政治' },
  { id: '复试', label: '复试面试' },
  { id: '经验', label: '经验分享' },
]
const subjects = [
  { id: '', label: '全部科目' },
  { id: 'ds', label: '数据结构' },
  { id: 'co', label: '计组' },
  { id: 'os', label: '操作系统' },
  { id: 'cn', label: '计网' },
]

const previewOpen = ref(false)
const active = ref<any>(null)
const uploading = ref(false)
const uploadOpen = ref(false)
const uploadFile = ref<{ url: string; name: string; size: number; type: string } | null>(null)
const saving = ref(false)
const form = ref({ title: '', description: '', category: '408', subject: 'ds', tags: '' })

/** 资源价值引擎：用户薄弱标签（来自个人画像） */
const weakTags = ref<string[]>([])
const smartMode = computed(() => sort.value === 'smart')

/** 适配度 = 薄弱点命中 + 质量口碑（下载/收藏），0-99 */
const scoreMaterial = (m: any) => {
  const hay = `${m.title || ''} ${m.description || ''} ${toTags(m.tags).join(' ')}`
  const hits = weakTags.value.filter((t) => t && hay.includes(t))
  const popularity = Math.min(m.downloadNum || 0, 60) * 0.3 + Math.min(m.favourNum || 0, 30) * 0.5
  return { score: Math.round(Math.min(99, 30 + hits.length * 22 + popularity)), hits }
}

const load = async () => {
  loading.value = true
  try {
    if (smartMode.value) {
      // 智能推荐：拉一批候选后在本地按画像适配度排序
      const page = await materialApi.list({
        current: 1,
        pageSize: 60,
        category: category.value || undefined,
        subject: subject.value || undefined,
        keyword: keyword.value || undefined,
        sort: 'hot',
      })
      const scored = (page.records || []).map((m: any) => ({ ...m, _smart: scoreMaterial(m) }))
      scored.sort((a: any, b: any) => b._smart.score - a._smart.score)
      total.value = scored.length
      rows.value = scored.slice((current.value - 1) * pageSize.value, current.value * pageSize.value)
    } else {
      const page = await materialApi.list({
        current: current.value,
        pageSize: pageSize.value,
        category: category.value || undefined,
        subject: subject.value || undefined,
        keyword: keyword.value || undefined,
        sort: sort.value,
      })
      rows.value = page.records || []
      total.value = Number(page.total || 0)
    }
  } catch (e: any) {
    Message.error(errorMessage(e))
  } finally {
    loading.value = false
  }
}

const selectSmart = () => {
  if (!auth.isAuthed) {
    Message.info('登录后，资源引擎会读取你的画像薄弱点做智能排序')
    return
  }
  sort.value = 'smart'
  current.value = 1
  load()
}

const loadMeta = async () => {
  try {
    categories.value = await materialApi.categories()
    stats.value = await materialApi.list({ current: 1, pageSize: 1 }).then((p) => ({ total: p.total }))
    if (auth.isAuthed) {
      favourIds.value = await materialApi.favourIds()
      const p = await studyApi.portrait().catch(() => null)
      weakTags.value = p?.weakTags || []
    }
  } catch {}
}

const selectCat = (id: string) => {
  category.value = id
  subject.value = ''
  current.value = 1
  load()
}

const selectSubject = (id: string) => {
  subject.value = subject.value === id ? '' : id
  current.value = 1
  load()
}

const openDetail = async (m: any) => {
  active.value = m
  previewOpen.value = true
  try {
    const res = await materialApi.get(m.id)
    active.value = res.material
  } catch {}
}

const toggleFavour = async (m: any, e: Event) => {
  e.stopPropagation()
  if (!auth.isAuthed) return Message.info('登录后可收藏资料')
  try {
    const res = await materialApi.favour(m.id)
    favourIds.value = res.favour ? [...favourIds.value, m.id] : favourIds.value.filter((x) => String(x) !== String(m.id))
    m.favourNum = Math.max(0, (m.favourNum || 0) + (res.favour ? 1 : -1))
    Message.success(res.favour ? '已收藏' : '已取消收藏')
  } catch (e: any) {
    Message.error(errorMessage(e))
  }
}

const download = async (m: any) => {
  try {
    // 在线资源：直接跳转外部学习页面
    if (m.fileType === 'link' && m.fileUrl) {
      await materialApi.download(m.id).catch(() => {})
      window.open(m.fileUrl, '_blank')
      return
    }
    const res = await materialApi.download(m.id)
    m.downloadNum = (m.downloadNum || 0) + 1
    if (res.url) window.open(res.url, '_blank')
    else Message.info('该资料暂未上传文件')
  } catch (e: any) {
    Message.error(errorMessage(e))
  }
}

const onFile = async (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  uploading.value = true
  try {
    uploadFile.value = await materialApi.upload(file)
    form.value.title = form.value.title || file.name.replace(/\.[^.]+$/, '')
    Message.success('文件上传成功')
  } catch (e: any) {
    Message.error(errorMessage(e))
  } finally {
    uploading.value = false
  }
}

const saveMaterial = async () => {
  if (!form.value.title.trim()) return Message.warning('请填写资料标题')
  saving.value = true
  try {
    await materialApi.save({
      title: form.value.title,
      description: form.value.description,
      category: form.value.category,
      subject: form.value.category === '408' ? form.value.subject : undefined,
      fileUrl: uploadFile.value?.url,
      fileSize: uploadFile.value?.size,
      fileType: uploadFile.value?.type || 'pdf',
      tags: form.value.tags ? JSON.stringify(form.value.tags.split(/[,，]/).map((t) => t.trim()).filter(Boolean)) : '[]',
    })
    Message.success('资料已发布')
    uploadOpen.value = false
    uploadFile.value = null
    form.value = { title: '', description: '', category: '408', subject: 'ds', tags: '' }
    await load()
  } catch (e: any) {
    Message.error(errorMessage(e))
  } finally {
    saving.value = false
  }
}

const catCount = (id: string) => categories.value.find((c) => c.name === id)?.count || 0

onMounted(() => { load(); loadMeta() })
</script>

<template>
  <div class="page">
    <div class="container">
      <div class="row between wrap gap16" style="margin-bottom: 20px">
        <div>
          <div class="kicker">Study Library</div>
          <h1 class="h1" style="margin-top: 6px">考研资料库</h1>
          <p class="muted" style="margin-top: 6px">408 笔记、公共课资料、复试真题，资源价值引擎按你的画像排出优先级。</p>
        </div>
        <div class="row gap12">
          <a-button v-if="auth.isAdmin" type="primary" @click="uploadOpen = true"><Icon name="plus" :size="15" /> 上传资料</a-button>
          <a-button @click="auth.isAuthed ? load() : null"><Icon name="star" :size="15" /> 我的收藏</a-button>
        </div>
      </div>

      <div class="grid" style="grid-template-columns: 220px minmax(0, 1fr); gap: 20px; align-items: start">
        <aside class="card card-pad" style="position: sticky; top: 88px">
          <div class="h4" style="margin-bottom: 10px">资料分类</div>
          <div class="cat-list">
            <div v-for="c in cats" :key="c.id" class="cat-item" :class="{ active: category === c.id }" @click="selectCat(c.id)">
              <span>{{ c.label }}</span>
              <span class="small muted">{{ c.id ? catCount(c.id) : '' }}</span>
            </div>
          </div>
          <div class="divider" />
          <div class="h4" style="margin-bottom: 10px">408 科目</div>
          <div class="row wrap gap8">
            <span v-for="s in subjects.slice(1)" :key="s.id" class="tag" :class="subject === s.id ? 'blue' : ''" style="cursor: pointer" @click="selectSubject(s.id)">{{ s.label }}</span>
          </div>
          <div class="divider" />
          <div class="card" style="padding: 12px; background: #fdfaf4; border: none">
            <div class="small muted">资料总量</div>
            <div class="h2" style="margin: 2px 0">{{ total }}</div>
            <div class="small muted">持续更新中</div>
          </div>
        </aside>

        <div>
          <div class="card card-pad" style="margin-bottom: 14px">
            <div class="row between wrap gap12">
              <div class="row wrap gap8">
                <span class="chip" :class="{ active: sort === 'smart' }" @click="selectSmart">
                  <Icon name="zap" :size="13" style="vertical-align: -2px" /> 智能推荐
                </span>
                <span class="chip" :class="{ active: sort === 'new' }" @click="sort = 'new'; current = 1; load()">最新</span>
                <span class="chip" :class="{ active: sort === 'hot' }" @click="sort = 'hot'; current = 1; load()">最热</span>
                <span class="chip" :class="{ active: sort === 'favour' }" @click="sort = 'favour'; current = 1; load()">收藏最多</span>
              </div>
              <div class="row gap8">
                <a-input v-model="keyword" placeholder="搜索资料标题 / 标签" allow-clear style="width: 240px" @press-enter="current = 1; load()" />
                <a-button type="primary" @click="current = 1; load()"><Icon name="search" :size="15" /></a-button>
              </div>
            </div>
          </div>

          <div v-if="loading" class="grid g3">
            <div v-for="i in 6" :key="i" class="card card-pad"><div class="skeleton" style="height: 90px" /></div>
          </div>
          <div v-else-if="rows.length" class="grid g3">
            <div v-for="m in rows" :key="m.id" class="card card-hover card-pad" style="cursor: pointer" @click="openDetail(m)">
              <div class="row between">
                <div class="file-badge pdf" :style="m.fileType === 'link' ? 'background:rgba(184,208,175,.35);color:#4e7a3f' : ''">{{ m.fileType === 'link' ? '在线' : (m.fileType || 'pdf').toUpperCase() }}</div>
                <button class="icon-btn" style="width: 30px; height: 30px" @click="toggleFavour(m, $event)">
                  <Icon name="star" :size="14" :style="{ color: favourIds.map(String).includes(String(m.id)) ? 'var(--wheat-deep)' : 'var(--muted-2)' }" />
                </button>
              </div>
              <div class="h4" style="margin-top: 12px; line-height: 1.5; min-height: 42px">{{ m.title }}</div>
              <div class="small muted" style="margin-top: 4px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; min-height: 38px">{{ m.description }}</div>
              <div class="row wrap gap8" style="margin-top: 8px">
                <span class="tag blue">{{ m.category }}</span>
                <span v-for="t in toTags(m.tags).slice(0, 1)" :key="t" class="tag">{{ t }}</span>
              </div>
              <div v-if="m._smart" class="fit-row" :title="m._smart.hits.length ? '命中你的薄弱点：' + m._smart.hits.join('、') : '按质量与口碑推荐'">
                <div class="fit-bar"><i :style="{ width: m._smart.score + '%' }" /></div>
                <span class="fit-num">适配 {{ m._smart.score }}%</span>
              </div>
              <div class="row between small muted" style="margin-top: 12px">
                <span>{{ m.downloadNum || 0 }} 次下载 · {{ formatSize(m.fileSize) }}</span>
                <span class="row gap8"><Icon name="download" :size="14" /> 预览</span>
              </div>
            </div>
          </div>
          <EmptyState v-else title="没有找到资料" desc="换个分类或关键词试试。" icon="file" />

          <div v-if="total > pageSize" class="row between" style="margin-top: 16px">
            <span class="small muted">共 {{ total }} 份资料</span>
            <a-pagination v-model:current="current" :page-size="pageSize" :total="total" size="small" @change="load" />
          </div>
        </div>
      </div>
    </div>

    <a-modal v-model:visible="previewOpen" :width="900" :footer="false" title="资料预览">
      <div v-if="active">
        <div class="row between wrap gap12" style="margin-bottom: 12px">
          <div>
            <div class="h3">{{ active.title }}</div>
            <div class="small muted" style="margin-top: 4px">{{ active.category }} · {{ formatSize(active.fileSize) }} · {{ active.downloadNum || 0 }} 次下载</div>
          </div>
          <div class="row gap8">
            <a-button size="small" @click="toggleFavour(active, $event)">
              <Icon name="star" :size="14" /> {{ favourIds.map(String).includes(String(active.id)) ? '已收藏' : '收藏' }}
            </a-button>
            <a-button type="primary" size="small" @click="download(active)">
              <Icon :name="active.fileType === 'link' ? 'link' : 'download'" :size="14" /> {{ active.fileType === 'link' ? '前往学习' : '下载' }}
            </a-button>
          </div>
        </div>
        <div class="small muted" style="margin-bottom: 10px">{{ active.description }}</div>
        <iframe v-if="active.fileUrl && active.fileType !== 'link'" class="pdf-frame" :src="active.fileUrl" title="PDF 预览" />
        <div v-else-if="active.fileType === 'link'" class="link-card" @click="download(active)">
          <Icon name="link" :size="18" />
          <div>
            <div class="h4">在线资源 · 点击前往学习</div>
            <div class="small muted" style="margin-top: 2px">{{ active.fileUrl }}</div>
          </div>
        </div>
        <div v-else class="empty small">该资料暂未上传文件，可先收藏，上传后第一时间查看。</div>
      </div>
    </a-modal>

    <a-modal v-model:visible="uploadOpen" title="上传考研资料" :footer="false" :width="520">
      <div style="padding-top: 10px">
        <div class="small muted" style="margin-bottom: 6px">资料文件（PDF / Word / 图片 / ZIP）</div>
        <label class="upload-box">
          <input type="file" accept=".pdf,.doc,.docx,.ppt,.pptx,.png,.jpg,.zip" style="display: none" @change="onFile" />
          <Icon name="file" :size="22" />
          <div style="margin-top: 6px">{{ uploadFile ? uploadFile.name : uploading ? '上传中…' : '点击选择文件' }}</div>
          <div class="small muted">单个文件不超过 30MB</div>
        </label>
        <div class="grid g2" style="margin-top: 14px">
          <div>
            <div class="small muted" style="margin-bottom: 5px">标题</div>
            <a-input v-model="form.title" placeholder="例如：数据结构冲刺笔记" />
          </div>
          <div>
            <div class="small muted" style="margin-bottom: 5px">分类</div>
            <a-select v-model="form.category" style="width: 100%">
              <a-option v-for="c in cats.slice(1)" :key="c.id" :value="c.id">{{ c.label }}</a-option>
            </a-select>
          </div>
        </div>
        <div style="margin-top: 12px">
          <div class="small muted" style="margin-bottom: 5px">简介</div>
          <a-textarea v-model="form.description" :auto-size="{ minRows: 3 }" placeholder="一句话说明资料内容与适用阶段" />
        </div>
        <div style="margin-top: 12px">
          <div class="small muted" style="margin-bottom: 5px">标签（逗号分隔）</div>
          <a-input v-model="form.tags" placeholder="408,核心考点,冲刺" />
        </div>
        <a-button long type="primary" :loading="saving" style="margin-top: 16px" @click="saveMaterial">发布资料</a-button>
      </div>
    </a-modal>
  </div>
</template>

<style scoped>
.link-card { display: flex; align-items: center; gap: 12px; padding: 16px 18px; border-radius: 12px; background: #f4f7ee; border: 1px solid rgba(184, 208, 175, 0.5); color: #4e7a3f; cursor: pointer; transition: 0.16s; }
.link-card:hover { background: #eef4e6; transform: translateY(-1px); }
.fit-row { display: flex; align-items: center; gap: 8px; margin-top: 10px; }
.fit-bar { flex: 1; height: 7px; border-radius: 999px; background: rgba(139, 115, 85, 0.12); overflow: hidden; }
.fit-bar i { display: block; height: 100%; border-radius: 999px; background: linear-gradient(90deg, #c9a227, var(--meadow)); }
.fit-num { font-size: 12px; font-weight: 600; color: var(--meadow-dark); white-space: nowrap; }
.cat-list { display: flex; flex-direction: column; gap: 2px; }
.cat-item { display: flex; justify-content: space-between; padding: 9px 10px; border-radius: 9px; cursor: pointer; font-size: 13.5px; color: var(--ink-2); }
.cat-item:hover { background: #fdfaf4; color: var(--brand); }
.cat-item.active { background: var(--brand-soft); color: var(--brand); font-weight: 500; }
.upload-box { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 22px; border: 1.5px dashed rgba(139, 115, 85, 0.30); border-radius: 12px; cursor: pointer; color: var(--muted); text-align: center; background: #fdfaf4; }
.upload-box:hover { border-color: #b3d0af; background: #fdfaf4; }
@media (max-width: 1024px) {
  .grid[style*="220px"] { grid-template-columns: 1fr !important; }
  aside { position: static !important; }
}
</style>
