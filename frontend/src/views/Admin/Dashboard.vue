<template>
  <div class="admin-dashboard">
    <div class="dashboard-header">
      <h1 class="dashboard-title">
        <el-icon><Odometer /></el-icon>
        管理后台
      </h1>
      <p>汽车美容服务预约平台管理系统</p>
    </div>
    
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon users">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-info">
            <h3>{{ stats.totalUsers }}</h3>
            <p>总用户数</p>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon appointments">
            <el-icon><Calendar /></el-icon>
          </div>
          <div class="stat-info">
            <h3>{{ stats.totalAppointments }}</h3>
            <p>总预约数</p>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon services">
            <el-icon><Service /></el-icon>
          </div>
          <div class="stat-info">
            <h3>{{ stats.totalServices }}</h3>
            <p>服务项目</p>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon revenue">
            <el-icon><Money /></el-icon>
          </div>
          <div class="stat-info">
            <h3>¥{{ stats.totalRevenue }}</h3>
            <p>总收入</p>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 图表区域 -->
    <div class="charts-grid">
      <el-card class="chart-card">
        <template #header>
          <span>预约趋势</span>
        </template>
        <div ref="appointmentChartRef" class="chart-container"></div>
      </el-card>
      
      <el-card class="chart-card">
        <template #header>
          <span>服务分布</span>
        </template>
        <div ref="serviceChartRef" class="chart-container"></div>
      </el-card>
    </div>
    
    <!-- 最近活动 -->
    <div class="recent-activities">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>最近预约</span>
            <el-button type="primary" @click="$router.push('/admin/appointments')">
              查看全部
            </el-button>
          </div>
        </template>
        
        <el-table :data="recentAppointments" v-loading="loadingAppointments">
          <el-table-column prop="orderNo" label="订单号" width="180" />
          
          <el-table-column prop="user.realName" label="用户" width="120">
            <template #default="{ row }">
              {{ row.user?.realName || row.user?.username }}
            </template>
          </el-table-column>
          
          <el-table-column prop="service.name" label="服务项目" />
          
          <el-table-column prop="appointmentTime" label="预约时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.appointmentTime) }}
            </template>
          </el-table-column>
          
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button 
                type="primary" 
                size="small" 
                @click="viewAppointment(row.id)"
              >
                查看
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getAdminStats, getRecentAppointments, getAppointmentTrend } from '@/api/admin'
import { ElMessage } from 'element-plus'
import { TrendCharts, User, Calendar, Trophy, Money, Odometer } from '@element-plus/icons-vue'
import { formatDateTime } from '@/utils/dateUtils'
import * as echarts from 'echarts'

const router = useRouter()
const userStore = useUserStore()

const loadingAppointments = ref(false)
const appointmentChartRef = ref()
const serviceChartRef = ref()

const stats = ref({
  totalUsers: 0,
  totalAppointments: 0,
  totalServices: 0,
  totalRevenue: 0
})

const recentAppointments = ref([])

