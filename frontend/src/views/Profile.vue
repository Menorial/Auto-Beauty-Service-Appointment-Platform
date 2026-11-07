<template>
  <div class="profile-page">
    <Header />
    
    <div class="container">
      <div class="profile-content">
        <div class="profile-sidebar">
          <div class="user-card">
            <div class="avatar-section">
              <el-avatar :size="80" :src="userInfo.avatarUrl">
                <el-icon><User /></el-icon>
              </el-avatar>
              <el-button type="primary" link @click="showAvatarUpload = true">
                更换头像
              </el-button>
            </div>
            <div class="user-info">
              <h3>{{ userInfo.realName || userInfo.username }}</h3>
              <p>{{ userInfo.email }}</p>
              <div class="user-stats">
                <div class="stat-item">
                  <span class="stat-value">{{ userStats.totalAppointments }}</span>
                  <span class="stat-label">总预约</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ userStats.points }}</span>
                  <span class="stat-label">积分</span>
                </div>
              </div>
            </div>
          </div>
          
          <el-menu
            :default-active="activeTab"
            class="profile-menu"
            @select="handleMenuSelect"
          >
            <el-menu-item index="basic">
              <el-icon><User /></el-icon>
              <span>基本信息</span>
            </el-menu-item>
            <el-menu-item index="security">
              <el-icon><Lock /></el-icon>
              <span>安全设置</span>
            </el-menu-item>
            <el-menu-item index="points">
              <el-icon><Star /></el-icon>
              <span>积分记录</span>
            </el-menu-item>
          </el-menu>
        </div>
        
        <div class="profile-main">
          <!-- 基本信息 -->
          <div v-if="activeTab === 'basic'" class="tab-content">
            <el-card>
              <template #header>
                <div class="card-header">
                  <span>基本信息</span>
                  <el-button type="primary" @click="editMode = !editMode">
                    {{ editMode ? '取消编辑' : '编辑信息' }}
                  </el-button>
                </div>
              </template>
              
              <el-form
                ref="profileFormRef"
                :model="profileForm"
                :rules="profileRules"
                label-width="100px"
                :disabled="!editMode"
              >
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="profileForm.username" disabled />
                </el-form-item>
                
                <el-form-item label="真实姓名" prop="realName">
                  <el-input v-model="profileForm.realName" />
                </el-form-item>
                
                <el-form-item label="邮箱地址" prop="email">
                  <el-input v-model="profileForm.email" />
                </el-form-item>
                
                <el-form-item label="手机号码" prop="phone">
                  <el-input v-model="profileForm.phone" />
                </el-form-item>
                
                <el-form-item label="注册时间">
                  <el-input :value="formatDate(userInfo.createdAt)" disabled />
                </el-form-item>
                
                <el-form-item v-if="editMode">
                  <el-button type="primary" @click="saveProfile" :loading="saving">
                    保存修改
                  </el-button>
                  <el-button @click="resetForm">重置</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </div>
          
          <!-- 安全设置 -->
          <div v-if="activeTab === 'security'" class="tab-content">
            <el-card>
              <template #header>
                <span>修改密码</span>
              </template>
              
              <el-form
                ref="passwordFormRef"
                :model="passwordForm"
                :rules="passwordRules"
                label-width="120px"
              >
                <el-form-item label="当前密码" prop="currentPassword">
                  <el-input
                    v-model="passwordForm.currentPassword"
                    type="password"
                    show-password
                  />
                </el-form-item>
                
                <el-form-item label="新密码" prop="newPassword">
                  <el-input
                    v-model="passwordForm.newPassword"
                    type="password"
                    show-password
                  />
                </el-form-item>
                
                <el-form-item label="确认新密码" prop="confirmPassword">
                  <el-input
                    v-model="passwordForm.confirmPassword"
                    type="password"
                    show-password
                  />
                </el-form-item>
                
                <el-form-item>
                  <el-button type="primary" @click="changePassword" :loading="changingPassword">
                    修改密码
                  </el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </div>
          
          <!-- 积分记录 -->
          <div v-if="activeTab === 'points'" class="tab-content">
            <el-card>
              <template #header>
                <div class="card-header">
                  <span>积分记录</span>
                  <div class="points-summary">
                    <span>当前积分：</span>
                    <span class="points-value">{{ userStats.points }}</span>
                  </div>
                </div>
              </template>
              
              <el-table :data="pointRecords" v-loading="loadingPoints">
                <el-table-column prop="createdAt" label="时间" width="180">
                  <template #default="{ row }">
                    {{ formatDateTime(row.createdAt) }}
                  </template>
                </el-table-column>
                
                <el-table-column prop="type" label="类型" width="120">
                  <template #default="{ row }">
                    <el-tag :type="row.type === 'EARN' ? 'success' : 'warning'">
                      {{ row.type === 'EARN' ? '获得' : '消费' }}
                    </el-tag>
                  </template>
                </el-table-column>
                
                <el-table-column prop="points" label="积分" width="100">
                  <template #default="{ row }">
                    <span :class="row.type === 'EARN' ? 'points-earn' : 'points-spend'">
                      {{ row.type === 'EARN' ? '+' : '-' }}{{ row.points }}
                    </span>
                  </template>
                </el-table-column>
                
                <el-table-column prop="reason" label="原因" />
              </el-table>
              
              <div class="pagination" v-if="pointsTotal > 0">
                <el-pagination
                  v-model:current-page="pointsPage"
                  v-model:page-size="pointsSize"
                  :total="pointsTotal"
                  layout="prev, pager, next"
                  @current-change="loadPointRecords"
                />
              </div>
            </el-card>
          </div>
        </div>
      </div>
    </div>
    
    <Footer />
    
    <!-- 头像上传对话框 -->
    <el-dialog v-model="showAvatarUpload" title="更换头像" width="400px">
      <el-upload
        class="avatar-uploader"
        :action="uploadUrl"
        :headers="uploadHeaders"
        :show-file-list="false"
        :on-success="handleAvatarSuccess"
        :on-error="handleAvatarError"
        :before-upload="beforeAvatarUpload"
        accept="image/*"
      >
        <img v-if="newAvatarUrl" :src="newAvatarUrl" class="avatar-preview" />
        <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
      </el-upload>
      <div class="upload-tips">
        <p>支持 JPG、PNG 格式，文件大小不超过 2MB</p>
      </div>
      <template #footer>
        <el-button @click="showAvatarUpload = false">取消</el-button>
        <el-button type="primary" @click="saveAvatar" :disabled="!newAvatarUrl">
          保存
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
import { getUserProfile, updateUserProfile, changeUserPassword, getPointRecords } from '@/api/auth'
import { ElMessage } from 'element-plus'
import { User, Lock, Trophy, Calendar, Plus } from '@element-plus/icons-vue'
import { formatDateTime } from '@/utils/dateUtils'
import dayjs from 'dayjs'

