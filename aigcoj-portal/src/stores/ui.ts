import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUiStore = defineStore('ui', () => {
  const searchOpen = ref(false)
  const aiOpen = ref(false)
  const mobileNavOpen = ref(false)

  const openSearch = () => (searchOpen.value = true)
  const closeSearch = () => (searchOpen.value = false)
  const toggleAi = () => (aiOpen.value = !aiOpen.value)

  return { searchOpen, aiOpen, mobileNavOpen, openSearch, closeSearch, toggleAi }
})
