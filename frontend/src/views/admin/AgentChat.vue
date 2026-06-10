<template>
  <div class="agent-chat-container">
    <div class="chat-header">
      <h2>🤖 智能助手</h2>
      <p>帮助您管理维修系统数据</p>
    </div>

    <div class="chat-body" ref="chatBodyRef">
      <div class="message-list" ref="messageListRef">
        <!-- 加载更多提示 -->
        <div v-if="hasMore" class="load-more" @click="loadMore">
          <span v-if="loadingMore">加载中...</span>
          <span v-else class="load-more-text">↑ 加载更多消息</span>
        </div>

        <div v-for="msg in messages" :key="msg.id" :class="['message-item', msg.role]">
          <div class="avatar">
            <span>{{ msg.role === 'user' ? '👤' : '🤖' }}</span>
          </div>
          <div class="message-content">
            <pre>{{ msg.content }}</pre>
            <span v-if="msg.timestamp" class="message-time">{{ formatTime(msg.timestamp) }}</span>
          </div>
        </div>
        
        <div v-if="loading && messages.length === 0" class="loading-item">
          <div class="loading-dots">
            <span></span>
            <span></span>
            <span></span>
          </div>
        </div>
        
        <div v-if="messages.length === 0 && !loading" class="empty-state">
          <div class="empty-icon">💬</div>
          <p>开始与智能助手对话吧</p>
        </div>
      </div>
    </div>

    <div class="chat-footer">
      <div class="quick-commands">
        <el-button 
          v-for="cmd in quickCommands" 
          :key="cmd.label"
          :type="cmd.type"
          size="small"
          @click="executeQuickCommand(cmd.action)"
        >
          {{ cmd.label }}
        </el-button>
      </div>
      
      <div class="input-area">
        <el-upload
          class="upload-btn"
          :show-file-list="false"
          :auto-upload="false"
          :on-change="handleFileChange"
          accept=".csv,.txt,.xlsx,.xls"
        >
          <el-button icon="upload" size="small">上传文件</el-button>
        </el-upload>
        
        <el-input
          v-model="inputMessage"
          @keyup.enter="sendMessage"
          placeholder="输入指令，如：'帮我总结本周报修情况'"
          size="large"
        />
        
        <el-button type="primary" @click="sendMessage" size="large">发送</el-button>
      </div>
      
      <div v-if="selectedFile" class="file-info">
        已选择文件: {{ selectedFile.name }}
        <el-button link size="small" @click="selectedFile = null">取消</el-button>
      </div>
      
      <div v-if="fileTypeModal" class="file-type-modal">
        <p>选择导入类型:</p>
        <el-button type="primary" @click="confirmImport('student')">导入学生</el-button>
        <el-button type="success" @click="confirmImport('repair')">导入修理工</el-button>
        <el-button @click="fileTypeModal = false">取消</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onActivated } from 'vue'
import axios from '../../utils/axios'
import { ElMessage } from 'element-plus'

const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const loadingMore = ref(false)
const selectedFile = ref(null)
const fileTypeModal = ref(false)
const messageListRef = ref(null)
const chatBodyRef = ref(null)
const hasMore = ref(false)
const totalLoaded = ref(0)
const currentOffset = ref(0)

const PAGE_SIZE = 3 // 每页加载3条

const quickCommands = [
  { label: '📊 报修情况汇总', action: 'summary-repair', type: 'primary' },
  { label: '👷 维修工绩效', action: 'summary-worker', type: 'success' },
  { label: '📈 故障趋势', action: 'summary-trend', type: 'warning' },
  { label: '❓ 帮助', action: 'help', type: 'info' }
]

const addMessage = (role, content, timestamp = null) => {
  const newMsg = {
    id: Date.now() + Math.random(),
    role,
    content,
    timestamp: timestamp || new Date().toISOString()
  }
  messages.value.push(newMsg)
  
  // 更新状态
  totalLoaded.value = messages.value.length
  
  // 滚动到底部
  nextTick(() => {
    scrollToBottom()
  })
}

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 滚动到底部
const scrollToBottom = () => {
  if (chatBodyRef.value) {
    // 设置滚动位置到最底部
    chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight
  }
}

