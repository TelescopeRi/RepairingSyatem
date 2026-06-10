<template>
  <div class="repair-history">
    <!-- 页面标题区域 -->
    <div class="page-header">
      <div class="header-content">
        <h2>我的报修记录</h2>
        <p class="subtitle">查看和管理您的所有报修工单</p>
      </div>
      <el-button type="primary" @click="goToRepair" class="new-repair-btn">
        <el-icon><Plus /></el-icon>
        新建报修
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="statistics-cards">
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <el-icon class="stat-icon pending"><Clock /></el-icon>
          <div class="stat-info">
            <span class="stat-number">{{ pendingCount }}</span>
            <span class="stat-label">待处理</span>
          </div>
        </div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <el-icon class="stat-icon processing"><Tools /></el-icon>
          <div class="stat-info">
            <span class="stat-number">{{ processingCount }}</span>
            <span class="stat-label">处理中</span>
          </div>
        </div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <el-icon class="stat-icon completed"><CircleCheck /></el-icon>
          <div class="stat-info">
            <span class="stat-number">{{ completedCount }}</span>
            <span class="stat-label">已完成</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 筛选区域 -->
    <el-card class="filter-card">
      <div class="filter-bar">
        <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width: 140px">
          <el-option label="全部状态" value="" />
          <el-option label="待分配" value="PENDING_ASSIGN" />
          <el-option label="待处理" value="PENDING_TREAT" />
          <el-option label="维修中" value="IN_PROGRESS" />
          <el-option label="待确认" value="PENDING_CONFIRM" />
          <el-option label="已完成" value="COMPLETED" />
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
      <el-table :data="paginatedOrders" border stripe :row-class-name="tableRowClassName">
        <el-table-column prop="id" label="工单号" width="90" align="center" />
        <el-table-column prop="building" label="楼栋" width="100" align="center" />
        <el-table-column prop="dormNumber" label="宿舍号" width="100" align="center" />
        <el-table-column prop="faultTypeName" label="故障类型" width="120" align="center" />
        <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
        <el-table-column label="相关图片" width="100" align="center">
          <template #default="scope">
            <el-popover v-if="(scope.row.images && scope.row.images.length > 0) || (scope.row.repairImages && scope.row.repairImages.length > 0)" trigger="hover" placement="top-start">
              <div class="popover-images-container">
                <!-- 报修图片 -->
                <div v-if="scope.row.images && scope.row.images.length > 0" class="image-group">
                  <span class="image-group-label">报修图片</span>
                  <div class="popover-images">
                    <el-image
                        v-for="(img, index) in scope.row.images.slice(0, 5)"
                        :key="'r-' + index"
                        :src="img"
                        :preview-src-list="scope.row.images"
                        fit="cover"
                        class="popover-image"
                    />
                  </div>
                </div>
                <!-- 维修完成图片 -->
                <div v-if="scope.row.repairImages && scope.row.repairImages.length > 0" class="image-group">
                  <span class="image-group-label">维修完成图片</span>
                  <div class="popover-images">
                    <el-image
                        v-for="(img, index) in scope.row.repairImages.slice(0, 5)"
                        :key="'c-' + index"
                        :src="img"
                        :preview-src-list="scope.row.repairImages"
                        fit="cover"
                        class="popover-image"
                    />
                  </div>
                </div>
              </div>
              <template #reference>
                <div class="thumbnail-wrapper">
                  <el-image
                      v-if="scope.row.images && scope.row.images.length > 0"
                      :src="scope.row.images[0]"
                      class="thumbnail-image"
                      fit="cover"
                  />
                  <el-image
                      v-else-if="scope.row.repairImages && scope.row.repairImages.length > 0"
                      :src="scope.row.repairImages[0]"
                      class="thumbnail-image"
                      fit="cover"
                  />
                  <span v-if="scope.row.repairImages && scope.row.repairImages.length > 0" class="repair-badge">已完成</span>
                </div>
              </template>
            </el-popover>
            <span v-else class="no-image">-</span>
          </template>
        </el-table-column>
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
            <template v-else-if="scope.row.status === 'PENDING_CONFIRM' || scope.row.status === 'COMPLETED'">
              <el-tag type="warning" size="small">待评价</el-tag>
            </template>
            <template v-else>
              <span class="no-evaluation">-</span>
            </template>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="160" align="center" />
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" link @click="goToDetail(scope.row.id)" size="small">
              详情
            </el-button>
            <el-button
                v-if="(scope.row.status === 'PENDING_CONFIRM' || scope.row.status === 'COMPLETED') && !scope.row.evaluation"
                type="warning"
                link
                @click="showRatingDialog(scope.row)"
                size="small"
            >
              评价
            </el-button>
            <el-button
                v-if="scope.row.status === 'PENDING_ASSIGN'"
                type="danger"
                link
                @click="cancelOrder(scope.row.id)"
                size="small"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <div v-if="filteredOrders.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无报修记录">
          <el-button type="primary" @click="goToRepair">立即报修</el-button>
        </el-empty>
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

    <!-- 取消原因对话框 -->
    <el-dialog title="取消报修" v-model="cancelDialogVisible" width="450px" :close-on-click-modal="false">
      <el-form :model="cancelForm" label-width="80px">
        <el-form-item label="取消原因" required>
          <el-input
              v-model="cancelForm.reason"
              type="textarea"
              :rows="3"
              placeholder="请填写取消原因，以便我们改进服务"
              maxlength="200"
              show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="cancelDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCancel" :loading="cancelling">
          确认取消
        </el-button>
      </template>
    </el-dialog>

    <!-- 评价对话框 -->
    <el-dialog title="评价维修服务" v-model="ratingDialogVisible" width="500px" :close-on-click-modal="false">
      <div class="rating-info" v-if="currentOrder">
        <p><strong>工单号：</strong>{{ currentOrder.id }}</p>
        <p><strong>故障类型：</strong>{{ currentOrder.faultTypeName }}</p>
        <p><strong>维修人员：</strong>{{ currentOrder.repairmanName || '未分配' }}</p>
      </div>

      <el-form :model="ratingForm" label-width="80px">
        <el-form-item label="服务评分" required>
          <div class="rating-selector">
            <el-rate v-model="ratingForm.rating" :max="5" show-text :texts="['非常差', '差', '一般', '满意', '非常满意']" />
          </div>
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input
              v-model="ratingForm.comment"
              type="textarea"
              :rows="4"
              placeholder="请分享您的维修体验（选填，最多500字）"
              maxlength="500"
              show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="ratingDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRating" :loading="submitting">
          提交评价
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import axios from '../../utils/axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Clock, Tools, CircleCheck, Search } from '@element-plus/icons-vue'

