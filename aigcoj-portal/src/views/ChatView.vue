<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { friendApi, type ChatMsg, type FriendItem, type FriendRequest, type SearchUser } from '@/api/friend'
import { errorMessage } from '@/api/http'
import { useAuthStore } from '@/stores/auth'
import Icon from '@/components/Icon.vue'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const friends = ref<FriendItem[]>([])
const requests = ref<FriendRequest[]>([])
const activeId = ref<number | null>(null)
const activeFriend = ref<{ id: number; userName: string; userAvatar?: string } | null>(null)
const messages = ref<ChatMsg[]>([])
const draft = ref('')
const sending = ref(false)
const lastMsgId = ref(0)
const searchName = ref('')
const searchResults = ref<SearchUser[]>([])
const searching = ref(false)
const showRequests = ref(false)
let poll: ReturnType<typeof setInterval> | null = null
let msgPoll: ReturnType<typeof setInterval> | null = null

const unreadTotal = computed(() => friends.value.reduce((s, f) => s + (f.unread || 0), 0))

const loadFriends = async () => {
  if (!auth.isAuthed) return
  try {
    friends.value = await friendApi.list()
    requests.value = await friendApi.requests()
    const target = Number(route.query.with)
    if (target && friends.value.some((f) => Number(f.id) === target)) {
      openChat(target)
    }
  } catch (e: any) {
    Message.error(errorMessage(e))
  }
}

const openChat = async (id: number) => {
  activeId.value = id
  lastMsgId.value = 0
  messages.value = []
  try {
    const res = await friendApi.messages(id)
    messages.value = res.messages || []
    activeFriend.value = res.friend || null
    if (messages.value.length) lastMsgId.value = Number(messages.value[messages.value.length - 1].id)
    friends.value = await friendApi.list()
    await nextTick()
    scrollBottom()
    startMsgPoll()
  } catch (e: any) {
    Message.error(errorMessage(e))
  }
}

const startMsgPoll = () => {
  stopMsgPoll()
  msgPoll = setInterval(async () => {
    if (!activeId.value) return
    try {
      const res = await friendApi.messages(activeId.value, lastMsgId.value)
      const fresh = res.messages || []
      if (fresh.length) {
        messages.value.push(...fresh)
        lastMsgId.value = Number(fresh[fresh.length - 1].id)
        await nextTick()
        scrollBottom()
      }
    } catch {}
  }, 3000)
}
const stopMsgPoll = () => {
  if (msgPoll) clearInterval(msgPoll)
  msgPoll = null
}

const send = async () => {
  const text = draft.value.trim()
  if (!text || !activeId.value) return
  sending.value = true
  try {
    await friendApi.send(activeId.value, text)
    draft.value = ''
    const res = await friendApi.messages(activeId.value, lastMsgId.value)
    const fresh = res.messages || []
    if (fresh.length) {
      messages.value.push(...fresh)
      lastMsgId.value = Number(fresh[fresh.length - 1].id)
      await nextTick()
      scrollBottom()
    }
  } catch (e: any) {
    Message.error(errorMessage(e))
  } finally {
    sending.value = false
  }
}

const scrollBottom = () => {
  const el = document.querySelector('.chat-body')
  if (el) el.scrollTop = el.scrollHeight
}

const doSearch = async () => {
  if (!searchName.value.trim()) return
  searching.value = true
  try {
    searchResults.value = await friendApi.search(searchName.value.trim())
  } catch (e: any) {
    Message.error(errorMessage(e))
  } finally {
    searching.value = false
  }
}

const sendRequest = async (u: SearchUser) => {
  try {
    await friendApi.request(u.id)
    Message.success(u.status === 'received' ? '已同意好友申请' : '好友申请已发送')
    searchResults.value = []
    searchName.value = ''
    await loadFriends()
  } catch (e: any) {
    Message.error(errorMessage(e))
  }
}

const accept = async (r: FriendRequest) => {
  try {
    await friendApi.accept(r.requestId)
    Message.success(`已添加 ${r.userName || '研友'} 为好友`)
    await loadFriends()
  } catch (e: any) {
    Message.error(errorMessage(e))
  }
}

const reject = async (r: FriendRequest) => {
  try {
    await friendApi.remove(r.fromId)
    await loadFriends()
    Message.success('已拒绝')
  } catch (e: any) {
    Message.error(errorMessage(e))
  }
}

const removeFriend = async (f: FriendItem) => {
  try {
    await friendApi.remove(f.id)
    if (activeId.value === Number(f.id)) {
      activeId.value = null
      stopMsgPoll()
    }
    await loadFriends()
    Message.success('已删除好友')
  } catch (e: any) {
    Message.error(errorMessage(e))
  }
}