// 处理滚动事件
const handleScroll = async () => {
  if (!chatBodyRef.value || loadingMore.value || !hasMore.value) return
  
  const { scrollTop } = chatBodyRef.value
  // 当滚动到顶部时加载更多
  if (scrollTop < 50) {
    await loadMore()
  }
}

// 加载更多消息
const loadMore = async () => {
  if (loadingMore.value || !hasMore.value) return
  
  loadingMore.value = true
  
  try {
    // 计算offset：当前已加载的消息数量
    const offset = messages.value.length
    const response = await axios.get('/api/agent/messages', {
      params: { offset, limit: PAGE_SIZE }
    })
    
    if (response.data && response.data.messages) {
      const newMessages = response.data.messages
      
      if (newMessages.length > 0) {
        // 将新消息添加到列表开头（更早的消息）
        messages.value = [...newMessages, ...messages.value]
        
        // 更新状态
        totalLoaded.value = messages.value.length
        
        // 检查是否还有更多消息
        hasMore.value = messages.value.length < response.data.total
      } else {
        hasMore.value = false
      }
    } else {
      hasMore.value = false
    }
  } catch (error) {
    console.error('加载更多消息失败:', error)
  } finally {
    loadingMore.value = false
  }
}

// 加载初始消息
const loadInitialMessages = async () => {
  loading.value = true
  
  try {
    const response = await axios.get('/api/agent/messages', {
      params: { offset: 0, limit: PAGE_SIZE }
    })
    
    if (response.data && response.data.messages) {
      // 按正序排列（ oldest -> newest）
      const loadedMessages = response.data.messages
      messages.value = loadedMessages
      totalLoaded.value = loadedMessages.length
      currentOffset.value = loadedMessages.length
      
      // 检查是否还有更多消息
      hasMore.value = loadedMessages.length < response.data.total
    }
    
    // 如果没有历史消息，显示欢迎消息
    if (messages.value.length === 0) {
      addMessage('agent', '您好！我是智能助手，请问有什么可以帮您的？\n\n您可以：\n• 上传CSV/TXT文件批量导入学生或修理工\n• 让我总结报修情况\n• 分析维修工绩效\n• 询问系统操作问题')
    } else {
      nextTick(() => {
        scrollToBottom()
      })
    }
  } catch (error) {
    console.error('加载消息失败:', error)
    if (messages.value.length === 0) {
      addMessage('agent', '您好！我是智能助手，请问有什么可以帮您的？')
    }
  } finally {
    loading.value = false
  }
}

const sendMessage = async () => {
  const message = inputMessage.value.trim()
  if (!message) return
  
  addMessage('user', message)
  inputMessage.value = ''
  loading.value = true
  
  try {
    const response = await axios.post('/api/agent/chat', { message })
    addMessage('agent', response.data.content)
    
    // 更新偏移量
    currentOffset.value = messages.value.length
  } catch (error) {
    addMessage('agent', '抱歉，暂时无法回答您的问题。')
    ElMessage.error('请求失败')
  } finally {
    loading.value = false
  }
}

const handleFileChange = (file) => {
  selectedFile.value = file.raw
  fileTypeModal.value = true  // 显示选择类型的弹窗
}

const executeQuickCommand = async (action) => {
  loading.value = true
  
  try {
    let response
    let userMessage = ''
    
    switch (action) {
      case 'summary-repair':
        response = await axios.get('/api/agent/summary/repair-status')
        userMessage = '帮我总结报修情况'
        break
      case 'summary-worker':
        response = await axios.get('/api/agent/summary/worker-performance')
        userMessage = '分析维修工绩效'
        break
      case 'summary-trend':
        response = await axios.get('/api/agent/summary/fault-trend')
        userMessage = '分析故障趋势'
        break
      case 'help':
        userMessage = '帮助'
        response = { data: { content: '我可以帮您：\n\n1. 📊 汇总报修情况 - 查看今日报修、待分配、处理中、已完成工单统计\n\n2. 👷 分析维修工绩效 - 查看每位维修工的完成工单数量\n\n3. 📈 分析故障趋势 - 查看近7天报修趋势\n\n4. 📁 批量导入 - 上传Excel/CSV/TXT文件导入学生或修理工\n\n5. ❓ 问答 - 解答系统操作问题\n\n文件格式要求（统一格式）：\n第一列: 学号/工号（必填）\n第二列: 姓名（必填）\n第三列: 手机号（可选）\n第四列: 楼栋（学生必填）\n第五列: 宿舍号（学生可选）\n\n示例：\n学号,姓名,手机号,楼栋,宿舍号\n2021001,张三,13800138001,1栋,101' } }
        break
    }
    
    if (userMessage) {
      addMessage('user', userMessage)
    }
    
    if (response) {
      addMessage('agent', response.data.content)
    }
  } catch (error) {
    addMessage('agent', '获取数据失败')
    ElMessage.error('请求失败')
  } finally {
    loading.value = false
  }
}

