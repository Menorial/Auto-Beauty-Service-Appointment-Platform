<template>
  <div class="appointment-calendar">
    <el-card>
      <template #header>
        <span class="title-with-icon">
          <el-icon><Calendar /></el-icon>
          预约日历
        </span>
      </template>
      
      <el-calendar v-model="selectedDate">
        <template #date-cell="{ data }">
          <div class="calendar-cell">
            <div class="date">{{ data.day.split('-').slice(-1)[0] }}</div>
            <div v-if="getAppointmentsForDate(data.day).length > 0" class="appointments">
              <el-badge 
                :value="getAppointmentsForDate(data.day).length" 
                :max="99"
                class="appointment-badge"
              >
                <div class="appointment-indicator"></div>
              </el-badge>
            </div>
          </div>
        </template>
      </el-calendar>
      
      <el-drawer v-model="drawerVisible" title="当日预约" size="50%">
        <div v-if="selectedDateAppointments.length > 0">
          <el-timeline>
            <el-timeline-item
              v-for="appointment in selectedDateAppointments"
              :key="appointment.id"
              :timestamp="formatTime(appointment.appointmentTime)"
            >
              <el-card shadow="hover">
                <div class="appointment-item">
                  <div class="appointment-info">
                    <h4>{{ appointment.serviceName }}</h4>
                    <p>客户: {{ appointment.userName }}</p>
                    <p>车辆: {{ appointment.vehicleInfo }}</p>
                    <p>金额: ¥{{ appointment.totalAmount }}</p>
                  </div>
                  <div class="appointment-status">
                    <el-tag :type="getStatusType(appointment.status)">
                      {{ getStatusText(appointment.status) }}
                    </el-tag>
                  </div>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </div>
        <el-empty v-else description="当日无预约" />
      </el-drawer>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { getAppointmentsByDate } from '@/api/appointment'
import { formatDate, formatTime } from '@/utils/date'
import { Calendar } from '@element-plus/icons-vue'

const selectedDate = ref(new Date())
const drawerVisible = ref(false)
const appointments = ref([])

const selectedDateAppointments = computed(() => {
  const dateStr = formatDate(selectedDate.value, 'YYYY-MM-DD')
  return getAppointmentsForDate(dateStr)
})

const getAppointmentsForDate = (dateStr) => {
  // 确保appointments.value是数组
  if (!Array.isArray(appointments.value)) {
    console.warn('appointments.value is not an array:', appointments.value)
    return []
  }
  
  return appointments.value.filter(appointment => {
    const appointmentDate = formatDate(appointment.appointmentTime, 'YYYY-MM-DD')
    return appointmentDate === dateStr
  })
}

const getStatusType = (status) => {
  const types = {
    'PENDING': 'warning',
    'CONFIRMED': 'primary',
    'IN_PROGRESS': 'info',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    'PENDING': '待确认',
    'CONFIRMED': '已确认',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return texts[status] || status
}

const fetchAppointments = async () => {
  try {
    // 获取当月的预约数据
    const startDate = new Date(selectedDate.value.getFullYear(), selectedDate.value.getMonth(), 1)
    const endDate = new Date(selectedDate.value.getFullYear(), selectedDate.value.getMonth() + 1, 0)
    
    console.log('正在获取预约数据...', {
      startDate: formatDate(startDate, 'YYYY-MM-DD'),
      endDate: formatDate(endDate, 'YYYY-MM-DD')
    })
    
    const response = await getAppointmentsByDate({
      startDate: formatDate(startDate, 'YYYY-MM-DD'),
      endDate: formatDate(endDate, 'YYYY-MM-DD')
    })
    
    console.log('API响应:', response)
    console.log('响应数据类型:', typeof response)
    console.log('响应数据:', response.data)
    
    // 处理不同的响应格式
    let appointmentData = []
    if (response && response.success && response.data) {
      // 如果是标准的API响应格式
      appointmentData = Array.isArray(response.data) ? response.data : 
                       (response.data.content ? response.data.content : [])
    } else if (Array.isArray(response)) {
      // 如果直接返回数组
      appointmentData = response
    } else if (response && Array.isArray(response.data)) {
      // 如果data字段是数组
      appointmentData = response.data
    }
    
    console.log('处理后的预约数据:', appointmentData)
    appointments.value = appointmentData
    
  } catch (error) {
    console.error('获取预约数据失败:', error)
    console.error('错误详情:', error.response || error.message)
    appointments.value = [] // 确保在错误时设置为空数组
  }
}

onMounted(() => {
  fetchAppointments()
})
</script>

<style scoped>
.appointment-calendar {
  padding: 20px;
}

.title-with-icon {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.calendar-cell {
  height: 60px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
}

.date {
  font-size: 16px;
  font-weight: bold;
}

.appointments {
  margin-top: 5px;
}

.appointment-badge {
  cursor: pointer;
}

.appointment-indicator {
  width: 8px;
  height: 8px;
  background-color: #409eff;
  border-radius: 50%;
}

.appointment-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.appointment-info h4 {
  margin: 0 0 10px 0;
  color: #303133;
}

.appointment-info p {
  margin: 5px 0;
  color: #606266;
  font-size: 14px;
}

.appointment-status {
  flex-shrink: 0;
}
</style>
