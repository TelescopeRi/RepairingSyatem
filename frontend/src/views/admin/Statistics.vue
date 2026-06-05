<template>
  <div class="dashboard">
    <!-- 顶部统计卡片 -->
    <div class="stats-cards">
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon today">
            <el-icon :size="32"><Plus /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ statistics.todayOrders || 0 }}</span>
            <span class="stat-label">今日新增报修</span>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card warning" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon pending">
            <el-icon :size="32"><Clock /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ statistics.pendingAssignOrders || 0 }}</span>
            <span class="stat-label">待分配工单</span>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon processing">
            <el-icon :size="32"><Tools /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ statistics.processingOrders || 0 }}</span>
            <span class="stat-label">处理中工单</span>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card success" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon completed">
            <el-icon :size="32"><CircleCheck /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ statistics.monthCompletionRate || 0 }}%</span>
            <span class="stat-label">本月完成率</span>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon avg">
            <el-icon :size="32"><Timer /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ statistics.avgProcessingHours || 0 }}h</span>
            <span class="stat-label">平均处理时长</span>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card danger" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon overtime">
            <el-icon :size="32"><Warning /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ statistics.overtimeOrders || 0 }}</span>
            <span class="stat-label">超时工单（>48h）</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 图表区域 -->
    <div class="charts-container">
      <!-- 近7天报修趋势 -->
      <el-card class="chart-card trend-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="card-title">
              <el-icon><TrendCharts /></el-icon>
              近7天报修趋势
            </span>
          </div>
        </template>
        <div ref="trendChartRef" class="chart"></div>
      </el-card>

      <!-- 故障类型分布 -->
      <el-card class="chart-card pie-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="card-title">
              <el-icon><PieChart /></el-icon>
              故障类型占比
            </span>
          </div>
        </template>
        <div ref="faultTypeChartRef" class="chart"></div>
      </el-card>
    </div>

    <!-- 楼栋报修排行 -->
    <el-card class="building-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><Histogram /></el-icon>
            各楼栋报修数量排行
          </span>
        </div>
      </template>
      <div ref="buildingChartRef" class="chart-large"></div>
    </el-card>

    <!-- 维修工绩效 -->
    <el-card class="performance-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><User /></el-icon>
            维修工绩效排名
          </span>
        </div>
      </template>
      <el-table :data="statistics.repairmanPerformance || []" border stripe>
        <el-table-column label="排名" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.$index === 0 ? 'danger' : scope.$index === 1 ? 'warning' : scope.$index === 2 ? 'success' : 'info'">
              {{ scope.$index + 1 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="repairmanName" label="维修工" width="150" align="center" />
        <el-table-column prop="completedCount" label="完成数量" width="120" align="center" />
        <el-table-column label="平均评分" width="200" align="center">
          <template #default="scope">
            <el-rate :model-value="scope.row.avgRating || 0" :max="5" disabled show-score />
          </template>
        </el-table-column>
        <el-table-column prop="avgProcessingHours" label="平均时长(h)" width="150" align="center" />
        <el-table-column prop="totalProcessingHours" label="总时长(h)" width="120" align="center" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import axios from '../../utils/axios'
import * as echarts from 'echarts'
import {
  Plus, Clock, Tools, CircleCheck, Timer, Warning,
  TrendCharts, PieChart, Histogram, User
} from '@element-plus/icons-vue'

const statistics = ref({})
const trendChartRef = ref(null)
const faultTypeChartRef = ref(null)
const buildingChartRef = ref(null)

// 加载统计数据
const loadStatistics = async () => {
  try {
    const response = await axios.get('/api/admin/statistics')
    statistics.value = response.data
    nextTick(() => {
      renderCharts()
    })
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

// 渲染所有图表
const renderCharts = () => {
  renderTrendChart()
  renderFaultTypeChart()
  renderBuildingChart()
}

// 近7天报修趋势图（折线图）
const renderTrendChart = () => {
  if (!trendChartRef.value) return
  
  const chart = echarts.init(trendChartRef.value)
  const dailyTrend = statistics.value.dailyTrend || []
  
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>报修数量: {c} 单'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dailyTrend.map(d => d.date),
      axisLine: { lineStyle: { color: '#ddd' } },
      axisLabel: { color: '#666' }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f0f0f0' } },
      axisLabel: { color: '#666' }
    },
    series: [
      {
        name: '报修数量',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          color: '#409EFF',
          width: 3
        },
        itemStyle: {
          color: '#409EFF',
          borderWidth: 2,
          borderColor: '#fff'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
              { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
            ]
          }
        },
        data: dailyTrend.map(d => d.count)
      }
    ]
  }
  
  chart.setOption(option)
  window.addEventListener('resize', () => chart.resize())
}

// 故障类型分布饼图
const renderFaultTypeChart = () => {
  if (!faultTypeChartRef.value) return
  
  const chart = echarts.init(faultTypeChartRef.value)
  const faultTypes = statistics.value.faultTypeCount || {}
  
  const chartData = Object.entries(faultTypes)
    .filter(([_, value]) => value > 0)
    .map(([name, value]) => ({ value, name }))
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} 单 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: { color: '#666' }
    },
    series: [
      {
        name: '故障类型',
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        data: chartData
      }
    ]
  }
  
  chart.setOption(option)
  window.addEventListener('resize', () => chart.resize())
}

// 各楼栋报修数量柱状图
const renderBuildingChart = () => {
  if (!buildingChartRef.value) return
  
  const chart = echarts.init(buildingChartRef.value)
  const buildingCount = statistics.value.buildingCount || {}
  
  // 排序并取前10
  const sortedData = Object.entries(buildingCount)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 10)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: '{b}<br/>报修数量: {c} 单'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '5%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: sortedData.map(([name]) => name),
      axisLabel: {
        rotate: sortedData.length > 5 ? 30 : 0,
        color: '#666'
      },
      axisLine: { lineStyle: { color: '#ddd' } }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f0f0f0' } },
      axisLabel: { color: '#666' }
    },
    series: [
      {
        name: '报修数量',
        type: 'bar',
        data: sortedData.map(([_, value]) => value),
        barWidth: '50%',
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#67C23A' },
            { offset: 1, color: '#95D475' }
          ])
        },
        label: {
          show: true,
          position: 'top',
          color: '#666',
          fontSize: 12
        }
      }
    ]
  }
  
  chart.setOption(option)
  window.addEventListener('resize', () => chart.resize())
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

/* 统计卡片区域 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 12px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 0;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-icon.today {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.pending {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.processing {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.completed {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.stat-icon.avg {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stat-icon.overtime {
  background: linear-gradient(135deg, #ff0844 0%, #ffb199 100%);
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

/* 图表卡片区域 */
.charts-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

@media (max-width: 1200px) {
  .charts-container {
    grid-template-columns: 1fr;
  }
}

.chart-card, .building-card, .performance-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.chart {
  height: 300px;
  width: 100%;
}

.chart-large {
  height: 350px;
  width: 100%;
}

/* 表格样式 */
:deep(.el-table) {
  border-radius: 8px;
}

:deep(.el-rate) {
  display: inline-flex;
}
</style>
