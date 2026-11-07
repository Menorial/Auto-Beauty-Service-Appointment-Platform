import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// 创建axios实例
const service = axios.create({
  baseURL: process.env.VUE_APP_API_BASE_URL || 'http://localhost:8081/api',
  timeout: 15000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    const localStorageToken = localStorage.getItem('token')
    
    console.log('Request interceptor - Store token exists:', !!userStore.token)
    console.log('Request interceptor - LocalStorage token exists:', !!localStorageToken)
    console.log('Request interceptor - Store token value:', userStore.token ? userStore.token.substring(0, 50) + '...' : 'No token')
    console.log('Request interceptor - LocalStorage token value:', localStorageToken ? localStorageToken.substring(0, 50) + '...' : 'No token')
    
    // 使用store中的token或localStorage中的token
    const tokenToUse = userStore.token || localStorageToken
    
    if (tokenToUse) {
      config.headers['Authorization'] = `Bearer ${tokenToUse}`
      console.log('Request interceptor - Authorization header set with token:', tokenToUse.substring(0, 50) + '...')
    } else {
      console.log('Request interceptor - No token available, skipping Authorization header')
    }
    
    console.log('Request interceptor - Final headers:', config.headers)
    return config
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    
    // 如果返回的状态码不是200，说明接口报错了
    if (response.status !== 200) {
      ElMessage.error(res.message || 'Error')
      return Promise.reject(new Error(res.message || 'Error'))
    } else {
      return res
    }
  },
  error => {
    console.error('Response error:', error)
    
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401: {
          const userStore = useUserStore()
          console.log('Response interceptor - 401 error, logging out')
          ElMessage.error('登录已过期，请重新登录')
          userStore.logout()
          router.push('/login')
          break
        }
        case 403:
          // 优先显示后端返回的具体错误消息
          ElMessage.error(data?.message || '没有权限访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          // 优先显示后端返回的错误消息
          if (data && data.message) {
            ElMessage.error(data.message)
          } else {
            ElMessage.error(`请求失败 ${status}`)
          }
      }
    } else if (error.request) {
      ElMessage.error('网络连接失败，请检查网络')
    } else {
      ElMessage.error('请求配置错误')
    }
    
    return Promise.reject(error)
  }
)

export default service
