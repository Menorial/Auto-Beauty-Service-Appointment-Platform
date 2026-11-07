<template>
  <div class="admin-appointments">
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Calendar /></el-icon>
        预约管理
      </h2>
      <div class="header-actions">
        <el-button type="success" size="large" @click="exportData" :icon="Download">
          导出Excel数据
        </el-button>
      </div>
    </div>
    
    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ stats.pending }}</div>
          <div class="stat-label">待确认</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ stats.confirmed }}</div>
          <div class="stat-label">已确认</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ stats.inProgress }}</div>
          <div class="stat-label">进行中</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ stats.completed }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </el-card>
    </div>
    
    <!-- 搜索和筛选 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="订单号">
          <el-input
            v-model="searchForm.orderNo"
            placeholder="请输入订单号"
            @keyup.enter="handleSearch"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="用户姓名">
          <el-input
            v-model="searchForm.userName"
            placeholder="请输入用户姓名"
            @keyup.enter="handleSearch"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="预约状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待确认" value="PENDING" />
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="预约日期">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 预约列表 -->
    <el-card>
      <el-table :data="appointments" v-loading="loading" stripe>
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="orderNo" label="订单号" width="180" />
        
        <el-table-column label="用户信息" width="120">
          <template #default="{ row }">
            <div>{{ row.user?.realName || row.user?.username }}</div>
            <div class="text-secondary">{{ row.user?.phone }}</div>
          </template>
        </el-table-column>
        
        <el-table-column label="车辆信息" width="150">
          <template #default="{ row }">
            <div>{{ row.vehicle?.brand }} {{ row.vehicle?.model }}</div>
            <div class="text-secondary">{{ row.vehicle?.licensePlate }}</div>
          </template>
        </el-table-column>
        
        <el-table-column prop="service.name" label="服务项目" min-width="150" />
        
        <el-table-column label="预约时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.appointmentTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="技师" width="100">
          <template #default="{ row }">
            {{ row.employee?.name || '未分配' }}
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="金额" width="100">
          <template #default="{ row }">
            ¥{{ row.service?.price }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-dropdown @command="(command) => handleAction(command, row)">
              <el-button size="small">
                操作 <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="view">查看详情</el-dropdown-item>
                  <el-dropdown-item 
                    command="confirm" 
                    v-if="row.status === 'PENDING'"
                  >
                    确认预约
                  </el-dropdown-item>
                  <el-dropdown-item 
                    command="assign" 
                    v-if="['PENDING', 'CONFIRMED'].includes(row.status)"
                  >
                    分配技师
                  </el-dropdown-item>
                  <el-dropdown-item 
                    command="start" 
                    v-if="row.status === 'CONFIRMED'"
                  >
                    开始服务
                  </el-dropdown-item>
                  <el-dropdown-item 
                    command="complete" 
                    v-if="row.status === 'IN_PROGRESS'"
                  >
                    完成服务
                  </el-dropdown-item>
                  <el-dropdown-item 
                    command="cancel" 
                    v-if="!['COMPLETED', 'CANCELLED'].includes(row.status)"
                    divided
                  >
                    取消预约
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadAppointments"
          @current-change="loadAppointments"
        />
      </div>
    </el-card>
    
    <!-- 分配技师对话框 -->
    <el-dialog v-model="showAssignDialog" title="分配技师" width="400px">
      <el-form>
        <el-form-item label="选择技师">
          <el-select v-model="selectedEmployee" placeholder="请选择技师" style="width: 100%">
            <el-option
              v-for="employee in availableEmployees"
              :key="employee.id"
              :label="employee.name"
              :value="employee.id"
            >
              <span>{{ employee.name }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">
                {{ employee.skills }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showAssignDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmAssign" :loading="assigning">
          确定分配
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 预约详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="预约详情" width="600px">
      <div v-if="selectedAppointment">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">
            {{ selectedAppointment.orderNo }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(selectedAppointment.status)">
              {{ getStatusText(selectedAppointment.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="用户姓名">
            {{ selectedAppointment.user?.realName || selectedAppointment.user?.username }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话">
            {{ selectedAppointment.user?.phone }}
          </el-descriptions-item>
          <el-descriptions-item label="车辆信息">
            {{ selectedAppointment.vehicle?.brand }} {{ selectedAppointment.vehicle?.model }}
          </el-descriptions-item>
          <el-descriptions-item label="车牌号">
            {{ selectedAppointment.vehicle?.licensePlate }}
          </el-descriptions-item>
          <el-descriptions-item label="服务项目">
            {{ selectedAppointment.service?.name }}
          </el-descriptions-item>
          <el-descriptions-item label="服务价格">
            ¥{{ selectedAppointment.service?.price }}
          </el-descriptions-item>
          <el-descriptions-item label="预约时间">
            {{ formatDateTime(selectedAppointment.appointmentTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="服务时长">
            {{ selectedAppointment.service?.duration }}分钟
          </el-descriptions-item>
          <el-descriptions-item label="分配技师">
            {{ selectedAppointment.employee?.name || '未分配' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDateTime(selectedAppointment.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2" v-if="selectedAppointment.notes">
            {{ selectedAppointment.notes }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAllAppointments, getAdminStats, updateAppointmentStatus, getAllEmployees, assignEmployee } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Edit, Delete, Check, Close, Download, Calendar, RefreshLeft } from '@element-plus/icons-vue'
import { formatDateTime } from '@/utils/dateUtils'

const router = useRouter()

const loading = ref(false)
const assigning = ref(false)
const showAssignDialog = ref(false)
const showDetailDialog = ref(false)

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const appointments = ref([])
const availableEmployees = ref([])
const selectedAppointment = ref(null)
const selectedEmployee = ref('')

const stats = ref({
  pending: 0,
  confirmed: 0,
  inProgress: 0,
  completed: 0
})

const searchForm = reactive({
  orderNo: '',
  userName: '',
  status: '',
  dateRange: []
})

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

const loadAppointments = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    
    // 只添加非空的搜索参数
    if (searchForm.orderNo) {
      params.orderNo = searchForm.orderNo
    }
    if (searchForm.userName) {
      params.userName = searchForm.userName
    }
    if (searchForm.status) {
      params.status = searchForm.status
    }
    
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    
    console.log('请求参数：', params)
    
    const response = await getAllAppointments(params)
    if (response.success) {
      appointments.value = response.data.content || response.data
      total.value = response.data.totalElements || response.data.length
      
      // 加载真实的统计数据
      await loadStats()
    }
  } catch (error) {
    console.error('加载预约列表失败:', error)
    ElMessage.error('加载预约列表失败')
  } finally {
    loading.value = false
  }
}

// 加载真实的统计数据（从数据库查询）
const loadStats = async () => {
  try {
    const response = await getAdminStats()
    if (response && response.success && response.data) {
      stats.value = {
        pending: response.data.pendingAppointments || 0,
        confirmed: response.data.confirmedAppointments || 0,
        inProgress: response.data.inProgressAppointments || 0,
        completed: response.data.completedAppointments || 0,
        total: response.data.totalAppointments || 0
      }
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadEmployees = async () => {
  try {
    const response = await getAllEmployees({ status: 'ACTIVE' })
    if (response.success) {
      availableEmployees.value = response.data.content || response.data
    }
  } catch (error) {
    console.error('加载员工列表失败:', error)
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadAppointments()
}

const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = key === 'dateRange' ? [] : ''
  })
  handleSearch()
}

const handleAction = async (command, appointment) => {
  selectedAppointment.value = appointment
  
  switch (command) {
    case 'view':
      showDetailDialog.value = true
      break
    case 'confirm':
      await updateStatus(appointment.id, 'CONFIRMED')
      break
    case 'assign':
      await loadEmployees()
      showAssignDialog.value = true
      break
    case 'start':
      await updateStatus(appointment.id, 'IN_PROGRESS')
      break
    case 'complete':
      await updateStatus(appointment.id, 'COMPLETED')
      break
    case 'cancel':
      await cancelAppointment(appointment)
      break
  }
}

const updateStatus = async (appointmentId, status) => {
  try {
    const response = await updateAppointmentStatus(appointmentId, status)
    if (response.success) {
      ElMessage.success('状态更新成功')
      await loadAppointments()
    }
  } catch (error) {
    console.error('更新状态失败:', error)
    ElMessage.error('更新状态失败')
  }
}

const confirmAssign = async () => {
  if (!selectedEmployee.value) {
    ElMessage.warning('请选择技师')
    return
  }
  
  try {
    assigning.value = true
    const response = await assignEmployee(selectedAppointment.value.id, selectedEmployee.value)
    
    if (response.success) {
      ElMessage.success('技师分配成功')
      showAssignDialog.value = false
      selectedEmployee.value = ''
      await loadAppointments()
    }
  } catch (error) {
    console.error('分配技师失败:', error)
    ElMessage.error('分配技师失败')
  } finally {
    assigning.value = false
  }
}

const cancelAppointment = async (appointment) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消订单 ${appointment.orderNo} 吗？`,
      '取消预约',
      {
        confirmButtonText: '确定取消',
        cancelButtonText: '不取消',
        type: 'warning'
      }
    )
    
    await updateStatus(appointment.id, 'CANCELLED')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消预约失败:', error)
    }
  }
}

const getPaymentStatusText = (paymentStatus) => {
  const paymentStatusMap = {
    'UNPAID': '未支付',
    'PAID': '已支付',
    'REFUNDED': '已退款'
  }
  return paymentStatusMap[paymentStatus] || paymentStatus
}

const exportData = () => {
  try {
    // 检查是否有数据
    if (!appointments.value || appointments.value.length === 0) {
      ElMessage.warning('暂无数据可导出')
      return
    }
    
    // 准备导出数据
    const exportList = appointments.value.map((apt, index) => {
      // 格式化日期为简洁格式
      let appointmentTime = '-'
      if (apt.appointmentTime) {
        const timeStr = formatDateTime(apt.appointmentTime)
        // 添加制表符前缀，让Excel识别为文本
        appointmentTime = '\t' + timeStr
      }
      
      return {
        '序号': index + 1,
        '订单号': apt.orderNo || '-',
        '用户姓名': apt.user?.realName || apt.user?.username || '-',
        '联系电话': apt.user?.phone || '-',
        '车辆信息': apt.vehicle ? `${apt.vehicle.brand} ${apt.vehicle.model} (${apt.vehicle.licensePlate})` : '-',
        '服务项目': apt.service?.name || '-',
        '预约时间': appointmentTime,
        '预约状态': getStatusText(apt.status) || '-',
        '支付状态': getPaymentStatusText(apt.paymentStatus) || '-',
        '服务金额': apt.totalAmount || 0,
        '员工': apt.employee?.name || '未分配',
        '备注': apt.notes || '-'
      }
    })
    
    // 转换为CSV格式
    const headers = Object.keys(exportList[0])
    const csvContent = [
      headers.join(','),
      ...exportList.map(row => headers.map(header => {
        let value = String(row[header] || '')
        // 处理包含逗号、换行或引号的值，用引号包裹并转义引号
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
    const fileName = `预约数据_${new Date().toISOString().split('T')[0]}.csv`
    link.download = fileName
    link.click()
    
    // 清理URL对象
    URL.revokeObjectURL(link.href)
    
    ElMessage.success(`已导出 ${exportList.length} 条预约数据`)
  } catch (error) {
    console.error('导出数据失败:', error)
    ElMessage.error('导出数据失败：' + error.message)
  }
}

onMounted(() => {
  loadAppointments()
})
</script>

<style lang="scss" scoped>
.admin-appointments {
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
    }
  }
  
  .stats-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 16px;
    margin-bottom: 24px;
    
    .stat-card {
      text-align: center;
      
      .stat-content {
        .stat-number {
          font-size: 32px;
          font-weight: 700;
          color: $primary-color;
          margin-bottom: 8px;
        }
        
        .stat-label {
          color: $text-secondary;
          font-size: 14px;
        }
      }
    }
  }
  
  .search-card {
    margin-bottom: 24px;
  }
  
  .text-secondary {
    color: $text-secondary;
    font-size: 12px;
  }
  
  .pagination {
    display: flex;
    justify-content: center;
    margin-top: 24px;
  }
}

@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
}
</style>
