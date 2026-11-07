import request from '@/utils/request'

// 创建预约
export const createAppointment = (data) => {
  return request({
    url: '/appointments',
    method: 'post',
    data
  })
}

// 获取用户预约列表
export const getAppointments = (page = 0, size = 10, status = null) => {
  const params = { page, size }
  if (status) {
    params.status = status
  }
  return request({
    url: '/appointments',
    method: 'get',
    params
  })
}

// 获取预约详情
export const getAppointmentById = (id) => {
  return request({
    url: `/appointments/${id}`,
    method: 'get'
  })
}

// 根据订单号获取预约
export const getAppointmentByOrderNo = (orderNo) => {
  return request({
    url: `/appointments/order/${orderNo}`,
    method: 'get'
  })
}

// 取消预约
export const cancelAppointment = (id) => {
  return request({
    url: `/appointments/${id}/cancel`,
    method: 'put'
  })
}

// 删除预约
export const deleteAppointment = (id) => {
  return request({
    url: `/appointments/${id}`,
    method: 'delete'
  })
}

// 获取可用时间段
export const getAvailableTimeSlots = (params) => {
  return request({
    url: '/appointments/available-slots',
    method: 'get',
    params
  })
}
