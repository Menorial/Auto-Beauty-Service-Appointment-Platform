<template>
  <div class="appointment-detail">
    <el-button @click="$router.back()" class="back-btn">
      <el-icon><ArrowLeft /></el-icon>
      返回列表
    </el-button>
    
    <el-row :gutter="20">
      <!-- 预约信息 -->
      <el-col :xs="24" :md="16">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span class="title">预约详情</span>
              <el-tag :type="getStatusType(appointment.status)">
                {{ getStatusText(appointment.status) }}
              </el-tag>
            </div>
          </template>
          
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单号">
              {{ appointment.orderNo }}
            </el-descriptions-item>
            <el-descriptions-item label="预约时间">
              {{ formatDateTime(appointment.appointmentTime) }}
            </el-descriptions-item>
            
            <el-descriptions-item label="服务项目">
              {{ appointment.service?.name }}
            </el-descriptions-item>
            <el-descriptions-item label="服务时长">
              {{ appointment.service?.duration }} 分钟
            </el-descriptions-item>
            
            <el-descriptions-item label="服务价格">
              ¥{{ appointment.totalAmount }}
            </el-descriptions-item>
            <el-descriptions-item label="支付状态">
              <el-tag :type="getPaymentStatusType(appointment.paymentStatus)">
                {{ getPaymentStatusText(appointment.paymentStatus) }}
              </el-tag>
            </el-descriptions-item>
            
            <el-descriptions-item label="车牌号" :span="2">
              {{ appointment.vehicle?.licensePlate }}
            </el-descriptions-item>
            
            <el-descriptions-item label="车型" :span="2">
              {{ appointment.vehicle?.brand }} {{ appointment.vehicle?.model }}
            </el-descriptions-item>
            
            <el-descriptions-item label="备注" :span="2">
              <div v-if="!editingNotes" class="notes-display">
                <p>{{ appointment.notes || '无' }}</p>
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="editingNotes = true"
                  style="margin-top: 8px"
                >
                  编辑备注
                </el-button>
              </div>
              <div v-else class="notes-edit">
                <el-input 
                  v-model="editedNotes" 
                  type="textarea" 
                  rows="4"
                  placeholder="请输入服务备注"
                />
                <div style="margin-top: 8px">
                  <el-button 
                    type="primary" 
                    size="small" 
                    @click="saveNotes"
                    :loading="savingNotes"
                  >
                    保存
                  </el-button>
                  <el-button 
                    size="small" 
                    @click="cancelEditNotes"
                  >
                    取消
                  </el-button>
                </div>
              </div>
            </el-descriptions-item>
            
            <el-descriptions-item label="创建时间" :span="2">
              {{ formatDateTime(appointment.createdAt) }}
            </el-descriptions-item>
          </el-descriptions>
          
          <!-- 操作按钮 -->
          <div class="action-buttons">
            <el-button 
              v-if="appointment.status === 'CONFIRMED'"
              type="success" 
              @click="updateStatus('IN_PROGRESS')"
            >
              开始服务
            </el-button>
            <el-button 
              v-if="appointment.status === 'IN_PROGRESS'"
              type="warning" 
              @click="updateStatus('COMPLETED')"
            >
              完成服务
            </el-button>
            <el-button @click="$router.back()">
              返回
            </el-button>
          </div>
        </el-card>
      </el-col>
      
      <!-- 客户信息 -->
      <el-col :xs="24" :md="8">
        <el-card class="box-card">
          <template #header>
            <span class="title">客户信息</span>
          </template>
          
          <div class="customer-info">
            <div class="info-item">
              <span class="label">客户ID：</span>
              <span class="value">{{ appointment.userId || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">车辆颜色：</span>
              <span class="value">{{ appointment.vehicle?.color || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">服务分类：</span>
              <span class="value">{{ appointment.service?.category || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">服务描述：</span>
              <span class="value">{{ appointment.service?.description || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">支付方式：</span>
              <span class="value">{{ appointment.paymentMethod || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">支付时间：</span>
              <span class="value">{{ appointment.paymentTime ? formatDateTime(appointment.paymentTime) : '-' }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAppointmentById, updateAppointmentStatus, updateAppointmentNotes } from '@/api/appointment'
import { ArrowLeft } from '@element-plus/icons-vue'
import { formatDateTime } from '@/utils/dateUtils'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const appointment = ref({
  service: {},
  vehicle: {}
})
const loading = ref(false)
const editingNotes = ref(false)
const editedNotes = ref('')
const savingNotes = ref(false)

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

const updateStatus = async (newStatus) => {
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
      const response = await updateAppointmentStatus(route.params.id, newStatus)
      if (response.success) {
        ElMessage.success('状态更新成功')
        loadDetail()
      }
    }).catch(() => {})
  } catch (error) {
    ElMessage.error('更新状态失败')
  }
}

const loadDetail = async () => {
  try {
    loading.value = true
    const response = await getAppointmentById(route.params.id)
    if (response.success) {
      appointment.value = response.data
      editedNotes.value = response.data.notes || ''
    } else {
      ElMessage.error(response.message || '加载失败')
    }
  } catch (error) {
    ElMessage.error('加载预约详情失败')
  } finally {
    loading.value = false
  }
}

const saveNotes = async () => {
  try {
    savingNotes.value = true
    
    // 调用后端API保存备注
    const response = await updateAppointmentNotes(route.params.id, editedNotes.value)
    
    if (response.success) {
      appointment.value.notes = editedNotes.value
      editingNotes.value = false
      ElMessage.success('备注已保存')
    } else {
      ElMessage.error(response.message || '保存备注失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '保存备注失败')
  } finally {
    savingNotes.value = false
  }
}

const cancelEditNotes = () => {
  editedNotes.value = appointment.value.notes || ''
  editingNotes.value = false
}

onMounted(() => {
  loadDetail()
})
</script>

<style lang="scss" scoped>
.appointment-detail {
  .back-btn {
    margin-bottom: 20px;
  }
  
  .box-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .title {
        font-size: 16px;
        font-weight: bold;
      }
    }
    
    .action-buttons {
      display: flex;
      gap: 10px;
      margin-top: 20px;
      justify-content: flex-end;
    }
    
    .customer-info {
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
    }
  }
}
</style>
