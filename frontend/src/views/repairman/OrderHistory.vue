<template>
  <div class="order-history">
    <!-- 页面标题区域 -->
    <div class="page-header">
      <div class="header-content">
        <h2>历史工单</h2>
        <p class="subtitle">查看已完成和已取消的维修工单</p>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="statistics-cards">
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <el-icon class="stat-icon completed"><CircleCheck /></el-icon>
          <div class="stat-info">
            <span class="stat-number">{{ completedCount }}</span>
            <span class="stat-label">已完成</span>
          </div>
        </div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <el-icon class="stat-icon pending"><Clock /></el-icon>
          <div class="stat-info">
            <span class="stat-number">{{ pendingConfirmCount }}</span>
            <span class="stat-label">待确认</span>
          </div>
        </div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <el-icon class="stat-icon cancelled"><CircleClose /></el-icon>
          <div class="stat-info">
            <span class="stat-number">{{ cancelledCount }}</span>
            <span class="stat-label">已取消</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 筛选区域 -->
    <el-card class="filter-card">
      <div class="filter-bar">
        <el-select v-model="statusFilter" placeholder="工单状态" clearable style="width: 140px">
          <el-option label="全部" value="" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="待确认" value="PENDING_CONFIRM" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>

        <el-input
            v-model="searchKeyword"
            placeholder="搜索楼栋/宿舍号"
            clearable
            style="width: 200px"
            @clear="loadOrders"
            @keyup.enter="loadOrders"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>

        <el-button type="primary" @click="loadOrders">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
      </div>
    </el-card>

    <!-- 工单列表 -->
    <el-card v-loading="loading">
      <el-table :data="paginatedOrders" border stripe>
        <el-table-column prop="building" label="楼栋" width="100" align="center" />
        <el-table-column prop="dormNumber" label="宿舍号" width="100" align="center" />
        <el-table-column prop="faultTypeName" label="故障类型" width="120" align="center" />
        <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
        <el-table-column prop="urgency" label="紧急程度" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.urgency === 'URGENT' ? 'danger' : 'info'" size="small">
              {{ scope.row.urgency === 'URGENT' ? '紧急' : '普通' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="评分" width="120" align="center">
          <template #default="scope">
            <template v-if="scope.row.evaluation">
              <el-rate :model-value="scope.row.evaluation.rating" disabled size="small" />
            </template>
            <template v-else>
              <span class="no-evaluation">-</span>
            </template>
          </template>
        </el-table-column>
        <el-table-column prop="completeTime" label="完成时间" width="160" align="center" />
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" link @click="goToDetail(scope.row.id)" size="small">
              详情
            </el-button>
            <el-button
                v-if="scope.row.evaluation"
                type="success"
                link
                @click="showEvaluation(scope.row.evaluation)"
                size="small"
            >
              查看评价
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <div v-if="filteredOrders.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无历史工单" />
      </div>

      <!-- 分页 -->
      <div v-if="filteredOrders.length > 0" class="pagination">
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[5, 10, 20, 50]"
            :total="filteredOrders.length"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 评价详情对话框 -->
    <el-dialog title="学生评价" v-model="evaluationDialogVisible" width="500px" :close-on-click-modal="false">
      <div class="evaluation-detail" v-if="currentEvaluation">
        <div class="rating-box">
          <span class="label">评分</span>
          <el-rate :model-value="currentEvaluation.rating" :max="5" disabled show-score />
        </div>
        <div class="comment-box">
          <span class="label">评价内容</span>
          <p class="comment">{{ currentEvaluation.comment || '暂无评价内容' }}</p>
        </div>
        <div class="time-box">
          <span class="label">评价时间</span>
          <span class="time">{{ currentEvaluation.createTime }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from '../../utils/axios'
import { ElMessage } from 'element-plus'
import { CircleCheck, Clock, CircleClose, Search } from '@element-plus/icons-vue'

const router = useRouter()

const orders = ref([])
const loading = ref(false)
const statusFilter = ref('')
const searchKeyword = ref('')

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)

// 评价对话框
const evaluationDialogVisible = ref(false)
const currentEvaluation = ref(null)

// 统计数量
const completedCount = computed(() => {
  return orders.value.filter(o => o.status === 'COMPLETED').length
})

const pendingConfirmCount = computed(() => {
  return orders.value.filter(o => o.status === 'PENDING_CONFIRM').length
})

const cancelledCount = computed(() => {
  return orders.value.filter(o => o.status === 'CANCELLED').length
})

// 获取状态文本
const getStatusText = (status) => {
  const map = {
    'PENDING_ASSIGN': '待分配',
    'PENDING_TREAT': '待处理',
    'IN_PROGRESS': '维修中',
    'PENDING_CONFIRM': '待确认',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return map[status] || status
}

// 获取状态标签类型
const getStatusType = (status) => {
  const map = {
    'PENDING_ASSIGN': 'info',
    'PENDING_TREAT': 'warning',
    'IN_PROGRESS': 'primary',
    'PENDING_CONFIRM': 'warning',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  }
  return map[status] || 'info'
}

// 筛选后的工单
const filteredOrders = computed(() => {
  let result = orders.value

  // 按状态筛选
  if (statusFilter.value) {
    result = result.filter(o => o.status === statusFilter.value)
  }

  // 按关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(o =>
        o.building?.toLowerCase().includes(keyword) ||
        o.dormNumber?.toLowerCase().includes(keyword)
    )
  }

  return result
})

// 分页后的工单
const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredOrders.value.slice(start, end)
})

