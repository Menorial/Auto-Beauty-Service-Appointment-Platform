<template>
  <div class="employee-dashboard">
    <el-row :gutter="20" class="stats-row">
      <!-- 统计卡片 -->
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-icon total">
            <el-icon><DocumentCopy /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.totalAppointments || 0 }}</div>
            <div class="stat-label">总预约数</div>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-icon pending">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.pendingAppointments || 0 }}</div>
            <div class="stat-label">待处理预约</div>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-icon completed">
            <el-icon><SuccessFilled /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.completedAppointments || 0 }}</div>
            <div class="stat-label">已完成预约</div>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-icon cancelled">
            <el-icon><CircleCloseFilled /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.cancelledAppointments || 0 }}</div>
            <div class="stat-label">已取消预约</div>
          </div>
        </div>
      </el-col>
    </el-row>
    
    <!-- 今日预约 -->
    <el-row :gutter="20" class="content-row">
      <el-col :xs="24" :md="12">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span class="title">
                <el-icon><Calendar /></el-icon>
                今日预约
              </span>
              <el-button type="primary" size="small" @click="goToAppointments">
                查看全部
              </el-button>
            </div>
          </template>
          
          <el-empty v-if="todayAppointments.length === 0" description="今天没有预约" />
          
          <el-table v-else :data="todayAppointments" stripe size="small">
            <el-table-column prop="appointmentTime" label="预约时间" width="150">
              <template #default="{ row }">
                {{ formatDateTime(row.appointmentTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="service.name" label="服务项目" />
            <el-table-column prop="status" label="状态">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="goToAppointmentDetail(row.id)"
                >
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <!-- 员工信息 -->
      <el-col :xs="24" :md="12">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span class="title">
                <el-icon><User /></el-icon>
                个人信息
              </span>
            </div>
          </template>
          
          <div class="employee-info">
            <div class="info-item">
              <span class="label">姓名：</span>
              <span class="value">{{ employeeInfo.name }}</span>
            </div>
            <div class="info-item">
              <span class="label">电话：</span>
              <span class="value">{{ employeeInfo.phone }}</span>
            </div>
            <div class="info-item">
              <span class="label">专长：</span>
              <span class="value">{{ employeeInfo.skills }}</span>
            </div>
            <div class="info-item">
              <span class="label">工作排班：</span>
              <span class="value">{{ employeeInfo.workSchedule }}</span>
            </div>
            <div class="info-item">
              <span class="label">状态：</span>
              <span class="value">
                <el-tag :type="employeeInfo.status === 1 ? 'success' : 'danger'">
                  {{ employeeInfo.status === 1 ? '在职' : '离职' }}
                </el-tag>
              </span>
            </div>
            <el-button type="primary" @click="goToProfile" class="edit-btn">
              编辑信息
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getEmployeeDashboard, getAppointmentStatistics, getTodayAppointments } from '@/api/employee'
import { DocumentCopy, Clock, SuccessFilled, CircleCloseFilled, Calendar, User } from '@element-plus/icons-vue'
import { formatDateTime } from '@/utils/dateUtils'

const router = useRouter()

const statistics = ref({
  totalAppointments: 0,
  pendingAppointments: 0,
  completedAppointments: 0,
  cancelledAppointments: 0
})

const todayAppointments = ref([])
const employeeInfo = ref({
  name: '',
  phone: '',
  skills: '',
  workSchedule: '',
  status: 1
})

const loading = ref(false)

const getStatusType = (status) => {
  const statusMap = {
    'PENDING': 'warning',
    'CONFIRMED': 'info',
    'IN_PROGRESS': 'primary',
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

const goToAppointments = () => {
  router.push('/employee/appointments')
}

const goToAppointmentDetail = (id) => {
  router.push(`/employee/appointments/${id}`)
}

const goToProfile = () => {
  router.push('/employee/profile')
}

const loadDashboard = async () => {
  try {
    loading.value = true
    const response = await getEmployeeDashboard()
    if (response.success) {
      const data = response.data
      if (data.employeeInfo) {
        employeeInfo.value = data.employeeInfo
      }
      todayAppointments.value = data.todayAppointments || []
      statistics.value = {
        totalAppointments: data.totalAppointments || 0,
        completedAppointments: data.completedAppointments || 0,
        pendingAppointments: data.pendingAppointments || 0,
        cancelledAppointments: data.cancelledAppointments || 0
      }
    }
  } catch (error) {
    ElMessage.error('加载仪表板失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDashboard()
})
</script>

<style lang="scss" scoped>
.employee-dashboard {
  .stats-row {
    margin-bottom: 20px;
    
    .stat-card {
      display: flex;
      align-items: center;
      background: white;
      border-radius: 8px;
      padding: 20px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
      }
      
      .stat-icon {
        width: 60px;
        height: 60px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 20px;
        font-size: 28px;
        color: white;
        
        &.total {
          background: linear-gradient(135deg, #85CACF 0%, #A8DEE0 100%);
        }
        
        &.pending {
          background: linear-gradient(135deg, #FDC68E 0%, #F9E2AE 100%);
        }
        
        &.completed {
          background: linear-gradient(135deg, #A7D676 0%, #85CACF 100%);
        }
        
        &.cancelled {
          background: linear-gradient(135deg, #F9E2AE 0%, #FDC68E 100%);
        }
      }
      
      .stat-content {
        .stat-value {
          font-size: 28px;
          font-weight: bold;
          color: #333;
        }
        
        .stat-label {
          font-size: 14px;
          color: #999;
          margin-top: 4px;
        }
      }
    }
  }
  
  .content-row {
    .box-card {
      height: 100%;
      
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .title {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 16px;
          font-weight: bold;
        }
      }
      
      .employee-info {
        .info-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 12px 0;
          border-bottom: 1px solid #f0f0f0;
          
          .label {
            color: #666;
            font-weight: 500;
          }
          
          .value {
            color: #333;
          }
          
          &:last-child {
            border-bottom: none;
          }
        }
        
        .edit-btn {
          width: 100%;
          margin-top: 20px;
        }
      }
    }
  }
}
</style>
