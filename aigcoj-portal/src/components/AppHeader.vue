<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useUiStore } from '../stores/ui'
import Icon from './Icon.vue'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const ui = useUiStore()

const navs = [
  { path: '/', label: '首页' },
  { path: '/portrait', label: '我的画像' },
  { path: '/simulator', label: '决策推演' },
  { path: '/problems', label: '题库' },
  { path: '/learn/408', label: '408 学习' },
  { path: '/materials', label: '资料库' },
  { path: '/forum', label: '论坛' },
]

const isActive = (path: string) => (path === '/' ? route.path === '/' : route.path.startsWith(path))
const menuOpen = ref(false)
const menuRef = ref<HTMLElement>()

const onDocClick = (e: MouseEvent) => {
  if (menuRef.value && !menuRef.value.contains(e.target as Node)) menuOpen.value = false
}

const go = (path: string) => {
  menuOpen.value = false
  ui.mobileNavOpen = false
  router.push(path)
}

const logout = async () => {
  menuOpen.value = false
  await auth.logout()
  router.push('/')
}

onMounted(() => document.addEventListener('click', onDocClick))
onBeforeUnmount(() => document.removeEventListener('click', onDocClick))

const userMenu = computed(() => [
  { label: '我的画像', path: '/portrait', icon: 'chart' },
  { label: '决策推演', path: '/simulator', icon: 'compass' },
  { label: '学习规划', path: '/plan', icon: 'layers' },
  { label: '个人中心', path: '/me', icon: 'user' },
  { label: '我的提交', path: '/submissions', icon: 'code' },
  { label: '错题本', path: '/wrong-book', icon: 'book' },
  { label: '就业与复试', path: '/career', icon: 'briefcase' },
  { label: '简历工坊', path: '/career/resume', icon: 'file' },
])
</script>

<template>
  <header class="site-header">
    <div class="container header-inner">
      <div class="brand" @click="go('/')">
        <div class="brand-logo">智</div>
        <div>
          <div class="brand-name">智研赋能</div>
          <div class="brand-sub">AI 记忆 · 决策推演 · 资源评估</div>
        </div>
      </div>

      <nav class="nav">
        <a v-for="n in navs" :key="n.path" :class="{ active: isActive(n.path) }" @click="go(n.path)">{{ n.label }}</a>
      </nav>

      <div class="header-actions">
        <button class="icon-btn" title="搜索" @click="ui.openSearch()">
          <Icon name="search" :size="17" />
        </button>
        <template v-if="auth.isAuthed">
          <div ref="menuRef" class="user-wrap">
            <div class="user-chip" @click.stop="menuOpen = !menuOpen">
              <img v-if="auth.user?.userAvatar" class="avatar" :src="auth.user.userAvatar" alt="" />
              <div v-else class="avatar">{{ auth.initial }}</div>
              <span class="user-name">{{ auth.displayName }}</span>
            </div>
            <div v-if="menuOpen" class="user-menu">
              <div class="menu-head">
                <div class="h4">{{ auth.displayName }}</div>
                <div class="small muted">{{ auth.user?.userProfile || '今天也要按计划复习' }}</div>
              </div>
              <div v-for="m in userMenu" :key="m.path" class="menu-item" @click="go(m.path)">
                <Icon :name="m.icon" :size="16" />{{ m.label }}
              </div>
              <div v-if="auth.isAdmin" class="menu-item" @click="go('/admin')">
                <Icon name="shield" :size="16" />管理后台
              </div>
              <div class="menu-divider" />
              <div class="menu-item danger" @click="logout"><Icon name="logout" :size="16" />退出登录</div>
            </div>
          </div>
        </template>
        <template v-else>
          <button class="btn-ghost" @click="go('/login')">登录</button>
          <a-button type="primary" size="small" @click="go('/register')">免费注册</a-button>
        </template>
        <button class="icon-btn mobile-toggle" @click="ui.mobileNavOpen = true">
          <Icon name="menu" :size="18" />
        </button>
      </div>
    </div>

    <a-drawer v-model:visible="ui.mobileNavOpen" placement="right" :width="280" :footer="false">
      <template #title>智研赋能</template>
      <div class="mobile-nav">
        <a v-for="n in navs" :key="n.path" :class="{ active: isActive(n.path) }" @click="go(n.path)">{{ n.label }}</a>
        <template v-if="auth.isAuthed">
          <a @click="go('/portrait')">我的画像</a>
          <a @click="go('/plan')">学习规划</a>
          <a @click="go('/me')">个人中心</a>
          <a @click="go('/submissions')">我的提交</a>
          <a @click="go('/wrong-book')">错题本</a>
          <a @click="go('/career')">就业与复试</a>
          <a @click="go('/career/resume')">简历工坊</a>
          <a v-if="auth.isAdmin" @click="go('/admin')">管理后台</a>
          <a class="danger" @click="logout">退出登录</a>
        </template>
        <template v-else>
          <a @click="go('/login')">登录</a>
          <a @click="go('/register')">注册</a>
        </template>
      </div>
    </a-drawer>
  </header>
</template>

<style scoped>
.user-wrap { position: relative; }
.user-name { font-size: 14px; color: var(--ink-2); max-width: 92px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.user-menu {
  position: absolute; right: 0; top: 50px; width: 238px; background: var(--cloth-2);
  border: 1px solid rgba(90, 143, 90, 0.18); border-radius: 20px;
  box-shadow: 0 18px 44px rgba(139, 115, 85, 0.18); padding: 8px; z-index: 70;
}
.menu-head { padding: 10px 12px 10px; border-bottom: 2px dashed rgba(139, 115, 85, 0.16); margin-bottom: 6px; }
.menu-item { display: flex; align-items: center; gap: 9px; padding: 9px 12px; border-radius: 12px; font-size: 14px; color: var(--ink-2); cursor: pointer; transition: background 0.3s, color 0.3s; }
.menu-item:hover { background: #fdfaf4; color: var(--brand); }
.menu-item.danger { color: var(--wild-rose-deep); }
.menu-item.danger:hover { background: rgba(212, 160, 160, 0.16); }
.menu-divider { height: 0; border-top: 2px dashed rgba(139, 115, 85, 0.14); margin: 6px 8px; }
.mobile-nav { display: flex; flex-direction: column; }
.mobile-nav a { padding: 13px 10px; border-radius: 10px; color: var(--ink-2); cursor: pointer; border-bottom: 1px dashed rgba(139, 115, 85, 0.16); }
.mobile-nav a.active { color: var(--brand); background: var(--brand-soft); }
.mobile-nav a.danger { color: var(--wild-rose-deep); }
@media (max-width: 820px) { .user-name { display: none; } .header-actions :deep(.arco-btn) { display: none; } }
</style>
