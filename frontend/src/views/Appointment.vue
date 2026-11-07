<template>
  <div class="appointment-page">
    <Header />
    
    <div class="container">
      <div class="page-header">
        <h1>预约服务</h1>
        <p>选择您需要的服务和时间</p>
      </div>
      
      <div class="appointment-content">
        <el-steps :active="currentStep" finish-status="success" align-center>
          <el-step title="选择服务" />
          <el-step title="选择车辆" />
          <el-step title="选择时间" />
          <el-step title="确认预约" />
        </el-steps>
        
        <div class="step-content">
          <!-- 步骤1: 选择服务 -->
          <div v-if="currentStep === 0" class="step-panel">
            <h3>选择服务项目</h3>
            <div class="services-list">
              <div 
                v-for="service in availableServices" 
                :key="service.id"
                class="service-item"
                :class="{ active: selectedService?.id === service.id }"
                @click="selectService(service)"
              >
                <div class="service-image">
                  <img :src="service.imageUrl || '/images/default-service.jpg'" :alt="service.name">
                </div>
                <div class="service-info">
                  <h4>{{ service.name }}</h4>
                  <p>{{ service.description }}</p>
                  <div class="service-meta">
                    <span class="price">¥{{ service.price }}</span>
                    <span class="duration">{{ service.duration }}分钟</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 步骤2: 选择车辆 -->
          <div v-if="currentStep === 1" class="step-panel">
            <h3>选择车辆</h3>
            <div class="vehicles-section">
              <div class="vehicles-list" v-if="userVehicles.length > 0">
                <div 
                  v-for="vehicle in userVehicles" 
                  :key="vehicle.id"
                  class="vehicle-item"
                  :class="{ active: selectedVehicle?.id === vehicle.id }"
                  @click="selectVehicle(vehicle)"
                >
                  <div class="vehicle-image">
                    <img :src="vehicle.imageUrl || '/images/default-car.jpg'" :alt="vehicle.brand + ' ' + vehicle.model">
                  </div>
                  <div class="vehicle-info">
                    <h4>{{ vehicle.brand }} {{ vehicle.model }}</h4>
                    <p>车牌号：{{ vehicle.licensePlate }}</p>
                    <p>颜色：{{ vehicle.color }}</p>
                  </div>
                </div>
              </div>
              <div v-else class="no-vehicles">
                <el-empty description="您还没有添加车辆信息">
                  <el-button type="primary" @click="addVehicle">添加车辆</el-button>
                </el-empty>
              </div>
              <el-button type="primary" plain @click="addVehicle" v-if="userVehicles.length > 0">
                添加新车辆
              </el-button>
            </div>
          </div>
          
          <!-- 步骤3: 选择时间 -->
          <div v-if="currentStep === 2" class="step-panel">
            <h3>选择预约时间</h3>
            <div class="time-selection">
              <!-- 月份标题和切换 -->
              <div class="calendar-header">
                <el-button @click="previousMonth" :icon="ArrowLeft" circle />
                <h4>{{ currentMonthYear }}</h4>
                <el-button @click="nextMonth" :icon="ArrowRight" circle />
              </div>
              
              <!-- 星期标题 -->
              <div class="weekdays-header">
                <div class="weekday">周日</div>
                <div class="weekday">周一</div>
                <div class="weekday">周二</div>
                <div class="weekday">周三</div>
                <div class="weekday">周四</div>
                <div class="weekday">周五</div>
                <div class="weekday">周六</div>
              </div>
              
              <!-- 日期列表 -->
              <div class="dates-list">
                <div 
                  v-for="date in availableDates" 
                  :key="date.date"
                  class="date-card"
                  :class="{ 
                    active: selectedDate && formatDateOnly(selectedDate) === date.date,
                    disabled: date.disabled,
                    'other-month': date.otherMonth
                  }"
                  @click="!date.disabled && selectDate(date.date)"
                >
                  <div class="date-number">{{ date.day }}</div>
                </div>
              </div>
              
              <!-- 时间段 -->
              <div class="time-slots" v-if="selectedDate">
                <h4>可预约时间段</h4>
                <div class="slots-grid" v-loading="loadingSlots">
                  <div 
                    v-for="slot in availableSlots" 
                    :key="slot.time"
                    class="time-slot"
                    :class="{ 
                      active: selectedTime === slot.time,
                      disabled: !slot.available 
                    }"
                    @click="selectTime(slot)"
                  >
                    {{ slot.time }}
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 步骤4: 确认预约 -->
          <div v-if="currentStep === 3" class="step-panel">
            <h3>确认预约信息</h3>
            <div class="confirmation-details">
              <el-card class="detail-card">
                <h4>服务信息</h4>
                <div class="detail-item">
                  <span>服务项目：</span>
                  <span>{{ selectedService?.name }}</span>
                </div>
                <div class="detail-item">
                  <span>服务价格：</span>
                  <span class="price">¥{{ selectedService?.price }}</span>
                </div>
                <div class="detail-item">
                  <span>服务时长：</span>
                  <span>{{ selectedService?.duration }}分钟</span>
                </div>
              </el-card>
              
              <el-card class="detail-card">
                <h4>车辆信息</h4>
                <div class="detail-item">
                  <span>车辆：</span>
                  <span>{{ selectedVehicle?.brand }} {{ selectedVehicle?.model }}</span>
                </div>
                <div class="detail-item">
                  <span>车牌号：</span>
                  <span>{{ selectedVehicle?.licensePlate }}</span>
                </div>
              </el-card>
              
              <el-card class="detail-card">
                <h4>预约时间</h4>
                <div class="detail-item">
                  <span>日期：</span>
                  <span>{{ formatDate(selectedDate) }}</span>
                </div>
                <div class="detail-item">
                  <span>时间：</span>
                  <span>{{ selectedTime }}</span>
                </div>
              </el-card>
              
              <el-card class="detail-card">
                <h4>备注信息</h4>
                <el-input
                  v-model="notes"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入特殊要求或备注信息（选填）"
                />
              </el-card>
            </div>
          </div>
        </div>
        
        <div class="step-actions">
          <el-button v-if="currentStep > 0" @click="prevStep">上一步</el-button>
          <el-button 
            v-if="currentStep < 3" 
            type="primary" 
            @click="nextStep"
            :disabled="!canProceed"
          >
            下一步
          </el-button>
          <el-button 
            v-if="currentStep === 3" 
            type="primary" 
            @click="confirmAppointment"
            :loading="submitting"
          >
            确认预约
          </el-button>
        </div>
      </div>
    </div>
    
    <Footer />
    
    <!-- 添加车辆对话框 -->
    <el-dialog v-model="showAddVehicle" title="添加车辆" width="500px">
      <el-form ref="vehicleFormRef" :model="vehicleForm" :rules="vehicleRules" label-width="80px">
        <el-form-item label="品牌" prop="brand">
          <el-input v-model="vehicleForm.brand" placeholder="请输入车辆品牌" />
        </el-form-item>
        <el-form-item label="型号" prop="model">
          <el-input v-model="vehicleForm.model" placeholder="请输入车辆型号" />
        </el-form-item>
        <el-form-item label="车牌号" prop="licensePlate">
          <el-input v-model="vehicleForm.licensePlate" placeholder="请输入车牌号" />
        </el-form-item>
        <el-form-item label="颜色" prop="color">
          <el-input v-model="vehicleForm.color" placeholder="请输入车辆颜色" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddVehicle = false">取消</el-button>
        <el-button type="primary" @click="saveVehicle" :loading="savingVehicle">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import Header from '@/components/Layout/Header.vue'
