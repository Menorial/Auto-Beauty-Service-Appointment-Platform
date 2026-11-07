<template>
  <div class="appointments-page">
    <Header />
    
    <div class="container">
      <div class="page-header">
        <h1>我的预约</h1>
        <p>管理您的所有预约记录</p>
      </div>
      
      <div class="appointments-content">
        <!-- 筛选标签 -->
        <div class="filter-tabs">
          <el-tabs v-model="activeStatus" @tab-change="handleStatusChange">
            <el-tab-pane label="全部" name="all" />
            <el-tab-pane label="待确认" name="PENDING" />
            <el-tab-pane label="已确认" name="CONFIRMED" />
            <el-tab-pane label="进行中" name="IN_PROGRESS" />
            <el-tab-pane label="已完成" name="COMPLETED" />
            <el-tab-pane label="已取消" name="CANCELLED" />
          </el-tabs>
        </div>
        
        <!-- 预约列表 -->
        <div class="appointments-list" v-loading="loading">
          <div v-if="appointments.length > 0" class="appointments-grid">
            <el-card 
              v-for="appointment in appointments" 
              :key="appointment.id"
              class="appointment-card"
            >
              <div class="appointment-header">
                <div class="appointment-info">
                  <h3>{{ appointment.service?.name }}</h3>
                  <p class="order-number">订单号：{{ appointment.orderNo }}</p>
                </div>
                <div class="status-tags">
                  <el-tag 
                    :type="getStatusType(appointment.status)"
                    size="large"
                  >
                    {{ getStatusText(appointment.status) }}
                  </el-tag>
                  <el-tag 
                    :type="getPaymentStatusType(appointment.paymentStatus)"
                    size="large"
                    style="margin-left: 8px;"
                  >
                    {{ getPaymentStatusText(appointment.paymentStatus) }}
                  </el-tag>
                </div>
              </div>
              
              <div class="appointment-details">
                <div class="detail-row">
                  <el-icon><Calendar /></el-icon>
                  <span>预约时间：{{ formatDateTime(appointment.appointmentTime) }}</span>
                </div>
                
                <div class="detail-row">
                  <el-icon><Car /></el-icon>
                  <span>车辆：{{ appointment.vehicle?.brand }} {{ appointment.vehicle?.model }} ({{ appointment.vehicle?.licensePlate }})</span>
                </div>
                
                <div class="detail-row">
                  <el-icon><User /></el-icon>
                  <span>技师：{{ appointment.employee?.name || '待分配' }}</span>
                </div>
                
                <div class="detail-row">
                  <el-icon><Money /></el-icon>
                  <span>订单金额：¥{{ appointment.totalAmount }}</span>
                </div>
                
                <div class="detail-row" v-if="appointment.paymentStatus === 'PAID'">
                  <el-icon><CreditCard /></el-icon>
                  <span>支付方式：{{ getPaymentMethodText(appointment.paymentMethod) }}</span>
                </div>
                
                <div class="detail-row" v-if="appointment.notes">
                  <el-icon><Document /></el-icon>
                  <span>备注：{{ appointment.notes }}</span>
                </div>
              </div>
              
              <div class="appointment-actions">
                <!-- 主要操作按钮 -->
                <div class="primary-actions">
                  <!-- 支付按钮 -->
                  <el-button 
                    v-if="appointment.paymentStatus === 'UNPAID'"
                    type="primary" 
                    size="large"
                    @click="handlePayment(appointment)"
                  >
                    立即支付
                  </el-button>
                  
                  <!-- 写评价按钮 -->
                  <el-button 
                    v-if="appointment.status === 'COMPLETED' && !appointment.hasReview"
                    type="primary" 
                    size="large"
                    @click="writeReview(appointment)"
                  >
                    写评价
                  </el-button>
                </div>
                
                <!-- 次要操作按钮 -->
                <div class="secondary-actions">
                  <!-- 查看详情按钮 -->
                  <el-button 
                    size="large"
                    @click="viewDetails(appointment)"
                  >
                    查看详情
                  </el-button>
                  
                  <!-- 查看评价按钮 -->
                  <el-button 
                    v-if="appointment.status === 'COMPLETED' && appointment.hasReview"
                    size="large"
                    @click="viewReview(appointment)"
                  >
                    查看评价
                  </el-button>
                  
                  <!-- 取消预约按钮 -->
                  <el-button 
                    v-if="appointment.status === 'PENDING' && appointment.paymentStatus === 'UNPAID'"
                    type="warning"
                    plain
                    size="large"
                    @click="handleCancelAppointment(appointment)"
                  >
                    取消预约
                  </el-button>
                  
                  <!-- 删除预约按钮 -->
                  <el-button 
                    v-if="appointment.status === 'CANCELLED' || appointment.status === 'COMPLETED'"
                    type="danger"
                    plain
                    size="large"
                    @click="handleDeleteAppointment(appointment)"
                  >
                    删除
                  </el-button>
                </div>
              </div>
            </el-card>
          </div>
          
          <el-empty v-else description="暂无预约记录">
            <el-button type="primary" @click="$router.push('/services')">
              立即预约
            </el-button>
          </el-empty>
        </div>
        
        <!-- 分页 -->
        <div class="pagination" v-if="total > 0">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="loadAppointments"
          />
        </div>
      </div>
    </div>
    
    <Footer />
    
    <!-- 预约详情对话框 -->
    <el-dialog v-model="showDetails" title="预约详情" width="600px">
      <div v-if="selectedAppointment" class="appointment-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">
            {{ selectedAppointment.orderNo }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(selectedAppointment.status)">
              {{ getStatusText(selectedAppointment.status) }}
            </el-tag>
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
          <el-descriptions-item label="车辆信息">
            {{ selectedAppointment.vehicle?.brand }} {{ selectedAppointment.vehicle?.model }}
          </el-descriptions-item>
          <el-descriptions-item label="车牌号">
            {{ selectedAppointment.vehicle?.licensePlate }}
          </el-descriptions-item>
          <el-descriptions-item label="技师">
            {{ selectedAppointment.employee?.name || '待分配' }}
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
    
    <!-- 支付对话框 -->
    <el-dialog v-model="showPayment" title="选择支付方式" width="500px">
      <div v-if="selectedAppointment" class="payment-dialog">
        <div class="payment-info">
          <h3>订单信息</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="订单号">
              {{ selectedAppointment.orderNo }}
            </el-descriptions-item>
            <el-descriptions-item label="服务项目">
              {{ selectedAppointment.service?.name }}
            </el-descriptions-item>
            <el-descriptions-item label="订单金额">
              <span class="amount">¥{{ selectedAppointment.totalAmount }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        
        <div class="payment-method">
          <h3>支付方式</h3>
          <el-radio-group v-model="paymentMethod" size="large" class="payment-options">
            <el-radio label="ALIPAY" border class="alipay-option">
              <div class="payment-option">
                <span class="payment-name">支 付 宝</span>
              </div>
            </el-radio>
          </el-radio-group>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showPayment = false" size="large">取消</el-button>
        <el-button 
          type="primary" 
          @click="confirmPayment" 
          :loading="paymentLoading"
          size="large"
          class="confirm-payment-btn"
        >
          确认支付
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 评价对话框 -->
    <el-dialog v-model="showReview" title="服务评价" width="600px">
      <el-form
        ref="reviewFormRef"
        :model="reviewForm"
        :rules="reviewRules"
        label-width="80px"
      >
        <el-form-item label="服务评分" prop="rating">
          <el-rate v-model="reviewForm.rating" />
        </el-form-item>
        
        <el-form-item label="评价内容" prop="comment">
          <el-input
            v-model="reviewForm.comment"
            type="textarea"
            :rows="4"
            placeholder="请分享您的服务体验..."
          />
        </el-form-item>
        
        <el-form-item label="上传图片">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            list-type="picture-card"
            :on-success="handleImageSuccess"
            :on-remove="handleImageRemove"
            accept="image/*"
            :limit="3"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showReview = false">取消</el-button>
        <el-button type="primary" @click="submitReview" :loading="submittingReview">
          提交评价
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import Header from '@/components/Layout/Header.vue'
import Footer from '@/components/Layout/Footer.vue'
import { getAppointments, cancelAppointment, deleteAppointment } from '@/api/appointments'
import { createReview } from '@/api/reviews'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Calendar, Car, User, Money, Document, Plus, CreditCard } from '@element-plus/icons-vue'
import { formatDateTime } from '@/utils/dateUtils'
import { createAlipayOrder, queryPaymentStatus } from '@/api/payment'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const submittingReview = ref(false)
const showDetails = ref(false)
const showReview = ref(false)
const showPayment = ref(false)
const paymentLoading = ref(false)

const activeStatus = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const appointments = ref([])
const selectedAppointment = ref(null)
const paymentMethod = ref('ALIPAY')

const reviewFormRef = ref()
const reviewForm = reactive({
  rating: 5,
  comment: '',
  images: []
})

const uploadUrl = `${process.env.VUE_APP_API_BASE_URL}/upload/review`
const uploadHeaders = {
  'Authorization': `Bearer ${userStore.token}`
}

const reviewRules = {
  rating: [
    { required: true, message: '请选择评分', trigger: 'change' }
  ],
  comment: [
    { required: true, message: '请输入评价内容', trigger: 'blur' },
    { min: 10, message: '评价内容不能少于10个字符', trigger: 'blur' }
  ]
}

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

const getPaymentStatusType = (status) => {
  const statusMap = {
    'UNPAID': 'danger',
    'PAID': 'success',
    'REFUNDED': 'info'
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

const getPaymentMethodText = (method) => {
  const methodMap = {
    'ALIPAY': '支付宝'
  }
  return methodMap[method] || method
}

// formatDateTime 函数已从 @/utils/dateUtils 导入

const handleStatusChange = () => {
  currentPage.value = 1
  loadAppointments()
}

const loadAppointments = async () => {
  loading.value = true
  try {
    const status = activeStatus.value === 'all' ? null : activeStatus.value
    
    const response = await getAppointments(currentPage.value - 1, pageSize.value, status)
    if (response.success) {
      appointments.value = response.data.content || response.data
      total.value = response.data.totalElements || response.data.length
    }
  } catch (error) {
    console.error('加载预约列表失败:', error)
    ElMessage.error('加载预约列表失败')
  } finally {
    loading.value = false
  }
}

const handleCancelAppointment = async (appointment) => {
  try {
    await ElMessageBox.confirm(
      '确定要取消这个预约吗？取消后不可恢复。',
      '取消预约',
      {
        confirmButtonText: '确定取消',
        cancelButtonText: '不取消',
        type: 'warning'
      }
    )
    
    const response = await cancelAppointment(appointment.id)
    if (response.success) {
      ElMessage.success('预约已取消')
      loadAppointments()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消预约失败:', error)
      ElMessage.error(error.message || '取消预约失败')
    }
  }
}

const handleDeleteAppointment = async (appointment) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条预约记录吗？删除后将无法恢复。',
      '删除预约',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    const response = await deleteAppointment(appointment.id)
    if (response.success) {
      ElMessage.success('预约已删除')
      loadAppointments()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除预约失败:', error)
      ElMessage.error(error.message || '删除预约失败')
    }
  }
}

const writeReview = (appointment) => {
  selectedAppointment.value = appointment
  showReview.value = true
  // 重置表单
  reviewForm.rating = 5
  reviewForm.comment = ''
  reviewForm.images = []
}

const viewReview = (appointment) => {
  router.push(`/reviews/${appointment.id}`)
}

const viewDetails = (appointment) => {
  selectedAppointment.value = appointment
  showDetails.value = true
}

const handleImageSuccess = (response) => {
  if (response.success) {
    const fullUrl = `${process.env.VUE_APP_API_BASE_URL}${response.data}?t=${Date.now()}`
    reviewForm.images.push(fullUrl)
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '图片上传失败')
  }
}

