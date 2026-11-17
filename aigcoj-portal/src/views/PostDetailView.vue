<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { forumApi } from '../api/forum'
import { errorMessage } from '../api/http'
import { useAuthStore } from '../stores/auth'
import { fromNow, toTags } from '../utils/format'
import MarkdownView from '../components/MarkdownView.vue'
import Icon from '../components/Icon.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const id = String(route.params.id)
const loading = ref(true)
const post = ref<any>(null)
const comments = ref<any[]>([])
const commentText = ref('')
const sending = ref(false)
const related = ref<any[]>([])

const tags = computed(() => post.value?.tagList || toTags(post.value?.tags))
const isOwner = computed(() => auth.user?.id === post.value?.userId || auth.isAdmin)

const load = async () => {
  loading.value = true
  try {
    post.value = await forumApi.get(id)
    comments.value = await forumApi.comments(id)
    const page = await forumApi.list({ current: 1, pageSize: 5, tags: tags.value.length ? [tags.value[0]] : undefined })
    related.value = (page.records || []).filter((p: any) => String(p.id) !== String(id)).slice(0, 4)
  } catch (e: any) {
    Message.error(errorMessage(e))
  } finally {
    loading.value = false
  }
}

const like = async () => {
  if (!auth.isAuthed) return Message.info('登录后可点赞')
  try {
    await forumApi.thumb(id)
    post.value.hasThumb = !post.value.hasThumb
    post.value.thumbNum = Math.max(0, (post.value.thumbNum || 0) + (post.value.hasThumb ? 1 : -1))
  } catch (e: any) {
    Message.error(errorMessage(e))
  }
}

const favour = async () => {
  if (!auth.isAuthed) return Message.info('登录后可收藏')
  try {
    await forumApi.favour(id)
    post.value.hasFavour = !post.value.hasFavour
    post.value.favourNum = Math.max(0, (post.value.favourNum || 0) + (post.value.hasFavour ? 1 : -1))
  } catch (e: any) {
    Message.error(errorMessage(e))
  }
}

const addComment = async () => {
  if (!auth.isAuthed) return Message.info('登录后参与讨论')
  if (!commentText.value.trim()) return
  sending.value = true
  try {
    await forumApi.addComment({ postId: id, content: commentText.value.trim() })
    commentText.value = ''
    comments.value = await forumApi.comments(id)
    Message.success('评论成功')
  } catch (e: any) {
    Message.error(errorMessage(e))
  } finally {
    sending.value = false
  }
}

const removeComment = async (cid: number) => {
  try {
    await forumApi.removeComment(cid)
    comments.value = comments.value.filter((c) => c.id !== cid)
  } catch (e: any) {
    Message.error(errorMessage(e))
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="container" style="max-width: 1040px">
      <div class="row between" style="margin-bottom: 14px">
        <button class="btn-ghost" @click="router.push('/forum')"><Icon name="arrow-left" :size="15" /> 返回论坛</button>
        <div v-if="isOwner" class="row gap8">
          <a-button size="small" @click="router.push(`/forum/${id}/edit`)"><Icon name="edit" :size="14" /> 编辑</a-button>
        </div>
      </div>

      <div v-if="loading" class="card card-pad">
        <div class="skeleton" style="height: 30px; width: 60%; margin-bottom: 14px" />
        <div v-for="i in 8" :key="i" class="skeleton" style="height: 14px; margin-bottom: 10px" />
      </div>

      <template v-else-if="post">
        <article class="card card-pad">
          <div class="row wrap gap8" style="margin-bottom: 10px">
            <span v-for="t in tags" :key="t" class="tag blue"># {{ t }}</span>
          </div>
          <h1 class="h1" style="font-size: 30px">{{ post.title }}</h1>
          <div class="row between wrap gap12" style="margin: 14px 0 22px">
            <div class="row gap8">
              <div class="avatar">{{ (post.user?.userName || '研').slice(0, 1) }}</div>
              <div>
                <div class="h4">{{ post.user?.userName || '研友' }}</div>
                <div class="small muted">{{ fromNow(post.createTime) }} · {{ post.user?.userProfile || '考研路上' }}</div>
              </div>
            </div>
            <div class="row gap8">
              <a-button size="small" :type="post.hasThumb ? 'primary' : 'secondary'" @click="like">
                <Icon name="heart" :size="14" /> {{ post.thumbNum || 0 }}
              </a-button>
              <a-button size="small" :type="post.hasFavour ? 'primary' : 'secondary'" @click="favour">
                <Icon name="star" :size="14" /> {{ post.favourNum || 0 }}
              </a-button>
            </div>
          </div>
          <MarkdownView :content="post.content" />
        </article>

        <div class="card card-pad" style="margin-top: 16px">
          <div class="h3" style="margin-bottom: 14px">讨论 · {{ comments.length }}</div>
          <div style="display: flex; gap: 10px; margin-bottom: 18px">
            <div class="avatar">{{ auth.initial }}</div>
            <div style="flex: 1">
              <a-textarea v-model="commentText" placeholder="友善交流，说说你的看法…" :auto-size="{ minRows: 2 }" />
              <div class="row between" style="margin-top: 8px">
                <span class="small muted">支持 Markdown 基础语法</span>
                <a-button type="primary" size="small" :loading="sending" @click="addComment">发表评论</a-button>
              </div>
            </div>
          </div>

          <div v-for="c in comments" :key="c.id" style="padding: 14px 0; border-top: 1px solid #f4eee2">
            <div class="row between">
              <div class="row gap8">
                <div class="avatar sm">{{ (c.user?.userName || '研').slice(0, 1) }}</div>
                <span class="h4">{{ c.user?.userName || '研友' }}</span>
                <span class="small muted">{{ fromNow(c.createTime) }}</span>
              </div>
              <button v-if="auth.user?.id === c.user?.id || auth.isAdmin" class="icon-btn" style="width: 26px; height: 26px" @click="removeComment(c.id)">
                <Icon name="trash" :size="12" />
              </button>
            </div>
            <div style="margin: 8px 0 0 32px; color: var(--ink-2); white-space: pre-wrap">{{ c.content }}</div>
          </div>
          <div v-if="!comments.length" class="empty small">还没有评论，来说两句。</div>
        </div>

        <div v-if="related.length" class="section">
          <div class="h3" style="margin-bottom: 12px">相关帖子</div>
          <div class="grid g2">
            <div v-for="r in related" :key="r.id" class="card card-hover card-pad" style="cursor: pointer" @click="router.push(`/forum/${r.id}`)">
              <div class="h4">{{ r.title }}</div>
              <div class="row gap12 small muted" style="margin-top: 6px">
                <span>{{ r.user?.userName }}</span>
                <span>{{ fromNow(r.createTime) }}</span>
                <span>{{ r.thumbNum || 0 }} 赞</span>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>