import Footer from '@/components/Layout/Footer.vue'
import { getServices } from '@/api/services'
import { getVehicles as getUserVehicles, addVehicle as apiAddVehicle } from '@/api/vehicles'
import { createAppointment, getAvailableTimeSlots } from '@/api/appointments'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const currentStep = ref(0)
const submitting = ref(false)
const loadingSlots = ref(false)
const savingVehicle = ref(false)
const showAddVehicle = ref(false)

const availableServices = ref([])
const userVehicles = ref([])
const availableSlots = ref([])
const availableDates = ref([])
const currentMonth = ref(new Date())

const selectedService = ref(null)
const selectedVehicle = ref(null)
const selectedDate = ref(null)
const selectedTime = ref('')
const notes = ref('')

// 当前月份年份显示
const currentMonthYear = computed(() => {
  const year = currentMonth.value.getFullYear()
  const month = currentMonth.value.getMonth() + 1
  return `${year}年${month}月`
})

const vehicleFormRef = ref()
const vehicleForm = reactive({
  brand: '',
  model: '',
  licensePlate: '',
  color: ''
})

const vehicleRules = {
  brand: [{ required: true, message: '请输入车辆品牌', trigger: 'blur' }],
  model: [{ required: true, message: '请输入车辆型号', trigger: 'blur' }],
  licensePlate: [
    { required: true, message: '请输入车牌号', trigger: 'blur' },
    { pattern: /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}[A-Z0-9]{4}[A-Z0-9挂学警港澳]{1}$/, message: '请输入正确的车牌号格式', trigger: 'blur' }
  ],
  color: [{ required: true, message: '请输入车辆颜色', trigger: 'blur' }]
}