const router = useRouter()

const orders = ref([])
const loading = ref(false)
const cancelling = ref(false)
const submitting = ref(false)
const statusFilter = ref('')
const searchKeyword = ref('')
const cancelDialogVisible = ref(false)
const ratingDialogVisible = ref(false)
const currentCancelId = ref(null)
const currentOrder = ref(null)

const currentPage = ref(1)
const pageSize = ref(10)

const cancelForm = ref({
  reason: ''
})

const ratingForm = ref({
  rating: 5,
  comment: ''
})

// 统计数量
const pendingCount = computed(() => {
  return orders.value.filter(o => ['PENDING_ASSIGN', 'PENDING_TREAT'].includes(o.status)).length
})

const processingCount = computed(() => {
  return orders.value.filter(o => o.status === 'IN_PROGRESS').length
})

const completedCount = computed(() => {
  return orders.value.filter(o => ['COMPLETED', 'PENDING_CONFIRM'].includes(o.status)).length
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

// 表格行样式
const tableRowClassName = ({ row }) => {
  if (row.urgency === 'URGENT') {
    return 'urgency-row'
  }
  return ''
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
  router.push(`/student/detail/${id}`)
}

// 跳转到报修页面
const goToRepair = () => {
  router.push('/student/repair')
}

// 显示评价对话框
const showRatingDialog = (order) => {
  currentOrder.value = order
  ratingForm.value = {
    rating: 5,
    comment: ''
  }
  ratingDialogVisible.value = true
}

// 提交评价
const submitRating = async () => {
  if (!ratingForm.value.rating) {
    ElMessage.error('请选择评分')
    return
  }

  submitting.value = true
  try {
    await axios.post(`/api/student/orders/${currentOrder.value.id}/evaluate`, {
      rating: ratingForm.value.rating,
      comment: ratingForm.value.comment
    })

    ElMessage.success('评价成功，感谢您的反馈！')
    ratingDialogVisible.value = false
    
    // 更新本地数据
    const orderIndex = orders.value.findIndex(o => o.id === currentOrder.value.id)
    if (orderIndex !== -1) {
      orders.value[orderIndex].evaluation = {
        rating: ratingForm.value.rating,
        comment: ratingForm.value.comment,
        createTime: new Date().toLocaleString('zh-CN')
      }
      orders.value[orderIndex].status = 'COMPLETED'
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '评价失败')
  } finally {
    submitting.value = false
  }
}

// 取消工单（显示对话框）
const cancelOrder = (id) => {
  currentCancelId.value = id
  cancelForm.value.reason = ''
  cancelDialogVisible.value = true
}

// 确认取消
const confirmCancel = async () => {
  if (!cancelForm.value.reason) {
    ElMessage.error('请填写取消原因')
    return
  }

  cancelling.value = true
  try {
    await axios.post(`/api/student/orders/${currentCancelId.value}/cancel`, {
      reason: cancelForm.value.reason
    })

    ElMessage.success('取消成功')
    cancelDialogVisible.value = false
    cancelForm.value.reason = ''
    currentCancelId.value = null
    loadOrders()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '取消失败')
  } finally {
    cancelling.value = false
  }
}

// 加载工单列表
const loadOrders = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/student/orders')
    orders.value = response.data || []
    currentPage.value = 1
  } catch (error) {
    console.error('加载工单失败', error)
    ElMessage.error('加载工单失败')
  } finally {
    loading.value = false
  }
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (page) => {
  currentPage.value = page
}

