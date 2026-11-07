import request from '@/utils/request'

// 获取管理员统计数据
export const getAdminStats = () => {
  return request({
    url: '/admin/stats',
    method: 'get'
  })
}

// 获取最近预约
export const getRecentAppointments = (params) => {
  return request({
    url: '/admin/appointments/recent',
    method: 'get',
    params
  })
}

// 获取预约趋势数据
export const getAppointmentTrend = (params) => {
  return request({
    url: '/admin/appointments/trend',
    method: 'get',
    params
  })
}

// 获取所有预约（管理员）
export const getAllAppointments = (params) => {
  return request({
    url: '/admin/appointments',
    method: 'get',
    params
  })
}

// 根据日期范围获取预约
export const getAppointmentsByDateRange = (params) => {
  return request({
    url: '/admin/appointments/date-range',
    method: 'get',
    params
  })
}

// 更新预约状态
export const updateAppointmentStatus = (id, status) => {
  return request({
    url: `/admin/appointments/${id}/status`,
    method: 'put',
    data: { status }
  })
}

// 分配员工
export const assignEmployee = (appointmentId, employeeId) => {
  return request({
    url: `/admin/appointments/${appointmentId}/assign`,
    method: 'put',
    data: { employeeId }
  })
}

// 获取所有用户（管理员）
export const getAllUsers = (params) => {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  })
}

// 获取用户详情（管理员）
export const getUserById = (id) => {
  return request({
    url: `/admin/users/${id}`,
    method: 'get'
  })
}

// 获取所有员工
export const getAllEmployees = (params) => {
  return request({
    url: '/admin/employees',
    method: 'get',
    params
  })
}

// 创建员工
export const createEmployee = (data) => {
  return request({
    url: '/admin/employees',
    method: 'post',
    data
  })
}

// 更新员工
export const updateEmployee = (id, data) => {
  return request({
    url: `/admin/employees/${id}`,
    method: 'put',
    data
  })
}

// 删除员工
export const deleteEmployee = (id) => {
  return request({
    url: `/admin/employees/${id}`,
    method: 'delete'
  })
}

// 获取所有服务（管理员）
export const getAllServices = (params) => {
  return request({
    url: '/admin/services',
    method: 'get',
    params
  })
}

// 创建服务
export const createService = (data) => {
  return request({
    url: '/admin/services',
    method: 'post',
    data
  })
}

// 更新服务
export const updateService = (id, data) => {
  return request({
    url: `/admin/services/${id}`,
    method: 'put',
    data
  })
}

// 删除服务
export const deleteService = (id) => {
  return request({
    url: `/admin/services/${id}`,
    method: 'delete'
  })
}

// 获取所有评价（管理员）
export const getAllReviews = (params) => {
  return request({
    url: '/admin/reviews',
    method: 'get',
    params
  })
}

// 回复评价
export const replyReview = (id, reply) => {
  return request({
    url: `/admin/reviews/${id}/reply`,
    method: 'post',
    data: { reply }
  })
}

// 删除评价
export const deleteReview = (id) => {
  return request({
    url: `/admin/reviews/${id}`,
    method: 'delete'
  })
}

// 获取数据报表
export const getReports = (params) => {
  return request({
    url: '/admin/reports',
    method: 'get',
    params
  })
}

// 获取收入统计
export const getRevenueStats = (params) => {
  return request({
    url: '/admin/reports/revenue',
    method: 'get',
    params
  })
}

// 获取预约统计
export const getAppointmentStats = (params) => {
  return request({
    url: '/admin/reports/appointments',
    method: 'get',
    params
  })
}

// 获取报表数据（别名）
export const getReportsData = getReports

// 回复评价（别名）
export const replyToReview = replyReview

// 更新用户信息
export const updateUser = (id, data) => {
  return request({
    url: `/admin/users/${id}`,
    method: 'put',
    data
  })
}

// 更新用户状态
export const updateUserStatus = (id, status) => {
  return request({
    url: `/admin/users/${id}/status`,
    method: 'put',
    data: { status }
  })
}

// 重置用户密码
export const resetUserPassword = (id, password) => {
  return request({
    url: `/admin/users/${id}/password`,
    method: 'put',
    data: { password }
  })
}

// 更新用户积分
export const updateUserPoints = (id, data) => {
  return request({
    url: `/admin/users/${id}/points`,
    method: 'put',
    data
  })
}
