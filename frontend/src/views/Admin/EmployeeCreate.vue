<template>
  <div class="employee-create">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title-with-icon">
            <el-icon><UserFilled /></el-icon>
            {{ isEdit ? '编辑员工' : '创建员工' }}
          </span>
          <el-button @click="$router.go(-1)">
            <el-icon><Back /></el-icon>
            返回
          </el-button>
        </div>
      </template>
      
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="员工姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入员工姓名" />
        </el-form-item>
        
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        
        <el-form-item label="技能描述" prop="skills">
          <el-input v-model="form.skills" type="textarea" :rows="3" placeholder="请输入技能描述" />
        </el-form-item>
        
        <el-form-item label="工作日" prop="workDays">
          <el-checkbox-group v-model="form.workDays">
            <el-checkbox label="周一">周一</el-checkbox>
            <el-checkbox label="周二">周二</el-checkbox>
            <el-checkbox label="周三">周三</el-checkbox>
            <el-checkbox label="周四">周四</el-checkbox>
            <el-checkbox label="周五">周五</el-checkbox>
            <el-checkbox label="周六">周六</el-checkbox>
            <el-checkbox label="周日">周日</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        
        <el-form-item label="工作时间" prop="workTime">
          <div class="work-time-selector">
            <el-time-picker
              v-model="form.startTime"
              placeholder="开始时间"
              format="HH:mm"
              value-format="HH:mm"
              style="width: 120px; margin-right: 10px;"
            />
            <span style="margin: 0 10px;">至</span>
            <el-time-picker
              v-model="form.endTime"
              placeholder="结束时间"
              format="HH:mm"
              value-format="HH:mm"
              style="width: 120px;"
            />
          </div>
        </el-form-item>
        
        <el-form-item label="员工头像">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
            accept="image/*"
          >
            <img v-if="form.avatarUrl" :src="form.avatarUrl" class="avatar" />
            <div v-else class="avatar-placeholder">
              <el-icon class="avatar-uploader-icon"><Plus /></el-icon>
              <div class="upload-text">点击上传头像</div>
            </div>
          </el-upload>
          <div class="upload-tip">建议上传 200x200 像素的图片</div>
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-switch 
            v-model="form.status" 
            :active-value="1" 
            :inactive-value="0"
            active-text="在职"
            inactive-text="离职"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            <el-icon><Check /></el-icon>
            {{ isEdit ? '更新' : '创建' }}
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, UserFilled, Back, Check, RefreshLeft } from '@element-plus/icons-vue'
import { createEmployee, updateEmployee, getEmployeeById } from '@/api/employee'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const loading = ref(false)
const isEdit = ref(false)

const form = reactive({
  name: '',
  phone: '',
  skills: '',
  workDays: [],
  startTime: '09:00',
  endTime: '18:00',
  workSchedule: '', // 保留用于后端兼容
  avatarUrl: '',
  status: 1
})

const userStore = useUserStore()
const uploadUrl = `${process.env.VUE_APP_API_BASE_URL}/upload/avatar`
const uploadHeaders = {
  'Authorization': `Bearer ${userStore.token}`
}

const rules = {
  name: [{ required: true, message: '请输入员工姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  skills: [{ required: true, message: '请输入技能描述', trigger: 'blur' }],
  workDays: [{ required: true, message: '请选择工作日', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

// 格式化工作时间为字符串
const formatWorkSchedule = () => {
  if (form.workDays.length === 0 || !form.startTime || !form.endTime) {
    return ''
  }
  
  const daysStr = form.workDays.join('、')
  return `${daysStr} ${form.startTime}-${form.endTime}`
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    loading.value = true

    // 格式化工作时间
    form.workSchedule = formatWorkSchedule()

    console.log('EmployeeCreate - 提交前检查认证状态:')
    console.log('- Store token exists:', !!userStore.token)
    console.log('- Store token value:', userStore.token ? userStore.token.substring(0, 50) + '...' : 'No token')
    console.log('- LocalStorage token exists:', !!localStorage.getItem('token'))
    console.log('- LocalStorage token value:', localStorage.getItem('token') ? localStorage.getItem('token').substring(0, 50) + '...' : 'No token')
    console.log('- User logged in:', userStore.isLoggedIn)

    if (isEdit.value) {
      await updateEmployee(route.params.id, form)
      ElMessage.success('员工信息更新成功')
    } else {
      await createEmployee(form)
      ElMessage.success('员工创建成功')
    }

    router.push('/admin/employees')
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  formRef.value.resetFields()
}

// 解析工作时间字符串
const parseWorkSchedule = (workSchedule) => {
  if (!workSchedule) return
  
  try {
    // 解析格式：周一、周二、周三 09:00-18:00
    const parts = workSchedule.split(' ')
    if (parts.length >= 2) {
      const daysStr = parts[0]
      const timeStr = parts[1]
      
      // 解析工作日
      if (daysStr) {
        form.workDays = daysStr.split('、').filter(day => day.trim())
      }
      
      // 解析时间
      if (timeStr && timeStr.includes('-')) {
        const [startTime, endTime] = timeStr.split('-')
        form.startTime = startTime.trim()
        form.endTime = endTime.trim()
      }
    }
  } catch (error) {
    console.error('解析工作时间失败:', error)
  }
}

const fetchEmployeeDetail = async () => {
  try {
    const response = await getEmployeeById(route.params.id)
    Object.assign(form, response.data)
    
    // 解析工作时间
    if (response.data.workSchedule) {
      parseWorkSchedule(response.data.workSchedule)
    }
  } catch (error) {
    console.error('获取员工详情失败:', error)
  }
}

// 头像上传相关
const beforeAvatarUpload = (file) => {
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

const handleAvatarSuccess = (response) => {
  if (response.success) {
    // 直接使用Base64数据
    form.avatarUrl = response.data
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(response.message || '头像上传失败')
  }
}

onMounted(() => {
  if (route.params.id) {
    isEdit.value = true
    fetchEmployeeDetail()
  }
})
</script>

<style scoped>
.employee-create {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .title-with-icon {
    display: inline-flex;
    align-items: center;
    gap: 8px;
  }
}

.work-time-selector {
  display: flex;
  align-items: center;
}

:deep(.el-checkbox-group) {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

:deep(.el-checkbox) {
  margin-right: 0;
}

.avatar-uploader {
  display: inline-block;
}

.avatar-uploader .avatar {
  width: 120px;
  height: 120px;
  display: block;
  border-radius: 6px;
  object-fit: cover;
  cursor: pointer;
  transition: all 0.3s;
}

.avatar-uploader .avatar:hover {
  opacity: 0.8;
}

.avatar-placeholder {
  width: 120px;
  height: 120px;
  border: 2px dashed #d9d9d9;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
  background-color: #fafafa;
}

.avatar-placeholder:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.upload-text {
  margin-top: 8px;
  font-size: 12px;
  color: #8c939d;
}

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
