<template>
  <div class="order-management">
    <el-card>
      <div class="filter-bar">
        <el-select v-model="statusFilter" placeholder="状态" clearable @change="debounceLoadOrders">
          <el-option label="全部" value="" />
          <el-option label="待分配" value="PENDING_ASSIGN" />
          <el-option label="待处理" value="PENDING_TREAT" />
          <el-option label="维修中" value="IN_PROGRESS" />
          <el-option label="待确认" value="PENDING_CONFIRM" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>

        <el-select v-model="buildingFilter" placeholder="楼栋" clearable @change="debounceLoadOrders">
          <el-option label="全部" value="" />
          <el-option v-for="building in buildings" :key="building.id" :label="building.name" :value="building.name" />
        </el-select>

        <div class="date-group">
          <el-date-picker
            v-model="startDate"
            type="date"
            placeholder="开始日期"
            class="date-input"
            @change="handleDateChange"
          />
          <span class="date-separator">至</span>
          <el-date-picker
            v-model="endDate"
            type="date"
            placeholder="结束日期"
            class="date-input"
            @change="handleDateChange"
          />
        </div>

        <el-button @click="resetFilters">重置</el-button>
        <el-button type="success" @click="exportOrders">导出Excel</el-button>
        <el-button 
            type="warning" 
            @click="showBatchAssignModal" 
            :disabled="pendingOrdersCount === 0"
        >
          批量智能分配
        </el-button>
      </div>

      <div v-if="pendingOrdersCount > 0" class="ai-prompt">
        <el-alert title="AI智能派单" type="info" closable>
          <span style="margin-right: 20px;">系统检测到 <strong>{{ pendingOrdersCount }}</strong> 个待分配工单</span>
          <el-button link @click="loadAllRecommendations">查看智能推荐</el-button>
        </el-alert>
      </div>

      <el-table :data="orders" border stripe>
        <el-table-column prop="id" label="工单ID" width="100" />
        <el-table-column prop="building" label="楼栋" width="100" />
        <el-table-column prop="dormNumber" label="宿舍号" width="100" />
        <el-table-column prop="faultTypeName" label="故障类型" width="120" />
        <el-table-column prop="studentName" label="学生" width="100" />
        <el-table-column prop="repairmanName" label="维修工" width="100" />
        <el-table-column label="相关图片" width="120">
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
        <el-table-column prop="urgency" label="紧急程度" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.urgency === 'URGENT' ? 'danger' : 'info'">
              {{ scope.row.urgency === 'URGENT' ? '紧急' : '普通' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="160" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="goToDetail(scope.row.id)">详情</el-button>
            <el-button
                v-if="scope.row.status === 'PENDING_ASSIGN'"
                type="success"
                link
                @click="showSmartAssignDialog(scope.row)"
            >
              智能派单
            </el-button>
            <el-button
                v-if="scope.row.status === 'PENDING_ASSIGN'"
                type="info"
                link
                @click="showManualAssignDialog(scope.row)"
            >
              手动分配
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 智能派单对话框 -->
    <el-dialog title="智能派单推荐" v-model="showSmartAssignModal" width="600px">
      <div v-if="recommendations.length === 0" class="empty-state">
        <el-icon size="48" color="#ccc"><User /></el-icon>
        <p>暂无可用的维修工推荐</p>
      </div>

      <div v-else>
        <div class="recommend-header">
          <span class="title">工单信息</span>
          <span class="order-info">{{ currentOrder?.building }} {{ currentOrder?.dormNumber }} - {{ currentOrder?.faultTypeName }}</span>
        </div>

        <el-table :data="recommendations" border>
          <el-table-column label="排名" width="60">
            <template #default="scope">
              <span v-if="scope.row.isBest" class="best-badge">最佳</span>
              <span v-else>{{ scope.$index + 1 }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="realName" label="维修工" width="100" />
          <el-table-column prop="totalScore" label="综合评分" width="100">
            <template #default="scope">
              <el-rate :model-value="Math.round(scope.row.totalScore / 20)" disabled size="small" />
              <span class="score-value">{{ scope.row.totalScore }}</span>
            </template>
          </el-table-column>
          <el-table-column label="技能匹配" width="100">
            <template #default="scope">
              <el-progress :percentage="Math.round(scope.row.skillScore * 100)" :color="getScoreColor(scope.row.skillScore)" :show-text="false" />
            </template>
          </el-table-column>
          <el-table-column label="负载均衡" width="100">
            <template #default="scope">
              <el-progress :percentage="Math.round(scope.row.loadScore * 100)" :color="getScoreColor(scope.row.loadScore)" :show-text="false" />
            </template>
          </el-table-column>
          <el-table-column prop="currentLoad" label="当前负载" width="100" />
          <el-table-column prop="recommendReason" label="推荐理由" width="150" />
          <el-table-column label="操作" width="80">
            <template #default="scope">
              <el-button type="primary" size="small" @click="confirmAssign(scope.row)">选择</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="predict-section">
          <el-card>
            <div class="predict-title">
              <el-icon><Clock /></el-icon>
              预计完成时间
            </div>
            <div class="predict-content">
              <span class="predict-time">{{ timePredict?.predictedTimeText }}</span>
              <span class="predict-confidence" :class="timePredict?.confidenceLevel">
                置信度：{{ timePredict?.confidenceLevel }} ({{ timePredict?.confidenceScore }}%)
              </span>
            </div>
            <p class="predict-explanation">{{ timePredict?.explanation }}</p>
          </el-card>
        </div>
      </div>

      <template #footer>
        <el-button @click="showSmartAssignModal = false">取消</el-button>
        <el-button 
            type="primary" 
            @click="autoAssignBest"
            :disabled="!bestRecommendation"
        >
          一键分配最佳
        </el-button>
      </template>
    </el-dialog>

    <!-- 手动分配工单对话框 -->
    <el-dialog title="手动分配工单" v-model="showManualAssignModal" width="400px">
      <el-form :model="assignForm">
        <el-form-item label="选择维修工" required>
          <el-select v-model="assignForm.repairmanId" placeholder="请选择维修工" style="width: 100%">
            <el-option v-for="repairman in repairmen" :key="repairman.id" :label="repairman.realName" :value="repairman.id" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showManualAssignModal = false">取消</el-button>
        <el-button type="primary" @click="manualAssignOrder">确认分配</el-button>
      </template>
    </el-dialog>

    <!-- 批量智能分配对话框 -->
    <el-dialog title="批量智能分配" v-model="showBatchAssignModalDialog" width="600px">
      <div class="batch-assign-content">
        <p>系统将为待分配的 {{ pendingOrdersCount }} 个工单自动选择最佳维修工</p>
        
        <div v-if="batchRecommendations.length > 0" class="batch-preview">
          <el-table :data="batchRecommendations" border>
            <el-table-column prop="orderId" label="工单ID" width="100" />
            <el-table-column prop="faultTypeName" label="故障类型" width="120" />
            <el-table-column prop="repairmanName" label="推荐维修工" width="120" />
            <el-table-column prop="totalScore" label="匹配度" width="100" />
          </el-table>
        </div>

        <div v-else class="loading-state" v-loading="true" element-loading-text="正在计算推荐...">
        </div>
      </div>

      <template #footer>
        <el-button @click="showBatchAssignModalDialog = false">取消</el-button>
        <el-button 
            type="primary" 
            @click="confirmBatchAssign"
            :disabled="batchRecommendations.length === 0"
        >
          确认批量分配
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from '../../utils/axios'
import { ElMessage } from 'element-plus'
import { User, Clock } from '@element-plus/icons-vue'

const router = useRouter()

// 定时刷新定时器
let refreshTimer = null

const orders = ref([])
const buildings = ref([])
const repairmen = ref([])

const statusFilter = ref('')
const buildingFilter = ref('')
const startDate = ref('')
const endDate = ref('')

const showSmartAssignModal = ref(false)
const showManualAssignModal = ref(false)
const showBatchAssignModalDialog = ref(false)
const currentOrderId = ref(null)
const currentOrder = ref(null)

const recommendations = ref([])
const timePredict = ref(null)
const batchRecommendations = ref([])

const assignForm = ref({
  repairmanId: ''
})

const pendingOrdersCount = computed(() => {
  return orders.value.filter(o => o.status === 'PENDING_ASSIGN').length
})

const bestRecommendation = computed(() => {
  return recommendations.value.find(r => r.isBest)
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
    'PENDING_CONFIRM': 'info',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  }
  return map[status] || 'info'
}

// 获取分数颜色
const getScoreColor = (score) => {
  if (score >= 0.8) return '#67c23a'
  if (score >= 0.5) return '#e6a23c'
  return '#f56c6c'
}

// 跳转到详情页
const goToDetail = (id) => {
  router.push(`/admin/order-detail/${id}`)
}

// 显示智能派单对话框
const showSmartAssignDialog = async (order) => {
  currentOrderId.value = order.id
  currentOrder.value = order
  showSmartAssignModal.value = true
  
  await loadRecommendations(order.id)
  await loadTimePredict(order.id)
}

// 显示手动分配对话框
const showManualAssignDialog = (order) => {
  currentOrderId.value = order.id
  assignForm.value.repairmanId = ''
  showManualAssignModal.value = true
}

// 显示批量分配对话框
const showBatchAssignModal = async () => {
  showBatchAssignModalDialog.value = true
  batchRecommendations.value = []
  
  const pendingOrders = orders.value.filter(o => o.status === 'PENDING_ASSIGN')
  const orderIds = pendingOrders.map(o => o.id)
  
  if (orderIds.length > 0) {
    await loadBatchRecommendations(orderIds)
  }
}

// 加载推荐列表
const loadRecommendations = async (orderId) => {
  try {
    const response = await axios.get(`/api/ai/recommend/${orderId}`)
    recommendations.value = response.data
  } catch (error) {
    console.error('加载推荐失败', error)
    recommendations.value = []
  }
}

// 加载时间预测
const loadTimePredict = async (orderId) => {
  try {
    const response = await axios.get(`/api/ai/predict/time/${orderId}`)
    timePredict.value = response.data
  } catch (error) {
    console.error('加载时间预测失败', error)
    timePredict.value = null
  }
}

// 加载批量推荐
const loadBatchRecommendations = async (orderIds) => {
  try {
    const response = await axios.post('/api/ai/recommend/batch', orderIds)
    batchRecommendations.value = response.data.map(r => ({
      orderId: r.orderId,
      faultTypeName: orders.value.find(o => o.id === r.orderId)?.faultTypeName,
      repairmanName: r.realName,
      totalScore: r.totalScore
    }))
  } catch (error) {
    console.error('加载批量推荐失败', error)
    batchRecommendations.value = []
  }
}

// 确认分配给指定维修工
const confirmAssign = async (recommend) => {
  try {
    await axios.post(`/api/admin/orders/${currentOrderId.value}/assign`, {
      repairmanId: recommend.repairmanId
    })

    ElMessage.success('分配成功')
    showSmartAssignModal.value = false
    loadOrders()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '分配失败')
  }
}

// 一键分配最佳推荐
const autoAssignBest = async () => {
  if (!bestRecommendation.value) return

  try {
    await axios.post(`/api/ai/assign/auto/${currentOrderId.value}`)
    ElMessage.success('智能分配成功')
    showSmartAssignModal.value = false
    loadOrders()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '分配失败')
  }
}

// 手动分配工单
const manualAssignOrder = async () => {
  if (!assignForm.value.repairmanId) {
    ElMessage.error('请选择维修工')
    return
  }

  try {
    await axios.post(`/api/admin/orders/${currentOrderId.value}/assign`, {
      repairmanId: assignForm.value.repairmanId
    })

    ElMessage.success('分配成功')
    showManualAssignModal.value = false
    loadOrders()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '分配失败')
  }
}

