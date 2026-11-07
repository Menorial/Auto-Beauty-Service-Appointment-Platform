<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-form">
        <div class="form-header">
          <h2>用户登录</h2>
          <p>欢迎回到汽车美容预约平台</p>
        </div>
        
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名/邮箱/手机号"
              size="large"
              prefix-icon="User"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item prop="captchaInput">
            <div class="captcha-block">
              <div class="captcha-label-text">图形验证码</div>
              <p class="captcha-desc">请输入验证码，提交时需区分大小写</p>
              <div class="captcha-row">
                <el-input
                  v-model="loginForm.captchaInput"
                  placeholder="请输入 6 位验证码"
                  size="large"
                  maxlength="6"
                  clearable
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
          
          <el-form-item>
            <div class="form-options">
              <el-checkbox v-model="rememberMe">记住我</el-checkbox>
              <el-link type="primary" @click="$router.push('/forgot-password')">
                忘记密码？
              </el-link>
            </div>
          </el-form-item>
          
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              @click="handleLogin"
              class="login-btn"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="form-footer">
          <span>还没有账号？</span>
          <el-link type="primary" @click="$router.push('/register')">
            立即注册
          </el-link>
        </div>
      </div>
      
      <div class="login-banner">
        <div class="banner-content">
          <h3>专业汽车美容服务</h3>
          <p>为您的爱车提供最优质的美容护理服务</p>
          <ul class="feature-list">
            <li><el-icon><Check /></el-icon> 专业技师团队</li>
            <li><el-icon><Check /></el-icon> 优质服务保障</li>
            <li><el-icon><Check /></el-icon> 便捷在线预约</li>
            <li><el-icon><Check /></el-icon> 贴心售后服务</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getCaptcha } from '@/api/auth'
import { ElMessage } from 'element-plus'
import { User, Lock, Check } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref()
const loading = ref(false)
const rememberMe = ref(false)
const captchaCode = ref('')

const refreshCaptcha = async () => {
  try {
    const res = await getCaptcha()
    if (res.success && res.data) {
      loginForm.captchaId = res.data.captchaId
      captchaCode.value = res.data.captcha
      loginForm.captchaInput = ''
    } else {
      ElMessage.error(res.message || '验证码加载失败，请稍后重试')
    }
  } catch (e) {
    console.error(e)
  }
}

const loginForm = reactive({
  username: '',
  password: '',
  captchaId: '',
  captchaInput: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名/邮箱/手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  captchaInput: [
    { required: true, message: '请填写图形验证码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请填写图形验证码'))
        } else if (value.length !== 6) {
          callback(new Error('验证码长度应为 6 位'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

onMounted(() => {
  refreshCaptcha()
})

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    if (!loginForm.captchaId) {
      ElMessage.warning('验证码尚未生成，请稍后或单击右侧区域刷新')
      await refreshCaptcha()
      return
    }
    loading.value = true
    
    await userStore.loginUser(loginForm)
    
    ElMessage.success('登录成功')
    
    // 根据角色重定向
    let redirectPath = '/'
    if (userStore.user?.role === 'ADMIN') {
      redirectPath = '/admin'
    } else if (userStore.user?.role === 'EMPLOYEE') {
      redirectPath = '/employee'
    }
    
    // 如果有查询参数中的重定向路径，优先使用
    const redirect = router.currentRoute.value.query.redirect
    if (redirect) {
      redirectPath = redirect
    }
    
    router.push(redirectPath)
    
  } catch (error) {
    console.error('登录失败:', error)
    // 错误消息已经在 request.js 的响应拦截器中显示了，这里不需要重复显示
    await refreshCaptcha()
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background: #A8DEE0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-container {
  display: flex;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  max-width: 900px;
  width: 100%;
}

.login-form {
  flex: 1;
  padding: 48px;
  
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
    margin-bottom: 24px;
  }
  
  .form-options {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .login-btn {
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

.login-banner {
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
      margin-bottom: 32px;
      opacity: 0.9;
    }
    
    .feature-list {
      list-style: none;
      text-align: left;
      
      li {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 16px;
        font-size: 16px;
        
        .el-icon {
          background: rgba(255, 255, 255, 0.2);
          border-radius: 50%;
          padding: 4px;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
    max-width: 400px;
  }
  
  .login-form {
    padding: 32px 24px;
  }
  
  .login-banner {
    padding: 32px 24px;
    
    .banner-content {
      h3 {
        font-size: 24px;
      }
      
      p {
        font-size: 16px;
      }
    }
  }
}
</style>
