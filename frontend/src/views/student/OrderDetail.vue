<template>
  <div class="order-detail">
    <!-- 返回按钮 -->
    <div class="back-header">
      <el-button @click="goBack" text>
        <el-icon><ArrowLeft /></el-icon>
        返回列表
      </el-button>
    </div>

    <el-card v-if="order" v-loading="loading" class="detail-card">
      <!-- 工单状态头部 -->
      <div class="detail-header">
        <div class="header-left">
          <h2>工单详情</h2>
          <span class="order-id">工单号：{{ order.id }}</span>
        </div>
        <el-tag :type="getStatusType(order.status)" size="large" class="status-tag">
          {{ getStatusText(order.status) }}
        </el-tag>
      </div>

      <!-- 基本信息区域 -->
      <div class="section">
        <h3 class="section-title">
          <el-icon><InfoFilled /></el-icon>
          基本信息
        </h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">楼栋</span>
            <span class="value">{{ order.building }}</span>
          </div>
          <div class="info-item">
            <span class="label">宿舍号</span>
            <span class="value">{{ order.dormNumber }}</span>
          </div>
          <div class="info-item">
            <span class="label">故障类型</span>
            <span class="value">{{ order.faultTypeName }}</span>
          </div>
          <div class="info-item">
            <span class="label">紧急程度</span>
            <el-tag :type="order.urgency === 'URGENT' ? 'danger' : 'info'" size="small">
              {{ order.urgency === 'URGENT' ? '紧急' : '普通' }}
            </el-tag>
          </div>
        </div>
        <div class="info-item full-width">
          <span class="label">故障描述</span>
          <span class="value description">{{ order.description }}</span>
        </div>
      </div>

      <!-- 预计完成时间区域 -->
      <div class="section predict-section" v-if="timePredict && order.status !== 'COMPLETED' && order.status !== 'CANCELLED'">
        <h3 class="section-title">
          <el-icon><Clock /></el-icon>
          预计完成时间
        </h3>
        <div class="predict-card">
          <div class="predict-main">
            <span class="predict-time">{{ timePredict.predictedTimeText }}</span>
            <span class="predict-confidence" :class="timePredict.confidenceLevel">
              置信度：{{ timePredict.confidenceLevel }} ({{ timePredict.confidenceScore }}%)
            </span>
          </div>
          <p class="predict-explanation">{{ timePredict.explanation }}</p>
        </div>
      </div>

      <!-- 图片区域 -->
      <div class="section" v-if="order.images && order.images.length > 0">
        <h3 class="section-title">
          <el-icon><Picture /></el-icon>
          相关图片
        </h3>
        <div class="images-container">
          <el-image
              v-for="(img, index) in order.images"
              :key="index"
              :src="img"
              :preview-src-list="order.images"
              class="preview-image"
              fit="cover"
          />
        </div>
      </div>

      <!-- 维修信息区域 -->
      <div class="section" v-if="order.repairmanName">
        <h3 class="section-title">
          <el-icon><Tools /></el-icon>
          维修信息
        </h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">维修人员</span>
            <span class="value">{{ order.repairmanName }}</span>
          </div>
          <div class="info-item" v-if="order.repairmanPhone">
            <span class="label">联系电话</span>
            <span class="value phone">{{ order.repairmanPhone }}</span>
          </div>
          <div class="info-item full-width" v-if="order.remark">
            <span class="label">维修备注</span>
            <span class="value">{{ order.remark }}</span>
          </div>
        </div>
      </div>

      <!-- 时间线区域 -->
      <div class="section">
        <h3 class="section-title">
          <el-icon><Clock /></el-icon>
          处理进度
        </h3>
        <el-timeline>
          <el-timeline-item v-if="order.submitTime" timestamp="order.submitTime" placement="top">
            <el-card shadow="hover">提交报修</el-card>
          </el-timeline-item>
          <el-timeline-item v-if="order.assignTime" timestamp="order.assignTime" placement="top">
            <el-card shadow="hover">已分配维修人员</el-card>
          </el-timeline-item>
          <el-timeline-item v-if="order.startTime" timestamp="order.startTime" placement="top">
            <el-card shadow="hover">开始维修</el-card>
          </el-timeline-item>
          <el-timeline-item v-if="order.completeTime" timestamp="order.completeTime" placement="top">
            <el-card shadow="hover">维修完成</el-card>
          </el-timeline-item>
          <el-timeline-item v-if="order.cancelReason" timestamp="order.cancelReason" placement="top" type="danger">
            <el-card shadow="hover">工单已取消</el-card>
          </el-timeline-item>
        </el-timeline>
      </div>

      <!-- 评价区域（待确认状态时显示） -->
      <div class="section evaluation-section" v-if="order.status === 'PENDING_CONFIRM' && !order.evaluation">
        <h3 class="section-title">
          <el-icon><Star /></el-icon>
          评价服务
        </h3>
        <div class="evaluation-form">
          <div class="rating-row">
            <span class="rating-label">服务评分</span>
            <el-rate v-model="evaluationForm.rating" :max="5" show-text :texts="['非常差', '差', '一般', '满意', '非常满意']" />
          </div>
          <el-input
              v-model="evaluationForm.comment"
              type="textarea"
              :rows="4"
              placeholder="请分享您的维修体验（选填，最多500字）"
              maxlength="500"
              show-word-limit
          />
          <el-button type="primary" @click="submitEvaluation" :loading="submitting" class="submit-btn">
            提交评价
          </el-button>
        </div>
      </div>

      <!-- 已评价信息 -->
      <div class="section evaluation-result" v-if="order.evaluation">
        <h3 class="section-title">
          <el-icon><CircleCheck /></el-icon>
          我的评价
        </h3>
        <div class="evaluation-content">
          <div class="rating-display">
            <span class="rating-label">评分</span>
            <el-rate :model-value="order.evaluation.rating" disabled />
          </div>
          <div class="comment-display" v-if="order.evaluation.comment">
            <span class="rating-label">评价内容</span>
            <p>{{ order.evaluation.comment }}</p>
          </div>
          <div class="time-display">
            <span class="rating-label">评价时间</span>
            <span>{{ order.evaluation.createTime }}</span>
          </div>
        </div>
      </div>

      <!-- 取消信息 -->
      <div class="section cancel-section" v-if="order.cancelReason">
        <h3 class="section-title danger">
          <el-icon><CircleClose /></el-icon>
          取消原因
        </h3>
        <p class="cancel-reason">{{ order.cancelReason }}</p>
      </div>
    </el-card>

    <!-- 加载中状态 -->
    <div v-else-if="loading" class="loading-container">
      <el-skeleton :rows="8" animated />
    </div>

    <!-- 无数据状态 -->
    <el-empty v-else description="工单不存在或无权访问" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from '../../utils/axios'
