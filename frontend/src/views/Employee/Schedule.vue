<template>
  <div class="employee-schedule">
    <el-card>
      <template #header>
        <span class="title">工作排班</span>
      </template>
      
      <el-alert 
        title="工作排班说明" 
        type="info" 
        :closable="false"
        description="这里显示您的工作排班信息。如需修改工作排班，请联系管理员。"
        style="margin-bottom: 20px"
      />
      
      <el-row :gutter="20">
        <el-col :xs="24" :md="12">
          <div class="schedule-card">
            <h3>当前排班信息</h3>
            <div class="schedule-content">
              <div class="schedule-item">
                <span class="label">工作排班：</span>
                <span class="value">{{ scheduleInfo.workSchedule || '未设置' }}</span>
              </div>
              <div class="schedule-item">
                <span class="label">专长技能：</span>
                <span class="value">{{ scheduleInfo.skills || '未设置' }}</span>
              </div>
              <div class="schedule-item">
                <span class="label">工作状态：</span>
                <span class="value">
                  <el-tag :type="scheduleInfo.status === 1 ? 'success' : 'danger'">
                    {{ scheduleInfo.status === 1 ? '在职' : '离职' }}
                  </el-tag>
                </span>
              </div>
            </div>
          </div>
        </el-col>
        
        <el-col :xs="24" :md="12">
          <div class="schedule-card">
            <h3>本周工作统计</h3>
            <div class="schedule-content">
              <div class="schedule-item">
                <span class="label">本周预约数：</span>
                <span class="value">{{ weekStats.appointments || 0 }}</span>
              </div>
              <div class="schedule-item">
                <span class="label">本周完成数：</span>
                <span class="value">{{ weekStats.completed || 0 }}</span>
              </div>
              <div class="schedule-item">
                <span class="label">完成率：</span>
                <span class="value">
                  {{ weekStats.appointments > 0 
                    ? ((weekStats.completed / weekStats.appointments) * 100).toFixed(1) 
                    : 0 }}%
                </span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
      
      <!-- 工作日历 -->
      <div class="calendar-section" style="margin-top: 30px">
        <h3>工作日历</h3>
        <el-calendar v-model="selectedDate">
          <template #default="{ data }">
            <div class="calendar-item">
              <span class="date">{{ data.day.split('-').pop() }}</span>
              <div class="appointments-count">
                预约: {{ getAppointmentsForDate(data.day) }}
              </div>
            </div>
          </template>
        </el-calendar>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCurrentEmployeeInfo, getTodayAppointments } from '@/api/employee'

const selectedDate = ref(new Date())

const scheduleInfo = ref({
  workSchedule: '',
  skills: '',
  status: 1
})

const weekStats = ref({
  appointments: 0,
  completed: 0
})

const appointmentsByDate = ref({})

const loading = ref(false)

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', { 
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getAppointmentsForDate = (dateStr) => {
  return appointmentsByDate.value[dateStr] || 0
}

const loadScheduleInfo = async () => {
  try {
    loading.value = true
    const response = await getCurrentEmployeeInfo()
    if (response.success) {
      scheduleInfo.value = response.data
    }
  } catch (error) {
    console.error('加载排班信息失败:', error)
    ElMessage.error('加载排班信息失败')
  } finally {
    loading.value = false
  }
}

const loadAppointments = async () => {
  try {
    const response = await getTodayAppointments()
    if (response.success) {
      const appointments = response.data
      
      // 统计本周数据
      const now = new Date()
      const weekStart = new Date(now)
      weekStart.setDate(now.getDate() - now.getDay())
      const weekEnd = new Date(weekStart)
      weekEnd.setDate(weekStart.getDate() + 6)
      
      let weekAppointments = 0
      let weekCompleted = 0
      
      appointments.forEach(apt => {
        const aptDate = new Date(apt.appointmentTime)
        if (aptDate >= weekStart && aptDate <= weekEnd) {
          weekAppointments++
          if (apt.status === 'COMPLETED') {
            weekCompleted++
          }
        }
        
        // 按日期统计
        const dateStr = aptDate.toISOString().split('T')[0]
        appointmentsByDate.value[dateStr] = (appointmentsByDate.value[dateStr] || 0) + 1
      })
      
      weekStats.value = {
        appointments: weekAppointments,
        completed: weekCompleted
      }
    }
  } catch (error) {
    // 加载预约数据失败，静默处理
  }
}

onMounted(() => {
  loadScheduleInfo()
  loadAppointments()
})
</script>

<style lang="scss" scoped>
.employee-schedule {
  .title {
    font-size: 16px;
    font-weight: bold;
  }
  
  .schedule-card {
    background: #f5f7fa;
    border-radius: 8px;
    padding: 20px;
    
    h3 {
      margin-top: 0;
      margin-bottom: 15px;
      font-size: 16px;
      color: #333;
    }
    
    .schedule-content {
      .schedule-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 10px 0;
        border-bottom: 1px solid #e0e0e0;
        
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
    }
  }
  
  .calendar-section {
    h3 {
      margin-bottom: 15px;
      font-size: 16px;
      color: #333;
    }
    
    :deep(.el-calendar) {
      .el-calendar__body {
        padding: 0;
      }
      
      .calendar-item {
        height: 80px;
        padding: 10px;
        border: 1px solid #e0e0e0;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        
        .date {
          font-weight: bold;
          font-size: 16px;
        }
        
        .appointments-count {
          font-size: 12px;
          color: #666;
        }
      }
    }
  }
}
</style>
