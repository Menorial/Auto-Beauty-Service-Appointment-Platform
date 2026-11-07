import request from '@/utils/request'

// 上传头像
export const uploadAvatar = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/upload/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 上传车辆图片
export const uploadVehicleImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/upload/vehicle',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 上传服务图片
export const uploadServiceImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/upload/service',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 上传评价图片
export const uploadReviewImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/upload/review',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
