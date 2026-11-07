import request from '@/utils/request'

// 获取系统配置
export function getSystemConfigs() {
  return request({
    url: '/admin/system/configs',
    method: 'get'
  })
}

// 更新系统配置
export function updateSystemConfig(key, value) {
  return request({
    url: '/admin/system/configs',
    method: 'put',
    data: {
      configKey: key,
      configValue: value
    }
  })
}
