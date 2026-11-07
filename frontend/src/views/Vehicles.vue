<template>
  <div class="vehicles-page">
    <Header />
    
    <div class="container">
      <div class="page-header">
        <h1>我的车辆</h1>
        <p>管理您的车辆信息</p>
      </div>
      
      <div class="vehicles-content">
        <div class="content-header">
          <el-button type="primary" @click="showAddDialog = true">
            <el-icon><Plus /></el-icon>
            添加车辆
          </el-button>
        </div>
        
        <div class="vehicles-list" v-loading="loading">
          <div v-if="vehicles.length > 0" class="vehicles-grid" :class="getGridClass">
            <el-card 
              v-for="vehicle in vehicles" 
              :key="vehicle.id"
              class="vehicle-card"
              :class="{ default: vehicle.isDefault }"
            >
              <div class="vehicle-image">
                <img :src="vehicle.imageUrl || '/images/default-car.jpg'" :alt="vehicle.brand + ' ' + vehicle.model">
                <div class="default-badge" v-if="vehicle.isDefault">
                  <el-tag type="success" size="small">默认</el-tag>
                </div>
              </div>
              
              <div class="vehicle-info">
                <h3>{{ vehicle.brand }} {{ vehicle.model }}</h3>
                <div class="vehicle-details">
                  <div class="detail-item">
                    <span class="label">车牌号：</span>
                    <span class="value">{{ vehicle.licensePlate }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">颜色：</span>
                    <span class="value">{{ vehicle.color }}</span>
                  </div>
                  <div class="detail-item" v-if="vehicle.year">
                    <span class="label">年份：</span>
                    <span class="value">{{ vehicle.year }}年</span>
                  </div>
                  <div class="detail-item" v-if="vehicle.notes">
                    <span class="label">备注：</span>
                    <span class="value">{{ vehicle.notes }}</span>
                  </div>
                </div>
              </div>
              
              <div class="vehicle-actions">
                <el-button 
                  v-if="!vehicle.isDefault"
                  type="primary" 
                  size="small"
                  @click="setDefault(vehicle)"
                >
                  设为默认
                </el-button>
                <el-button size="small" @click="editVehicle(vehicle)">
                  编辑
                </el-button>
                <el-button 
                  type="danger" 
                  size="small" 
                  @click="deleteVehicle(vehicle)"
                  :disabled="vehicle.isDefault"
                >
                  删除
                </el-button>
              </div>
            </el-card>
          </div>
          
          <el-empty v-else description="暂无车辆信息">
            <el-button type="primary" @click="showAddDialog = true">
              添加第一辆车
            </el-button>
          </el-empty>
        </div>
      </div>
    </div>
    
    <Footer />
    
    <!-- 添加/编辑车辆对话框 -->
    <el-dialog 
      v-model="showAddDialog" 
      :title="editingVehicle ? '编辑车辆' : '添加车辆'" 
      width="500px"
    >
      <el-form
        ref="vehicleFormRef"
        :model="vehicleForm"
        :rules="vehicleRules"
        label-width="80px"
      >
        <el-form-item label="品牌" prop="brand">
          <el-input v-model="vehicleForm.brand" placeholder="请输入车辆品牌" />
        </el-form-item>
        
        <el-form-item label="型号" prop="model">
          <el-input v-model="vehicleForm.model" placeholder="请输入车辆型号" />
        </el-form-item>
        
        <el-form-item label="车辆类型" prop="vehicleType">
          <el-select v-model="vehicleForm.vehicleType" placeholder="请选择车辆类型" @change="onVehicleTypeChange">
            <el-option label="燃油车" value="FUEL" />
            <el-option label="普通混动车" value="HEV" />
            <el-option label="插电式混动车" value="PHEV" />
            <el-option label="纯电动车" value="BEV" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="车牌号" prop="licensePlate">
          <el-input 
            v-model="vehicleForm.licensePlate" 
            :placeholder="licensePlatePlaceholder"
            @blur="validateLicensePlate"
          />
          <div v-if="licensePlateError" class="error-message">{{ licensePlateError }}</div>
          <div class="help-text">{{ licensePlateHelpText }}</div>
        </el-form-item>
        
        <el-form-item label="颜色" prop="color">
          <el-select v-model="vehicleForm.color" placeholder="请选择颜色">
            <el-option label="白色" value="白色" />
            <el-option label="黑色" value="黑色" />
            <el-option label="银色" value="银色" />
            <el-option label="灰色" value="灰色" />
            <el-option label="红色" value="红色" />
            <el-option label="蓝色" value="蓝色" />
            <el-option label="金色" value="金色" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="年份">
          <el-date-picker
            v-model="vehicleForm.year"
            type="year"
            placeholder="请选择年份"
            value-format="YYYY"
          />
        </el-form-item>
        
        <el-form-item label="车辆图片">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleImageSuccess"
            :before-upload="beforeImageUpload"
            accept="image/*"
          >
            <img v-if="vehicleForm.imageUrl" :src="vehicleForm.imageUrl" class="vehicle-image-preview" />
            <el-icon v-else class="image-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="备注">
          <el-input
            v-model="vehicleForm.notes"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息（选填）"
          />
        </el-form-item>
        
        <el-form-item v-if="!editingVehicle && vehicles.length === 0">
          <el-checkbox v-model="vehicleForm.isDefault">设为默认车辆</el-checkbox>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="cancelEdit">取消</el-button>
        <el-button type="primary" @click="saveVehicle" :loading="saving">
          {{ editingVehicle ? '保存' : '添加' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import Header from '@/components/Layout/Header.vue'
import Footer from '@/components/Layout/Footer.vue'
import { 
  getVehicles as getUserVehicles, 
  addVehicle, 
  updateVehicle, 
  deleteVehicle as apiDeleteVehicle,
  setDefaultVehicle 
} from '@/api/vehicles'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const showAddDialog = ref(false)
const editingVehicle = ref(null)

const vehicles = ref([])
const vehicleFormRef = ref()

const vehicleForm = reactive({
  brand: '',
  model: '',
  licensePlate: '',
  vehicleType: 'FUEL',
  color: '',
  year: '',
  imageUrl: '',
  notes: '',
  isDefault: false
})

// 车牌号验证相关
const licensePlateError = ref('')
const licensePlatePlaceholder = ref('请输入车牌号')
const licensePlateHelpText = ref('燃油车车牌格式：省份简称+字母+5位数字或字母，如：京A12345')

const uploadUrl = `${process.env.VUE_APP_API_BASE_URL}/upload/vehicle`
const uploadHeaders = {
  'Authorization': `Bearer ${userStore.token}`
}

const vehicleRules = {
  brand: [
    { required: true, message: '请输入车辆品牌', trigger: 'blur' }
  ],
  model: [
    { required: true, message: '请输入车辆型号', trigger: 'blur' }
  ],
  vehicleType: [
    { required: true, message: '请选择车辆类型', trigger: 'change' }
  ],
  licensePlate: [
    { required: true, message: '请输入车牌号', trigger: 'blur' },
    { validator: validateLicensePlateRule, trigger: 'blur' }
  ],
  color: [
    { required: true, message: '请选择车辆颜色', trigger: 'change' }
  ]
}

// 车牌号验证规则
function validateLicensePlateRule(rule, value, callback) {
  if (!value) {
    callback()
    return
  }
  
  // 传统车牌格式（燃油车、普通混动车）- 排除字母O和I
  const traditionalPattern = /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][0-9A-HJ-NP-Z]{5}$/
  
  // 新能源车牌格式 - 排除字母O和I
  const newEnergySmallDPattern = /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z]D[0-9A-HJ-NP-Z]{5}$/
  const newEnergySmallFPattern = /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z]F[0-9A-HJ-NP-Z]{5}$/
  const newEnergyLargeDPattern = /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][0-9A-HJ-NP-Z]{5}D$/
  const newEnergyLargeFPattern = /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][0-9A-HJ-NP-Z]{5}F$/
  
  const upperValue = value.toUpperCase()
  
  if (vehicleForm.vehicleType === 'FUEL' || vehicleForm.vehicleType === 'HEV') {
    if (!traditionalPattern.test(upperValue)) {
      const vehicleTypeName = vehicleForm.vehicleType === 'FUEL' ? '燃油车' : '普通混动车'
      callback(new Error(`${vehicleTypeName}车牌格式不正确`))
    } else {
      callback()
    }
  } else if (vehicleForm.vehicleType === 'PHEV') {
    if (!newEnergySmallFPattern.test(upperValue) && !newEnergyLargeFPattern.test(upperValue)) {
      callback(new Error('插电式混动车车牌格式不正确'))
    } else {
      callback()
    }
  } else if (vehicleForm.vehicleType === 'BEV') {
    if (!newEnergySmallDPattern.test(upperValue) && !newEnergyLargeDPattern.test(upperValue)) {
      callback(new Error('纯电动车车牌格式不正确'))
    } else {
      callback()
    }
  } else {
    callback()
  }
}