const fmtTime = (t?: string) => (t ? String(t).slice(11, 16) : '')

const pollFriends = () => {
  poll = setInterval(() => {
    if (auth.isAuthed && !activeId.value) loadFriends()
  }, 10000)
}

onMounted(() => {
  loadFriends()
  pollFriends()
})
onUnmounted(() => {
  stopMsgPoll()
  if (poll) clearInterval(poll)
})
</script>

<template>
  <div class="page">
    <div class="container chat-page">
      <div class="row between wrap gap12" style="margin-bottom: 14px">
        <div class="row gap12">
          <button class="btn-ghost" @click="router.push('/forum')"><Icon name="arrow-left" :size="15" /> 返回论坛</button>
          <h1 class="h3" style="margin: 0">好友私信</h1>
          <span v-if="unreadTotal" class="chat-unread-badge">{{ unreadTotal }} 条未读</span>
        </div>
        <a-button v-if="requests.length" size="small" @click="showRequests = true">
          <Icon name="plus" :size="14" /> 好友申请 {{ requests.length }}
        </a-button>
      </div>

      <div class="chat-layout">
        <!-- 左：好友列表 -->
        <aside class="card chat-side">
          <div style="padding: 12px 14px; border-bottom: 1px solid #f0ead9">
            <div class="row gap8">
              <a-input v-model="searchName" placeholder="搜索用户名 / 账号加好友" allow-clear size="small" style="flex: 1" @press-enter="doSearch" />
              <a-button type="primary" size="small" :loading="searching" @click="doSearch"><Icon name="search" :size="14" /></a-button>
            </div>
            <div v-if="searchResults.length" class="chat-search-panel">
              <div v-for="u in searchResults" :key="u.id" class="row between" style="padding: 8px 0">
                <div class="row gap8">
                  <div class="avatar sm">{{ (u.userName || '研').slice(0, 1) }}</div>
                  <div>
                    <div class="small" style="font-weight: 600">{{ u.userName }}</div>
                    <div class="small muted">@{{ u.userAccount }}</div>
                  </div>
                </div>
                <a-button size="mini" type="primary" @click="sendRequest(u)">
                  {{ u.status === 'friends' ? '已是好友' : u.status === 'sent' ? '已发送' : u.status === 'received' ? '同意' : '+ 加好友' }}
                </a-button>
              </div>
            </div>
          </div>
          <div class="chat-friend-list">
            <div
              v-for="f in friends"
              :key="f.id"
              class="chat-friend"
              :class="{ active: activeId === Number(f.id) }"
              @click="openChat(Number(f.id))"
            >
              <div class="avatar sm">
                <img v-if="f.userAvatar" :src="f.userAvatar" style="width: 100%; height: 100%; object-fit: cover; border-radius: 999px" />
                <template v-else>{{ (f.userName || '研').slice(0, 1) }}</template>
              </div>
              <div style="flex: 1; min-width: 0">
                <div class="row between">
                  <span class="small" style="font-weight: 600">{{ f.userName }}</span>
                  <span v-if="f.unread" class="chat-dot">{{ f.unread }}</span>
                </div>
                <div class="small muted" style="overflow: hidden; text-overflow: ellipsis; white-space: nowrap">{{ f.lastMessage || '打个招呼吧 ~' }}</div>
              </div>
            </div>
            <EmptyState v-if="!friends.length" title="还没有好友" desc="在左侧搜索研友，或在论坛帖子中点击「加好友」。" icon="chat" />
          </div>
        </aside>

        <!-- 右：聊天窗口 -->
        <div class="card chat-main">
          <template v-if="activeId && activeFriend">
            <div class="chat-head">
              <div class="row gap8">
                <div class="avatar sm">
                  <img v-if="activeFriend.userAvatar" :src="activeFriend.userAvatar" style="width: 100%; height: 100%; object-fit: cover; border-radius: 999px" />
                  <template v-else>{{ (activeFriend.userName || '研').slice(0, 1) }}</template>
                </div>
                <span class="h4">{{ activeFriend.userName }}</span>
              </div>
              <a-button size="mini" status="danger" @click="removeFriend(friends.find((f) => Number(f.id) === activeId)!)">删除好友</a-button>
            </div>
            <div class="chat-body">
              <div v-for="m in messages" :key="m.id" class="chat-msg" :class="{ mine: m.mine }">
                <div class="chat-bubble">
                  <div class="chat-content">{{ m.content }}</div>
                  <div class="chat-time">{{ fmtTime(m.createTime) }}</div>
                </div>
              </div>
              <div v-if="!messages.length" class="empty small" style="padding: 40px 0">还没有聊天记录，发一句「你好，一起加油上岸！」开始交流吧。</div>
            </div>
            <div class="chat-input">
              <a-textarea
                v-model="draft"
                placeholder="输入消息，Enter 发送…"
                :auto-size="{ minRows: 2, maxRows: 5 }"
                @keydown.enter.exact.prevent="send"
              />
              <div class="row between" style="margin-top: 8px">
                <span class="small muted">探讨学习内容、交流经验</span>
                <a-button type="primary" size="small" :loading="sending" @click="send"><Icon name="send" :size="14" /> 发送</a-button>
              </div>
            </div>
          </template>
          <div v-else class="empty" style="margin: auto; padding: 60px 0">
            <Icon name="chat" :size="34" />
            <div class="h4" style="margin-top: 12px">选择一位好友开始聊天</div>
            <div class="small muted" style="margin-top: 6px">添加好友后即可像微信一样随时私聊探讨学习</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 好友申请弹窗 -->
    <a-modal v-model:visible="showRequests" title="好友申请" :footer="false" :width="420">
      <div v-if="requests.length">
        <div v-for="r in requests" :key="r.requestId" class="row between" style="padding: 10px 0; border-bottom: 1px solid #f0ead9">
          <div class="row gap8">
            <div class="avatar sm">{{ (r.userName || '研').slice(0, 1) }}</div>
            <div>
              <div class="small" style="font-weight: 600">{{ r.userName }}</div>
              <div class="small muted">{{ r.userProfile || '想和你成为好友' }}</div>
            </div>
          </div>
          <div class="row gap8">
            <a-button size="mini" type="primary" @click="accept(r); showRequests = false">同意</a-button>
            <a-button size="mini" @click="reject(r); showRequests = false">拒绝</a-button>
          </div>
        </div>
      </div>
      <div v-else class="empty small">暂无好友申请</div>
    </a-modal>
  </div>