const confirmImport = async (type) => {
  fileTypeModal.value = false
  if (!selectedFile.value) return
  
  loading.value = true
  
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    formData.append('type', type)
    
    const response = await axios.post('/api/agent/batch-import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    
    const actionText = `${type === 'student' ? '导入学生' : '导入修理工'}`
    addMessage('user', `上传文件并${actionText}`)
    addMessage('agent', response.data.content)
    selectedFile.value = null
  } catch (error) {
    addMessage('agent', '文件导入失败')
    ElMessage.error('导入失败')
  } finally {
    loading.value = false
  }
}

// 页面加载时加载初始消息
onMounted(() => {
  loadInitialMessages()
})

// 组件被激活时（从其他页面切换回来）
onActivated(() => {
  // 滚动到最近一条消息
  nextTick(() => {
    scrollToBottom()
  })
})
</script>

<style scoped>
.agent-chat-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 100px);
  background: #f5f7fa;
  border-radius: 12px;
  overflow: hidden;
}

.chat-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  text-align: center;
  flex-shrink: 0;
}

.chat-header h2 {
  margin: 0 0 5px 0;
  font-size: 20px;
}

.chat-header p {
  margin: 0;
  opacity: 0.8;
  font-size: 14px;
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  min-height: 0;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.message-item {
  display: flex;
  gap: 10px;
  max-width: 80%;
}

.message-item.user {
  align-self: flex-end;
}

.message-item.agent {
  align-self: flex-start;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #e8eef5;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 20px;
}

.message-item.user .avatar {
  background: #667eea;
  color: white;
}

.message-content {
  background: white;
  padding: 12px 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.message-item.user .message-content {
  background: #667eea;
  color: white;
}

.message-content pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.6;
}

.message-time {
  display: block;
  font-size: 12px;
  opacity: 0.6;
  margin-top: 4px;
}

.message-item.user .message-time {
  color: rgba(255, 255, 255, 0.8);
}

.loading-item {
  align-self: flex-start;
  padding: 12px;
}

.loading-dots {
  display: flex;
  gap: 5px;
}

.loading-dots span {
  width: 8px;
  height: 8px;
  background: #667eea;
  border-radius: 50%;
  animation: loading 1.4s infinite ease-in-out both;
}

.loading-dots span:nth-child(1) { animation-delay: -0.32s; }
.loading-dots span:nth-child(2) { animation-delay: -0.16s; }

@keyframes loading {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.load-more {
  text-align: center;
  padding: 10px;
  cursor: pointer;
  color: #667eea;
  font-size: 14px;
  transition: opacity 0.3s;
}

.load-more:hover {
  opacity: 0.7;
}

.load-more-text {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #9ca3af;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.empty-state p {
  margin: 0;
  font-size: 16px;
}

.chat-footer {
  background: white;
  padding: 15px 20px;
  border-top: 1px solid #e8eef5;
  flex-shrink: 0;
}

.quick-commands {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
  flex-wrap: wrap;
}

.input-area {
  display: flex;
  gap: 10px;
  align-items: center;
}

.upload-btn {
  flex-shrink: 0;
}

.input-area .el-input {
  flex: 1;
}

.file-info {
  margin-top: 10px;
  padding: 8px 12px;
  background: #f0f9ff;
  border-radius: 6px;
  font-size: 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.file-type-modal {
  margin-top: 15px;
  padding: 15px;
  background: #fafafa;
  border-radius: 8px;
  text-align: center;
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: center;
}

.file-type-modal p {
  margin: 0;
}
</style>
