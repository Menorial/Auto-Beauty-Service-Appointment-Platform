import request from '@/utils/request'

// 获取员工列表
export function getEmployees(params) {
  return request({
    url: '/admin/employees',
    method: 'get',
    params
  })
}

// 获取员工详情
export function getEmployeeById(id) {
  return request({
    url: `/admin/employees/${id}`,
    method: 'get'
  })
}

// 创建员工
export function createEmployee(data) {
  return request({
    url: '/admin/employees',
    method: 'post',
    data
  })
}

// 更新员工
export function updateEmployee(id, data) {
  return request({
    url: `/admin/employees/${id}`,
    method: 'put',
    data
  })
}

// 删除员工
export function deleteEmployee(id) {
  return request({
    url: `/admin/employees/${id}`,
    method: 'delete'
  })
}

// ===== 员工端API =====

// 获取当前员工信息
export function getCurrentEmployeeInfo() {
  return request({
    url: '/employee/info',
    method: 'get'
  })
}

// 获取员工的预约列表
export function getEmployeeAppointments(params) {
  return request({
    url: '/employee/appointments',
    method: 'get',
    params
  })
}

// 获取员工的今日预约
export function getTodayAppointments() {
  return request({
    url: '/employee/appointments/today',
    method: 'get'
  })
}

// 获取员工的预约统计
export function getAppointmentStatistics() {
  return request({
    url: '/employee/statistics',
    method: 'get'
  })
}

// 获取员工的仪表板数据
export function getEmployeeDashboard() {
  return request({
    url: '/employee/dashboard',
    method: 'get'
  })
}

// 更新员工个人信息
export function updateEmployeeProfile(data) {
  return request({
    url: '/employee/profile',
    method: 'put',
    data
  })
}

// 上传员工头像
export function uploadEmployeeAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/employee/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