import { ElMessage } from 'element-plus'
import { ArrowLeft, InfoFilled, Picture, Tools, Clock, Star, CircleCheck, CircleClose } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const order = ref(null)
const timePredict = ref(null)
const loading = ref(false)
const submitting = ref(false)

const evaluationForm = ref({
  rating: 5,
  comment: ''
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

// 加载工单详情
const loadOrder = async () => {
  loading.value = true
  try {
    const [orderResponse, predictResponse] = await Promise.all([
      axios.get(`/api/student/orders/${route.params.id}`),
      axios.get(`/api/ai/predict/time/${route.params.id}`)
    ])
    order.value = orderResponse.data
    timePredict.value = predictResponse.data
  } catch (error) {
    console.error('加载工单详情失败', error)
    ElMessage.error('加载工单详情失败')
  } finally {
    loading.value = false
  }
}

// 提交评价
const submitEvaluation = async () => {
  if (!evaluationForm.value.rating) {
    ElMessage.error('请选择评分')
    return
  }

  submitting.value = true
  try {
    await axios.post(`/api/student/orders/${route.params.id}/evaluate`, {
      rating: evaluationForm.value.rating,
      comment: evaluationForm.value.comment
    })

    ElMessage.success('评价成功，感谢您的反馈！')
    // 重新加载工单详情以获取最新数据
    await loadOrder()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '评价失败')
  } finally {
    submitting.value = false
  }
}

// 返回列表
const goBack = () => {
  router.push('/student/history')
}

onMounted(() => {
  loadOrder()
})
</script>

<style scoped>
.order-detail {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

/* 返回头部 */
.back-header {
  margin-bottom: 20px;
}

/* 详情卡片 */
.detail-card {
  border-radius: 12px;
  overflow: hidden;
}

/* 详情头部 */
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 20px;
}

.header-left h2 {
  margin: 0 0 8px 0;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.order-id {
  color: #909399;
  font-size: 14px;
}

.status-tag {
  padding: 8px 16px;
  font-size: 14px;
}

/* 区域 */
.section {
  margin-bottom: 28px;
  padding-bottom: 20px;
  border-bottom: 1px dashed #ebeef5;
}

.section:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.section-title.danger {
  color: #f56c6c;
}

/* 信息网格 */
.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-item .label {
  color: #909399;
  font-size: 13px;
}

.info-item .value {
  color: #303133;
  font-size: 15px;
}

.info-item .value.description {
  line-height: 1.6;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-top: 4px;
}

.info-item .value.phone {
  color: #409eff;
}

/* 图片网格 */
.images-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 12px;
}

.preview-image {
  width: 140px;
  height: 140px;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.preview-image:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 时间线 */
:deep(.el-timeline) {
  margin-top: 10px;
}

:deep(.el-timeline-item__content) {
  padding-bottom: 8px;
}

/* 评价区域 */
.evaluation-section {
  background: linear-gradient(135deg, #fff8e1 0%, #ffecb3 100%);
  padding: 20px;
  border-radius: 12px;
  border: 1px solid #ffe082;
}

.evaluation-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.rating-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.rating-label {
  color: #606266;
  font-size: 14px;
  min-width: 70px;
}

.submit-btn {
  align-self: flex-end;
  margin-top: 8px;
}

/* 已评价内容 */
.evaluation-result {
  background: #f0f9eb;
  padding: 20px;
  border-radius: 12px;
  border: 1px solid #c2e7b0;
}

.evaluation-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rating-display,
.comment-display,
.time-display {
  display: flex;
  align-items: center;
  gap: 12px;
}

.comment-display {
  flex-direction: column;
  align-items: flex-start;
}

.comment-display p {
  margin: 8px 0 0 0;
  padding: 12px;
  background: white;
  border-radius: 8px;
  color: #333;
  line-height: 1.6;
  width: 100%;
}

.time-display {
  font-size: 12px;
  color: #909399;
}

/* 取消原因 */
.cancel-section {
  background: #fef0f0;
  padding: 20px;
  border-radius: 12px;
  border: 1px solid #fde2e2;
}

.cancel-reason {
  margin: 0;
  color: #f56c6c;
  line-height: 1.6;
}

/* 预测时间区域 */
.predict-section {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  padding: 20px;
  border-radius: 12px;
  border: 1px solid #bae6fd;
}

.predict-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.predict-main {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.predict-time {
  font-size: 24px;
  font-weight: bold;
  color: #0ea5e9;
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
  margin: 0;
  color: #909399;
  font-size: 13px;
  line-height: 1.5;
}

/* 加载容器 */
.loading-container {
  padding: 20px;
  background: white;
  border-radius: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .order-detail {
    padding: 12px;
  }

  .detail-header {
    flex-direction: column;
    gap: 12px;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .images-container {
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  }

  .preview-image {
    width: 100px;
    height: 100px;
  }

  .rating-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .submit-btn {
    width: 100%;
  }
}
</style>