</template>

<style scoped>
.chat-page { max-width: 1200px; }
.chat-layout { display: grid; grid-template-columns: 320px minmax(0, 1fr); gap: 16px; align-items: stretch; height: calc(100vh - 190px); min-height: 480px; }
.chat-side { display: flex; flex-direction: column; overflow: hidden; }
.chat-friend-list { overflow-y: auto; flex: 1; padding: 6px; }
.chat-friend {
  display: flex; gap: 10px; align-items: center; padding: 10px 12px; border-radius: 12px; cursor: pointer; transition: 0.12s;
}
.chat-friend:hover { background: #fdfaf4; }
.chat-friend.active { background: rgba(90, 143, 90, 0.1); }
.chat-dot {
  background: #d9534f; color: #fff; font-size: 11px; min-width: 18px; height: 18px; border-radius: 99px;
  display: inline-grid; place-items: center; padding: 0 5px;
}
.chat-unread-badge { background: rgba(217, 83, 79, 0.12); color: #a84040; font-size: 13px; padding: 3px 12px; border-radius: 99px; font-weight: 600; }
.chat-search-panel { margin-top: 8px; border-top: 1px dashed #e8e0cd; padding-top: 6px; max-height: 240px; overflow-y: auto; }
.chat-main { display: flex; flex-direction: column; overflow: hidden; }
.chat-head { display: flex; justify-content: space-between; align-items: center; padding: 12px 18px; border-bottom: 2px dashed rgba(139, 115, 85, 0.18); }
.chat-body { flex: 1; overflow-y: auto; padding: 18px; display: flex; flex-direction: column; gap: 12px; background: #fdfcf7; }
.chat-msg { display: flex; }
.chat-msg.mine { justify-content: flex-end; }
.chat-bubble { max-width: 72%; }
.chat-msg:not(.mine) .chat-bubble { background: #fff; border: 1px solid #ece5d4; }
.chat-msg.mine .chat-bubble { background: #dff0d8; }
.chat-bubble { border-radius: 14px; padding: 10px 14px; }
.chat-content { font-size: 14px; line-height: 1.7; white-space: pre-wrap; word-break: break-word; }
.chat-time { font-size: 11px; color: #999; margin-top: 4px; text-align: right; }
.chat-input { padding: 12px 16px; border-top: 1px solid #f0ead9; }
@media (max-width: 1024px) {
  .chat-layout { grid-template-columns: 1fr; height: auto; }
  .chat-main { min-height: 480px; }
}
</style>
