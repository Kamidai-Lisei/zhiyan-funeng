import { createApp } from 'vue'
import ArcoVue from '@arco-design/web-vue'
import '@arco-design/web-vue/dist/arco.css'
import './styles/main.css'
import App from './App.vue'
import { createPinia } from 'pinia'
import { router } from './router'
import { useAuthStore } from './stores/auth'
import Icon from './components/Icon.vue'

const app = createApp(App).use(createPinia()).use(router).use(ArcoVue)
// 全局注册图标组件，避免各页面重复引入
app.component('Icon', Icon)

router.isReady().then(async () => {
  try {
    await useAuthStore().hydrate()
  } finally {
    app.mount('#app')
  }
})
