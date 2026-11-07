<template>
  <div class="employee-profile">
    <el-card>
      <template #header>
        <span class="title">个人信息</span>
      </template>
      
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        @submit.prevent="handleSubmit"
      >
        <!-- 头像上传 -->
        <el-row :gutter="20">
          <el-col :xs="24" :md="12">
            <el-form-item label="头像">
              <div class="avatar-upload">
                <el-avatar 
                  :src="form.avatarUrl || ''" 
                  :size="100"
                  class="current-avatar"
                  icon="User"
                />
                <el-upload
                  class="avatar-uploader"
                  action="#"
                  :auto-upload="false"
                  :on-change="handleAvatarChange"
                  accept="image/*"
                  :show-file-list="false"
                >
                  <template #trigger>
                    <el-button type="primary" size="small">
                      选择图片
                    </el-button>
                  </template>
                </el-upload>
                <el-button 
                  v-if="avatarFile"
                  type="success" 
                  size="small"
                  @click="uploadAvatar"
                  :loading="uploadingAvatar"
                >
                  上传
                </el-button>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :xs="24" :md="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" disabled />
            </el-form-item>
          </el-col>
          
          <el-col :xs="24" :md="12">
            <el-form-item label="电话" prop="phone">
              <el-input v-model="form.phone" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :xs="24" :md="12">
            <el-form-item label="专长" prop="skills">
              <el-input 
                v-model="form.skills" 
                type="textarea" 
                rows="3"
                placeholder="请输入您的专长技能"
              />
            </el-form-item>
          </el-col>
          
          <el-col :xs="24" :md="12">
            <el-form-item label="工作排班" prop="workSchedule">
              <el-input 
                v-model="form.workSchedule" 
                type="textarea" 
                rows="3"
                placeholder="请输入您的工作排班信息"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :xs="24" :md="12">
            <el-form-item label="状态">
              <el-tag :type="form.status === 1 ? 'success' : 'danger'">
                {{ form.status === 1 ? '在职' : '离职' }}
              </el-tag>
            </el-form-item>
          </el-col>
          
          <el-col :xs="24" :md="12">
            <el-form-item label="创建时间">
              {{ formatDateTime(form.createdAt) }}
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit">
            保存修改
          </el-button>
          <el-button @click="resetForm">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCurrentEmployeeInfo, updateEmployeeProfile, uploadEmployeeAvatar } from '@/api/employee'
import { formatDateTime } from '@/utils/dateUtils'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const formRef = ref()

const form = ref({
  id: '',
  name: '',
  phone: '',
  skills: '',
  workSchedule: '',
  status: 1,
  createdAt: '',
  avatarUrl: ''
})

const rules = {
  name: [
    { required: true, message: '姓名不能为空', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '电话不能为空', trigger: 'blur' }
  ]
}

const loading = ref(false)
const originalForm = ref({})
const avatarFile = ref(null)
const uploadingAvatar = ref(false)

const loadProfile = async () => {
  try {
    loading.value = true
    const response = await getCurrentEmployeeInfo()
    if (response.success) {
      form.value = response.data
      originalForm.value = JSON.parse(JSON.stringify(response.data))
    }
  } catch (error) {
    ElMessage.error('加载个人信息失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    // 调用后端API更新个人信息
    const response = await updateEmployeeProfile({
      phone: form.value.phone,
      skills: form.value.skills,
      workSchedule: form.value.workSchedule
    })
    
    if (response.success) {
      ElMessage.success('信息已保存')
      originalForm.value = JSON.parse(JSON.stringify(form.value))
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  }
}

const resetForm = () => {
  form.value = JSON.parse(JSON.stringify(originalForm.value))
  avatarFile.value = null
}

const handleAvatarChange = (file) => {
  avatarFile.value = file
}

const uploadAvatar = async () => {
  if (!avatarFile.value) {
    ElMessage.warning('请先选择图片')
    return
  }
  
  try {
    uploadingAvatar.value = true
    
    // 先本地预览
    const reader = new FileReader()
    reader.onload = (e) => {
      form.value.avatarUrl = e.target.result
    }
    reader.readAsDataURL(avatarFile.value.raw)
    
    // 调用后端API上传头像
    const response = await uploadEmployeeAvatar(avatarFile.value.raw)
    
    if (response.success) {
      // 使用后端返回的头像URL
      form.value.avatarUrl = response.data.avatarUrl
      ElMessage.success('头像已上传')
      avatarFile.value = null
    } else {
      ElMessage.error(response.message || '上传头像失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '上传头像失败')
  } finally {
    uploadingAvatar.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style lang="scss" scoped>
.employee-profile {
  .title {
    font-size: 16px;
    font-weight: bold;
  }
  
  .avatar-upload {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .current-avatar {
      border-radius: 4px;
      border: 1px solid #dcdfe6;
    }
  }
}
</style>
