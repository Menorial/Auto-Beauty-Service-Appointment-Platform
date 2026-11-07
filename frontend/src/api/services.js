import request from '@/utils/request'

// 获取所有服务
export const getServices = (params) => {
  return request({
    url: '/services',
    method: 'get',
    params
  })
}

// 获取服务详情
export const getServiceById = (id) => {
  return request({
    url: `/services/${id}`,
    method: 'get'
  })
}

// 根据分类获取服务
export const getServicesByCategory = (category) => {
  return request({
    url: `/services/category/${category}`,
    method: 'get'
  })
}

// 获取服务分类
export const getServiceCategories = () => {
  return request({
    url: '/services/categories',
    method: 'get'
  })
}

// 搜索服务（可附带分页/分类/排序参数）
export const searchServices = (keyword, params = {}) => {
  return request({
    url: '/services/search',
    method: 'get',
    params: { ...params, keyword }
  })
}

// 根据价格范围获取服务
export const getServicesByPriceRange = (minPrice, maxPrice) => {
  return request({
    url: '/services/price-range',
    method: 'get',
    params: { minPrice, maxPrice }
  })
}

// 获取热门服务
export const getPopularServices = () => {
  return request({
    url: '/services/popular',
    method: 'get'
  })
}

// 获取推荐服务
export const getRecommendedServices = () => {
  return request({
    url: '/services/recommended',
    method: 'get'
  })
}
