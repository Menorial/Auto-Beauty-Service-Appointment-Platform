<template>
  <div class="employee-appointments">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">我的预约</span>
          <div class="filters">
            <el-input 
              v-model="searchKeyword" 
              placeholder="搜索订单号或车牌号"
              clearable
              style="width: 200px; margin-right: 10px"
              @input="loadAppointments"
            />
            <el-select 
              v-model="filterStatus" 
              placeholder="筛选状态"
              clearable
              style="width: 150px"
              @change="loadAppointments"
            >
              <el-option label="全部" value="" />
              <el-option label="待确认" value="PENDING" />
              <el-option label="已确认" value="CONFIRMED" />
              <el-option label="进行中" value="IN_PROGRESS" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>
          </div>
        </div>
      </template>
      
      <el-table 
        :data="appointments" 
        stripe 
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="orderNo" label="订单号" width="150" />
        <el-table-column prop="appointmentTime" label="预约时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.appointmentTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="service.name" label="服务项目" width="150" />
        <el-table-column prop="vehicle.licensePlate" label="车牌号" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentStatus" label="支付状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getPaymentStatusType(row.paymentStatus)">
              {{ getPaymentStatusText(row.paymentStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              @click="goToDetail(row.id)"
            >
              详情
            </el-button>
            <el-button 
              v-if="row.status === 'CONFIRMED'"
              type="success" 
              size="small" 
              @click="updateStatus(row.id, 'IN_PROGRESS')"
            >
              开始
            </el-button>
            <el-button 
              v-if="row.status === 'IN_PROGRESS'"
              type="warning" 
              size="small" 
              @click="updateStatus(row.id, 'COMPLETED')"
            >
              完成
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @change="loadAppointments"
        style="margin-top: 20px; text-align: right"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getEmployeeAppointments } from '@/api/employee'
import { updateAppointmentStatus } from '@/api/appointment'
import { formatDateTime } from '@/utils/dateUtils'

const router = useRouter()

const appointments = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterStatus = ref('')
const searchKeyword = ref('')

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

const getPaymentStatusType = (status) => {
  const statusMap = {
    'UNPAID': 'danger',
    'PAID': 'success',
    'REFUNDED': 'warning'
  }
  return statusMap[status] || 'info'
}

const getPaymentStatusText = (status) => {
  const statusMap = {
    'UNPAID': '未支付',
    'PAID': '已支付',
    'REFUNDED': '已退款'
  }
  return statusMap[status] || status
}

const loadAppointments = async () => {
  try {
    loading.value = true
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    if (filterStatus.value) {
      params.status = filterStatus.value
    }
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    
    const response = await getEmployeeAppointments(params)
    if (response.success) {
      // 前端本地搜索过滤（如果后端不支持）
      let filteredData = response.data.content || []
      if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase()
        filteredData = filteredData.filter(item => 
          item.orderNo?.toLowerCase().includes(keyword) ||
          item.vehicle?.licensePlate?.toLowerCase().includes(keyword)
        )
      }
      appointments.value = filteredData
      total.value = response.data.totalElements || 0
    }
  } catch (error) {
    ElMessage.error('加载预约列表失败')
  } finally {
    loading.value = false
  }
}

const goToDetail = (id) => {
  router.push(`/employee/appointments/${id}`)
}

const updateStatus = async (id, newStatus) => {
  try {
    const statusText = {
      'IN_PROGRESS': '进行中',
      'COMPLETED': '已完成'
    }
    
    ElMessageBox.confirm(`确定要将预约状态更新为"${statusText[newStatus]}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      const response = await updateAppointmentStatus(id, newStatus)
      if (response.success) {
        ElMessage.success('状态更新成功')
        loadAppointments()
      }
    }).catch(() => {})
  } catch (error) {
    ElMessage.error('更新状态失败')
  }
}

onMounted(() => {
  loadAppointments()
})
</script>

<style lang="scss" scoped>
.employee-appointments {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .title {
      font-size: 16px;
      font-weight: bold;
    }
    
    .filters {
      display: flex;
      gap: 10px;
      
      :deep(.el-select) {
        width: 150px;
      }
    }
  }
}
</style>