const userStore = useUserStore()

const activeTab = ref('basic')
const editMode = ref(false)
const saving = ref(false)
const changingPassword = ref(false)
const loadingPoints = ref(false)
const showAvatarUpload = ref(false)
const newAvatarUrl = ref('')

const userInfo = ref({
  username: '',
  realName: '',
  email: '',
  phone: '',
  avatarUrl: '',
  address: '',
  gender: '',
  createdAt: null,
  updatedAt: null
})
const userStats = ref({
  totalAppointments: 0,
  points: 0
})
const pointRecords = ref([])
const pointsPage = ref(1)
const pointsSize = ref(10)
const pointsTotal = ref(0)

const profileFormRef = ref()
const passwordFormRef = ref()

const profileForm = reactive({
  username: '',
  realName: '',
  email: '',
  phone: ''
})

const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const uploadUrl = `${process.env.VUE_APP_API_BASE_URL}/upload/avatar`
const uploadHeaders = {
  'Authorization': `Bearer ${userStore.token}`
}

const profileRules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, message: '姓名长度不能少于2位', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ]
}

const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
    { pattern: /^(?=.*[a-zA-Z])(?=.*\d)/, message: '密码必须包含字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const formatDate = (date) => {
  if (!date) {
    return '暂无'
  }
  
  try {
    // 处理数组格式的日期 [year, month, day, hour, minute, second]
    if (Array.isArray(date) && date.length >= 3) {
      const [year, month, day, hour = 0, minute = 0, second = 0] = date
      // 注意：数组中的月份是1-12，不需要减1
      const d = dayjs(new Date(year, month - 1, day, hour, minute, second))
      if (d.isValid()) {
        return d.format('YYYY-MM-DD HH:mm:ss')
      }
    }
    
    // 处理字符串格式的日期
    const d = dayjs(date)
    if (d.isValid()) {
      return d.format('YYYY-MM-DD HH:mm:ss')
    }
    
    return '日期格式错误'
  } catch (error) {
    console.error('formatDate error:', error, 'date:', date)
    return '日期格式错误'
  }
}

// formatDateTime 函数已从 @/utils/dateUtils 导入

const handleMenuSelect = (key) => {
  activeTab.value = key
  if (key === 'points') {
    loadPointRecords()
  }
}