const handleImageRemove = (file) => {
  let urlToRemove = null
  if (file.response && file.response.success) {
    urlToRemove = `${process.env.VUE_APP_API_BASE_URL}${file.response.data}`
  } else if (file.url) {
    urlToRemove = file.url
  }
  
  if (urlToRemove) {
    const index = reviewForm.images.findIndex(url => url.includes(urlToRemove.split('?')[0]))
    if (index > -1) {
      reviewForm.images.splice(index, 1)
    }
  }
}

const submitReview = async () => {
  if (!reviewFormRef.value) return
  
  try {
    await reviewFormRef.value.validate()
    submittingReview.value = true
    
    const reviewData = {
      appointmentId: selectedAppointment.value.id,
      serviceId: selectedAppointment.value.serviceId || selectedAppointment.value.service?.id,
      rating: reviewForm.rating,
      comment: reviewForm.comment,
      images: reviewForm.images.join(',')
    }
    
    console.log('提交评价数据：', reviewData)
    
    const response = await createReview(reviewData)
    if (response.success) {
      ElMessage.success('评价提交成功')
      showReview.value = false
      loadAppointments()
    } else {
      ElMessage.error(response.message || '提交评价失败')
    }
  } catch (error) {
    console.error('提交评价失败:', error)
    ElMessage.error(error.response?.data?.message || error.message || '提交评价失败')
  } finally {
    submittingReview.value = false
  }
}

