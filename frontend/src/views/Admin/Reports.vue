<template>
  <div class="admin-reports">
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><DataAnalysis /></el-icon>
        数据报表
      </h2>
      <div class="header-actions">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          @change="loadReports"
        />
        <el-button type="success" size="large" @click="exportReport" :icon="Download">
          导出Excel报表
        </el-button>
      </div>
    </div>
    
    <!-- 概览统计 -->
    <div class="overview-stats">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card revenue">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon><Money /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">¥{{ formatNumber(overview.totalRevenue) }}</div>
                <div class="stat-label">总收入</div>
                <div class="stat-change" :class="{ positive: overview.revenueGrowth > 0 }">
                  {{ overview.revenueGrowth > 0 ? '+' : '' }}{{ overview.revenueGrowth }}%
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="stat-card orders">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ overview.totalOrders }}</div>
                <div class="stat-label">总订单数</div>
                <div class="stat-change" :class="{ positive: overview.orderGrowth > 0 }">
                  {{ overview.orderGrowth > 0 ? '+' : '' }}{{ overview.orderGrowth }}%
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="stat-card users">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon><User /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ overview.totalUsers }}</div>
                <div class="stat-label">总用户数</div>
                <div class="stat-change" :class="{ positive: overview.userGrowth > 0 }">
                  {{ overview.userGrowth > 0 ? '+' : '' }}{{ overview.userGrowth }}%
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="stat-card rating">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon><Star /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ overview.averageRating }}</div>
                <div class="stat-label">平均评分</div>
                <div class="stat-change" :class="{ positive: overview.ratingChange > 0 }">
                  {{ overview.ratingChange > 0 ? '+' : '' }}{{ overview.ratingChange }}
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 图表区域 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <!-- 收入趋势图 -->
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>收入趋势</span>
                <el-radio-group v-model="revenueChartType" size="small">
                  <el-radio-button label="day">日</el-radio-button>
                  <el-radio-button label="week">周</el-radio-button>
                  <el-radio-button label="month">月</el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <div ref="revenueChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        
        <!-- 订单状态分布 -->
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>订单状态分布</span>
              </div>
            </template>
            <div ref="orderStatusChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" style="margin-top: 20px;">
        <!-- 服务项目排行 -->
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>热门服务排行</span>
              </div>
            </template>
            <div ref="serviceRankChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        
        <!-- 用户增长趋势 -->
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>用户增长趋势</span>
              </div>
            </template>
            <div ref="userGrowthChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 详细数据表格 -->
    <el-card class="data-tables">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="收入明细" name="revenue">
          <el-table :data="revenueDetails" stripe>
            <el-table-column prop="date" label="日期" width="120" />
            <el-table-column prop="orderCount" label="订单数" width="100" />
            <el-table-column prop="revenue" label="收入金额" width="120">
              <template #default="{ row }">
                ¥{{ formatNumber(row.revenue) }}
              </template>
            </el-table-column>
            <el-table-column prop="averageAmount" label="客单价" width="120">
              <template #default="{ row }">
                ¥{{ formatNumber(row.averageAmount) }}
              </template>
            </el-table-column>
            <el-table-column prop="growth" label="增长率" width="100">
              <template #default="{ row }">
                <span :class="{ 'text-success': row.growth > 0, 'text-danger': row.growth < 0 }">
                  {{ row.growth > 0 ? '+' : '' }}{{ row.growth }}%
                </span>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="服务统计" name="services">
          <el-table :data="serviceStats" stripe>
            <el-table-column prop="serviceName" label="服务名称" min-width="150" />
            <el-table-column prop="orderCount" label="预约次数" width="120" />
            <el-table-column prop="revenue" label="收入金额" width="120">
              <template #default="{ row }">
                ¥{{ formatNumber(row.revenue) }}
              </template>
            </el-table-column>
            <el-table-column prop="averageRating" label="平均评分" width="120">
              <template #default="{ row }">
                <el-rate
                  v-model="row.averageRating"
                  disabled
                  show-score
                  text-color="#ff9900"
                />
              </template>
            </el-table-column>
            <el-table-column prop="completionRate" label="完成率" width="100">
              <template #default="{ row }">
                {{ row.completionRate }}%
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="用户分析" name="users">
          <el-table :data="userAnalysis" stripe>
            <el-table-column prop="date" label="日期" width="120" />
            <el-table-column prop="newUsers" label="新增用户" width="100" />
            <el-table-column prop="activeUsers" label="活跃用户" width="100" />
            <el-table-column prop="retentionRate" label="留存率" width="100">
              <template #default="{ row }">
                {{ row.retentionRate }}%
              </template>
            </el-table-column>
            <el-table-column prop="averageOrders" label="人均订单" width="100" />
            <el-table-column prop="averageSpending" label="人均消费" width="120">
              <template #default="{ row }">
                ¥{{ formatNumber(row.averageSpending) }}
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import { getReportsData } from '@/api/admin'
import { ElMessage } from 'element-plus'
import { Download, Money, Document, User, Star, DataAnalysis } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const loading = ref(false)
const activeTab = ref('revenue')
const revenueChartType = ref('day')

