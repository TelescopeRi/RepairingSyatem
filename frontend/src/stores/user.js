
import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from '../utils/axios'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const login = async (username, password) => {
    console.log('userStore.login 被调用，参数:', { username, password })
    const response = await axios.post('/api/auth/login', { username, password })
    console.log('登录 API 响应:', response.data)
    token.value = response.data.token
    user.value = response.data.user
    localStorage.setItem('token', token.value)
    localStorage.setItem('user', JSON.stringify(user.value))
  }

  const register = async (data) => {
    await axios.post('/api/auth/register', data)
  }

  const logout = () => {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return { token, user, login, register, logout }
})
