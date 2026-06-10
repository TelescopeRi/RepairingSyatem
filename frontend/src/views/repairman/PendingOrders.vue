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
      
      <el-form-item label="维修后照片">
        <div class="upload-area">
          <el-upload
              :action="uploadUrl"
              :file-list="completeImages"
              :limit="5"
              :on-success="handleCompleteUploadSuccess"
              :on-remove="handleCompleteUploadRemove"
              :before-upload="beforeUpload"
              list-type="picture-card"
          >
            <el-icon :size="30">
              <Plus />
            </el-icon>
          </el-upload>
        </div>
        <div class="upload-tip">可上传最多5张图片，支持jpg/png格式，单张不超过5MB</div>
      </el-form-item>
      
      <el-form-item label="维修耗时" v-if="orderDuration">
        <span class="duration-info">本次维修耗时：{{ orderDuration }}</span>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="showCompleteModal = false">取消</el-button>
      <el-button type="primary" @click="completeRepair" :loading="completing">
        完成维修
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from '../../utils/axios'
import { ElMessage } from 'element-plus'
import { Document, Plus } from '@element-plus/icons-vue'

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

// 维修完成图片上传
const completeImages = ref([])
const completeFormImages = ref([])

// 上传配置
const uploadUrl = `/api/repair/upload?token=${localStorage.getItem('token') || ''}`

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

// 上传前验证
const beforeUpload = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isImage) {
    ElMessage.error('只能上传 jpg/png 格式的图片')
    return false
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB')
    return false
  }
  return true
}

// 完成工单上传成功回调
const handleCompleteUploadSuccess = (response, file) => {
  console.log('上传响应:', response)
  // 兼容两种响应格式：{ url: 'xxx' } 和 { code: 200, data: { url: 'xxx' } }
  let url = null
  if (response.code === 200 && response.data && response.data.url) {
    url = response.data.url
  } else if (response.url) {
    url = response.url
  }
  
  if (url) {
    completeFormImages.value.push(url)
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response?.message || '上传失败')
  }
}

// 移除完成工单图片
const handleCompleteUploadRemove = (file) => {
  const url = file.response?.data?.url
  if (url) {
    const index = completeFormImages.value.indexOf(url)
    if (index > -1) {
      completeFormImages.value.splice(index, 1)
    }
  }
}

// 显示完成对话框
const showCompleteDialog = (order) => {
  currentOrderId.value = order.id
  currentOrderStartTime.value = order.startTime
  completeForm.value.remark = ''
  completeImages.value = []
  completeFormImages.value = []
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
      remark: completeForm.value.remark,
      images: completeFormImages.value
    })

    ElMessage.success('工单已完成')
    showCompleteModal.value = false
    completeForm.value.remark = ''
    completeImages.value = []
    completeFormImages.value = []
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

// 处理工单更新事件
const handleOrderUpdated = (event) => {
  console.log('修理工端收到工单更新事件:', event.detail)
  loadOrders()
}

onMounted(() => {
  loadOrders()
  
  // 监听WebSocket推送的工单更新事件
  window.addEventListener('repairmanOrderUpdated', handleOrderUpdated)
})

onUnmounted(() => {
  // 清理事件监听
  window.removeEventListener('repairmanOrderUpdated', handleOrderUpdated)
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