// 处理支付
const handlePayment = (appointment) => {
  selectedAppointment.value = appointment
  showPayment.value = true
}

// 确认支付
const confirmPayment = async () => {
  try {
    paymentLoading.value = true
    
    // 支付宝支付
    const response = await createAlipayOrder(selectedAppointment.value.id)
    if (response.success) {
      // 在新窗口打开支付页面
      const newWindow = window.open('', '_blank')
      newWindow.document.write(response.data)
      newWindow.document.close()
      
      // 提示用户
      ElMessage({
        message: '支付窗口已打开，完成支付后系统将自动更新订单状态',
        type: 'info',
        duration: 5000
      })
      
      // 关闭支付对话框
      showPayment.value = false
      
      // 开始轮询支付状态
      startPaymentPolling()
    } else {
      ElMessage.error(response.message || '创建支付订单失败')
    }
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error(error.message || '支付失败')
  } finally {
    paymentLoading.value = false
  }
}

// 轮询支付状态
let pollingTimer = null
const startPaymentPolling = () => {
  // 清除之前的定时器
  if (pollingTimer) {
    clearInterval(pollingTimer)
  }
  
  let pollingCount = 0
  const maxPolling = 60 // 最多轮询60次（5分钟）
  
  pollingTimer = setInterval(async () => {
    pollingCount++
    
    if (pollingCount > maxPolling) {
      clearInterval(pollingTimer)
      ElMessage.warning('支付超时，请手动刷新查看支付状态')
      return
    }
    
    try {
      const response = await queryPaymentStatus(selectedAppointment.value.id)
      if (response.success && response.data.paymentStatus === 'PAID') {
        clearInterval(pollingTimer)
        ElMessage.success('支付成功！')
        showPayment.value = false
        loadAppointments()
      }
    } catch (error) {
      console.error('查询支付状态失败:', error)
    }
  }, 5000) // 每5秒查询一次
}

