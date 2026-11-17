<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="nf">
    <div class="nf-inner">
      <p class="code">404</p>
      <h1>这页笔记走丢了</h1>
      <p class="desc">页面可能已被移动或删除。不如回首页继续刷题吧。</p>
      <div class="actions">
        <RouterLink to="/" class="btn-primary">返回首页</RouterLink>
        <button class="btn-ghost" @click="router.back()">返回上一页</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.nf {
  min-height: 70vh; display: grid; place-items: center; padding: 40px 20px;
}
.nf-inner { text-align: center; display: grid; gap: 10px; }
.code {
  margin: 0; font-size: 96px; font-weight: 800; letter-spacing: -6px;
  background: linear-gradient(135deg, var(--meadow), #b3d0af);
  -webkit-background-clip: text; background-clip: text; color: transparent;
}
h1 { margin: 0; font-size: 22px; color: var(--ink); }
.desc { margin: 0 0 12px; color: var(--muted); font-size: 14px; }
.actions { display: flex; gap: 12px; justify-content: center; }
</style>
