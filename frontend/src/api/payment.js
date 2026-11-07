import request from '@/utils/request'

/**
 * 创建支付宝支付订单
 */
export function createAlipayOrder(appointmentId) {
  return request({
    url: '/payment/alipay/create',
    method: 'post',
    params: { appointmentId }
  })
}

/**
 * 查询支付状态
 */
export function queryPaymentStatus(appointmentId) {
  return request({
    url: '/payment/status',
    method: 'get',
    params: { appointmentId }
  })
}
