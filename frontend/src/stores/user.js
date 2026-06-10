
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from '../utils/axios'

export const useUserStore = defineStore('user', () => {
  // 使用 sessionStorage 代替 localStorage，实现标签页隔离
  const token = ref(sessionStorage.getItem('token') || '')
  const user = ref(JSON.parse(sessionStorage.getItem('user') || 'null'))

  // 获取用户角色
  const role = computed(() => user.value?.role || '')

  const login = async (username, password) => {
    console.log('userStore.login 被调用，参数:', { username, password })
    const response = await axios.post('/api/auth/login', { username, password })
    console.log('登录 API 响应:', response.data)
    token.value = response.data.token
    user.value = response.data.user
    // 使用 sessionStorage 存储，实现标签页隔离
    sessionStorage.setItem('token', token.value)
    sessionStorage.setItem('user', JSON.stringify(user.value))
  }

  const register = async (data) => {
    await axios.post('/api/auth/register', data)
  }

  const logout = () => {
    token.value = ''
    user.value = null
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('user')
  }

  // 更新用户信息（用于多端同步）
  const updateUser = (newUserData) => {
    if (newUserData) {
      user.value = newUserData
      sessionStorage.setItem('user', JSON.stringify(newUserData))
    }
  }

  // 从sessionStorage重新加载用户信息
  const reloadUser = () => {
    const storedUser = sessionStorage.getItem('user')
    if (storedUser) {
      user.value = JSON.parse(storedUser)
    }
  }

  return { token, user, role, login, register, logout, updateUser, reloadUser }
})