const canProceed = computed(() => {
  switch (currentStep.value) {
    case 0:
      return selectedService.value !== null
    case 1:
      return selectedVehicle.value !== null
    case 2:
      return selectedDate.value && selectedTime.value
    default:
      return true
  }
})

const disabledDate = (time) => {
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

const loadServices = async () => {
  try {
    const response = await getServices({ status: 'ACTIVE' })
    if (response.success) {
      availableServices.value = response.data.content || response.data
      
      // 如果URL中有serviceId参数，自动选择该服务
      const serviceId = route.query.serviceId
      if (serviceId) {
        const service = availableServices.value.find(s => s.id == serviceId)
        if (service) {
          selectedService.value = service
        }
      }
    }
  } catch (error) {
    console.error('加载服务列表失败:', error)
    ElMessage.error('加载服务列表失败')
  }
}

const loadUserVehicles = async () => {
  try {
    const response = await getUserVehicles()
    if (response.success) {
      userVehicles.value = response.data
      // 自动选择默认车辆
      const defaultVehicle = userVehicles.value.find(v => v.isDefault)
      if (defaultVehicle) {
        selectedVehicle.value = defaultVehicle
      }
    }
  } catch (error) {
    console.error('加载车辆列表失败:', error)
    ElMessage.error('加载车辆列表失败')
  }
}

// 生成当月日历（包括前后月份补齐的日期）
const generateAvailableDates = () => {
  const dates = []
  const year = currentMonth.value.getFullYear()
  const month = currentMonth.value.getMonth()
  
  // 获取当月第一天和最后一天
  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  
  // 获取当月第一天是星期几（0-6，0是周日）
  const firstDayOfWeek = firstDay.getDay()
  
  // 获取当前日期（用于禁用过去的日期）
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  
  // 补齐上个月的日期
  const prevMonthLastDay = new Date(year, month, 0).getDate()
  for (let i = firstDayOfWeek - 1; i >= 0; i--) {
    const day = prevMonthLastDay - i
    const date = new Date(year, month - 1, day)
    dates.push({
      date: date.toISOString().split('T')[0],
      day: day,
      disabled: date < today,
      otherMonth: true
    })
  }
  
  // 当月的所有日期
  for (let day = 1; day <= lastDay.getDate(); day++) {
    const date = new Date(year, month, day)
    dates.push({
      date: date.toISOString().split('T')[0],
      day: day,
      disabled: date < today,
      otherMonth: false
    })
  }
  
  // 补齐下个月的日期，使日历总是6行（42个格子）
  const remainingDays = 42 - dates.length
  for (let day = 1; day <= remainingDays; day++) {
    const date = new Date(year, month + 1, day)
    dates.push({
      date: date.toISOString().split('T')[0],
      day: day,
      disabled: date < today,
      otherMonth: true
    })
  }
  
  availableDates.value = dates
}

// 上一个月
const previousMonth = () => {
  const newMonth = new Date(currentMonth.value)
  newMonth.setMonth(newMonth.getMonth() - 1)
  
  // 不允许选择当前月之前的月份
  const today = new Date()
  today.setDate(1)
  today.setHours(0, 0, 0, 0)
  
  if (newMonth >= today) {
    currentMonth.value = newMonth
    generateAvailableDates()
  }
}

// 下一个月
const nextMonth = () => {
  const newMonth = new Date(currentMonth.value)
  newMonth.setMonth(newMonth.getMonth() + 1)
  
  // 允许预约未来6个月
  const maxMonth = new Date()
  maxMonth.setMonth(maxMonth.getMonth() + 6)
  
  if (newMonth <= maxMonth) {
    currentMonth.value = newMonth
    generateAvailableDates()
  }
}

// 选择日期
const selectDate = (dateStr) => {
  selectedDate.value = new Date(dateStr)
  selectedTime.value = ''
  loadAvailableSlots()
}

// 格式化日期为 YYYY-MM-DD
const formatDateOnly = (date) => {
  if (!date) return ''
  return new Date(date).toISOString().split('T')[0]
}

const loadAvailableSlots = async () => {
  if (!selectedDate.value || !selectedService.value) return
  
  loadingSlots.value = true
  try {
    const response = await getAvailableTimeSlots({
      date: selectedDate.value.toISOString().split('T')[0],
      serviceId: selectedService.value.id
    })
    
    if (response.success) {
      availableSlots.value = response.data
    }
  } catch (error) {
    console.error('加载可用时间段失败:', error)
    ElMessage.error('加载可用时间段失败')
  } finally {
    loadingSlots.value = false
  }
}

const selectService = (service) => {
  selectedService.value = service
}

const selectVehicle = (vehicle) => {
  selectedVehicle.value = vehicle
}

const selectTime = (slot) => {
  if (!slot.available) return
  selectedTime.value = slot.time
}

const nextStep = () => {
  if (canProceed.value) {
    currentStep.value++
    
    // 进入车辆选择步骤时加载用户车辆
    if (currentStep.value === 1) {
      loadUserVehicles()
    }
  }
}

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

const addVehicle = () => {
  showAddVehicle.value = true
  // 重置表单
  Object.keys(vehicleForm).forEach(key => {
    vehicleForm[key] = ''
  })
}

const saveVehicle = async () => {
  if (!vehicleFormRef.value) return
  
  try {
    await vehicleFormRef.value.validate()
    savingVehicle.value = true
    
    const response = await apiAddVehicle(vehicleForm)
    if (response.success) {
      ElMessage.success('车辆添加成功')
      showAddVehicle.value = false
      await loadUserVehicles()
      // 自动选择新添加的车辆
      selectedVehicle.value = response.data
    }
  } catch (error) {
    console.error('添加车辆失败:', error)
    ElMessage.error(error.message || '添加车辆失败')
  } finally {
    savingVehicle.value = false
  }
}

const confirmAppointment = async () => {
  try {
    await ElMessageBox.confirm('确认提交预约吗？', '确认预约', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    submitting.value = true
    
    const appointmentData = {
      vehicleId: selectedVehicle.value.id,
      serviceId: selectedService.value.id,
      appointmentTime: new Date(`${selectedDate.value.toISOString().split('T')[0]} ${selectedTime.value}`).toISOString(),
      notes: notes.value
    }
    
    const response = await createAppointment(appointmentData)
    if (response.success) {
      ElMessage.success('预约成功！')
      router.push('/appointments')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('创建预约失败:', error)
      ElMessage.error(error.message || '预约失败，请重试')
    }
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  loadServices()
  generateAvailableDates()
})
</script>

<style lang="scss" scoped>
.appointment-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  
  .container {
    max-width: 1600px !important;
    width: 100%;
    padding: 0 64px;
  }
}

.page-header {
  text-align: center;
  padding: 64px 0;
  background: linear-gradient(135deg, #85CACF 0%, #A8DEE0 100%);
  margin-bottom: 48px;
  border-radius: 16px;
  position: relative;
  overflow: hidden;
  
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

.appointment-content {
  flex: 1;
  margin-bottom: 64px;
  background: white;
  padding: 64px 80px;
  border-radius: 16px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  
  .el-steps {
    margin-bottom: 64px;
  }
}

.step-content {
  min-height: 600px;
  margin-bottom: 64px;
}

.step-panel {
  h3 {
    font-size: 32px;
    font-weight: 700;
    margin-bottom: 48px;
    color: $text-primary;
    padding-bottom: 20px;
    border-bottom: 2px solid #f3f4f6;
  }
}

.services-list {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 32px;
  margin-bottom: 48px;
}

.service-item {
  display: flex;
  flex-direction: column;
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 16px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);
  
  &:hover {
    border-color: $primary-color;
    box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
    transform: translateY(-4px);
  }
  
  &.active {
    border-color: $primary-color;
    background: linear-gradient(135deg, rgba(133, 202, 207, 0.1), rgba(168, 222, 224, 0.1));
    box-shadow: 0 0 0 3px rgba(133, 202, 207, 0.2);
  }
  
  .service-image {
    width: 100%;
    height: 160px;
    border-radius: 12px;
    overflow: hidden;
    margin-bottom: 16px;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s ease;
    }
  }
  
  &:hover .service-image img {
    transform: scale(1.05);
  }
  
  .service-info {
    flex: 1;
    
    h4 {
      font-size: 17px;
      font-weight: 600;
      margin-bottom: 10px;
      color: $text-primary;
    }
    
    p {
      color: $text-secondary;
      margin-bottom: 14px;
      font-size: 14px;
      line-height: 1.5;
      min-height: 42px;
    }
    
    .service-meta {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: 14px;
      border-top: 1px solid #f3f4f6;
      
      .price {
        font-size: 24px;
        font-weight: 700;
        background: linear-gradient(135deg, #EF4444 0%, #DC2626 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
      }
      
      .duration {
        color: $text-secondary;
        font-size: 15px;
        font-weight: 500;
      }
    }
  }
}

.vehicles-section {
  .vehicles-list {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 32px;
    margin-bottom: 32px;
  }
  
  .no-vehicles {
    text-align: center;
    padding: 64px 40px;
    background: #f9fafb;
    border-radius: 12px;
  }
}

.vehicle-item {
  display: flex;
  flex-direction: column;
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 16px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);
  
  &:hover {
    border-color: $primary-color;
    box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
    transform: translateY(-4px);
  }
  
  &.active {
    border-color: $primary-color;
    background: linear-gradient(135deg, rgba(133, 202, 207, 0.1), rgba(168, 222, 224, 0.1));
    box-shadow: 0 0 0 3px rgba(133, 202, 207, 0.2);
  }
  
  .vehicle-image {
    width: 100%;
    height: 160px;
    border-radius: 12px;
    overflow: hidden;
    margin-bottom: 20px;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s ease;
    }
  }
  
  &:hover .vehicle-image img {
    transform: scale(1.05);
  }
  
  .vehicle-info {
    flex: 1;
    
    h4 {
      font-size: 20px;
      font-weight: 600;
      margin-bottom: 8px;
      color: $text-primary;
    }
    
    p {
      color: $text-secondary;
      margin-bottom: 6px;
      font-size: 15px;
    }
  }
}

