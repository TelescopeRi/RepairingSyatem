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
      </div>

      <!-- 学生评价区域 -->
      <div class="evaluation-section" v-if="order.status === 'COMPLETED' && showEvaluation">
        <h4>学生评价</h4>
        <div class="evaluation-content" v-if="evaluation">
          <el-rate :model-value="evaluation.rating" :max="5" disabled />
          <p>{{ evaluation.comment }}</p>
          <span class="evaluation-time">评价时间：{{ evaluation.createTime }}</span>
        </div>
        <div v-else class="no-evaluation">
          <p>暂无评价</p>
        </div>
      </div>

      <div class="action-buttons">
        <el-button @click="goBack">返回列表</el-button>
        <el-button
            v-if="order.status === 'COMPLETED' && !showEvaluation"
            type="primary"
            @click="loadEvaluation"
        >
          查看评价
        </el-button>
      </div>
    </el-card>

    <!-- 加载中状态 -->
    <div v-else class="loading-container">
      <el-skeleton :rows="8" animated />
    </div>
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
const evaluation = ref(null)
const showEvaluation = ref(false)

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
    const response = await axios.get(`/api/repair/orders/${route.params.id}`)
    order.value = response.data
  } catch (error) {
    console.error('加载工单详情失败', error)
    ElMessage.error('加载工单详情失败')
  }
}

// 加载评价
const loadEvaluation = async () => {
  try {
    const response = await axios.get(`/api/repair/orders/${route.params.id}/evaluation`)
    evaluation.value = response.data
    showEvaluation.value = true
  } catch (error) {
    if (error.response?.status === 404) {
      // 评价不存在，显示暂无评价
      showEvaluation.value = true
    } else {
      ElMessage.error('加载评价失败')
    }
  }
}

// 返回列表
const goBack = () => {
  router.push('/repairman/pending')
}

onMounted(() => {
  loadOrder()
})
</script>

<style scoped>
.order-detail {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.loading-container {
  padding: 20px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 10px;
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
  word-break: break-all;
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
  border-radius: 8px;
  transition: transform 0.3s ease;
}

.preview-image:hover {
  transform: scale(1.05);
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
  line-height: 1.5;
}

.evaluation-time {
  font-size: 12px;
  color: #999;
}

.no-evaluation {
  color: #999;
  text-align: center;
  padding: 20px;
}

.action-buttons {
  text-align: right;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .order-detail {
    padding: 10px;
  }

  .detail-content {
    padding: 15px;
  }

  .info-row {
    flex-direction: column;
  }

  .label {
    width: 100%;
    margin-bottom: 5px;
  }

  .detail-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>