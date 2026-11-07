import request from '@/utils/request'

// 获取用户车辆列表
export const getVehicles = () => {
  return request({
    url: '/vehicles',
    method: 'get'
  })
}

// 获取车辆详情
export const getVehicleById = (id) => {
  return request({
    url: `/vehicles/${id}`,
    method: 'get'
  })
}

// 添加车辆
export const addVehicle = (data) => {
  return request({
    url: '/vehicles',
    method: 'post',
    data
  })
}

// 更新车辆信息
export const updateVehicle = (id, data) => {
  return request({
    url: `/vehicles/${id}`,
    method: 'put',
    data
  })
}

// 删除车辆
export const deleteVehicle = (id) => {
  return request({
    url: `/vehicles/${id}`,
    method: 'delete'
  })
}

// 设置默认车辆
export const setDefaultVehicle = (id) => {
  return request({
    url: `/vehicles/${id}/default`,
    method: 'put'
  })
}

// 获取默认车辆
export const getDefaultVehicle = () => {
  return request({
    url: '/vehicles/default',
    method: 'get'
  })
}
