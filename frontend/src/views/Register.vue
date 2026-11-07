<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-form">
        <div class="form-header">
          <h2>用户注册</h2>
          <p>创建您的账号，开始享受专业汽车美容服务</p>
        </div>
        
        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          @submit.prevent="handleRegister"
        >
          <el-form-item prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="请输入用户名"
              size="large"
              prefix-icon="User"
              @blur="checkUsername"
            />
          </el-form-item>
          
          <el-form-item prop="realName">
            <el-input
              v-model="registerForm.realName"
              placeholder="请输入真实姓名"
              size="large"
              prefix-icon="UserFilled"
            />
          </el-form-item>
          
          <el-form-item prop="email">
            <el-input
              v-model="registerForm.email"
              placeholder="请输入邮箱地址"
              size="large"
              prefix-icon="Message"
              @blur="checkEmail"
            />
          </el-form-item>
          
          <el-form-item prop="phone">
            <el-input
              v-model="registerForm.phone"
              placeholder="请输入手机号码"
              size="large"
              prefix-icon="Phone"
              @blur="checkPhone"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请确认密码"
              size="large"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item prop="captchaInput">
            <div class="captcha-block">
              <div class="captcha-label-text">图形验证码</div>
              <p class="captcha-desc">请输入验证码，提交时需区分大小写</p>
              <div class="captcha-row">
                <el-input
                  v-model="registerForm.captchaInput"
                  placeholder="请输入 6 位验证码"
                  size="large"
                  maxlength="6"
                  clearable
                  @keyup.enter="handleRegister"
                />
                <div
                  class="captcha-code"
                  tabindex="0"
                  role="button"
                  aria-label="刷新图形验证码"
                  @click="refreshCaptcha"
                  @keyup.enter="refreshCaptcha"
                  title="刷新验证码"
                >
                  {{ captchaCode || '载入中' }}
                </div>
              </div>
              <p class="captcha-hint">若验证码难以辨认，单击重新获取</p>
            </div>
          </el-form-item>
          
          <el-form-item prop="agreement">
            <el-checkbox v-model="registerForm.agreement">
              我已阅读并同意
              <el-link type="primary" @click.prevent="goUserAgreement">《用户服务协议》</el-link>
              与
              <el-link type="primary" @click.prevent="goPrivacyPolicy">《隐私政策》</el-link>
            </el-checkbox>
          </el-form-item>
          
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              @click="handleRegister"
              class="register-btn"
            >
              注册
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="form-footer">
          <span>已有账号？</span>
          <el-link type="primary" @click="$router.push('/login')">
            立即登录
          </el-link>
        </div>
      </div>
      
      <div class="register-banner">
        <div class="banner-content">
          <h3>加入我们</h3>
          <p>享受专业的汽车美容服务体验</p>
          <div class="steps">
            <div class="step">
              <div class="step-icon">1</div>
              <h4>注册账号</h4>
              <p>快速创建您的专属账号</p>
            </div>
            <div class="step">
              <div class="step-icon">2</div>
              <h4>选择服务</h4>
              <p>浏览并选择适合的服务项目</p>
            </div>
            <div class="step">
              <div class="step-icon">3</div>
              <h4>在线预约</h4>
              <p>便捷预约，享受专业服务</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { checkUsernameExists, checkEmailExists, checkPhoneExists, getCaptcha } from '@/api/auth'
import { ElMessage } from 'element-plus'
import { User, UserFilled, Message, Phone, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const registerFormRef = ref()
const loading = ref(false)
const captchaCode = ref('')

const refreshCaptcha = async () => {
  try {
    const res = await getCaptcha()
    if (res.success && res.data) {
      registerForm.captchaId = res.data.captchaId
      captchaCode.value = res.data.captcha
      registerForm.captchaInput = ''
    } else {
      ElMessage.error(res.message || '验证码加载失败，请稍后重试')
    }
  } catch (e) {
    console.error(e)
  }
}

const registerForm = reactive({
  username: '',
  realName: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
  captchaId: '',
  captchaInput: '',
  agreement: false
})

const validateUsername = async (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入用户名'))
  } else if (value.length < 3) {
    callback(new Error('用户名长度不能少于3位'))
  } else if (!/^[a-zA-Z0-9_]+$/.test(value)) {
    callback(new Error('用户名只能包含字母、数字和下划线'))
  } else {
    callback()
  }
}

const validateEmail = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入邮箱地址'))
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
    callback(new Error('请输入正确的邮箱格式'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入手机号码'))
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号格式'))
  } else {
    callback()
  }
}

const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入密码'))
  } else if (value.length < 6) {
    callback(new Error('密码长度不能少于6位'))
  } else if (!/^(?=.*[a-zA-Z])(?=.*\d)/.test(value)) {
    callback(new Error('密码必须包含字母和数字'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请确认密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const validateCaptcha = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请填写图形验证码'))
  } else if (value.length !== 6) {
    callback(new Error('验证码长度应为 6 位'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [{ validator: validateUsername, trigger: 'blur' }],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, message: '姓名长度不能少于2位', trigger: 'blur' }
  ],
  email: [{ validator: validateEmail, trigger: 'blur' }],
  phone: [{ validator: validatePhone, trigger: 'blur' }],
  password: [{ validator: validatePassword, trigger: 'blur' }],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }],
  captchaInput: [{ validator: validateCaptcha, trigger: 'blur' }],
  agreement: [
    { 
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请阅读并同意《用户服务协议》与《隐私政策》'))
        } else {
          callback()
        }
      }, 
      trigger: 'change' 
    }
  ]
}

