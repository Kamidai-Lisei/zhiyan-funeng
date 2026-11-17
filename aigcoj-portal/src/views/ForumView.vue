<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { forumApi } from '../api/forum'
import { statsApi } from '../api/stats'
import { errorMessage } from '../api/http'
import { useAuthStore } from '../stores/auth'
import { fromNow, plainText, toTags } from '../utils/format'
import Icon from '../components/Icon.vue'
import EmptyState from '../components/EmptyState.vue'

const router = useRouter()
const auth = useAuthStore()

const loading = ref(true)
const rows = ref<any[]>([])
const total = ref(0)
const current = ref(1)
const pageSize = ref(10)
const sort = ref('new')
const keyword = ref('')
const tag = ref('')
const ranking = ref<any[]>([])

const tabs = [
  { id: 'new', label: '最新发布', field: 'createTime' },
  { id: 'hot', label: '热门讨论', field: 'thumbNum' },
  { id: 'best', label: '收藏最多', field: 'favourNum' },
]
const tags = ['经验', '数据结构', '操作系统', '计算机网络', '复试', '择校', '求助']

const load = async () => {
  loading.value = true
  try {
    const t = tabs.find((x) => x.id === sort.value)!
    const page = await forumApi.list({
      current: current.value,
      pageSize: pageSize.value,
      searchText: keyword.value || undefined,
      tags: tag.value ? [tag.value] : undefined,
      sortField: t.field,
      sortOrder: 'desc',
    })
    rows.value = page.records || []
    total.value = Number(page.total || 0)
  } catch (e: any) {
    Message.error(errorMessage(e))
  } finally {
    loading.value = false
  }
}

const changeTab = (id: string) => {
  sort.value = id
  current.value = 1
  load()
}

const changeTag = (t: string) => {
  tag.value = tag.value === t ? '' : t
  current.value = 1
  load()
}

onMounted(async () => {
  load()
  try { ranking.value = await statsApi.ranking() } catch {}
})
</script>

<template>
  <div class="page">
    <div class="container">
      <div class="row between wrap gap16" style="margin-bottom: 20px">
        <div>
          <div class="kicker">Community</div>
          <h1 class="h1" style="margin-top: 6px">考研论坛</h1>
          <p class="muted" style="margin-top: 6px">经验、答疑、择校与复试，和一起上岸的人交流。</p>
        </div>
        <a-button type="primary" size="large" @click="router.push(auth.isAuthed ? '/forum/new' : '/login')">
          <Icon name="edit" :size="15" /> 发布帖子
        </a-button>
      </div>

      <div class="grid" style="grid-template-columns: minmax(0, 1fr) 300px; gap: 20px; align-items: start">
        <div>
          <div class="card">
            <div class="row between wrap gap12" style="padding: 14px 18px; border-bottom: 1px solid var(--line)">
              <div class="row gap8">
                <span v-for="t in tabs" :key="t.id" class="chip" :class="{ active: sort === t.id }" @click="changeTab(t.id)">{{ t.label }}</span>
              </div>
              <div class="row gap8">
                <a-input v-model="keyword" placeholder="搜索帖子" allow-clear style="width: 200px" @press-enter="current = 1; load()" />
                <a-button size="small" @click="current = 1; load()"><Icon name="search" :size="14" /></a-button>
              </div>
            </div>
            <div class="row wrap gap8" style="padding: 10px 18px; border-bottom: 1px solid #f4eee2">
              <span class="small muted">话题</span>
              <span v-for="t in tags" :key="t" class="tag" :class="tag === t ? 'blue' : ''" style="cursor: pointer" @click="changeTag(t)">#{{ t }}</span>
            </div>

            <div v-if="loading" style="padding: 18px">
              <div v-for="i in 5" :key="i" class="skeleton" style="height: 64px; margin-bottom: 12px" />
            </div>
            <template v-else-if="rows.length">
              <div v-for="p in rows" :key="p.id" class="post-item" @click="router.push(`/forum/${p.id}`)">
                <div class="avatar" style="margin-top: 2px">{{ (p.user?.userName || '研').slice(0, 1) }}</div>
                <div style="flex: 1; min-width: 0">
                  <div class="post-title">{{ p.title }}</div>
                  <div class="post-excerpt">{{ plainText(p.content, 110) }}</div>
                  <div class="row wrap gap12 small muted" style="margin-top: 8px">
                    <span>{{ p.user?.userName || '研友' }}</span>
                    <span>{{ fromNow(p.createTime) }}</span>
                    <span><Icon name="heart" :size="12" /> {{ p.thumbNum || 0 }}</span>
                    <span><Icon name="star" :size="12" /> {{ p.favourNum || 0 }}</span>
                    <span v-for="t in (p.tagList || toTags(p.tags)).slice(0, 2)" :key="t" class="tag">#{{ t }}</span>
                  </div>
                </div>
              </div>
              <div class="row between" style="padding: 14px 18px; border-top: 1px solid var(--line)">
                <span class="small muted">共 {{ total }} 篇帖子</span>
                <a-pagination v-model:current="current" :page-size="pageSize" :total="total" size="small" @change="load" />
              </div>
            </template>
            <EmptyState v-else title="这个话题还没有帖子" desc="来发布第一篇经验帖，帮助更多研友。" icon="chat">
              <a-button type="primary" size="small" style="margin-top: 12px" @click="router.push('/forum/new')">去发帖</a-button>
            </EmptyState>
          </div>
        </div>

        <aside style="display: grid; gap: 14px; position: sticky; top: 88px">
          <div class="card card-pad" style="background: linear-gradient(135deg, #f4f0e4, #ffffff)">
            <div class="h4">写下你的上岸故事</div>
            <div class="small muted" style="margin: 6px 0 12px">经验帖会帮助到正在迷茫的研友，也会被更多人记住。</div>
            <a-button long type="primary" size="small" @click="router.push(auth.isAuthed ? '/forum/new' : '/login')">开始写帖</a-button>
          </div>
          <div class="card card-pad">
            <div class="row between" style="margin-bottom: 8px"><span class="h4">活跃研友</span><Icon name="trophy" :size="16" style="color: var(--wheat-deep)" /></div>
            <div v-for="(u, i) in ranking.slice(0, 6)" :key="u.id" class="row between" style="padding: 7px 0">
              <div class="row gap8">
                <span class="small" style="width: 16px; color: var(--muted-2)">{{ i + 1 }}</span>
                <div class="avatar sm">{{ (u.userName || '研').slice(0, 1) }}</div>
                <span class="small">{{ u.userName || u.userAccount }}</span>
              </div>
              <span class="small muted">{{ u.acCount || 0 }} AC</span>
            </div>
          </div>
          <div class="card card-pad">
            <div class="h4" style="margin-bottom: 8px">社区公约</div>
            <div class="small muted" style="line-height: 1.9">
              1. 友善交流，不人身攻击<br />
              2. 不发布广告与侵权资料<br />
              3. 经验帖尽量具体、可执行<br />
              4. 提问前先搜索历史帖子
            </div>
          </div>
        </aside>
      </div>
    </div>
  </div>
</template>

<style scoped>
@media (max-width: 1024px) {
  .grid[style*="300px"] { grid-template-columns: 1fr !important; }
  aside { position: static !important; }
}
</style>
