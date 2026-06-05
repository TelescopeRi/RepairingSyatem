<template>
  <el-container class="student-layout">
    <el-header class="header">
      <div class="header-left">
        <span class="title">宿舍报修管理系统</span>
      </div>
      <div class="header-right">
        <span class="user-info">{{ userStore.user?.realName }} | {{ userStore.user?.building }} {{ userStore.user?.dormNumber }}</span>
        <el-button link @click="handleLogout">退出登录</el-button>
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
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { House, Tools, Document } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

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