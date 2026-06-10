import SockJS from 'sockjs-client'
import Stomp from 'stompjs'

/**
 * WebSocket工具类
 * 使用Stomp协议实现实时消息推送
 */
class WebSocketService {
  constructor() {
    this.stompClient = null
    this.connectStatus = 'DISCONNECTED' // DISCONNECTED, CONNECTING, CONNECTED
    this.subscriptions = {} // 存储订阅信息
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectDelay = 3000 // 重连延迟（毫秒）
    this.callbacks = {} // 存储回调函数
    
    // 获取当前环境的WebSocket地址
    this.wsUrl = this.getWsUrl()
  }

  /**
   * 获取WebSocket连接地址
   */
  getWsUrl() {
    // 使用相对路径，让 Vite 代理处理转发到后端
    return '/ws'
  }

  /**
   * 连接WebSocket
   * @param {Object} options - 连接选项
   * @param {string} options.token - JWT令牌
   * @param {Function} options.onConnected - 连接成功回调
   * @param {Function} options.onDisconnected - 断开连接回调
   * @param {Function} options.onError - 错误回调
   */
  connect(options = {}) {
    return new Promise((resolve, reject) => {
      if (this.connectStatus === 'CONNECTING' || this.connectStatus === 'CONNECTED') {
        resolve(this.stompClient)
        return
      }

      this.connectStatus = 'CONNECTING'

      const socket = new SockJS(this.wsUrl)
      this.stompClient = Stomp.over(socket)

      // 设置心跳
      this.stompClient.heartbeat.outgoing = 20000
      this.stompClient.heartbeat.incoming = 20000

      const headers = {}
      if (options.token) {
        headers.Authorization = `Bearer ${options.token}`
      }

      this.stompClient.connect(headers,
        (frame) => {
          console.log('WebSocket连接成功:', frame)
          this.connectStatus = 'CONNECTED'
          this.reconnectAttempts = 0
          
          if (options.onConnected) {
            options.onConnected(frame)
          }
          resolve(this.stompClient)
        },
        (error) => {
          console.error('WebSocket连接失败:', error)
          this.connectStatus = 'DISCONNECTED'
          
          if (options.onError) {
            options.onError(error)
          }
          
          // 尝试重连
          this.attemptReconnect(options)
          reject(error)
        }
      )

      // 监听连接关闭
      socket.onclose = () => {
        console.log('WebSocket连接关闭')
        this.connectStatus = 'DISCONNECTED'
        
        if (options.onDisconnected) {
          options.onDisconnected()
        }
        
        // 尝试重连
        this.attemptReconnect(options)
      }
    })
  }

  /**
   * 尝试重连
   */
  attemptReconnect(options) {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('已达到最大重连次数，停止尝试')
      return
    }

    this.reconnectAttempts++
    console.log(`尝试重连 ${this.reconnectAttempts}/${this.maxReconnectAttempts}...`)

    setTimeout(() => {
      this.connect(options).catch(() => {})
    }, this.reconnectDelay * this.reconnectAttempts)
  }

  /**
   * 断开连接
   */
  disconnect() {
    if (this.stompClient) {
      // 取消所有订阅
      Object.values(this.subscriptions).forEach((subscription) => {
        subscription.unsubscribe()
      })
      this.subscriptions = {}

      this.stompClient.disconnect(() => {
        console.log('WebSocket已断开连接')
        this.connectStatus = 'DISCONNECTED'
      })
      this.stompClient = null
    }
  }

  /**
   * 订阅消息
   * @param {string} destination - 订阅地址
   * @param {Function} callback - 消息回调
   * @param {Object} headers - 额外头信息
   * @returns {string} - 订阅ID
   */
  subscribe(destination, callback, headers = {}) {
    return new Promise((resolve, reject) => {
      if (!this.stompClient || this.connectStatus !== 'CONNECTED') {
        reject(new Error('WebSocket未连接'))
        return
      }

      const subscription = this.stompClient.subscribe(destination, (message) => {
        try {
          const payload = JSON.parse(message.body)
          callback(payload)
        } catch (error) {
          console.error('消息解析失败:', error)
          callback({ type: 'ERROR', error: error.message })
        }
      }, headers)

      const subscriptionId = `${destination}_${Date.now()}`
      this.subscriptions[subscriptionId] = subscription
      
      resolve(subscriptionId)
    })
  }

  /**
   * 取消订阅
   * @param {string} subscriptionId - 订阅ID
   */
  unsubscribe(subscriptionId) {
    const subscription = this.subscriptions[subscriptionId]
    if (subscription) {
      subscription.unsubscribe()
      delete this.subscriptions[subscriptionId]
    }
  }

  /**
   * 发送消息
   * @param {string} destination - 目标地址
   * @param {Object} payload - 消息内容
   * @param {Object} headers - 额外头信息
   */
  send(destination, payload, headers = {}) {
    if (!this.stompClient || this.connectStatus !== 'CONNECTED') {
      throw new Error('WebSocket未连接')
    }

    const message = typeof payload === 'string' ? payload : JSON.stringify(payload)
    this.stompClient.send(destination, headers, message)
  }

  /**
   * 获取连接状态
   */
  getStatus() {
    return this.connectStatus
  }

  /**
   * 订阅管理员工单消息
   * @param {Function} callback - 消息回调
   * @returns {string} - 订阅ID
   */
  subscribeAdminOrders(callback) {
    return this.subscribe('/topic/admin/orders', callback)
  }

  /**
   * 订阅学生工单消息（广播）
   * @param {Function} callback - 消息回调
   * @returns {string} - 订阅ID
   */
  subscribeStudentOrders(callback) {
    return this.subscribe('/topic/student/orders', callback)
  }

  /**
   * 订阅特定学生的消息队列（精确推送）
   * @param {number} studentId - 学生ID
   * @param {Function} callback - 消息回调
   * @returns {string} - 订阅ID
   */
  subscribeStudentQueue(studentId, callback) {
    return this.subscribe(`/queue/student/${studentId}`, callback)
  }

  /**
   * 订阅修理工工单消息
   * @param {Function} callback - 消息回调
   * @returns {string} - 订阅ID
   */
  subscribeRepairmanOrders(callback) {
    return this.subscribe('/topic/repairman/orders', callback)
  }

  /**
   * 订阅特定修理工的消息
   * @param {number} repairmanId - 修理工ID
   * @param {Function} callback - 消息回调
   * @returns {string} - 订阅ID
   */
  subscribeRepairmanQueue(repairmanId, callback) {
    return this.subscribe(`/queue/repairman/${repairmanId}`, callback)
  }
}

// 创建单例实例
const webSocketService = new WebSocketService()

export default webSocketService

// 也可以作为插件使用
export function useWebSocket() {
  return webSocketService
}