const getStatusType = (status) => {
  const statusMap = {
    'PENDING': 'warning',
    'CONFIRMED': 'primary',
    'IN_PROGRESS': 'info',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待确认',
    'CONFIRMED': '已确认',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

// formatDateTime 函数已从 @/utils/dateUtils 导入

const loadStats = async () => {
  try {
    const response = await getAdminStats()
    
    if (response && response.success) {
      stats.value = response.data
      
      // 更新服务分布图表
      updateServiceChart()
    } else {
      console.error('API响应success为false或响应为空')
      ElMessage.error('获取统计数据失败')
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    console.error('错误详情:', error.response || error.message)
    ElMessage.error(`加载统计数据失败: ${error.response?.data?.message || error.message}`)
  }
}

const loadRecentAppointments = async () => {
  loadingAppointments.value = true
  try {
    console.log('开始加载最近预约...')
    const response = await getRecentAppointments({ size: 10 })
    console.log('预约API响应:', response)
    console.log('预约响应success字段:', response.success)
    console.log('预约响应data字段:', response.data)
    
    if (response && response.success) {
      recentAppointments.value = response.data.content || response.data
      console.log('最近预约更新成功:', recentAppointments.value)
      ElMessage.success('最近预约加载成功')
    } else {
      console.error('预约API响应success为false或响应为空')
      ElMessage.error('获取最近预约失败')
    }
  } catch (error) {
    console.error('加载最近预约失败:', error)
    console.error('错误详情:', error.response || error.message)
    ElMessage.error(`加载最近预约失败: ${error.response?.data?.message || error.message}`)
  } finally {
    loadingAppointments.value = false
  }
}

// 加载预约趋势图数据
const loadAppointmentTrend = async () => {
  try {
    console.log('开始加载预约趋势数据...')
    const response = await getAppointmentTrend({ days: 7 })
    console.log('预约趋势API响应:', response)
    
    if (response && response.success && response.data) {
      const trendData = response.data
      console.log('趋势数据:', trendData)
      
      // 提取日期和数量
      const dates = trendData.map(item => item.date)
      const counts = trendData.map(item => item.count)
      
      console.log('日期:', dates)
      console.log('数量:', counts)
      
      // 更新图表
      updateAppointmentChart(dates, counts)
      
      ElMessage.success('预约趋势加载成功')
    } else {
      console.error('API返回数据格式错误:', response)
      ElMessage.warning('预约趋势数据加载失败')
      // 使用空数据
      updateAppointmentChart(['周一', '周二', '周三', '周四', '周五', '周六', '周日'], [0, 0, 0, 0, 0, 0, 0])
    }
  } catch (error) {
    console.error('加载预约趋势失败:', error)
    console.error('错误详情:', error.response || error.message)
    ElMessage.error(`加载预约趋势失败: ${error.response?.data?.message || error.message}`)
    // 如果加载失败，使用空数据
    updateAppointmentChart(['周一', '周二', '周三', '周四', '周五', '周六', '周日'], [0, 0, 0, 0, 0, 0, 0])
  }
}

// 更新预约趋势图
const updateAppointmentChart = (dates, counts) => {
  if (!appointmentChartRef.value) return
  
  const appointmentChart = echarts.init(appointmentChartRef.value)
  const appointmentOption = {
    title: {
      text: '最近7天预约趋势',
      textStyle: { fontSize: 14 }
    },
    tooltip: {
      trigger: 'axis',
      formatter: '{b}: {c} 个预约'
    },
    xAxis: {
      type: 'category',
      data: dates
    },
    yAxis: {
      type: 'value',
      minInterval: 1
    },
    series: [{
      name: '预约数',
      data: counts,
      type: 'line',
      smooth: true,
      itemStyle: {
        color: '#409EFF'
      },
      areaStyle: {
        opacity: 0.3,
        color: '#409EFF'
      }
    }]
  }
  appointmentChart.setOption(appointmentOption)
  
  // 响应式处理
  window.addEventListener('resize', () => {
    appointmentChart.resize()
    if (window.serviceChartInstance) {
      window.serviceChartInstance.resize()
    }
  })
}

const initCharts = () => {
  // 加载真实的预约趋势数据
  loadAppointmentTrend()
}

// 更新服务分布图表
const updateServiceChart = () => {
  if (!serviceChartRef.value) return
  
  // 销毁旧图表
  if (window.serviceChartInstance) {
    window.serviceChartInstance.dispose()
  }
  
  // 创建新图表
  const serviceChart = echarts.init(serviceChartRef.value)
  window.serviceChartInstance = serviceChart
  
  // 使用真实数据
  const categoryData = stats.value.categoryDistribution || []
  
  const serviceOption = {
    title: {
      text: '服务项目分布',
      textStyle: { fontSize: 14 }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    series: [{
      type: 'pie',
      radius: '60%',
      data: categoryData.length > 0 ? categoryData : [
        { value: 0, name: '暂无数据' }
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  }
  serviceChart.setOption(serviceOption)
}

const viewAppointment = (id) => {
  router.push(`/admin/appointments/${id}`)
}

onMounted(async () => {
  // 检查用户登录状态
  if (!userStore.isLoggedIn) {
    ElMessage.error('请先登录')
    router.push('/login')
    return
  }
  
  // 检查管理员权限
  if (!userStore.isAdmin) {
    ElMessage.error('需要管理员权限')
    router.push('/')
    return
  }
  
  // 如果用户信息为空，尝试获取用户信息
  if (!userStore.user) {
    try {
      await userStore.fetchUserProfile()
    } catch (error) {
      console.error('获取用户信息失败:', error)
      ElMessage.error('获取用户信息失败，请重新登录')
      router.push('/login')
      return
    }
  }
  
  loadStats()
  loadRecentAppointments()
  
  nextTick(() => {
    initCharts()
  })
})
</script>

<style lang="scss" scoped>
.admin-dashboard {
  padding: 24px;
}

.dashboard-header {
  margin-bottom: 32px;
  
  .dashboard-title {
    font-size: 32px;
    font-weight: 600;
    color: $text-primary;
    margin-bottom: 8px;
    display: flex;
    align-items: center;
    gap: 10px;
  }
  
  p {
    color: $text-secondary;
    font-size: 16px;
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.stat-card {
  .stat-content {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      
      .el-icon {
        font-size: 24px;
        color: white;
      }
      
      &.users {
        background: linear-gradient(135deg, #85CACF, #A8DEE0);
      }
      
      &.appointments {
        background: linear-gradient(135deg, #FDC68E, #F9E2AE);
      }
      
      &.services {
        background: linear-gradient(135deg, #A7D676, #85CACF);
      }
      
      &.revenue {
        background: linear-gradient(135deg, #A7D676, #A8DEE0);
      }
    }
    
    .stat-info {
      h3 {
        font-size: 28px;
        font-weight: 600;
        color: $text-primary;
        margin-bottom: 4px;
      }
      
      p {
        color: $text-secondary;
        font-size: 14px;
      }
    }
  }
}

.charts-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 32px;
}

.chart-card {
  .chart-container {
    height: 300px;
  }
}

.recent-activities {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .charts-grid {
    grid-template-columns: 1fr;
  }
  
  .dashboard-header {
    h1 {
      font-size: 24px;
    }
  }
}
</style>
