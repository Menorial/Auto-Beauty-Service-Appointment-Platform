import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, register, getUserProfile, logout as logoutApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(null)
  
  // 初始化时从localStorage恢复用户信息
  const initializeFromStorage = () => {
    const storedUser = localStorage.getItem('user')
    if (storedUser) {
      try {
        user.value = JSON.parse(storedUser)
        console.log('User store initialized with user from localStorage')
      } catch (error) {
        console.error('Error parsing stored user:', error)
        localStorage.removeItem('user')
      }
    }
  }
  
  // 立即初始化
  initializeFromStorage()
  
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const isEmployee = computed(() => user.value?.role === 'EMPLOYEE')
  
  // 登录
  const loginUser = async (loginData) => {
    try {
      const payload = {
        username: loginData.username,
        password: loginData.password,
        captchaId: loginData.captchaId,
        captcha: loginData.captchaInput
      }
      const response = await login(payload)
      if (response.success) {
        token.value = response.data.token
        user.value = {
          id: response.data.id,
          username: response.data.username,
          email: response.data.email,
          realName: response.data.realName,
          role: response.data.role,
          avatarUrl: response.data.avatarUrl
        }
        
        // 保存token到localStorage
        localStorage.setItem('token', token.value)
        localStorage.setItem('user', JSON.stringify(user.value))
        
        return response
      }
      return response
    } catch (error) {
      throw error
    }
  }
  
  // 注册
  const registerUser = async (registerData) => {
    try {
      const {
        confirmPassword,
        agreement,
        captchaInput,
        ...fields
      } = registerData
      const payload = {
        ...fields,
        captcha: captchaInput
      }
      const response = await register(payload)
      return response
    } catch (error) {
      throw error
    }
  }
  
  // 获取用户信息
  const fetchUserProfile = async () => {
    try {
      const response = await getUserProfile()
      if (response.success) {
        user.value = response.data
        // 同步更新localStorage
        localStorage.setItem('user', JSON.stringify(user.value))
      }
      return response
    } catch (error) {
      throw error
    }
  }
  
  // 登出
  const logout = async () => {
    try {
      // 调用后端注销接口
      await logoutApi()
    } catch (error) {
      // 即使后端调用失败，也要清除前端状态
      console.error('注销API调用失败:', error)
    } finally {
      // 清除前端状态
      token.value = ''
      user.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
  
  // 更新用户信息
  const updateUserInfo = (userData) => {
    if (user.value) {
      Object.assign(user.value, userData)
      // 同步更新localStorage
      localStorage.setItem('user', JSON.stringify(user.value))
    }
  }
  
  // 检查token是否有效
  const isTokenValid = () => {
    if (!token.value) return false
    
    try {
      // 简单的token格式检查
      const parts = token.value.split('.')
      if (parts.length !== 3) return false
      
      // 解析payload检查过期时间
      const payload = JSON.parse(atob(parts[1]))
      const now = Date.now() / 1000
      
      if (payload.exp && payload.exp < now) {
        console.log('Token已过期')
        return false
      }
      
      return true
    } catch (error) {
      console.error('Token验证失败:', error)
      return false
    }
  }
  
  // 初始化用户信息
  const initUser = async () => {
    if (token.value && !user.value) {
      // 首先检查token是否有效
      if (!isTokenValid()) {
        console.log('Token无效或已过期，清除登录状态')
        await logout()
        return
      }
      
      try {
        console.log('初始化用户信息，当前token:', token.value.substring(0, 20) + '...')
        await fetchUserProfile()
      } catch (error) {
        console.error('获取用户信息失败，清除token:', error)
        // 如果获取用户信息失败，清除token
        await logout()
      }
    }
  }
  
  return {
    token,
    user,
    isLoggedIn,
    isAdmin,
    isEmployee,
    loginUser,
    registerUser,
    register: registerUser,  // 添加别名，供Register.vue使用
    fetchUserProfile,
    updateUserInfo,
    logout,
    initUser,
    isTokenValid
  }
})