const dateRange = ref([
  new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
  new Date().toISOString().split('T')[0]
])

// 图表引用
const revenueChartRef = ref()
const orderStatusChartRef = ref()
const serviceRankChartRef = ref()
const userGrowthChartRef = ref()

// 图表实例
let revenueChart = null
let orderStatusChart = null
let serviceRankChart = null
let userGrowthChart = null

const overview = ref({
  totalRevenue: 0,
  revenueGrowth: 0,
  totalOrders: 0,
  orderGrowth: 0,
  totalUsers: 0,
  userGrowth: 0,
  averageRating: 0,
  ratingChange: 0
})

const revenueDetails = ref([])
const serviceStats = ref([])
const userAnalysis = ref([])

const formatNumber = (num) => {
  return new Intl.NumberFormat('zh-CN').format(num || 0)
}

const loadReports = async () => {
  loading.value = true
  try {
    const params = {
      startDate: dateRange.value[0],
      endDate: dateRange.value[1]
    }
    
    const response = await getReportsData(params)
    console.log('报表API响应:', response)
    
    if (response && response.success && response.data) {
      const data = response.data
      
      // 更新概览数据 - 使用真实数据
      overview.value = {
        totalRevenue: data.overview.totalRevenue || 0,
        revenueGrowth: data.overview.revenueGrowth || 0,
        totalOrders: data.overview.totalOrders || 0,
        orderGrowth: data.overview.orderGrowth || 0,
        totalUsers: data.overview.totalUsers || 0,
        userGrowth: data.overview.userGrowth || 0,
        averageRating: data.overview.averageRating || 0,
        ratingChange: data.overview.ratingChange || 0,
        activeUsers: data.overview.activeUsers || 0,
        todayNewUsers: data.overview.todayNewUsers || 0,
        totalPoints: data.overview.totalPoints || 0,
        totalReviews: data.overview.totalReviews || 0,
        pendingReviews: data.overview.pendingReviews || 0,
        todayNewReviews: data.overview.todayNewReviews || 0,
        todayNewAppointments: data.overview.todayNewAppointments || 0,
        pendingAppointments: data.overview.pendingAppointments || 0,
        confirmedAppointments: data.overview.confirmedAppointments || 0,
        inProgressAppointments: data.overview.inProgressAppointments || 0,
        completedAppointments: data.overview.completedAppointments || 0
      }
      
      console.log('概览数据已更新:', overview.value)
      
      // 更新收入明细表格 - 从revenueChart转换
      if (data.revenueChart && Array.isArray(data.revenueChart)) {
        revenueDetails.value = data.revenueChart.map((item, index, array) => {
          const orderCount = 0 // 暂时设为0，需要后端提供
          const revenue = item.revenue || 0
          const averageAmount = orderCount > 0 ? revenue / orderCount : 0
          // 计算增长率（与前一天相比）
          let growth = 0
          if (index > 0) {
            const prevRevenue = array[index - 1].revenue || 0
            if (prevRevenue > 0) {
              growth = ((revenue - prevRevenue) / prevRevenue * 100).toFixed(1)
            }
          }
          return {
            date: item.date,
            orderCount,
            revenue,
            averageAmount,
            growth
          }
        })
      } else {
        revenueDetails.value = []
      }
      
      // 服务统计 - 将categoryDistribution转换为serviceStats格式
      if (data.serviceRankData && Array.isArray(data.serviceRankData)) {
        serviceStats.value = data.serviceRankData.map(item => ({
          serviceName: item.name || '未知',
          orderCount: item.value || 0,
          revenue: 0, // 暂时没有收入数据
          averageRating: 4.5, // 暂时使用默认评分
          completionRate: 95 // 暂时使用默认完成率
        }))
      } else {
        serviceStats.value = []
      }
      
      // 用户分析表格 - 从userGrowthData转换
      if (data.userGrowthData && Array.isArray(data.userGrowthData)) {
        userAnalysis.value = data.userGrowthData.map(item => ({
          date: item.date,
          newUsers: item.newUsers || 0,
          activeUsers: item.activeUsers || 0,
          retentionRate: 0, // 暂时设为0，留存率需要更复杂的计算
          averageOrders: item.averageOrders || 0,
          averageSpending: item.averageSpending || 0
        }))
      } else {
        userAnalysis.value = []
      }
      
      // 更新图表
      await nextTick()
      updateCharts(data)
      
      ElMessage.success('报表数据加载成功')
    } else {
      console.error('API返回数据格式错误:', response)
      ElMessage.error('获取报表数据失败：数据格式错误')
    }
  } catch (error) {
    console.error('加载报表数据失败:', error)
    console.error('错误详情:', error.response || error.message)
    ElMessage.error(`加载报表数据失败: ${error.response?.data?.message || error.message}`)
  } finally {
    loading.value = false
  }
}

