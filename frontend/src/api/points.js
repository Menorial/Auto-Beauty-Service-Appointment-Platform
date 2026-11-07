import request from '@/utils/request'

// 获取用户积分记录
export const getUserPoints = (params) => {
  return request({
    url: '/api/user/points',
    method: 'get',
    params
  })
}

// 获取用户积分统计
export const getUserPointsStats = () => {
  return request({
    url: '/api/user/points/stats',
    method: 'get'
  })
}
