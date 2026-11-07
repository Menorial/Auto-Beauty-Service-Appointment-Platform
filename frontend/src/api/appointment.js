import request from '@/utils/request'

// 创建预约
export function createAppointment(data) {
  return request({
    url: '/appointments',
    method: 'post',
    data
  })
}

// 获取用户预约列表
export function getUserAppointments(params) {
  return request({
    url: '/appointments/user',
    method: 'get',
    params
  })
}

// 获取预约详情
export function getAppointmentById(id) {
  return request({
    url: `/appointments/${id}`,
    method: 'get'
  })
}

// 根据订单号获取预约
export function getAppointmentByOrderNo(orderNo) {
  return request({
    url: `/appointments/order/${orderNo}`,
    method: 'get'
  })
}

// 取消预约
export function cancelAppointment(id) {
  return request({
    url: `/appointments/${id}/cancel`,
    method: 'put'
  })
}

// 管理员获取今日预约
export function getTodayAppointments() {
  return request({
    url: '/admin/appointments/today',
    method: 'get'
  })
}

// 根据状态获取预约
export function getAppointmentsByStatus(status, params) {
  return request({
    url: `/admin/appointments/status/${status}`,
    method: 'get',
    params
  })
}

// 根据日期范围获取预约
export function getAppointmentsByDate(params) {
  return request({
    url: '/admin/appointments/date-range',
    method: 'get',
    params
  })
}

// 更新预约状态
export function updateAppointmentStatus(id, status) {
  return request({
    url: `/admin/appointments/${id}/status`,
    method: 'put',
    data: { status }
  })
}

// 更新支付状态
export function updatePaymentStatus(id, paymentStatus) {
  return request({
    url: `/admin/appointments/${id}/payment-status`,
    method: 'put',
    data: { paymentStatus }
  })
}

// 更新预约备注（员工专用）
export function updateAppointmentNotes(id, notes) {
  return request({
    url: `/appointments/${id}/notes`,
    method: 'put',
    data: { notes }
  })
}