// 车辆类型变化处理
const onVehicleTypeChange = (type) => {
  licensePlateError.value = ''
  if (type === 'FUEL') {
    licensePlatePlaceholder.value = '请输入燃油车车牌号，如：京A12345'
    licensePlateHelpText.value = '燃油车车牌格式：省份简称+字母+5位数字或字母，如：京A12345'
  } else if (type === 'HEV') {
    licensePlatePlaceholder.value = '请输入普通混动车车牌号，如：京A12345'
    licensePlateHelpText.value = '普通混动车车牌格式：省份简称+字母+5位数字或字母，如：京A12345（使用传统燃油车车牌）'
  } else if (type === 'PHEV') {
    licensePlatePlaceholder.value = '请输入插电式混动车车牌号，如：京AF12345'
    licensePlateHelpText.value = '插电式混动车车牌格式：小型车为省份简称+字母+F+5位数字或字母（如：京AF12345），大型车为省份简称+字母+5位数字或字母+F（如：京A12345F）'
  } else if (type === 'BEV') {
    licensePlatePlaceholder.value = '请输入纯电动车车牌号，如：京AD12345'
    licensePlateHelpText.value = '纯电动车车牌格式：小型车为省份简称+字母+D+5位数字或字母（如：京AD12345），大型车为省份简称+字母+5位数字或字母+D（如：京A12345D）'
  }
  
  // 如果已有车牌号，重新验证
  if (vehicleForm.licensePlate) {
    validateLicensePlate()
  }
}

