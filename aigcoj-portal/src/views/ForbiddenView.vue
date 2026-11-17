<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import Icon from '../components/Icon.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const isAdminArea = computed(() => route.query.from === 'admin')
</script>

<template>
  <div class="forbidden">
    <div class="card card-pad box">
      <div class="badge"><Icon name="shield" :size="26" /></div>
      <div class="h1" style="font-size: 26px; margin-top: 16px">403 · 无访问权限</div>
      <p class="muted" style="margin-top: 10px; line-height: 1.9">
        <template v-if="isAdminArea">
          管理后台仅对<strong>管理员账号</strong>开放。当前登录的是普通用户账号，无法进入后台控制面板。
        </template>
        <template v-else>
          你访问的页面需要更高权限，当前账号无法查看。
        </template>
      </p>

      <div class="tips">
        <div class="tip"><Icon name="user" :size="15" /><span>当前账号：{{ auth.isAuthed ? (auth.displayName + '（' + (auth.isAdmin ? '管理员' : '普通用户') + '）') : '未登录' }}</span></div>
        <div class="tip"><Icon name="compass" :size="15" /><span>如需管理权限，请使用管理员账号 admin 重新登录</span></div>
      </div>

      <div class="row gap12" style="margin-top: 22px">
        <a-button type="primary" @click="router.push('/')">返回首页</a-button>
        <a-button @click="router.push('/login')">切换账号登录</a-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.forbidden { min-height: 70vh; display: grid; place-items: center; padding: 40px 20px; }
.box { max-width: 520px; width: 100%; text-align: center; padding: 40px 32px; }
.badge {
  width: 58px; height: 58px; border-radius: 18px; display: grid; place-items: center; margin: 0 auto;
  background: rgba(194, 86, 86, 0.1); color: #c25656;
}
.tips { display: grid; gap: 10px; margin-top: 22px; text-align: left; }
.tip {
  display: flex; align-items: center; gap: 9px; font-size: 13px; color: var(--ink-2);
  background: #fdfaf4; border-radius: 10px; padding: 11px 14px;
}
.row { display: flex; justify-content: center; }
</style>
