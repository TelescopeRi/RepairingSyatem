<template>
  <el-container class="admin-layout">
    <el-header class="header">
      <div class="header-left">
        <span class="title">宿舍报修管理系统</span>
      </div>
      <div class="header-right">
        <span class="user-info">{{ userStore.user?.realName }} (管理员)</span>
        <el-button class="header-btn" @click="handleLogout">退出登录</el-button>
      </div>
    </el-header>

    <el-container>
      <el-aside class="aside" width="220px">
        <el-menu :default-active="activeMenu" class="el-menu-vertical-demo" @select="handleMenuSelect">
          <el-menu-item index="statistics">
            <el-icon><PieChart /></el-icon>
            <span>统计报表</span>
          </el-menu-item>
          <el-menu-item index="orders">
            <el-icon><List /></el-icon>
            <span>工单管理</span>
          </el-menu-item>
          <el-menu-item index="students">
            <el-icon><UserFilled /></el-icon>
            <span>学生管理</span>
          </el-menu-item>
          <el-menu-item index="repairmen">
            <el-icon><User /></el-icon>
            <span>维修工管理</span>
          </el-menu-item>
          <el-menu-item index="fault-types">
            <el-icon><CollectionTag /></el-icon>
            <span>故障类型</span>
          </el-menu-item>
          <el-menu-item index="buildings">
            <el-icon><OfficeBuilding /></el-icon>
            <span>楼栋管理</span>
          </el-menu-item>
          <el-menu-item index="agent">
            <el-icon><ChatDotRound /></el-icon>
            <span>智能助手</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-main class="main">
        <router-view v-slot="{ Component }">
          <keep-alive>
            <component :is="Component" />
          </keep-alive>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { List, UserFilled, User, CollectionTag, OfficeBuilding, PieChart, ChatDotRound } from '@element-plus/icons-vue'
import webSocketService from '../utils/websocket'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

let subscriptionId = null

// WebSocket消息处理
const handleMessage = (message) => {
  console.log('管理员端收到WebSocket消息:', message)
  
  switch (message.type) {
    case 'NEW_ORDER':
      ElMessage.success('有新的报修工单！')
      // 触发全局事件通知工单列表刷新
      window.dispatchEvent(new CustomEvent('orderUpdated', { detail: { type: 'new' } }))
      break
    case 'ORDER_UPDATE':
    case 'STATUS_CHANGE':
    case 'ORDER_ASSIGNED':
      // 触发全局事件通知工单列表刷新
      window.dispatchEvent(new CustomEvent('orderUpdated', { detail: { type: 'update' } }))
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
        console.log('管理员端WebSocket连接成功')
      },
      onDisconnected: () => {
        console.log('管理员端WebSocket连接断开')
      },
      onError: (error) => {
        console.error('管理员端WebSocket连接错误:', error)
      }
    })
    
    // 订阅管理员消息
    subscriptionId = await webSocketService.subscribeAdminOrders(handleMessage)
    console.log('管理员端订阅成功，ID:', subscriptionId)
  } catch (error) {
    console.error('WebSocket连接失败:', error)
  }
}

onMounted(() => {
  connectWebSocket()
})

onUnmounted(() => {
  // 取消订阅并断开连接
  if (subscriptionId) {
    webSocketService.unsubscribe(subscriptionId)
  }
})

const activeMenu = computed(() => {
  const name = route.name?.replace('Admin', '') || 'orders'
  return name === 'OrderDetail' ? 'orders' : name.toLowerCase()
})

const handleMenuSelect = (index) => {
  router.push(`/admin/${index}`)
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  overflow: hidden;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  flex-shrink: 0;
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
  color: #f5576c;
  border: none;
  font-size: 14px;
}

.header-btn:hover {
  background: #fff5f5;
  color: #f5576c;
}

.aside {
  background: #f5f5f5;
  height: calc(100vh - 60px);
  overflow-y: auto;
  flex-shrink: 0;
}

.main {
  padding: 20px;
  background: #fafafa;
  height: calc(100vh - 60px);
  overflow-y: auto;
  box-sizing: border-box;
}

/* 菜单样式优化 */
.el-menu-vertical-demo:not(.el-menu--collapse) {
  width: 220px;
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