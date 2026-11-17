import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { authApi } from '../api/auth'
import type { LoginUserVO } from '../types'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<LoginUserVO | null>(null)
  const hydrated = ref(false)
  const loading = ref(false)

  const isAuthed = computed(() => !!user.value?.id)
  const isAdmin = computed(() => user.value?.userRole === 'admin')
  const displayName = computed(() => user.value?.userName || user.value?.userAccount || '同学')
  const initial = computed(() => displayName.value.slice(0, 1).toUpperCase())

  const hydrate = async () => {
    if (hydrated.value) return
    try {
      user.value = await authApi.me()
    } catch {
      user.value = null
    } finally {
      hydrated.value = true
    }
  }

  type LoginPayload = { userAccount: string; userPassword: string }
  type RegisterPayload = { userAccount: string; userPassword: string; checkPassword: string; userName?: string }

  /** 支持 login({userAccount, userPassword}) 或 login(account, password) 两种调用方式 */
  const login = async (
    params: LoginPayload | string,
    password?: string,
  ) => {
    const payload: LoginPayload =
      typeof params === 'string' ? { userAccount: params, userPassword: password || '' } : params
    loading.value = true
    try {
      user.value = await authApi.login(payload)
      hydrated.value = true
      return user.value
    } finally {
      loading.value = false
    }
  }

  const register = async (
    params: RegisterPayload | string,
    password?: string,
    checkPassword?: string,
    userName?: string,
  ) => {
    const payload: RegisterPayload =
      typeof params === 'string'
        ? { userAccount: params, userPassword: password || '', checkPassword: checkPassword || password || '', userName }
        : params
    await authApi.register({
      userAccount: payload.userAccount,
      userPassword: payload.userPassword,
      checkPassword: payload.checkPassword,
    })
    await login({ userAccount: payload.userAccount, userPassword: payload.userPassword })
    if (payload.userName) {
      try {
        await updateMy({ userName: payload.userName })
      } catch {
        /* 昵称设置失败不影响注册流程 */
      }
    }
  }

  const logout = async () => {
    try {
      await authApi.logout()
    } finally {
      user.value = null
    }
  }

  const updateMy = async (payload: Record<string, unknown>) => {
    await authApi.updateMy(payload)
    user.value = await authApi.me()
  }

  return { user, hydrated, loading, isAuthed, isAdmin, displayName, initial, hydrate, login, register, logout, updateMy }
})