onMounted(() => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  loadAppointments()
})
</script>

<style lang="scss" scoped>
.appointments-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.page-header {
  text-align: center;
  padding: 64px 0;
  background: linear-gradient(135deg, #85CACF 0%, #A8DEE0 100%);
  margin-bottom: 48px;
  position: relative;
  overflow: hidden;
  border-radius: 16px;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: url('data:image/svg+xml,<svg width="100" height="100" xmlns="http://www.w3.org/2000/svg"><defs><pattern id="grid" width="100" height="100" patternUnits="userSpaceOnUse"><path d="M 100 0 L 0 0 0 100" fill="none" stroke="rgba(255,255,255,0.1)" stroke-width="1"/></pattern></defs><rect width="100%" height="100%" fill="url(%23grid)"/></svg>');
    opacity: 0.5;
  }
  
  h1 {
    font-size: 36px;
    font-weight: 600;
    color: #FFFFFF;
    margin-bottom: 16px;
    position: relative;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
  
  p {
    font-size: 18px;
    color: rgba(255, 255, 255, 0.9);
    position: relative;
  }
}

.appointments-content {
  flex: 1;
  margin-bottom: 64px;
}

.filter-tabs {
  margin-bottom: 48px;
  
  :deep(.el-tabs__header) {
    margin: 0;
  }
  
  :deep(.el-tabs__nav) {
    width: 100%;
    display: flex;
    justify-content: space-around;
  }
  
  :deep(.el-tabs__item) {
    font-size: 17px;
    font-weight: 500;
    padding: 0 32px;
    height: 48px;
    line-height: 48px;
    flex: 1;
    text-align: center;
  }
  
  :deep(.el-tabs__active-bar) {
    display: none;
  }
}

.appointments-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
}

