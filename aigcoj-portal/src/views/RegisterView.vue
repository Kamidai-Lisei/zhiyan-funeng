<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { useAuthStore } from '@/stores/auth'
import Floral from '@/components/Floral.vue'

const auth = useAuthStore()
const router = useRouter()
const loading = ref(false)

const form = reactive({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
  userName: '',
})

async function submit() {
  if (form.userAccount.trim().length < 4) {
    Message.warning('账号至少 4 个字符')
    return
  }
  if (form.userPassword.length < 8) {
    Message.warning('密码至少 8 位')
    return
  }
  if (form.userPassword !== form.checkPassword) {
    Message.warning('两次输入的密码不一致')
    return
  }
  loading.value = true
  try {
    await auth.register(form.userAccount.trim(), form.userPassword, form.checkPassword, form.userName.trim() || form.userAccount.trim())
    await auth.hydrate()
    Message.success('注册成功，已自动登录')
    router.replace('/')
  } catch (e: any) {
    Message.error(e?.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <aside class="auth-side">
        <Floral class="sprig tl rot-l" variant="wheat" :size="92" color="#c9a227" :stroke="1.3" />
        <Floral class="sprig br rot-r" variant="mushroom" :size="86" color="#8b7355" :stroke="1.3" />

        <RouterLink to="/" class="auth-brand">
          <span class="logo">智</span>
          <div>
            <b>加入智研赋能</b>
            <p>建立你的专属考研数字画像</p>
          </div>
        </RouterLink>

        <p class="auth-quote">“从今天开始，<br />让 AI 慢慢认识你。”</p>

        <ul class="auth-points">
          <li>一人一档 · 学情数据持续沉淀为个人画像</li>
          <li>决策推演 · 择校、调目标都有数据依据</li>
          <li>个性化规划 · 计划跟着你走，而不是模板</li>
          <li>错题诊断 · 薄弱点定位 + 靶向练习</li>
        </ul>

        <div class="auth-foot">
          每天进步一点点，<b>408</b> 也能拿下。
        </div>
      </aside>

      <div class="auth-main">
        <div class="h1" style="font-size: 32px">创建账号</div>
        <p class="sub">已有账号？<RouterLink to="/login">去登录</RouterLink></p>

        <form class="auth-form" @submit.prevent="submit">
          <label>账号</label>
          <input v-model="form.userAccount" class="ipt" placeholder="4 位以上，登录时使用" autocomplete="username" />
          <label>昵称</label>
          <input v-model="form.userName" class="ipt" placeholder="选填，会显示在论坛与排行" />
          <label>密码</label>
          <input v-model="form.userPassword" class="ipt" type="password" placeholder="至少 8 位" autocomplete="new-password" />
          <label>确认密码</label>
          <input v-model="form.checkPassword" class="ipt" type="password" placeholder="再输入一次" autocomplete="new-password" />
          <button class="btn-primary auth-btn" type="submit" :disabled="loading">
            {{ loading ? '提交中…' : '注 册' }}
          </button>
        </form>

        <RouterLink to="/" class="auth-back">← 先随便逛逛</RouterLink>
      </div>
    </div>
  </div>
</template>
