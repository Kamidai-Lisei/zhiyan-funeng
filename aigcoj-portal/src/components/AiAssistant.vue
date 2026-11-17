<script setup lang="ts">
import { nextTick, ref, watch } from 'vue'
import { Message } from '@arco-design/web-vue'
import { aiApi } from '../api/ai'
import { useUiStore } from '../stores/ui'
import { useAuthStore } from '../stores/auth'
import { errorMessage } from '../api/http'
import Icon from './Icon.vue'

const ui = useUiStore()
const auth = useAuthStore()

const scenes = [
  { id: 'study', label: '408 答疑' },
  { id: 'code', label: '代码分析' },
  { id: 'plan', label: '复习规划' },
  { id: 'interview', label: '模拟面试' },
  { id: 'resume', label: '简历建议' },
]
const scene = ref('study')
const input = ref('')
const loading = ref(false)
const bodyRef = ref<HTMLElement>()
const messages = ref<{ role: 'bot' | 'user'; text: string }[]>([
  { role: 'bot', text: '你好，我是智研赋能 AI 助教。我会记住你的学习轨迹，可以问我 408 知识点、帮你分析代码、规划复习节奏，或者模拟一次复试面试。' },
])

const quick = ['帮我规划今天的学习', '解释 TCP 三次握手', '页面置换 FIFO 怎么算', '如何准备复试英语']

const scrollBottom = async () => {
  await nextTick()
  if (bodyRef.value) bodyRef.value.scrollTop = bodyRef.value.scrollHeight
}

const send = async (text?: string) => {
  const content = (text ?? input.value).trim()
  if (!content || loading.value) return
  if (!auth.isAuthed) {
    Message.info('登录后可使用 AI 助教')
    return
  }
  messages.value.push({ role: 'user', text: content })
  input.value = ''
  loading.value = true
  await scrollBottom()
  try {
    const res = await aiApi.ask({ message: content, scene: scene.value })
    messages.value.push({ role: 'bot', text: res.answer })
  } catch (e: any) {
    messages.value.push({ role: 'bot', text: errorMessage(e) })
  } finally {
    loading.value = false
    await scrollBottom()
  }
}

watch(() => ui.aiOpen, (open) => { if (open) scrollBottom() })
</script>

<template>
  <div>
    <button v-if="!ui.aiOpen" class="ai-fab" title="AI 学习助手" @click="ui.toggleAi()">
      <Icon name="sparkles" :size="23" />
    </button>

    <div v-if="ui.aiOpen" class="ai-panel">
      <div class="ai-head">
        <div class="row gap8">
          <div class="stat-icon violet" style="width: 32px; height: 32px; border-radius: 9px"><Icon name="sparkles" :size="16" /></div>
          <div>
            <div class="h4">智研 AI 助教</div>
            <div class="small muted" style="line-height: 1.2">408 · 代码 · 复试</div>
          </div>
        </div>
        <button class="icon-btn" style="width: 30px; height: 30px" @click="ui.aiOpen = false"><Icon name="close" :size="15" /></button>
      </div>

      <div class="row wrap gap8" style="padding: 10px 14px; border-bottom: 1px solid var(--line)">
        <span v-for="s in scenes" :key="s.id" class="tag" :class="scene === s.id ? 'blue' : ''" style="cursor: pointer" @click="scene = s.id">{{ s.label }}</span>
      </div>

      <div ref="bodyRef" class="ai-body">
        <div v-for="(m, i) in messages" :key="i" class="ai-msg" :class="m.role">
          <div v-if="m.role === 'bot'" class="stat-icon violet" style="width: 26px; height: 26px; border-radius: 8px"><Icon name="sparkles" :size="13" /></div>
          <div class="bubble">{{ m.text }}</div>
        </div>
        <div v-if="loading" class="ai-msg bot">
          <div class="stat-icon violet" style="width: 26px; height: 26px; border-radius: 8px"><Icon name="sparkles" :size="13" /></div>
          <div class="bubble">思考中…</div>
        </div>
        <div v-if="messages.length <= 1" class="row wrap gap8" style="margin-top: 6px">
          <span v-for="q in quick" :key="q" class="chip" style="font-size: 12px" @click="send(q)">{{ q }}</span>
        </div>
      </div>

      <div class="ai-foot">
        <a-input v-model="input" placeholder="问点什么…" size="small" @press-enter="send()" />
        <a-button type="primary" size="small" :loading="loading" @click="send()"><Icon name="send" :size="14" /></a-button>
      </div>
    </div>
  </div>
</template>