// 确认批量分配
const confirmBatchAssign = async () => {
  const pendingOrders = orders.value.filter(o => o.status === 'PENDING_ASSIGN')
  const orderIds = pendingOrders.map(o => o.id)

  try {
    await axios.post('/api/ai/assign/batch', orderIds)
    ElMessage.success('批量分配成功')
    showBatchAssignModalDialog.value = false
    loadOrders()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '批量分配失败')
  }
}

// 查看所有推荐
const loadAllRecommendations = () => {
  const pendingOrders = orders.value.filter(o => o.status === 'PENDING_ASSIGN')
  if (pendingOrders.length > 0) {
    showSmartAssignDialog(pendingOrders[0])
  }
}

// 导出Excel
const exportOrders = async () => {
  try {
    const params = {}
    if (statusFilter.value) params.status = statusFilter.value
    if (buildingFilter.value) params.building = buildingFilter.value

    const response = await axios.get('/api/admin/orders/export', {
      params,
      responseType: 'blob'
    })

    const blob = new Blob([response.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    const date = new Date().toLocaleDateString('zh-CN').replace(/\//g, '-')
    a.download = `工单列表_${date}.xlsx`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)

    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败', error)
    ElMessage.error('导出失败')
  }
}

// 加载工单列表
const loadOrders = async () => {
  try {
    const params = {}
    if (statusFilter.value) params.status = statusFilter.value
    if (buildingFilter.value) params.building = buildingFilter.value
    if (startDate.value) params.startDate = formatDate(startDate.value)
    if (endDate.value) params.endDate = formatDate(endDate.value)

    const response = await axios.get('/api/admin/orders', { params })
    orders.value = response.data
  } catch (error) {
    console.error('加载工单失败', error)
    ElMessage.error('加载工单失败')
  }
}

// 防抖函数
const debounce = (fn, delay) => {
  let timer = null
  return function (...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => fn(...args), delay)
  }
}

// 防抖加载工单列表
const debounceLoadOrders = debounce(loadOrders, 300)

// 日期变化处理（带校验）
const handleDateChange = () => {
  if (startDate.value && endDate.value) {
    const start = new Date(startDate.value)
    const end = new Date(endDate.value)
    if (end < start) {
      ElMessage.error('结束日期不能早于开始日期')
      endDate.value = ''
    }
  }
  debounceLoadOrders()
}

// 格式化日期
const formatDate = (date) => {
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

// 重置筛选条件
const resetFilters = () => {
  statusFilter.value = ''
  buildingFilter.value = ''
  startDate.value = ''
  endDate.value = ''
  debounceLoadOrders()
}

// 加载楼栋列表
const loadBuildings = async () => {
  try {
    const response = await axios.get('/api/admin/buildings')
    const data = response.data || []
    // 使用 Map 去重，保持原始顺序
    const uniqueData = [...new Map(data.map(item => [item.id, item])).values()]
    buildings.value = uniqueData
  } catch (error) {
    console.error('加载楼栋失败', error)
  }
}

// 加载维修工列表
const loadRepairmen = async () => {
  try {
    const response = await axios.get('/api/admin/repairmen')
    repairmen.value = response.data
  } catch (error) {
    console.error('加载维修工失败', error)
  }
}

// 处理工单更新事件
const handleOrderUpdated = (event) => {
  console.log('收到工单更新事件:', event.detail)
  loadOrders()
}

onMounted(() => {
  loadOrders()
  loadBuildings()
  loadRepairmen()
  
  // 监听WebSocket推送的工单更新事件
  window.addEventListener('orderUpdated', handleOrderUpdated)
})

onUnmounted(() => {
  // 清理事件监听
  window.removeEventListener('orderUpdated', handleOrderUpdated)
})
</script>

<style scoped>
.order-management {
  padding: 20px;
}

.filter-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.filter-bar .el-select {
  width: 150px;
}

.date-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.date-input {
  width: 140px;
}

.date-separator {
  color: #999;
}

.ai-prompt {
  margin-bottom: 20px;
}

.recommend-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.recommend-header .title {
  font-weight: bold;
  font-size: 14px;
}

.recommend-header .order-info {
  color: #666;
  font-size: 13px;
}

.best-badge {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.score-value {
  margin-left: 8px;
  font-weight: bold;
  color: #667eea;
}

.predict-section {
  margin-top: 20px;
}

.predict-title {
  font-weight: bold;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.predict-content {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 10px;
}

.predict-time {
  font-size: 24px;
  font-weight: bold;
  color: #667eea;
}

.predict-confidence {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 13px;
}

.predict-confidence.高 {
  background-color: #f0f9eb;
  color: #67c23a;
}

.predict-confidence.中 {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.predict-confidence.低 {
  background-color: #fef0f0;
  color: #f56c6c;
}

.predict-explanation {
  color: #999;
  font-size: 13px;
  margin: 0;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
}

.batch-preview {
  margin-top: 15px;
}

.thumbnail-wrapper {
  position: relative;
  display: inline-block;
}

.thumbnail-image {
  width: 40px;
  height: 40px;
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
  max-width: 350px;
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
  width: 80px;
  height: 80px;
  border-radius: 4px;
}

.no-image {
  color: #999;
  font-size: 12px;
}
</style>