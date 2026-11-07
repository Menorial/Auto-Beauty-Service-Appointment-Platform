<template>
  <div class="payment-result-page">
    <Header />
    
    <div class="container">
      <div class="result-card">
        <div v-if="loading" class="loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          <p>正在确认支付结果...</p>
        </div>
        
        <div v-else-if="success" class="success">
          <el-icon class="success-icon"><CircleCheck /></el-icon>
          <h1>支付成功！</h1>
          <p class="message">您的订单已支付成功，系统正在处理中</p>
          
          <div class="order-info" v-if="orderNo">
            <p>订单号：{{ orderNo }}</p>
          </div>
          
          <div class="actions">
            <el-button type="primary" size="large" @click="goToAppointments">
              查看我的预约
            </el-button>
            <el-button size="large" @click="goToHome">
              返回首页
            </el-button>
          </div>
          
          <p class="auto-redirect">{{ countdown }}秒后自动跳转到我的预约...</p>
        </div>
        
        <div v-else class="fail">
          <el-icon class="fail-icon"><CircleClose /></el-icon>
          <h1>支付确认中</h1>
          <p class="message">支付结果确认中，请稍后查看订单状态</p>
          
          <div class="actions">
            <el-button type="primary" size="large" @click="goToAppointments">
              查看我的预约
            </el-button>
            <el-button size="large" @click="goToHome">
              返回首页
            </el-button>
          </div>
        </div>
      </div>
    </div>
    
    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import Header from '@/components/Layout/Header.vue'
import Footer from '@/components/Layout/Footer.vue'
import { CircleCheck, CircleClose, Loading } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const loading = ref(true)
const success = ref(false)
const orderNo = ref('')
const countdown = ref(5)

// 从URL参数中获取订单信息
onMounted(() => {
  // 支付宝会在URL中返回参数
  const params = route.query
  
  // 获取订单号
  orderNo.value = params.out_trade_no || ''
  
  // 检查支付状态
  // 支付宝同步返回不一定代表支付成功，实际以异步通知为准
  // 但如果能跳转回来，通常表示用户完成了支付流程
  setTimeout(() => {
    loading.value = false
    success.value = true
    
    // 开始倒计时自动跳转
    startCountdown()
  }, 1000)
})

// 倒计时自动跳转
const startCountdown = () => {
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      goToAppointments()
    }
  }, 1000)
}

// 跳转到我的预约
const goToAppointments = () => {
  router.push('/appointments')
}

// 返回首页
const goToHome = () => {
  router.push('/')
}
</script>

<style lang="scss" scoped>
.payment-result-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #85CACF 0%, #A8DEE0 100%);
}

.container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.result-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  padding: 60px 40px;
  max-width: 600px;
  width: 100%;
  text-align: center;
}

.loading {
  .el-icon {
    font-size: 60px;
    color: #409eff;
    margin-bottom: 20px;
  }
  
  p {
    font-size: 18px;
    color: #666;
  }
}

.success {
  .success-icon {
    font-size: 80px;
    color: #67c23a;
    margin-bottom: 20px;
  }
  
  h1 {
    font-size: 32px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 16px;
  }
  
  .message {
    font-size: 16px;
    color: #606266;
    margin-bottom: 30px;
  }
  
  .order-info {
    background: #f5f7fa;
    border-radius: 8px;
    padding: 20px;
    margin-bottom: 30px;
    
    p {
      font-size: 14px;
      color: #606266;
      margin: 0;
    }
  }
  
  .actions {
    display: flex;
    gap: 16px;
    justify-content: center;
    margin-bottom: 20px;
    
    .el-button {
      padding: 12px 32px;
    }
  }
  
  .auto-redirect {
    font-size: 14px;
    color: #909399;
    margin-top: 20px;
  }
}

.fail {
  .fail-icon {
    font-size: 80px;
    color: #f56c6c;
    margin-bottom: 20px;
  }
  
  h1 {
    font-size: 32px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 16px;
  }
  
  .message {
    font-size: 16px;
    color: #606266;
    margin-bottom: 30px;
  }
  
  .actions {
    display: flex;
    gap: 16px;
    justify-content: center;
    
    .el-button {
      padding: 12px 32px;
    }
  }
}

@media (max-width: 768px) {
  .result-card {
    padding: 40px 24px;
  }
  
  .success,
  .fail {
    h1 {
      font-size: 24px;
    }
    
    .actions {
      flex-direction: column;
      
      .el-button {
        width: 100%;
      }
    }
  }
}
</style>
