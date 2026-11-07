import request from '@/utils/request'

// 获取用户信息
export function getUserProfile() {
  return request({
    url: '/users/profile',
    method: 'get'
  })
}

// 更新用户信息
export function updateUserProfile(data) {
  return request({
    url: '/users/profile',
    method: 'put',
    data
  })
}

// 获取用户积分
export function getUserPoints() {
  return request({
    url: '/users/points',
    method: 'get'
  })
}

// 获取积分记录
export function getPointRecords(params) {
  return request({
    url: '/users/point-records',
    method: 'get',
    params
  })
}
