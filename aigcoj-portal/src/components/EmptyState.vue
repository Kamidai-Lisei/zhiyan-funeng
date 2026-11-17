<script setup lang="ts">
import Icon from './Icon.vue'
withDefaults(defineProps<{ title?: string; desc?: string; icon?: string }>(), {
  title: '这里还空着',
  desc: '换个筛选条件试试，或者稍后再来看看。',
  icon: 'file',
})
</script>

<template>
  <div class="empty">
    <div class="empty-icon"><Icon :name="icon" :size="24" /></div>
    <div class="h4" style="color: var(--ink-2)">{{ title }}</div>
    <div class="small muted" style="margin-top: 4px">{{ desc }}</div>
    <slot />
  </div>
</template>
