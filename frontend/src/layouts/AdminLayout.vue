<template>
  <el-container class="admin-layout">
    <el-header class="header">
      <div class="header-left">
        <span class="title">宿舍报修管理系统</span>
      </div>
      <div class="header-right">
        <span class="user-info">{{ userStore.user?.realName }} (管理员)</span>
        <el-button link @click="handleLogout">退出登录</el-button>
      </div>
    </el-header>

    <el-container>
      <el-aside class="aside" width="220px">
        <el-menu :default-active="activeMenu" class="el-menu-vertical-demo" @select="handleMenuSelect">
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
          <el-menu-item index="statistics">
            <el-icon><PieChart /></el-icon>
            <span>统计报表</span>
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
import { List, UserFilled, User, CollectionTag, OfficeBuilding, PieChart } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

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
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
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