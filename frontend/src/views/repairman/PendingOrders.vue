<template>
  <div class="pending-orders">
    <!-- 待处理工单 -->
    <el-card header="待处理工单" class="order-card">
      <el-table :data="orders" border stripe v-loading="pendingLoading">
        <el-table-column prop="building" label="楼栋" width="100" />
        <el-table-column prop="dormNumber" label="宿舍号" width="100" />
        <el-table-column prop="faultTypeName" label="故障类型" width="120" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="urgency" label="紧急程度" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.urgency === 'URGENT' ? 'danger' : 'info'">
              {{ scope.row.urgency === 'URGENT' ? '紧急' : '普通' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="160" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="startRepair(scope.row.id)">
              开始处理
            </el-button>
            <el-button type="info" size="small" link @click="goToDetail(scope.row.id)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <div v-if="orders.length === 0 && !pendingLoading" class="empty-state">
        <el-icon :size="60" color="#ccc">
          <Document />
        </el-icon>
        <p>暂无待处理工单</p>
      </div>
    </el-card>

    <!-- 进行中工单 -->
    <el-card
        v-if="inProgressOrders.length > 0"
        header="进行中工单"
        class="order-card"
    >
      <el-table :data="inProgressOrders" border stripe>
        <el-table-column prop="building" label="楼栋" width="100" />
        <el-table-column prop="dormNumber" label="宿舍号" width="100" />
        <el-table-column prop="faultTypeName" label="故障类型" width="120" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="startTime" label="开始时间" width="160" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button type="success" size="small" @click="showCompleteDialog(scope.row)">
              完成工单
            </el-button>
            <el-button type="info" size="small" link @click="goToDetail(scope.row.id)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>

  <!-- 完成工单对话框 -->
  <el-dialog title="完成工单" v-model="showCompleteModal" width="500px">
    <el-form :model="completeForm">
      <el-form-item label="维修备注" required>
        <el-input
            v-model="completeForm.remark"
            type="textarea"
            placeholder="请输入维修备注，描述维修过程和结果"
            :rows="4"
            maxlength="500"
            show-word-limit
        />
      </el-form-item>
      <el-form-item label="维修耗时" v-if="orderDuration">
        <span class="duration-info">本次维修耗时：{{ orderDuration }}</span>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="showCompleteModal = false">取消</el-button>
      <el-button type="primary" @click="completeRepair" :loading="completing">
        确认完成
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from '../../utils/axios'
import { ElMessage } from 'element-plus'
import { Document } from '@element-plus/icons-vue'

const router = useRouter()

const orders = ref([])
const inProgressOrders = ref([])
const pendingLoading = ref(false)
const completing = ref(false)
const showCompleteModal = ref(false)
const currentOrderId = ref(null)
const currentOrderStartTime = ref(null)

const completeForm = ref({
  remark: ''
})

// 计算维修耗时
const orderDuration = computed(() => {
  if (!currentOrderStartTime.value) return ''
  const start = new Date(currentOrderStartTime.value)
  const now = new Date()
  const diffMs = now - start
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
  const diffMinutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60))

  if (diffHours > 0) {
    return `${diffHours}小时${diffMinutes}分钟`
  }
  return `${diffMinutes}分钟`
})

// 跳转到详情页
const goToDetail = (id) => {
  router.push(`/repairman/detail/${id}`)
}

// 开始处理工单
const startRepair = async (id) => {
  try {
    await axios.post(`/api/repair/orders/${id}/start`)
    ElMessage.success('已开始处理')
    loadOrders()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

// 显示完成对话框
const showCompleteDialog = (order) => {
  currentOrderId.value = order.id
  currentOrderStartTime.value = order.startTime
  completeForm.value.remark = ''
  showCompleteModal.value = true
}

// 完成工单
const completeRepair = async () => {
  if (!completeForm.value.remark) {
    ElMessage.error('请填写维修备注')
    return
  }

  completing.value = true
  try {
    await axios.post(`/api/repair/orders/${currentOrderId.value}/complete`, {
      remark: completeForm.value.remark
    })

    ElMessage.success('工单已完成')
    showCompleteModal.value = false
    completeForm.value.remark = ''
    currentOrderId.value = null
    currentOrderStartTime.value = null
    loadOrders()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    completing.value = false
  }
}

// 加载工单列表
const loadOrders = async () => {
  pendingLoading.value = true
  try {
    // 加载待处理工单（状态为 PENDING_TREAT）
    const pendingResponse = await axios.get('/api/repair/orders/pending')
    orders.value = pendingResponse.data || []

    // 加载所有工单，筛选出进行中的
    const allResponse = await axios.get('/api/repair/orders')
    inProgressOrders.value = (allResponse.data || []).filter(o => o.status === 'IN_PROGRESS')
  } catch (error) {
    console.error('加载工单失败', error)
    ElMessage.error('加载工单失败')
  } finally {
    pendingLoading.value = false
  }
}

// 定时刷新（每30秒自动刷新一次进行中的工单）
let refreshTimer = null

const startAutoRefresh = () => {
  refreshTimer = setInterval(() => {
    if (inProgressOrders.value.length > 0) {
      loadOrders()
    }
  }, 30000) // 30秒刷新一次
}

onMounted(() => {
  loadOrders()
  startAutoRefresh()
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.pending-orders {
  padding: 20px;
}

.order-card {
  margin-bottom: 20px;
}

.order-card:last-child {
  margin-bottom: 0;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: #999;
}

.empty-state p {
  margin-top: 15px;
  font-size: 14px;
}

.duration-info {
  color: #409EFF;
  font-weight: bold;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .pending-orders {
    padding: 10px;
  }

  :deep(.el-card__header) {
    padding: 12px 15px;
  }

  :deep(.el-table) {
    font-size: 12px;
  }

  :deep(.el-table .cell) {
    padding: 8px 6px;
  }

  :deep(.el-button--small) {
    padding: 5px 8px;
  }
}
</style>