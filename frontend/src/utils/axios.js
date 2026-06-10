
import axios from 'axios'
import { ElMessage } from 'element-plus'

const instance = axios.create({
  baseURL: '',
  timeout: 30000
})

instance.interceptors.request.use(
  (config) => {
    // 使用 sessionStorage 获取 token，实现标签页隔离
    const token = sessionStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

instance.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    if (error.response?.status === 401) {
      // 清除 sessionStorage
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('user')
      // 延迟跳转，确保状态已更新
      setTimeout(() => {
        window.location.href = '/login'
      }, 100)
    } else {
      ElMessage.error(error.response?.data?.message || '请求失败')
    }
    return Promise.reject(error)
  }
)

export default instance