// 跳转到详情页
const goToDetail = (id) => {
  router.push(`/repairman/detail/${id}`)
}

// 显示评价详情
const showEvaluation = (evaluation) => {
  currentEvaluation.value = evaluation
  evaluationDialogVisible.value = true
}

// 分页大小改变
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

// 当前页改变
const handleCurrentChange = (page) => {
  currentPage.value = page
}

// 加载历史工单
const loadOrders = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/repair/orders')
    // 显示已完成、已取消和待确认的工单
    orders.value = response.data.filter(o =>
        o.status === 'COMPLETED' || o.status === 'CANCELLED' || o.status === 'PENDING_CONFIRM'
    )
    // 重置分页
    currentPage.value = 1
  } catch (error) {
    console.error('加载工单失败', error)
    ElMessage.error('加载工单失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.order-history {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.header-content h2 {
  margin: 0 0 5px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.subtitle {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

/* 统计卡片 */
.statistics-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 12px;
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  font-size: 40px;
  padding: 12px;
  border-radius: 12px;
}

.stat-icon.completed {
  color: #67c23a;
  background: #f0f9eb;
}

.stat-icon.pending {
  color: #e6a23c;
  background: #fdf6ec;
}

.stat-icon.cancelled {
  color: #f56c6c;
  background: #fef0f0;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

/* 筛选卡片 */
.filter-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

/* 空状态 */
.empty-state {
  padding: 60px 0;
}

/* 分页 */
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 评价详情样式 */
.evaluation-detail {
  padding: 10px 0;
}

.rating-box,
.comment-box,
.time-box {
  margin-bottom: 20px;
}

.rating-box {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rating-box .label,
.comment-box .label,
.time-box .label {
  font-weight: bold;
  color: #666;
  min-width: 70px;
}

.comment-box {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.comment {
  flex: 1;
  margin: 0;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  line-height: 1.6;
  color: #333;
}

.time-box {
  display: flex;
  align-items: center;
  gap: 12px;
}

.time-box .time {
  color: #999;
  font-size: 12px;
}

.no-evaluation {
  color: #c0c4cc;
}

/* 表格样式 */
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table th) {
  background: #f5f7fa;
  color: #606266;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .order-history {
    padding: 10px;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
    text-align: center;
    padding: 16px;
  }

  .statistics-cards {
    grid-template-columns: 1fr;
  }

  .filter-bar {
    flex-direction: column;
  }

  .filter-bar .el-select,
  .filter-bar .el-input {
    width: 100% !important;
  }

  :deep(.el-table) {
    font-size: 12px;
  }

  :deep(.el-table .cell) {
    padding: 8px 6px;
  }
}
</style>