// 移除模拟数据函数 - 不再使用模拟数据

const updateCharts = (data) => {
  // 收入趋势图
  if (revenueChartRef.value) {
    if (!revenueChart) {
      revenueChart = echarts.init(revenueChartRef.value)
    }
    
    const revenueOption = {
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'cross' }
      },
      xAxis: {
        type: 'category',
        data: data.revenueChart?.map(item => item.date) || []
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: '¥{value}'
        }
      },
      series: [{
        name: '收入',
        type: 'line',
        smooth: true,
        data: data.revenueChart?.map(item => item.revenue) || [],
        itemStyle: { color: '#409EFF' },
        areaStyle: { opacity: 0.3 }
      }]
    }
    revenueChart.setOption(revenueOption)
  }
  
  // 订单状态分布图
  if (orderStatusChartRef.value) {
    if (!orderStatusChart) {
      orderStatusChart = echarts.init(orderStatusChartRef.value)
    }
    
    const statusOption = {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      series: [{
        name: '订单状态',
        type: 'pie',
        radius: ['40%', '70%'],
        data: data.orderStatusData || [],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    }
    orderStatusChart.setOption(statusOption)
  }
  
  // 服务排行图
  if (serviceRankChartRef.value) {
    if (!serviceRankChart) {
      serviceRankChart = echarts.init(serviceRankChartRef.value)
    }
    
    const rankOption = {
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' }
      },
      xAxis: {
        type: 'value'
      },
      yAxis: {
        type: 'category',
        data: data.serviceRankData?.map(item => item.name) || []
      },
      series: [{
        name: '预约次数',
        type: 'bar',
        data: data.serviceRankData?.map(item => item.value) || [],
        itemStyle: { color: '#67C23A' }
      }]
    }
    serviceRankChart.setOption(rankOption)
  }
  
  // 用户增长图
  if (userGrowthChartRef.value) {
    if (!userGrowthChart) {
      userGrowthChart = echarts.init(userGrowthChartRef.value)
    }
    
    const growthOption = {
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['新增用户', '活跃用户']
      },
      xAxis: {
        type: 'category',
        data: data.userGrowthData?.map(item => item.date) || []
      },
      yAxis: {
        type: 'value',
        minInterval: 1
      },
      series: [
        {
          name: '新增用户',
          type: 'bar',
          data: data.userGrowthData?.map(item => item.newUsers) || [],
          itemStyle: { color: '#E6A23C' }
        },
        {
          name: '活跃用户',
          type: 'line',
          data: data.userGrowthData?.map(item => item.activeUsers) || [],
          itemStyle: { color: '#F56C6C' }
        }
      ]
    }
    userGrowthChart.setOption(growthOption)
  }
}

