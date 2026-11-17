<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { useAuthStore } from '@/stores/auth'
import Floral from '@/components/Floral.vue'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const loading = ref(false)
const form = reactive({ userAccount: '', userPassword: '' })

const demos = [
  { label: '管理员', account: 'jayjay', password: 'kaoyan2026' },
  { label: '普通用户', account: 'aiaiai', password: 'kaoyan2026' },
]

const days = computed(() => Math.max(0, Math.ceil((new Date('2026-12-19').getTime() - Date.now()) / 86400000)))

async function submit() {
  if (!form.userAccount || !form.userPassword) {
    Message.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    await auth.login(form.userAccount.trim(), form.userPassword)
    Message.success(`欢迎回来，${auth.displayName}`)
    const redirect = (route.query.redirect as string) || '/'
    router.replace(redirect)
  } catch (e: any) {
    Message.error(e?.message || '登录失败')
  } finally {
    loading.value = false
  }
}

function fill(demo: { account: string; password: string }) {
  form.userAccount = demo.account
  form.userPassword = demo.password
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <aside class="auth-side">
        <Floral class="sprig tl rot-l" variant="sprig" :size="92" color="#5a8f5a" :stroke="1.3" />
        <Floral class="sprig br rot-r" variant="flower" :size="88" color="#b06b6b" :stroke="1.3" />

        <RouterLink to="/" class="auth-brand">
          <span class="logo">智</span>
          <div>
            <b>智研赋能</b>
            <p>AI 记忆 · 决策推演 · 资源评估</p>
          </div>
        </RouterLink>

        <p class="auth-quote">“一人一档，一人一策，<br />让 AI 真正认识你。”</p>

        <ul class="auth-points">
          <li>AI 长期记忆 · 动态更新的个人考研画像</li>
          <li>决策推演 · 冲稳保择校概率模拟</li>
          <li>资源价值引擎 · 只推当下最适配的资料</li>
          <li>在线判题 · 错题诊断 · 个性化规划</li>
        </ul>

        <div class="auth-foot">
          距离初试还有 <b>{{ days }}</b> 天
          <span class="sub">· 慢慢来，比较快</span>
        </div>
      </aside>

      <div class="auth-main">
        <div class="h1" style="font-size: 32px">账号登录</div>
        <p class="sub">还没有账号？<RouterLink to="/register">去注册一个</RouterLink></p>

        <form class="auth-form" @submit.prevent="submit">
          <label>账号</label>
          <input v-model="form.userAccount" class="ipt" placeholder="请输入账号" autocomplete="username" />
          <label>密码</label>
          <input
            v-model="form.userPassword"
            class="ipt"
            type="password"
            placeholder="请输入密码"
            autocomplete="current-password"
          />
          <button class="btn-primary auth-btn" type="submit" :disabled="loading">
            {{ loading ? '登录中…' : '登 录' }}
          </button>
        </form>

        <div class="auth-demo">
          <span>演示账号</span>
          <button v-for="d in demos" :key="d.account" class="chip" type="button" @click="fill(d)">
            {{ d.label }} · {{ d.account }}
          </button>
        </div>

        <RouterLink to="/" class="auth-back">← 先随便逛逛</RouterLink>
      </div>
    </div>
  </div>
</template>
