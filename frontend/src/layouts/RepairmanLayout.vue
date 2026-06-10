<template>
  <el-container class="repairman-layout">
    <el-header class="header">
      <div class="header-left">
        <span class="title">宿舍报修管理系统</span>
      </div>
      <div class="header-right">
        <span class="user-info">{{ userStore.user?.realName }}</span>
        <el-button class="header-btn" @click="showPasswordDialog">修改密码</el-button>
        <el-button class="header-btn" @click="handleLogout">退出登录</el-button>
      </div>
    </el-header>

    <el-container>
      <el-aside class="aside" width="200px">
        <el-menu :default-active="activeMenu" class="el-menu-vertical-demo" @select="handleMenuSelect">
          <el-menu-item index="pending">
            <el-icon><Clock /></el-icon>
            <span>待处理工单</span>
          </el-menu-item>
          <el-menu-item index="history">
            <el-icon><Document /></el-icon>
            <span>历史工单</span>
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
import { Clock, Document } from '@element-plus/icons-vue'
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
  console.log('修理工端收到WebSocket消息:', message)
  
  switch (message.type) {
    case 'ORDER_ASSIGNED':
      ElMessage.success('有新的工单分配给您！')
      // 触发全局事件通知工单列表刷新
      window.dispatchEvent(new CustomEvent('repairmanOrderUpdated', { detail: message }))
      break
    case 'STATUS_CHANGE':
      // 状态变更也需要刷新列表
      window.dispatchEvent(new CustomEvent('repairmanOrderUpdated', { detail: message }))
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
        console.log('修理工端WebSocket连接成功')
      },
      onDisconnected: () => {
        console.log('修理工端WebSocket连接断开')
      },
      onError: (error) => {
        console.error('修理工端WebSocket连接错误:', error)
      }
    })
    
    // 订阅修理工广播消息
    subscriptionId = await webSocketService.subscribeRepairmanOrders(handleMessage)
    console.log('修理工端订阅成功，ID:', subscriptionId)
    
    // 订阅特定修理工的消息队列
    const userId = userStore.user?.id
    if (userId) {
      queueSubscriptionId = await webSocketService.subscribeRepairmanQueue(userId, handleMessage)
      console.log('修理工端队列订阅成功，ID:', queueSubscriptionId)
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
  return route.name?.replace('Repairman', '').toLowerCase() || 'pending'
})

const handleMenuSelect = (index) => {
  router.push(`/repairman/${index}`)
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
        await axios.put('/api/repair/password', {
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
.repairman-layout {
  height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
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
  color: #11998e;
  border: none;
  font-size: 14px;
}

.header-btn:hover {
  background: #f0fff5;
  color: #11998e;
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
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: white;
}

.el-menu-item.is-active .el-icon {
  color: white;
}
</style>