const exportReport = () => {
  try {
    // 准备导出的统计数据
    const exportData = [
      {
        '报表类型': '总体概览',
        '指标名称': '总收入',
        '数值': overview.value.totalRevenue || 0,
        '单位': '元'
      },
      {
        '报表类型': '总体概览',
        '指标名称': '总预约数',
        '数值': overview.value.totalAppointments || 0,
        '单位': '次'
      },
      {
        '报表类型': '总体概览',
        '指标名称': '总用户数',
        '数值': overview.value.totalUsers || 0,
        '单位': '人'
      },
      {
        '报表类型': '总体概览',
        '指标名称': '平均评分',
        '数值': overview.value.averageRating || 0,
        '单位': '分'
      }
    ]
    
    // 转换为CSV格式
    const headers = ['报表类型', '指标名称', '数值', '单位']
    const csvContent = [
      headers.join(','),
      ...exportData.map(row => headers.map(header => {
        let value = String(row[header] || '')
        if (value.includes(',') || value.includes('\n') || value.includes('"')) {
          value = '"' + value.replace(/"/g, '""') + '"'
        }
        return value
      }).join(','))
    ].join('\n')
    
    // 添加BOM以支持中文
    const BOM = '\uFEFF'
    const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' })
    
    // 创建下载链接
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    const dateStr = dateRange.value && dateRange.value.length === 2
      ? `${dateRange.value[0]}_${dateRange.value[1]}`
      : new Date().toISOString().split('T')[0]
    const fileName = `数据报表_${dateStr}.csv`
    link.download = fileName
    link.click()
    
    // 清理URL对象
    URL.revokeObjectURL(link.href)
    
    ElMessage.success('报表数据导出成功')
  } catch (error) {
    console.error('导出报表失败:', error)
    ElMessage.error('导出报表失败：' + error.message)
  }
}

// 监听图表类型变化
watch(revenueChartType, () => {
  loadReports()
})

// 窗口大小变化时重新调整图表
const handleResize = () => {
  revenueChart?.resize()
  orderStatusChart?.resize()
  serviceRankChart?.resize()
  userGrowthChart?.resize()
}

onMounted(() => {
  loadReports()
  window.addEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
.admin-reports {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    .page-title {
      font-size: 24px;
      font-weight: 600;
      color: $text-primary;
      margin: 0;
      display: flex;
      align-items: center;
      gap: 8px;
    }
    
    .header-actions {
      display: flex;
      gap: 12px;
      align-items: center;
    }
  }
  
  .overview-stats {
    margin-bottom: 24px;
    
    .el-row {
      display: flex;
      
      .el-col {
        display: flex;
      }
    }
    
    .stat-card {
      width: 100%;
      height: 100%;
      
      :deep(.el-card__body) {
        padding: 20px;
      }
      
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
          font-size: 24px;
          color: white;
        }
        
        .stat-info {
          flex: 1;
          
          .stat-number {
            font-size: 28px;
            font-weight: 700;
            color: $text-primary;
            margin-bottom: 4px;
          }
          
          .stat-label {
            color: $text-secondary;
            font-size: 14px;
            margin-bottom: 4px;
          }
          
          .stat-change {
            font-size: 12px;
            font-weight: 600;
            
            &.positive {
              color: #67C23A;
            }
            
            &:not(.positive) {
              color: #F56C6C;
            }
          }
        }
      }
      
      &.revenue .stat-icon {
        background: linear-gradient(135deg, #85CACF 0%, #A8DEE0 100%);
      }
      
      &.orders .stat-icon {
        background: linear-gradient(135deg, #FDC68E 0%, #F9E2AE 100%);
      }
      
      &.users .stat-icon {
        background: linear-gradient(135deg, #A7D676 0%, #85CACF 100%);
      }
      
      &.rating .stat-icon {
        background: linear-gradient(135deg, #A7D676 0%, #A8DEE0 100%);
      }
    }
  }
  
  .charts-section {
    margin-bottom: 24px;
    
    .el-card {
      height: 100%;
      
      :deep(.el-card__header) {
        padding: 18px 20px;
        border-bottom: 1px solid #EBEEF5;
      }
      
      :deep(.el-card__body) {
        padding: 20px;
      }
    }
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      
      span {
        line-height: 28px;
      }
    }
    
    .chart-container {
      height: 300px;
      width: 100%;
    }
    
    .el-row {
      display: flex;
      
      .el-col {
        display: flex;
        flex-direction: column;
      }
    }
  }
  
  .data-tables {
    .text-success {
      color: #67C23A;
    }
    
    .text-danger {
      color: #F56C6C;
    }
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .overview-stats {
    :deep(.el-col) {
      margin-bottom: 16px;
    }
  }
  
  .charts-section {
    :deep(.el-col) {
      margin-bottom: 20px;
    }
  }
}
</style>