// 车牌号验证
const validateLicensePlate = () => {
  licensePlateError.value = ''
  if (!vehicleForm.licensePlate || !vehicleForm.vehicleType) {
    return
  }
  
  // 传统车牌格式（燃油车、普通混动车）- 排除字母O和I
  const traditionalPattern = /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][0-9A-HJ-NP-Z]{5}$/
  
  // 新能源车牌格式 - 排除字母O和I
  const newEnergySmallDPattern = /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z]D[0-9A-HJ-NP-Z]{5}$/
  const newEnergySmallFPattern = /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z]F[0-9A-HJ-NP-Z]{5}$/
  const newEnergyLargeDPattern = /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][0-9A-HJ-NP-Z]{5}D$/
  const newEnergyLargeFPattern = /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][0-9A-HJ-NP-Z]{5}F$/
  
  const upperValue = vehicleForm.licensePlate.toUpperCase()
  
  if (vehicleForm.vehicleType === 'FUEL') {
    if (!traditionalPattern.test(upperValue)) {
      licensePlateError.value = '燃油车车牌格式不正确，正确格式如：京A12345'
    }
  } else if (vehicleForm.vehicleType === 'HEV') {
    if (!traditionalPattern.test(upperValue)) {
      licensePlateError.value = '普通混动车车牌格式不正确，正确格式如：京A12345'
    }
  } else if (vehicleForm.vehicleType === 'PHEV') {
    if (!newEnergySmallFPattern.test(upperValue) && !newEnergyLargeFPattern.test(upperValue)) {
      licensePlateError.value = '插电式混动车车牌格式不正确，正确格式如：京AF12345 或 京A12345F'
    }
  } else if (vehicleForm.vehicleType === 'BEV') {
    if (!newEnergySmallDPattern.test(upperValue) && !newEnergyLargeDPattern.test(upperValue)) {
      licensePlateError.value = '纯电动车车牌格式不正确，正确格式如：京AD12345 或 京A12345D'
    }
  }
}