// 监听筛选条件变化，重置分页
watch([statusFilter, searchKeyword], () => {
  currentPage.value = 1
})

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.repair-history {
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

.new-repair-btn {
  padding: 12px 24px;
  border-radius: 8px;
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

.stat-icon.pending {
  color: #409eff;
  background: #ecf5ff;
}

.stat-icon.processing {
  color: #e6a23c;
  background: #fdf6ec;
}

.stat-icon.completed {
  color: #67c23a;
  background: #f0f9eb;
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

/* 评价对话框 */
.rating-info {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.rating-info p {
  margin: 8px 0;
  color: #606266;
  font-size: 14px;
}

.rating-info strong {
  color: #303133;
}

.rating-selector {
  display: flex;
  align-items: center;
  gap: 12px;
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

:deep(.urgency-row) {
  background: #fff5f5;
}

.no-evaluation {
  color: #c0c4cc;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .repair-history {
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

  .thumbnail-wrapper {
    position: relative;
    display: inline-block;
  }

  .thumbnail-image {
    width: 36px;
    height: 36px;
    border-radius: 4px;
    cursor: pointer;
  }

  .repair-badge {
    position: absolute;
    bottom: -8px;
    right: -8px;
    background: #67c23a;
    color: white;
    font-size: 10px;
    padding: 1px 6px;
    border-radius: 10px;
  }

  .popover-images-container {
    padding: 10px;
    max-width: 320px;
  }

  .image-group {
    margin-bottom: 12px;
  }

  .image-group:last-child {
    margin-bottom: 0;
  }

  .image-group-label {
    display: block;
    font-size: 12px;
    font-weight: 600;
    color: #666;
    margin-bottom: 6px;
    padding-bottom: 4px;
    border-bottom: 1px solid #eee;
  }

  .popover-images {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .popover-image {
    width: 70px;
    height: 70px;
    border-radius: 4px;
  }

  .no-image {
    color: #999;
    font-size: 12px;
  }
}
</style>
