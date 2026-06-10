<template>
  <div class="order-detail">
    <el-card v-if="order">
      <div class="detail-header">
        <h3>工单详情</h3>
        <el-tag :type="getStatusType(order.status)" size="large">
          {{ getStatusText(order.status) }}
        </el-tag>
      </div>

      <div class="detail-content">
        <div class="info-row">
          <span class="label">工单编号：</span>
          <span class="value">{{ order.id }}</span>
        </div>

        <div class="info-row">
          <span class="label">楼栋：</span>
          <span class="value">{{ order.building }}</span>
        </div>

        <div class="info-row">
          <span class="label">宿舍号：</span>
          <span class="value">{{ order.dormNumber }}</span>
        </div>

        <div class="info-row">
          <span class="label">故障类型：</span>
          <span class="value">{{ order.faultTypeName }}</span>
        </div>

        <div class="info-row">
          <span class="label">紧急程度：</span>
          <span class="value">
            <el-tag :type="order.urgency === 'URGENT' ? 'danger' : 'info'">
              {{ order.urgency === 'URGENT' ? '紧急' : '普通' }}
            </el-tag>
          </span>
        </div>

        <div class="info-row">
          <span class="label">故障描述：</span>
          <span class="value">{{ order.description }}</span>
        </div>

        <div class="info-row" v-if="order.images && order.images.length > 0">
          <span class="label">报修图片：</span>
          <div class="images-container">
            <el-image
                v-for="(img, index) in order.images"
                :key="index"
                :src="img"
                :preview-src-list="order.images"
                class="preview-image"
            />
          </div>
        </div>

        <div class="info-row" v-if="order.repairImages && order.repairImages.length > 0">
          <span class="label">维修完成图片：</span>
          <div class="images-container">
            <el-image
                v-for="(img, index) in order.repairImages"
                :key="index"
                :src="img"
                :preview-src-list="order.repairImages"
                class="preview-image"
            />
          </div>
        </div>

        <div class="info-row">
          <span class="label">学生姓名：</span>
          <span class="value">{{ order.studentName }}</span>
        </div>

        <div class="info-row" v-if="order.repairmanName">
          <span class="label">维修人员：</span>
          <span class="value">{{ order.repairmanName }}</span>
        </div>

        <div class="info-row" v-if="order.remark">
          <span class="label">维修备注：</span>
          <span class="value">{{ order.remark }}</span>
        </div>

        <div class="info-row">
          <span class="label">提交时间：</span>
          <span class="value">{{ order.submitTime }}</span>
        </div>

        <div class="info-row" v-if="order.assignTime">
          <span class="label">分配时间：</span>
          <span class="value">{{ order.assignTime }}</span>
        </div>

        <div class="info-row" v-if="order.startTime">
          <span class="label">开始时间：</span>
          <span class="value">{{ order.startTime }}</span>
        </div>

        <div class="info-row" v-if="order.completeTime">
          <span class="label">完成时间：</span>
          <span class="value">{{ order.completeTime }}</span>
        </div>

        <div class="info-row" v-if="order.cancelReason">
          <span class="label">取消原因：</span>
          <span class="value">{{ order.cancelReason }}</span>
        </div>
      </div>

      <div class="evaluation-section" v-if="order.evaluation">
        <h4>评价信息</h4>
        <div class="evaluation-content">
          <el-rate :model-value="order.evaluation.rating" :max="5" disabled />
          <p>{{ order.evaluation.comment }}</p>
          <span class="evaluation-time">评价时间：{{ order.evaluation.createTime }}</span>
        </div>
      </div>

      <div class="action-buttons">
        <el-button @click="goBack">返回列表</el-button>
        <el-button
            v-if="order.status === 'PENDING_ASSIGN'"
            type="primary"
            @click="showAssignDialog"
        >
          分配工单
        </el-button>
      </div>
    </el-card>

    <!-- 分配工单对话框 -->
    <el-dialog title="分配工单" v-model="showAssignModal" width="400px">
      <el-form :model="assignForm">
        <el-form-item label="选择维修工" required>
          <el-select v-model="assignForm.repairmanId" placeholder="请选择维修工">
            <el-option v-for="repairman in repairmen" :key="repairman.id" :label="repairman.realName" :value="repairman.id" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showAssignModal = false">取消</el-button>
        <el-button type="primary" @click="assignOrder">确认分配</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from '../../utils/axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

const order = ref(null)
const repairmen = ref([])
const showAssignModal = ref(false)

const assignForm = ref({
  repairmanId: ''
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

// 加载工单详情
const loadOrder = async () => {
  try {
    const response = await axios.get(`/api/admin/orders/${route.params.id}`)
    order.value = response.data
  } catch (error) {
    console.error('加载工单详情失败', error)
    ElMessage.error('加载工单详情失败')
  }
}

// 加载维修人员列表
const loadRepairmen = async () => {
  try {
    const response = await axios.get('/api/admin/repairmen')
    repairmen.value = response.data
  } catch (error) {
    console.error('加载维修工失败', error)
  }
}

// 显示分配对话框
const showAssignDialog = () => {
  assignForm.value.repairmanId = ''
  showAssignModal.value = true
}

// 分配工单
const assignOrder = async () => {
  if (!assignForm.value.repairmanId) {
    ElMessage.error('请选择维修工')
    return
  }

  try {
    await axios.post('/api/admin/orders/assign', {
      orderId: order.value.id,
      repairmanId: assignForm.value.repairmanId
    })

    ElMessage.success('分配成功')
    showAssignModal.value = false
    // 重新加载工单详情
    await loadOrder()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '分配失败')
  }
}

// 返回列表
const goBack = () => {
  router.push('/admin/orders')
}

onMounted(() => {
  loadOrder()
  loadRepairmen()
})
</script>

<style scoped>
.order-detail {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.detail-content {
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
  margin-bottom: 20px;
}

.info-row {
  display: flex;
  margin-bottom: 15px;
  align-items: flex-start;
}

.label {
  width: 100px;
  font-weight: bold;
  color: #666;
  flex-shrink: 0;
}

.value {
  flex: 1;
  color: #333;
}

.images-container {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.preview-image {
  width: 120px;
  height: 120px;
  object-fit: cover;
  cursor: pointer;
}

.evaluation-section {
  padding: 20px;
  background: #e8f5e9;
  border-radius: 8px;
  margin-bottom: 20px;
}

.evaluation-section h4 {
  margin: 0 0 15px 0;
  color: #2e7d32;
}

.evaluation-content {
  padding: 10px;
}

.evaluation-content p {
  margin: 10px 0;
  color: #666;
}

.evaluation-time {
  font-size: 12px;
  color: #999;
}

.action-buttons {
  text-align: right;
}
</style>