const loadVehicles = async () => {
  loading.value = true
  try {
    const response = await getUserVehicles()
    if (response.success) {
      // 处理车辆图片URL，确保显示完整路径
      vehicles.value = response.data.map(vehicle => {
        // 如果是相对路径（不是http或data开头），拼接完整URL
        if (vehicle.imageUrl && !vehicle.imageUrl.startsWith('http') && !vehicle.imageUrl.startsWith('data:')) {
          vehicle.imageUrl = `${process.env.VUE_APP_API_BASE_URL}${vehicle.imageUrl}`
        }
        return vehicle
      })
    }
  } catch (error) {
    console.error('加载车辆列表失败:', error)
    ElMessage.error('加载车辆列表失败')
  } finally {
    loading.value = false
  }
}

const editVehicle = (vehicle) => {
  editingVehicle.value = vehicle
  Object.keys(vehicleForm).forEach(key => {
    vehicleForm[key] = vehicle[key] || ''
  })
  showAddDialog.value = true
}

const cancelEdit = () => {
  showAddDialog.value = false
  editingVehicle.value = null
  resetForm()
}

const resetForm = () => {
  Object.keys(vehicleForm).forEach(key => {
    if (key === 'isDefault') {
      vehicleForm[key] = false
    } else if (key === 'vehicleType') {
      vehicleForm[key] = 'FUEL'
    } else {
      vehicleForm[key] = ''
    }
  })
  
  // 重置验证状态
  licensePlateError.value = ''
  licensePlatePlaceholder.value = '请输入燃油车车牌号，如：京A12345'
  licensePlateHelpText.value = '燃油车车牌格式：省份简称+字母+5位数字或字母，如：京A12345'
}

const saveVehicle = async () => {
  if (!vehicleFormRef.value) return
  
  try {
    await vehicleFormRef.value.validate()
    saving.value = true
    
    const vehicleData = { ...vehicleForm }
    if (vehicleData.year) {
      vehicleData.year = parseInt(vehicleData.year)
    }
    
    // 将布尔值转换为整数，后端期望接收整数类型
    vehicleData.isDefault = vehicleData.isDefault ? 1 : 0
    
    let response
    if (editingVehicle.value) {
      response = await updateVehicle(editingVehicle.value.id, vehicleData)
    } else {
      response = await addVehicle(vehicleData)
    }
    
    if (response.success) {
      ElMessage.success(editingVehicle.value ? '车辆更新成功' : '车辆添加成功')
      showAddDialog.value = false
      editingVehicle.value = null
      resetForm()
      await loadVehicles()
    }
  } catch (error) {
    console.error('保存车辆失败:', error)
    ElMessage.error(error.message || '保存车辆失败')
  } finally {
    saving.value = false
  }
}

const setDefault = async (vehicle) => {
  try {
    const response = await setDefaultVehicle(vehicle.id)
    if (response.success) {
      ElMessage.success('默认车辆设置成功')
      await loadVehicles()
    }
  } catch (error) {
    console.error('设置默认车辆失败:', error)
    ElMessage.error('设置默认车辆失败')
  }
}

