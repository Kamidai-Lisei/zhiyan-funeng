<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { forumApi } from '../api/forum'
import { errorMessage } from '../api/http'
import { useAuthStore } from '../stores/auth'
import MarkdownView from '../components/MarkdownView.vue'
import Icon from '../components/Icon.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const editing = computed(() => !!route.params.id)
const id = String(route.params.id || 0)
const title = ref('')
const tagsText = ref('')
const content = ref('')
const preview = ref(false)
const saving = ref(false)
const loading = ref(false)

const tagSuggestions = ['经验', '数据结构', '操作系统', '计算机网络', '复试', '择校', '求助']
const insertTag = (t: string) => {
  const parts = tagsText.value.split(/[,，]/).map((x) => x.trim()).filter(Boolean)
  if (!parts.includes(t)) parts.push(t)
  tagsText.value = parts.join(',')
}

const load = async () => {
  if (!editing.value) return
  loading.value = true
  try {
    const post = await forumApi.get(id)
    if (post.userId !== auth.user?.id && !auth.isAdmin) {
      Message.error('无权编辑该帖子')
      router.push(`/forum/${id}`)
      return
    }
    title.value = post.title
    content.value = post.content
    tagsText.value = (post.tagList || []).join(',')
  } catch (e: any) {
    Message.error(errorMessage(e))
  } finally {
    loading.value = false
  }
}

const save = async () => {
  if (!title.value.trim()) return Message.warning('请填写标题')
  if (!content.value.trim()) return Message.warning('请填写正文')
  saving.value = true
  const tags = tagsText.value.split(/[,，]/).map((t) => t.trim()).filter(Boolean)
  try {
    if (editing.value) {
      await forumApi.edit({ id, title: title.value.trim(), content: content.value, tags })
      Message.success('已保存')
      router.push(`/forum/${id}`)
    } else {
      const newId = await forumApi.add({ title: title.value.trim(), content: content.value, tags })
      Message.success('发布成功')
      router.push(`/forum/${newId}`)
    }
  } catch (e: any) {
    Message.error(errorMessage(e))
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="container" style="max-width: 980px">
      <div class="row between" style="margin-bottom: 16px">
        <button class="btn-ghost" @click="router.back()"><Icon name="arrow-left" :size="15" /> 返回</button>
        <span class="small muted">支持 Markdown</span>
      </div>

      <div v-if="loading" class="card card-pad"><div class="skeleton" style="height: 300px" /></div>

      <div v-else class="grid" style="grid-template-columns: minmax(0, 1fr) 240px; gap: 18px; align-items: start">
        <div class="card card-pad">
          <input v-model="title" class="title-input" placeholder="写一个清晰、具体的标题…" maxlength="80" />
          <div class="row gap8" style="margin: 12px 0">
            <span class="small muted">标签</span>
            <a-input v-model="tagsText" placeholder="经验,408,复试（逗号分隔）" style="flex: 1" />
          </div>
          <div class="row between" style="margin-bottom: 8px">
            <div class="row gap8">
              <button class="btn-ghost" :class="{ active: !preview }" @click="preview = false">编辑</button>
              <button class="btn-ghost" :class="{ active: preview }" @click="preview = true">预览</button>
            </div>
            <span class="small muted">{{ content.length }} 字</span>
          </div>
          <a-textarea v-if="!preview" v-model="content" :auto-size="{ minRows: 18 }" placeholder="分享你的经验、思路或问题…" />
          <div v-else class="preview-box">
            <MarkdownView :content="content || '*还没有内容*'" />
          </div>
          <div class="row between" style="margin-top: 16px">
            <span class="small muted">发布即表示同意社区公约</span>
            <a-button type="primary" :loading="saving" @click="save">{{ editing ? '保存修改' : '发布帖子' }}</a-button>
          </div>
        </div>

        <aside style="display: grid; gap: 14px">
          <div class="card card-pad">
            <div class="h4" style="margin-bottom: 8px">推荐标签</div>
            <div class="row wrap gap8">
              <span v-for="t in tagSuggestions" :key="t" class="tag" style="cursor: pointer" @click="insertTag(t)">#{{ t }}</span>
            </div>
          </div>
          <div class="card card-pad">
            <div class="h4" style="margin-bottom: 8px">写作建议</div>
            <div class="small muted" style="line-height: 1.9">
              · 标题写清楚年份 / 科目 / 结果<br />
              · 用二级标题拆分阶段<br />
              · 多写具体做法，少写空泛感受<br />
              · 结尾给学弟学妹一句建议
            </div>
          </div>
        </aside>
      </div>
    </div>
  </div>
</template>

<style scoped>
.title-input { width: 100%; border: none; outline: none; font-size: 24px; font-weight: 600; font-family: var(--font); padding: 8px 0 12px; border-bottom: 1px solid var(--line); color: var(--ink); }
.preview-box { min-height: 420px; border: 1px solid var(--line); border-radius: 10px; padding: 18px; background: #fdfaf4; }
@media (max-width: 900px) { .grid[style*="240px"] { grid-template-columns: 1fr !important; } }
</style>
