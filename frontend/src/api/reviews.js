import request from '@/utils/request'

// 创建评价
export const createReview = (data) => {
  return request({
    url: '/reviews',
    method: 'post',
    data
  })
}

// 根据服务ID获取评价列表
export const getReviewsByServiceId = (serviceId, page = 0, size = 10) => {
  return request({
    url: `/reviews/service/${serviceId}`,
    method: 'get',
    params: { page, size }
  })
}

// 根据用户ID获取评价列表
export const getReviewsByUserId = (userId, page = 0, size = 10) => {
  return request({
    url: `/reviews/user/${userId}`,
    method: 'get',
    params: { page, size }
  })
}

// 根据预约ID获取评价
export const getReviewByAppointmentId = (appointmentId) => {
  return request({
    url: `/reviews/appointment/${appointmentId}`,
    method: 'get'
  })
}

// 获取服务评分信息
export const getServiceRating = (serviceId) => {
  return request({
    url: `/reviews/service/${serviceId}/rating`,
    method: 'get'
  })
}

// 获取最新评价
export const getLatestReviews = () => {
  return request({
    url: '/reviews/latest',
    method: 'get'
  })
}

// 获取高分评价
export const getHighRatingReviews = () => {
  return request({
    url: '/reviews/high-rating',
    method: 'get'
  })
}

// 获取用户评价列表
export const getUserReviews = (userId, page = 0, size = 10) => {
  return request({
    url: `/reviews/user/${userId}`,
    method: 'get',
    params: { page, size }
  })
}

// 更新评价
export const updateReview = (id, data) => {
  return request({
    url: `/reviews/${id}`,
    method: 'put',
    data
  })
}

// 删除评价
export const deleteReview = (id) => {
  return request({
    url: `/reviews/${id}`,
    method: 'delete'
  })
}

// 获取服务评价（别名）
export const getServiceReviews = getReviewsByServiceId
