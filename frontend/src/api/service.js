import request from '@/utils/request'

// 获取服务列表
export function getServices(params) {
  return request({
    url: '/services',
    method: 'get',
    params
  })
}

// 获取服务详情
export function getServiceById(id) {
  return request({
    url: `/services/${id}`,
    method: 'get'
  })
}

// 根据分类获取服务
export function getServicesByCategory(category) {
  return request({
    url: `/services/category/${category}`,
    method: 'get'
  })
}

// 管理员获取所有服务
export function getAllServicesForAdmin() {
  return request({
    url: '/admin/services',
    method: 'get'
  })
}

// 创建服务
export function createService(data) {
  return request({
    url: '/admin/services',
    method: 'post',
    data
  })
}

// 更新服务
export function updateService(id, data) {
  return request({
    url: `/admin/services/${id}`,
    method: 'put',
    data
  })
}

// 删除服务
export function deleteService(id) {
  return request({
    url: `/admin/services/${id}`,
    method: 'delete'
  })
}
