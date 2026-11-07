<template>
  <div class="change-password">
    <el-card>
      <template #header>
        <span class="title">修改密码</span>
      </template>
      
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        style="max-width: 500px"
      >
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input 
            v-model="form.oldPassword" 
            type="password"
            show-password
            placeholder="请输入当前密码"
          />
        </el-form-item>
        
        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="form.newPassword" 
            type="password"
            show-password
            placeholder="请输入新密码（至少6位）"
          />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="form.confirmPassword" 
            type="password"
            show-password
            placeholder="请再次输入新密码"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            修改密码
          </el-button>
          <el-button @click="resetForm">
            重置
          </el-button>
        </el-form-item>
      </el-form>
      
      <el-alert 
        title="安全提示" 
        type="warning" 
        :closable="false"
        description="请定期修改密码以保护您的账户安全。修改密码后，您需要重新登录。"
        style="margin-top: 20px"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { changePassword } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()

const form = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateNewPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('新密码不能为空'))
  } else if (value.length < 6) {
    callback(new Error('新密码长度不能少于6位'))
  } else if (value === form.value.oldPassword) {
    callback(new Error('新密码不能与当前密码相同'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('确认密码不能为空'))
  } else if (value !== form.value.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  oldPassword: [
    { required: true, message: '当前密码不能为空', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  newPassword: [
    { validator: validateNewPassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const loading = ref(false)

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    loading.value = true
    
    // 调用后端API修改密码
    const response = await changePassword({
      oldPassword: form.value.oldPassword,
      newPassword: form.value.newPassword,
      confirmPassword: form.value.confirmPassword
    })
    
    if (response.success) {
      ElMessageBox.confirm(
        '密码修改成功，请重新登录',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'success'
        }
      ).then(() => {
        // 清除登录信息
        userStore.logout()
        router.push('/login')
      }).catch(() => {})
    } else {
      ElMessage.error(response.message || '修改密码失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '修改密码失败')
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  formRef.value?.resetFields()
}
</script>

<style lang="scss" scoped>
.change-password {
  .title {
    font-size: 16px;
    font-weight: bold;
  }
}
</style>
