<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import Icon from './Icon.vue'
import { useAuthStore } from '../stores/auth'

const year = new Date().getFullYear()
const router = useRouter()
const auth = useAuthStore()

const cols = computed(() => [
  {
    title: '学习',
    links: [
      { label: '题库练习', path: '/problems' },
      { label: '408 笔记', path: '/learn/408' },
      { label: '资料库', path: '/materials' },
      { label: '错题本', path: '/wrong-book' },
    ],
  },
  {
    title: '社区',
    links: [
      { label: '考研论坛', path: '/forum' },
      { label: '发布经验帖', path: '/forum/new' },
      { label: '就业与复试', path: '/career' },
      { label: '简历工坊', path: '/career/resume' },
    ],
  },
])
</script>

<template>
  <footer class="site-footer">
    <div class="container footer-inner">
      <div style="max-width: 320px">
        <div class="row gap8" style="margin-bottom: 8px">
          <div class="brand-logo" style="width: 30px; height: 30px; border-radius: 9px; font-size: 14px">智</div>
          <strong style="color: var(--ink)">智研赋能</strong>
        </div>
        <div>一人一档、一人一策、一人一资源 —— 让 AI 真正认识你，把有限的备考时间投入到最适合自己的目标、策略与内容中。</div>
      </div>
      <div v-for="col in cols" :key="col.title">
        <div class="h4" style="margin-bottom: 8px; color: var(--ink)">{{ col.title }}</div>
        <div v-for="l in col.links" :key="l.path" class="footer-links" style="margin-bottom: 5px">
          <a @click="router.push(l.path)">{{ l.label }}</a>
        </div>
      </div>
      <div>
        <div class="h4" style="margin-bottom: 8px; color: var(--ink)">关于</div>
        <div>Spring Boot · Vue 3 · DeepSeek</div>
        <div style="margin-top: 4px">© {{ year }} 智研赋能 · 学习交流用途</div>
        <div v-if="!auth.isAuthed" class="row gap8" style="margin-top: 10px">
          <Icon name="sparkles" :size="14" />
          <a style="color: var(--brand); cursor: pointer" @click="router.push('/register')">加入学习社区</a>
        </div>
      </div>
    </div>
  </footer>
</template>
