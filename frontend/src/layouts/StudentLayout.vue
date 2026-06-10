<template>
  <el-container class="student-layout">
    <el-header class="header">
      <div class="header-left">
        <span class="title">宿舍报修管理系统</span>
      </div>
      <div class="header-right">
        <span class="user-info">{{ userStore.user?.realName }} | {{ userStore.user?.building }} {{ userStore.user?.dormNumber }}</span>
        <el-button class="header-btn" @click="showPasswordDialog">修改密码</el-button>
        <el-button class="header-btn" @click="handleLogout">退出登录</el-button>
      </div>
    </el-header>

    <el-container>
      <el-aside class="aside" width="200px">
        <el-menu :default-active="activeMenu" class="el-menu-vertical-demo" @select="handleMenuSelect">
          <el-menu-item index="dashboard">
            <el-icon><House /></el-icon>
            <span>首页看板</span>
          </el-menu-item>
          <el-menu-item index="repair">
            <el-icon><Tools /></el-icon>
            <span>提交报修</span>
          </el-menu-item>
          <el-menu-item index="history">
            <el-icon><Document /></el-icon>
            <span>报修历史</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="400px">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword">确认修改</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { House, Tools, Document } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from '../utils/axios'
import webSocketService from '../utils/websocket'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

let subscriptionId = null
let queueSubscriptionId = null

// WebSocket消息处理
const handleMessage = (message) => {
  console.log('学生端收到WebSocket消息:', message)
  
  switch (message.type) {
    case 'STATUS_CHANGE':
      if (message.status === 'COMPLETED') {
        ElMessage.success('您的报修工单已完成！')
      } else if (message.status === 'IN_PROGRESS') {
        ElMessage.info('维修工已开始处理您的工单')
      } else if (message.status === 'PENDING_TREAT') {
        ElMessage.info('您的工单已分配给维修工')
      }
      // 触发全局事件通知工单列表刷新
      window.dispatchEvent(new CustomEvent('studentOrderUpdated', { detail: message }))
      break
    case 'ORDER_ASSIGNED':
      ElMessage.info('您的工单已分配给维修工')
      window.dispatchEvent(new CustomEvent('studentOrderUpdated', { detail: message }))
      break
    case 'NEW_ORDER':
      ElMessage.success('工单提交成功！')
      window.dispatchEvent(new CustomEvent('studentOrderUpdated', { detail: message }))
      break
    default:
      console.log('未知消息类型:', message.type)
  }
}

// 连接WebSocket
const connectWebSocket = async () => {
  // 使用 sessionStorage 获取 token
  const token = sessionStorage.getItem('token')
  if (!token) return
  
  try {
    await webSocketService.connect({
      token: token,
      onConnected: () => {
        console.log('学生端WebSocket连接成功')
      },
      onDisconnected: () => {
        console.log('学生端WebSocket连接断开')
      },
      onError: (error) => {
        console.error('学生端WebSocket连接错误:', error)
      }
    })
    
    // 订阅学生广播消息
    subscriptionId = await webSocketService.subscribeStudentOrders(handleMessage)
    console.log('学生端广播订阅成功，ID:', subscriptionId)
    
    // 订阅特定学生的消息队列（精确推送）
    const userId = userStore.user?.id
    if (userId) {
      queueSubscriptionId = await webSocketService.subscribeStudentQueue(userId, handleMessage)
      console.log('学生端队列订阅成功，ID:', queueSubscriptionId)
    }
  } catch (error) {
    console.error('WebSocket连接失败:', error)
  }
}

onMounted(() => {
  connectWebSocket()
})

onUnmounted(() => {
  // 取消订阅
  if (subscriptionId) {
    webSocketService.unsubscribe(subscriptionId)
  }
  if (queueSubscriptionId) {
    webSocketService.unsubscribe(queueSubscriptionId)
  }
})

const activeMenu = computed(() => {
  return route.name?.replace('Student', '').toLowerCase() || 'dashboard'
})

const handleMenuSelect = (index) => {
  router.push(`/student/${index}`)
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

// 修改密码相关
const passwordDialogVisible = ref(false)
const passwordFormRef = ref(null)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.value.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const showPasswordDialog = () => {
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  passwordDialogVisible.value = true
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await axios.put('/api/student/password', {
          oldPassword: passwordForm.value.oldPassword,
          newPassword: passwordForm.value.newPassword
        })
        ElMessage.success('密码修改成功')
        passwordDialogVisible.value = false
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '密码修改失败')
      }
    }
  })
}
</script>

<style scoped>
.student-layout {
  height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.header-left .title {
  font-size: 18px;
  font-weight: bold;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  font-size: 14px;
}

.header-btn {
  background: white;
  color: #667eea;
  border: none;
  font-size: 14px;
}

.header-btn:hover {
  background: #f0f5ff;
  color: #667eea;
}

.aside {
  background: #f5f5f5;
  overflow-y: auto;
}

.main {
  padding: 20px;
  background: #fafafa;
  overflow-y: auto;
}

/* 菜单样式优化 */
.el-menu-vertical-demo:not(.el-menu--collapse) {
  width: 200px;
  min-height: calc(100vh - 60px);
  border-right: none;
}

.el-menu-item {
  margin: 4px 8px;
  border-radius: 8px;
}

.el-menu-item:hover {
  background-color: #e8e8e8;
}

.el-menu-item.is-active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.el-menu-item.is-active .el-icon {
  color: white;
}
</style>