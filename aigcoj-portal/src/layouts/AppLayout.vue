<script setup lang="ts">
import { onBeforeUnmount, onMounted } from 'vue'
import { RouterView } from 'vue-router'
import AppHeader from '../components/AppHeader.vue'
import AppFooter from '../components/AppFooter.vue'
import SearchDialog from '../components/SearchDialog.vue'
import AiAssistant from '../components/AiAssistant.vue'
import { useUiStore } from '../stores/ui'

const ui = useUiStore()

const onKey = (e: KeyboardEvent) => {
  if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === 'k') {
    e.preventDefault()
    ui.openSearch()
  }
}

onMounted(() => window.addEventListener('keydown', onKey))
onBeforeUnmount(() => window.removeEventListener('keydown', onKey))
</script>

<template>
  <div style="min-height: 100vh; display: flex; flex-direction: column">
    <AppHeader />
    <main style="flex: 1">
      <RouterView />
    </main>
    <AppFooter />
    <SearchDialog />
    <AiAssistant />
  </div>
</template>