const deleteVehicle = async (vehicle) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除车辆 ${vehicle.brand} ${vehicle.model} 吗？`,
      '删除车辆',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await apiDeleteVehicle(vehicle.id)
    if (response.success) {
      ElMessage.success('车辆删除成功')
      await loadVehicles()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除车辆失败:', error)
      ElMessage.error(error.message || '删除车辆失败')
    }
  }
}

const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handleImageSuccess = (response) => {
  if (response.success) {
    // 后端返回的是Base64 data URL，直接使用即可
    vehicleForm.imageUrl = response.data
    ElMessage.success('车辆图片上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 根据车辆数量返回网格布局类名
const getGridClass = computed(() => {
  const count = vehicles.value.length
  if (count === 1) return 'single'
  if (count === 2) return 'double'
  return 'triple-or-more'
})

onMounted(() => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  loadVehicles()
})
</script>

<style lang="scss" scoped>
.vehicles-page {
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
    font-weight: 700;
    color: #FFFFFF;
    margin-bottom: 16px;
    position: relative;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
  
  p {
    font-size: 20px;
    color: rgba(255, 255, 255, 0.9);
    position: relative;
  }
}

.vehicles-content {
  flex: 1;
  margin-bottom: 64px;
}

.content-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 48px;
  
  .el-button {
    padding: 14px 32px;
    font-size: 17px;
    font-weight: 600;
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(133, 202, 207, 0.3);
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 16px rgba(133, 202, 207, 0.4);
    }
  }
}

.vehicles-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
}

.vehicle-card {
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: white;
  overflow: hidden;
  
  :deep(.el-card__body) {
    padding: 0;
  }
  
  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
    border-color: $primary-light;
  }
  
  &.default {
    border: 2px solid $primary-color;
    box-shadow: 0 0 0 3px rgba(133, 202, 207, 0.2);
  }
  
  .vehicle-image {
    height: 240px;
    position: relative;
    overflow: hidden;
    border-radius: 0;
    margin-bottom: 0;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s ease;
    }
    
    .default-badge {
      position: absolute;
      top: 16px;
      right: 16px;
      
      .el-tag {
        font-size: 14px;
        font-weight: 600;
        padding: 6px 16px;
        border-radius: 20px;
        box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
      }
    }
  }
  
  &:hover .vehicle-image img {
    transform: scale(1.05);
  }
  
  .vehicle-info {
    padding: 24px;
    margin-bottom: 0;
    border-bottom: 1px solid #f3f4f6;
    
    h3 {
      font-size: 24px;
      font-weight: 700;
      color: $text-primary;
      margin-bottom: 16px;
    }
    
    .vehicle-details {
      .detail-item {
        display: flex;
        margin-bottom: 12px;
        font-size: 15px;
        
        .label {
          width: 80px;
          color: $text-secondary;
          font-weight: 500;
        }
        
        .value {
          flex: 1;
          color: $text-primary;
          font-weight: 500;
        }
      }
    }
  }
  
  .vehicle-actions {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
    padding: 24px;
    background: #f9fafb;
    
    .el-button {
      flex: 1;
      min-width: 0;
      font-size: 15px;
      font-weight: 500;
      border-radius: 8px;
      padding: 10px 16px;
    }
  }
}

.vehicle-image-preview {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
  border-radius: 6px;
}

.image-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
  line-height: 178px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    border-color: $primary-color;
  }
}

@media (max-width: 768px) {
  .vehicles-grid {
    grid-template-columns: 1fr;
  }
  
  .page-header {
    padding: 48px 0;
    
    h1 {
      font-size: 28px;
    }
    
    p {
      font-size: 16px;
    }
  }
  
  .content-header {
    .el-button {
      padding: 12px 24px;
      font-size: 15px;
    }
  }
  
  .vehicle-card {
    .vehicle-image {
      height: 200px;
    }
    
    .vehicle-info {
      padding: 20px;
      
      h3 {
        font-size: 20px;
      }
    }
    
    .vehicle-actions {
      padding: 20px;
      
      .el-button {
        font-size: 14px;
        padding: 8px 12px;
      }
    }
  }
}

// 车牌号验证样式
.error-message {
  color: #EF4444;
  font-size: 14px;
  margin-top: 6px;
  line-height: 1.5;
  font-weight: 500;
}

.help-text {
  color: #6B7280;
  font-size: 13px;
  margin-top: 6px;
  line-height: 1.5;
}

.vehicle-type-select {
  .el-select {
    width: 100%;
  }
}
</style>
