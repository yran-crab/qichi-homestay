import request from '@/utils/request'

/** 预订试算 */
export const confirmBooking = (data) => request.post('/orders/confirm', data)
/** 创建订单 */
export const createOrder = (data) => request.post('/orders', data)
/** 发起支付宝沙箱支付（返回自动提交的支付表单 HTML） */
export const alipayOrder = (id) => request.post(`/orders/${id}/alipay`)
/** 校验支付宝支付结果 */
export const verifyAlipayPay = (id) => request.post(`/orders/${id}/alipay/verify`)
/** 取消订单 */
export const cancelOrder = (id, reason) =>
  request.post(`/orders/${id}/cancel`, { reason })
/** 我的订单分页 */
export const myOrders = (params) => request.get('/orders/my', { params })
/** 订单详情 */
export const orderDetail = (id) => request.get(`/orders/${id}`)
/** 房源已被预订的日期区间 */
export const bookedDates = (homestayId) =>
  request.get(`/homestays/${homestayId}/booked-dates`)
