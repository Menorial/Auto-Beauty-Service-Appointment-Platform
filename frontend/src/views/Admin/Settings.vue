<template>
  <div class="admin-settings">
    <el-card>
      <template #header>
        <span class="title-with-icon">
          <el-icon><Setting /></el-icon>
          系统设置
        </span>
      </template>
      
      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane label="基本设置" name="basic">
          <el-form ref="basicFormRef" :model="basicForm" :rules="basicRules" label-width="150px">
            <el-form-item label="营业开始时间" prop="businessHoursStart">
              <el-time-picker 
                v-model="basicForm.businessHoursStart" 
                format="HH:mm" 
                value-format="HH:mm"
                placeholder="请选择营业开始时间" />
            </el-form-item>
            
            <el-form-item label="营业结束时间" prop="businessHoursEnd">
              <el-time-picker 
                v-model="basicForm.businessHoursEnd" 
                format="HH:mm" 
                value-format="HH:mm"
                placeholder="请选择营业结束时间" />
            </el-form-item>
            
            <el-form-item label="可预约提前天数" prop="appointmentAdvanceDays">
              <el-input-number v-model="basicForm.appointmentAdvanceDays" :min="1" :max="30" />
              <span style="margin-left: 10px;">天</span>
            </el-form-item>
            
            <el-form-item label="每日最大预约数" prop="maxDailyAppointments">
              <el-input-number v-model="basicForm.maxDailyAppointments" :min="1" :max="100" />
            </el-form-item>
            
            <el-form-item label="预约最小间隔" prop="minAppointmentInterval">
              <el-input-number v-model="basicForm.minAppointmentInterval" :min="15" :step="15" />
              <span style="margin-left: 10px;">分钟</span>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="saveBasicSettings" :loading="basicLoading">
                保存基本设置
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="积分设置" name="points">
          <el-form ref="pointsFormRef" :model="pointsForm" :rules="pointsRules" label-width="150px">
            <el-form-item label="消费积分比例" prop="pointsPerYuan">
              <el-input-number v-model="pointsForm.pointsPerYuan" :min="0" :precision="1" :step="0.1" />
              <span style="margin-left: 10px;">积分/元</span>
            </el-form-item>
            
            <el-form-item label="评价奖励积分" prop="reviewPointsReward">
              <el-input-number v-model="pointsForm.reviewPointsReward" :min="0" />
              <span style="margin-left: 10px;">积分</span>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="savePointsSettings" :loading="pointsLoading">
                保存积分设置
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSystemConfigs, updateSystemConfig } from '@/api/system'
import { Setting } from '@element-plus/icons-vue'

const activeTab = ref('basic')
const basicLoading = ref(false)
const pointsLoading = ref(false)

const basicFormRef = ref()
const pointsFormRef = ref()

const basicForm = reactive({
  businessHoursStart: null,
  businessHoursEnd: null,
  appointmentAdvanceDays: 7,
  maxDailyAppointments: 20,
  minAppointmentInterval: 30
})

const pointsForm = reactive({
  pointsPerYuan: 1,
  reviewPointsReward: 10
})

const basicRules = {
  businessHoursStart: [{ required: true, message: '请选择营业开始时间', trigger: 'change' }],
  businessHoursEnd: [{ required: true, message: '请选择营业结束时间', trigger: 'change' }],
  appointmentAdvanceDays: [{ required: true, message: '请输入可预约提前天数', trigger: 'blur' }],
  maxDailyAppointments: [{ required: true, message: '请输入每日最大预约数', trigger: 'blur' }],
  minAppointmentInterval: [{ required: true, message: '请输入预约最小间隔', trigger: 'blur' }]
}

const pointsRules = {
  pointsPerYuan: [{ required: true, message: '请输入消费积分比例', trigger: 'blur' }],
  reviewPointsReward: [{ required: true, message: '请输入评价奖励积分', trigger: 'blur' }]
}

const saveBasicSettings = async () => {
  try {
    await basicFormRef.value.validate()
    basicLoading.value = true
    
    const configs = [
      { key: 'business_hours_start', value: basicForm.businessHoursStart || '' },
      { key: 'business_hours_end', value: basicForm.businessHoursEnd || '' },
      { key: 'appointment_advance_days', value: basicForm.appointmentAdvanceDays.toString() },
      { key: 'max_daily_appointments', value: basicForm.maxDailyAppointments.toString() },
      { key: 'min_appointment_interval', value: basicForm.minAppointmentInterval.toString() }
    ]
    
    for (const config of configs) {
      await updateSystemConfig(config.key, config.value)
    }
    
    ElMessage.success('基本设置保存成功')
  } catch (error) {
    console.error('保存基本设置失败:', error)
    ElMessage.error('保存失败')
  } finally {
    basicLoading.value = false
  }
}

const savePointsSettings = async () => {
  try {
    await pointsFormRef.value.validate()
    pointsLoading.value = true
    
    const configs = [
      { key: 'points_per_yuan', value: pointsForm.pointsPerYuan.toString() },
      { key: 'review_points_reward', value: pointsForm.reviewPointsReward.toString() }
    ]
    
    for (const config of configs) {
      await updateSystemConfig(config.key, config.value)
    }
    
    ElMessage.success('积分设置保存成功')
  } catch (error) {
    console.error('保存积分设置失败:', error)
    ElMessage.error('保存失败')
  } finally {
    pointsLoading.value = false
  }
}

const loadSettings = async () => {
  try {
    const response = await getSystemConfigs()
    const configs = response.data || []
    
    configs.forEach(config => {
      switch (config.configKey) {
        case 'business_hours_start':
          basicForm.businessHoursStart = config.configValue
          break
        case 'business_hours_end':
          basicForm.businessHoursEnd = config.configValue
          break
        case 'appointment_advance_days':
          basicForm.appointmentAdvanceDays = parseInt(config.configValue)
          break
        case 'max_daily_appointments':
          basicForm.maxDailyAppointments = parseInt(config.configValue)
          break
        case 'min_appointment_interval':
          basicForm.minAppointmentInterval = parseInt(config.configValue)
          break
        case 'points_per_yuan':
          pointsForm.pointsPerYuan = parseFloat(config.configValue)
          break
        case 'review_points_reward':
          pointsForm.reviewPointsReward = parseInt(config.configValue)
          break
      }
    })
  } catch (error) {
    console.error('加载设置失败:', error)
  }
}

onMounted(() => {
  loadSettings()
})
</script>

<style scoped>
.admin-settings {
  padding: 20px;
}

.title-with-icon {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
</style>
