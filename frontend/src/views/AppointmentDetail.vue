<template>
  <div class="appointment-detail">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>预约详情</span>
          <el-button type="primary" @click="$router.go(-1)">返回</el-button>
        </div>
      </template>
      
      <div v-if="loading" class="loading">
        <el-skeleton :rows="5" animated />
      </div>
      
      <div v-else-if="appointment" class="appointment-info">
        <el-descriptions title="预约信息" :column="2" border>
          <el-descriptions-item label="订单号">{{ appointment.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="预约时间">{{ formatDate(appointment.appointmentTime) }}</el-descriptions-item>
          <el-descriptions-item label="服务项目">{{ appointment.serviceName }}</el-descriptions-item>
          <el-descriptions-item label="服务价格">¥{{ appointment.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="车辆信息">{{ appointment.vehicleInfo }}</el-descriptions-item>
          <el-descriptions-item label="预约状态">
            <el-tag :type="getStatusType(appointment.status)">{{ getStatusText(appointment.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="支付状态">
            <el-tag :type="getPaymentStatusType(appointment.paymentStatus)">{{ getPaymentStatusText(appointment.paymentStatus) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ appointment.notes || '无' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      
      <div v-else class="no-data">
        <el-empty description="预约信息不存在" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getAppointmentById } from '@/api/appointment'
import { formatDate } from '@/utils/date'

const route = useRoute()
const loading = ref(true)
const appointment = ref(null)

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

const getPaymentStatusType = (status) => {
  const types = {
    'UNPAID': 'warning',
    'PAID': 'success',
    'REFUNDED': 'info'
  }
  return types[status] || 'info'
}

const getPaymentStatusText = (status) => {
  const texts = {
    'UNPAID': '未支付',
    'PAID': '已支付',
    'REFUNDED': '已退款'
  }
  return texts[status] || status
}

const fetchAppointmentDetail = async () => {
  try {
    loading.value = true
    const response = await getAppointmentById(route.params.id)
    appointment.value = response.data
  } catch (error) {
    console.error('获取预约详情失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchAppointmentDetail()
})
</script>

<style scoped>
.appointment-detail {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.loading, .no-data {
  padding: 40px 0;
  text-align: center;
}

.appointment-info {
  padding: 20px 0;
}
</style>