onMounted(() => {
  refreshCaptcha()
})

const checkUsername = async () => {
  if (registerForm.username && registerForm.username.length >= 3) {
    try {
      const response = await checkUsernameExists(registerForm.username)
      // response.data = true 表示可用，false 表示已存在
      if (response.data === false) {
        ElMessage.warning('用户名已存在')
      }
    } catch (error) {
      console.error('检查用户名失败:', error)
    }
  }
}

const checkEmail = async () => {
  if (registerForm.email && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(registerForm.email)) {
    try {
      const response = await checkEmailExists(registerForm.email)
      // response.data = true 表示可用，false 表示已存在
      if (response.data === false) {
        ElMessage.warning('邮箱已被注册')
      }
    } catch (error) {
      console.error('检查邮箱失败:', error)
    }
  }
}

const checkPhone = async () => {
  if (registerForm.phone && /^1[3-9]\d{9}$/.test(registerForm.phone)) {
    try {
      const response = await checkPhoneExists(registerForm.phone)
      // response.data = true 表示可用，false 表示已存在
      if (response.data === false) {
        ElMessage.warning('手机号已被注册')
      }
    } catch (error) {
      console.error('检查手机号失败:', error)
    }
  }
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  try {
    await registerFormRef.value.validate()
    if (!registerForm.captchaId) {
      ElMessage.warning('验证码尚未生成，请稍后或单击右侧区域刷新')
      await refreshCaptcha()
      return
    }
    loading.value = true
    
    const { confirmPassword, agreement, ...registerData } = registerForm
    await userStore.register(registerData)
    
    ElMessage.success('注册成功，请登录')
    router.push('/login')
    
  } catch (error) {
    console.error('注册失败:', error)
    ElMessage.error(error.message || '注册失败，请重试')
    await refreshCaptcha()
  } finally {
    loading.value = false
  }
}

const goUserAgreement = () => {
  router.push({ name: 'UserAgreement' })
}

const goPrivacyPolicy = () => {
  router.push({ name: 'PrivacyPolicy' })
}
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  background: #A8DEE0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.register-container {
  display: flex;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  max-width: 1000px;
  width: 100%;
}

.register-form {
  flex: 1;
  padding: 48px;
  max-height: 90vh;
  overflow-y: auto;
  
  .form-header {
    text-align: center;
    margin-bottom: 32px;
    
    h2 {
      font-size: 28px;
      font-weight: 600;
      color: $text-primary;
      margin-bottom: 8px;
    }
    
    p {
      color: $text-secondary;
      font-size: 16px;
    }
  }
  
  .el-form-item {
    margin-bottom: 20px;
  }
  
  .register-btn {
    width: 100%;
    height: 48px;
    font-size: 16px;
    font-weight: 600;
    --el-button-bg-color: #85cacf;
    --el-button-border-color: #85cacf;
    --el-button-hover-bg-color: #A8DEE0;
    --el-button-hover-border-color: #A8DEE0;
    --el-button-active-bg-color: #A7D676;
    --el-button-active-border-color: #A7D676;
  }

  .captcha-block {
    width: 100%;
  }

  .captcha-row {
    display: flex;
    width: 100%;
    gap: 12px;
    align-items: center;
  }

  .captcha-label-text {
    font-size: 14px;
    font-weight: 500;
    color: $text-primary;
    margin-bottom: 6px;
    letter-spacing: 0.02em;
  }

  .captcha-desc {
    margin: 0 0 10px;
    font-size: 12px;
    line-height: 1.55;
    color: $text-secondary;
  }

  .captcha-hint {
    margin: 8px 0 0;
    font-size: 12px;
    line-height: 1.5;
    color: #909399;
  }

  .captcha-code {
    min-width: 120px;
    height: 40px;
    line-height: 40px;
    text-align: center;
    border-radius: 6px;
    border: 1px solid #dcdfe6;
    background: #f5f7fa;
    font-weight: 700;
    letter-spacing: 2px;
    color: #303133;
    user-select: none;
    cursor: pointer;
  }
  
  .form-footer {
    text-align: center;
    margin-top: 24px;
    color: $text-secondary;
    
    .el-link {
      margin-left: 8px;
    }
  }
}

.register-banner {
  flex: 1;
  background: linear-gradient(135deg, #A7D676 0%, #85CACF 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  padding: 48px;
  
  .banner-content {
    text-align: center;
    
    h3 {
      font-size: 32px;
      font-weight: 700;
      margin-bottom: 16px;
    }
    
    p {
      font-size: 18px;
      margin-bottom: 48px;
      opacity: 0.9;
    }
    
    .steps {
      .step {
        margin-bottom: 32px;
        
        .step-icon {
          width: 48px;
          height: 48px;
          background: rgba(255, 255, 255, 0.2);
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 20px;
          font-weight: 600;
          margin: 0 auto 16px;
        }
        
        h4 {
          font-size: 18px;
          margin-bottom: 8px;
        }
        
        p {
          font-size: 14px;
          opacity: 0.8;
          margin: 0;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .register-container {
    flex-direction: column;
    max-width: 400px;
  }
  
  .register-form {
    padding: 32px 24px;
  }
  
  .register-banner {
    padding: 32px 24px;
    
    .banner-content {
      h3 {
        font-size: 24px;
      }
      
      p {
        font-size: 16px;
        margin-bottom: 32px;
      }
      
      .steps .step {
        margin-bottom: 24px;
      }
    }
  }
}
</style>