.appointment-card {
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 32px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: white;
  
  &:hover {
    box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
    transform: translateY(-4px);
    border-color: $primary-light;
  }
  
  .appointment-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 24px;
    padding-bottom: 20px;
    border-bottom: 2px solid #f3f4f6;
    
    .appointment-info {
      flex: 1;
      
      h3 {
        font-size: 22px;
        font-weight: 700;
        color: $text-primary;
        margin-bottom: 8px;
      }
      
      .order-number {
        color: $text-secondary;
        font-size: 15px;
        font-weight: 500;
      }
    }
  }
  
  .appointment-details {
    margin-bottom: 24px;
    
    .detail-row {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 12px;
      color: $text-regular;
      font-size: 15px;
      
      .el-icon {
        color: $primary-color;
        font-size: 18px;
      }
    }
  }
  
  .appointment-actions {
    display: flex;
    flex-direction: column;
    gap: 16px;
    padding-top: 24px;
    border-top: 2px solid #f3f4f6;
    
    .primary-actions {
      display: flex;
      gap: 12px;
      
      .el-button {
        flex: 1;
        font-weight: 600;
        height: 44px;
        font-size: 16px;
        border-radius: 12px;
      }
    }
    
    .secondary-actions {
      display: flex;
      gap: 12px;
      flex-wrap: wrap;
      
      .el-button {
        font-weight: 500;
        height: 40px;
        font-size: 15px;
        border-radius: 10px;
        padding: 0 24px;
      }
    }
  }
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 64px;
}

.appointment-detail {
  :deep(.el-descriptions__label) {
    font-weight: 600;
    font-size: 15px;
  }
  
  :deep(.el-descriptions__content) {
    font-size: 15px;
  }
}

.status-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.payment-dialog {
  .payment-info {
    margin-bottom: 32px;
    padding: 24px;
    background: #f9fafb;
    border-radius: 12px;
    
    h3 {
      font-size: 18px;
      font-weight: 600;
      margin-bottom: 16px;
      color: $text-primary;
    }
    
    .amount {
      font-size: 28px;
      font-weight: 700;
      background: linear-gradient(135deg, #EF4444 0%, #DC2626 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
  }
  
  .payment-method {
    margin-bottom: 32px;
    
    h3 {
      font-size: 18px;
      font-weight: 600;
      margin-bottom: 16px;
      color: $text-primary;
    }
    
    :deep(.el-radio-group) {
      display: flex;
      flex-direction: column;
      gap: 16px;
      width: 100%;
    }
    
    :deep(.el-radio.is-bordered) {
      width: 100%;
      margin: 0;
      padding: 20px 24px;
      border-radius: 12px;
      transition: all 0.3s ease;
      background: #1677FF;
      border-color: #1677FF;
      
      &:hover {
        background: #0958D9;
        border-color: #0958D9;
        box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
        transform: translateY(-2px);
      }
      
      &.is-checked {
        background: #0958D9;
        border-color: #0958D9;
        box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
      }
      
      .el-radio__label {
        color: #FFFFFF !important;
      }
    }
    
    .payment-option {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 100%;
      
      .payment-name {
        font-size: 20px;
        font-weight: 600;
        color: #FFFFFF;
        letter-spacing: 8px;
        text-align: center;
      }
    }
  }
  
  .confirm-payment-btn {
    background: #1677FF !important;
    border: none !important;
    padding: 12px 32px !important;
    font-size: 16px !important;
    font-weight: 600 !important;
    
    &:hover {
      background: #0958D9 !important;
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3) !important;
    }
    
    &:active {
      transform: translateY(0);
    }
  }
}

@media (max-width: 768px) {
  .page-header {
    padding: 48px 0;
    
    h1 {
      font-size: 28px;
    }
    
    p {
      font-size: 16px;
    }
  }
  
  .appointments-grid {
    grid-template-columns: 1fr;
  }
  
  .appointment-card {
    .appointment-header {
      flex-direction: column;
      gap: 16px;
      align-items: flex-start;
    }
    
    .appointment-actions {
      .el-button {
        flex: 1;
        min-width: 0;
      }
    }
  }
}
</style>