const loadUserInfo = async () => {
  try {
    const response = await getUserProfile()
    if (response.success) {
      console.log('User profile data:', response.data)
      console.log('createdAt:', response.data.createdAt)
      console.log('createdAt type:', typeof response.data.createdAt)
      
      userInfo.value = response.data
      // 更新表单数据
      Object.keys(profileForm).forEach(key => {
        profileForm[key] = userInfo.value[key] || ''
      })
      
      // 更新用户统计
      userStats.value = {
        totalAppointments: userInfo.value.totalAppointments || 0,
        points: userInfo.value.points || 0
      }
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    ElMessage.error('加载用户信息失败')
  }
}

const saveProfile = async () => {
  if (!profileFormRef.value) return
  
  try {
    await profileFormRef.value.validate()
    saving.value = true
    
    const response = await updateUserProfile(profileForm)
    if (response.success) {
      ElMessage.success('保存成功')
      editMode.value = false
      await loadUserInfo()
      // 更新store中的用户信息
      userStore.updateUserInfo(response.data)
    }
  } catch (error) {
    console.error('保存用户信息失败:', error)
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const resetForm = () => {
  Object.keys(profileForm).forEach(key => {
    profileForm[key] = userInfo.value[key] || ''
  })
}

const changePassword = async () => {
  if (!passwordFormRef.value) return
  
  try {
    await passwordFormRef.value.validate()
    changingPassword.value = true
    
    const response = await changeUserPassword({
      oldPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword
    })
    
    if (response.success) {
      ElMessage.success('密码修改成功')
      // 重置表单
      Object.keys(passwordForm).forEach(key => {
        passwordForm[key] = ''
      })
      passwordFormRef.value.resetFields()
    }
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error(error.message || '修改密码失败')
  } finally {
    changingPassword.value = false
  }
}

const loadPointRecords = async () => {
  loadingPoints.value = true
  try {
    const response = await getPointRecords({
      page: pointsPage.value - 1,
      size: pointsSize.value
    })
    
    if (response.success) {
      pointRecords.value = response.data.content || response.data
      pointsTotal.value = response.data.totalElements || response.data.length
    }
  } catch (error) {
    console.error('加载积分记录失败:', error)
    ElMessage.error('加载积分记录失败')
  } finally {
    loadingPoints.value = false
  }
}

const beforeAvatarUpload = (file) => {
  console.log('Before avatar upload:', { file, uploadUrl, uploadHeaders })
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
  console.log('Avatar upload success - Full response:', response)
  console.log('Response data:', response.data)
  console.log('Current newAvatarUrl before:', newAvatarUrl.value)
  
  if (response.success) {
    // 后端返回的是Base64 data URL，直接使用即可
    newAvatarUrl.value = response.data
    
    console.log('New avatar URL set to:', response.data)
    console.log('Current newAvatarUrl after:', newAvatarUrl.value)
    
    ElMessage.success('头像上传成功，请点击保存按钮')
  } else {
    console.error('Upload failed:', response.message)
    ElMessage.error(response.message || '上传失败')
  }
}

const handleAvatarError = (error) => {
  console.error('Avatar upload error:', error)
  ElMessage.error('头像上传失败，请检查网络连接')
}

const saveAvatar = async () => {
  try {
    const response = await updateUserProfile({ avatarUrl: newAvatarUrl.value })
    if (response.success) {
      ElMessage.success('头像更新成功')
      showAvatarUpload.value = false
      newAvatarUrl.value = ''
      await loadUserInfo()
      userStore.updateUserInfo(response.data)
    }
  } catch (error) {
    console.error('更新头像失败:', error)
    ElMessage.error('更新头像失败')
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style lang="scss" scoped>
.profile-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.profile-content {
  display: flex;
  gap: 24px;
  margin: 40px 0;
}

.profile-sidebar {
  width: 280px;
  
  .user-card {
    background: white;
    border-radius: 12px;
    padding: 24px;
    box-shadow: $box-shadow-base;
    margin-bottom: 24px;
    text-align: center;
    
    .avatar-section {
      margin-bottom: 16px;
      
      .el-button {
        margin-top: 8px;
      }
    }
    
    .user-info {
      h3 {
        font-size: 20px;
        font-weight: 600;
        margin-bottom: 8px;
        color: $text-primary;
      }
      
      p {
        color: $text-secondary;
        margin-bottom: 16px;
      }
      
      .user-stats {
        display: flex;
        justify-content: space-around;
        
        .stat-item {
          text-align: center;
          
          .stat-value {
            display: block;
            font-size: 24px;
            font-weight: 600;
            color: $primary-color;
          }
          
          .stat-label {
            font-size: 12px;
            color: $text-secondary;
          }
        }
      }
    }
  }
  
  .profile-menu {
    background: white;
    border-radius: 12px;
    box-shadow: $box-shadow-base;
    border: none;
  }
}

.profile-main {
  flex: 1;
}

.tab-content {
  .el-card {
    box-shadow: $box-shadow-base;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .points-summary {
    .points-value {
      font-size: 18px;
      font-weight: 600;
      color: $primary-color;
    }
  }
}

.points-earn {
  color: $success-color;
  font-weight: 600;
}

.points-spend {
  color: $warning-color;
  font-weight: 600;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.avatar-uploader {
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: all 0.3s;
    
    &:hover {
      border-color: $primary-color;
    }
  }
  
  .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 178px;
    height: 178px;
    text-align: center;
    line-height: 178px;
  }
  
  .avatar-preview {
    width: 178px;
    height: 178px;
    display: block;
    object-fit: cover;
  }
}

.upload-tips {
  text-align: center;
  margin-top: 16px;
  
  p {
    color: $text-secondary;
    font-size: 14px;
  }
}

@media (max-width: 768px) {
  .profile-content {
    flex-direction: column;
  }
  
  .profile-sidebar {
    width: 100%;
  }
}
</style>