.time-selection {
  .calendar-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 32px;
    padding: 0 24px;
    
    h4 {
      font-size: 28px;
      font-weight: 700;
      color: $text-primary;
      margin: 0;
    }
  }
  
  .weekdays-header {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    gap: 16px;
    margin-bottom: 16px;
    
    .weekday {
      text-align: center;
      font-size: 16px;
      font-weight: 600;
      color: $text-secondary;
      padding: 12px 0;
    }
  }
  
  .dates-list {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    gap: 16px;
    margin-bottom: 48px;
  }
  
  .date-card {
    background: white;
    border: 2px solid #e5e7eb;
    border-radius: 16px;
    padding: 20px 16px;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);
    aspect-ratio: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    
    &:hover:not(.disabled) {
      border-color: $primary-color;
      box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
      transform: translateY(-4px);
    }
    
    &.active {
      border-color: $primary-color;
      background: linear-gradient(135deg, rgba(133, 202, 207, 0.1), rgba(168, 222, 224, 0.1));
      box-shadow: 0 0 0 3px rgba(133, 202, 207, 0.2);
    }
    
    &.disabled {
      background: #f9fafb;
      border-color: #e5e7eb;
      cursor: not-allowed;
      opacity: 0.5;
    }
    
    &.other-month {
      opacity: 0.4;
      
      &:not(.disabled):hover {
        opacity: 0.6;
      }
    }
    
    .date-number {
      font-size: 24px;
      font-weight: 700;
      color: $text-primary;
      line-height: 1;
    }
    
    &.active .date-number {
      color: $primary-color;
    }
    
    &.disabled .date-number {
      color: $text-secondary;
    }
  }
  
  h4 {
    font-size: 24px;
    font-weight: 700;
    margin-bottom: 32px;
    color: $text-primary;
  }
  
  .slots-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 16px;
  }
  
  .time-slot {
    padding: 16px 20px;
    text-align: center;
    border: 2px solid #e5e7eb;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    font-size: 16px;
    font-weight: 500;
    
    &:hover:not(.disabled) {
      border-color: $primary-color;
      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
      transform: translateY(-2px);
    }
    
    &.active {
      border-color: $primary-color;
      background: $gradient-primary;
      color: white;
      box-shadow: 0 4px 12px rgba(133, 202, 207, 0.4);
    }
    
    &.disabled {
      background: #f5f7fa;
      color: #c0c4cc;
      cursor: not-allowed;
      opacity: 0.6;
    }
  }
}

.confirmation-details {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 32px;
  
  .detail-card {
    padding: 24px;
    
    h4 {
      font-size: 20px;
      font-weight: 600;
      margin-bottom: 20px;
      color: $text-primary;
      padding-bottom: 12px;
      border-bottom: 2px solid #f3f4f6;
    }
    
    .detail-item {
      display: flex;
      justify-content: space-between;
      margin-bottom: 16px;
      font-size: 16px;
      
      span:first-child {
        color: $text-secondary;
      }
      
      span:last-child {
        color: $text-primary;
        font-weight: 500;
      }
      
      .price {
        font-size: 28px;
        font-weight: 700;
        background: linear-gradient(135deg, #EF4444 0%, #DC2626 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
      }
    }
  }
}

.step-actions {
  display: flex;
  justify-content: center;
  gap: 24px;
  padding-top: 32px;
  border-top: 2px solid #e5e7eb;
  margin-top: 32px;
}

@media (max-width: 768px) {
  .services-list,
  .vehicles-list {
    grid-template-columns: 1fr;
  }
  
  .confirmation-details {
    grid-template-columns: 1fr;
  }
  
  .slots-grid {
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  }
}
</style>
