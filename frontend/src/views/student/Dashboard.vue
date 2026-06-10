<template>
  <div class="dashboard">
    <!-- 欢迎卡片 -->
    <el-card class="welcome-card">
      <div class="welcome-content">
        <h3>欢迎回来，{{ userStore.user?.realName }}同学</h3>
        <p>您的宿舍：{{ userStore.user?.building }} {{ userStore.user?.dormNumber }}</p>
      </div>
    </el-card>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <el-card class="action-card" @click="goToRepair">
        <div class="action-icon">
          <el-icon :size="40" color="#667eea">
            <Tools />
          </el-icon>
        </div>
        <div class="action-text">
          <span class="action-title">快速提交报修</span>
          <span class="action-desc">快速提交报修申请</span>
        </div>
      </el-card>

      <el-card class="action-card" @click="goToHistory">
        <div class="action-icon">
          <el-icon :size="40" color="#11998e">
            <Document />
          </el-icon>
        </div>
        <div class="action-text">
          <span class="action-title">报修记录</span>
          <span class="action-desc">查看历史报修记录</span>
        </div>
      </el-card>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <el-card class="stat-card">
        <div class="stat-icon pending">
          <el-icon :size="32">
            <Clock />
          </el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ pendingCount }}</span>
          <span class="stat-label">待处理</span>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-icon in-progress">
          <el-icon :size="32">
            <Loading />
          </el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ inProgressCount }}</span>
          <span class="stat-label">维修中</span>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-icon completed">
          <el-icon :size="32">
            <Select />
          </el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ completedCount }}</span>
          <span class="stat-label">已完成</span>
        </div>
      </el-card>
    </div>

    <!-- 公告栏 -->
    <el-card class="notice-card">
      <template #header>
        <span class="card-title">公告</span>
      </template>
      <div class="notice-content">
        <p>尊敬的同学，欢迎使用宿舍报修管理系统！</p>
        <p>报修流程：提交报修 → 管理员分配 → 维修工处理 → 评价</p>
        <p>紧急报修请拨打后勤服务热线：12345</p>
        <p>维修完成后请及时确认并给予评价，您的反馈是我们进步的动力！</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import axios from '../../utils/axios'
import { Tools, Document, Clock, Loading, CircleCheck, Select } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 定时刷新定时器
let refreshTimer = null

const orders = ref([])
const loading = ref(false)

// 统计各状态数量
const pendingCount = computed(() =>
    orders.value.filter(o => o.status === 'PENDING_ASSIGN' || o.status === 'PENDING_TREAT').length
)

const inProgressCount = computed(() =>
    orders.value.filter(o => o.status === 'IN_PROGRESS').length
)

const completedCount = computed(() =>
    orders.value.filter(o => o.status === 'COMPLETED').length
)

// 跳转到报修页面
const goToRepair = () => {
  router.push('/student/repair')
}

// 跳转到历史记录
const goToHistory = () => {
  router.push('/student/history')
}

// 跳转到详情页面
const goToDetail = (id) => {
  router.push(`/student/detail/${id}`)
}

// 跳转到确认页面
const goToConfirm = (id) => {
  router.push(`/student/confirm/${id}`)
}

// 加载工单列表
const loadOrders = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/student/orders')
    orders.value = response.data || []
  } catch (error) {
    console.error('加载工单失败', error)
  } finally {
    loading.value = false
  }
}

// 获取用户信息
const getUserInfo = () => {
  // 确保用户信息已加载
  if (!userStore.user) {
    userStore.getUserInfo()
  }
}

// 处理工单更新事件
const handleOrderUpdated = (event) => {
  console.log('学生端收到工单更新事件:', event.detail)
  loadOrders()
}

onMounted(() => {
  getUserInfo()
  loadOrders()
  
  // 监听WebSocket推送的工单更新事件
  window.addEventListener('studentOrderUpdated', handleOrderUpdated)
})

onUnmounted(() => {
  // 清理事件监听
  window.removeEventListener('studentOrderUpdated', handleOrderUpdated)
})
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 20px;
}

/* 欢迎卡片 */
.welcome-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
}

.welcome-card :deep(.el-card__body) {
  padding: 25px;
}

.welcome-content h3 {
  margin: 0 0 10px 0;
  font-size: 20px;
}

.welcome-content p {
  margin: 0;
  opacity: 0.9;
  font-size: 14px;
}

/* 快捷操作 */
.quick-actions {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.action-card {
  flex: 1;
  min-width: 200px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.action-icon {
  float: left;
  margin-right: 20px;
}

.action-text {
  display: flex;
  flex-direction: column;
}

.action-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.action-desc {
  font-size: 13px;
  color: #999;
}

/* 统计卡片 */
.stats-row {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.stat-card {
  flex: 1;
  min-width: 150px;
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 10px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon.pending {
  background: #f5f5f5;
  color: #999;
}

.stat-icon.in-progress {
  background: #fff3e0;
  color: #ff9800;
}

.stat-icon.pending-confirm {
  background: #f3e5f5;
  color: #9c27b0;
}

.stat-icon.completed {
  background: #e8f5e9;
  color: #4caf50;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 5px;
}

/* 待确认提醒卡片 */
.reminder-card {
  border-left: 4px solid #ff9800;
}

.reminder-card :deep(.el-card__header) {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff7e6;
}

.card-title {
  font-weight: bold;
  color: #ff9800;
}

.card-tip {
  font-size: 12px;
  color: #999;
}

/* 公告卡片 */
.notice-card {
  background: #f5f5f5;
}

.notice-card :deep(.el-card__header) {
  background: #e8e8e8;
}

.card-title {
  font-weight: bold;
  color: #333;
}

.notice-content p {
  margin: 8px 0;
  line-height: 1.6;
  color: #666;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dashboard {
    padding: 10px;
  }

  .quick-actions {
    flex-direction: column;
  }

  .stats-row {
    flex-direction: column;
  }

  .stat-card {
    min-width: auto;
  }

  .action-card {
    min-width: auto;
  }
}
</style>