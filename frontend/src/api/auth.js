import request from '@/utils/request'

// 获取验证码（服务端生成，返回 captchaId + 展示用 captcha）
export const getCaptcha = () => {
  return request({
    url: '/auth/captcha',
    method: 'get'
  })
}

// 用户登录
export const login = (data) => {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

// 用户注册
export const register = (data) => {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

// 获取用户信息
export const getUserProfile = () => {
  return request({
    url: '/user/profile',
    method: 'get'
  })
}

// 更新用户信息
export const updateUserProfile = (data) => {
  return request({
    url: '/user/profile',
    method: 'put',
    data
  })
}

// 获取用户积分
export const getUserPoints = () => {
  return request({
    url: '/user/points',
    method: 'get'
  })
}

// 获取积分记录
export const getPointRecords = (params) => {
  return request({
    url: '/user/point-records',
    method: 'get',
    params
  })
}

// 修改用户密码
export const changeUserPassword = (data) => {
  return request({
    url: '/user/change-password',
    method: 'put',
    data
  })
}

// 检查用户名是否可用
export const checkUsername = (username) => {
  return request({
    url: '/auth/check-username',
    method: 'post',
    params: { username }
  })
}

// 检查邮箱是否可用
export const checkEmail = (email) => {
  return request({
    url: '/auth/check-email',
    method: 'post',
    params: { email }
  })
}

// 检查手机号是否可用
export const checkPhone = (phone) => {
  return request({
    url: '/auth/check-phone',
    method: 'post',
    params: { phone }
  })
}

// 用户登出
export const logout = () => {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

// 检查用户名是否存在（别名）
export const checkUsernameExists = checkUsername

// 检查邮箱是否存在（别名）
export const checkEmailExists = checkEmail

// 检查手机号是否存在（别名）
export const checkPhoneExists = checkPhone

// 修改密码（员工专用）
export const changePassword = (data) => {
  return request({
    url: '/auth/change-password',
    method: 'post',
    data
  })
}
